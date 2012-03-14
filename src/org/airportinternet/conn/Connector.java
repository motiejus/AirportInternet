package org.airportinternet.conn;

import org.airportinternet.Setting;
import org.airportinternet.ui.ConnectionActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public abstract class Connector extends Service {
	public enum Status {
		STATUS_UNKNOWN,
		STATUS_CONNECTING,
		STATUS_CONNECTED,
		STATUS_DISCONNECTED
	};
	
	/* 
	 * Connector sends fullLog:String and connected:boolean to Activity.
	 * Only when service is newly bound, but was already running
	 * (so ConnectionActivity can properly update its state)
	 */
	public static final int MSG_STATUS_UPDATE = 7;
	/*
	 * Activity sends termination action to the service
	 */
	public static final int MSG_ACTION_TERMINATE = 6;
	/*
	 * Connector sends status updates in real-time to activity
	 * (connected, connecting, disconnected, update log)
	 */
	public static final int MSG_CONNECTING = 5;
    public static final int MSG_DISCONNECTED = 4;
	public static final int MSG_CONNECTED = 3;
    public static final int MSG_SET_LOG = 2;
    /*
     * Activity sends its message handler to Connector
     */
    public static final int MSG_REGISTER_CLIENT = 1;
    
	private Messenger client;
	private Setting setting = null;
	
	/* Connector appends to this log */
	protected StringBuilder fullLog = new StringBuilder();
	
	/*
	 * This is called when communication with Activity is started
	 * and it's safe to send messages to the activity
	 */
	protected abstract void start(Setting setting);
	protected abstract void stop();

	/* If activity is/should be running */
	protected boolean running = false, terminate = false;

	protected Status status = Status.STATUS_UNKNOWN;

	private Handler mHandler = new Handler();

	@Override
	public void onDestroy() {
		mNM.cancelAll();
		stop();
	}
	
	@Override
	public boolean onUnbind(Intent i) {
		client = null;
		return false;
	}
	
	// Target we publish for clients to send messages to IncomingHandler
    private final Messenger mMessenger = new Messenger(
    		new Handler() {
    			@Override
    			public void handleMessage(Message msg) {
    				switch (msg.what) {
    				case MSG_REGISTER_CLIENT:
    					Log.d("Connector:handleMsg", "registering new client");
    					client = msg.replyTo;
    					if (!terminate) {
    						start(setting);
    					}
    					else {
    						/* Send status to activity */
        					Message msg2 = Message.obtain();
        					msg2.what = MSG_STATUS_UPDATE;
        					msg2.obj = fullLog.toString(); 
        					try {
        						client.send(msg2);
        					} catch (RemoteException e) {
        						Log.d("Connector:handleMsg",
        								"Failed to update activity status");
        						e.printStackTrace();
        					}
        					sendStatusToActivity();
    					}
    					break;
    				case MSG_ACTION_TERMINATE:
    					Log.d("Connector:handleMsg", "Got disconnect request");
    					terminate = true;
    					stopSelf();
    					break;
    				default:
    					Log.d("handleMessage", "passing msg to parent");
    					super.handleMessage(msg);
    				}
    			}
    		}
    );

    private CharSequence contentTitle = "AirportConnect";
    private CharSequence contentText;
    private PendingIntent contentIntent;
	private NotificationManager mNM = null;
	private Notification notification = null;
	
    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {
    	String settingName = intent.getExtras().getString("setting");
    	/* If setting is null, then we know that activity is started for the
    	 * first time.
    	 * 
    	 * settingName can be null only if user is getting from notification.
    	 * Therefore it can't happen that both setting and settingName are null
    	 */
    	setting = setting != null ? setting :
    		Setting.getSettingByName(settingName, getApplicationContext());

    	setUpNotifications();
    	return START_FLAG_REDELIVERY;
    }
    
    private void setUpNotifications() {
    	String ns = Context.NOTIFICATION_SERVICE;
    	mNM = (NotificationManager) getSystemService(ns);

    	int icon = android.R.drawable.star_on;
    	CharSequence tickerText = "Hello";
    	long when = System.currentTimeMillis();

    	notification = new Notification(icon, tickerText, when);

    	Context c = getApplicationContext();
    	contentText = status2text();
    	Intent notificationIntent = new Intent(c, ConnectionActivity.class);
    	notificationIntent.putExtra("setting", (String)null);
    	contentIntent = PendingIntent.getActivity(c, 0, notificationIntent,
    			PendingIntent.FLAG_UPDATE_CURRENT);

    	notification.setLatestEventInfo(c, contentTitle, contentText,
    			contentIntent);
    	mNM.notify(1, notification);
    }
    
    @Override
    public IBinder onBind(Intent intent) {
    	return mMessenger.getBinder();
    }

    /**
     * Called from Connector when message was received from iodine
     */
	protected void sendLog(String message) {
		Log.e("sendLog", message);
		fullLog.append(message);
		if (client != null) {
			Message msg = Message.obtain();
			msg.obj = message;
			msg.what = MSG_SET_LOG;
			try { // Could not send Object to UI.. UI crashed?
				client.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	private void sendStatusToActivity() {
		/* Send status to notification if not disconnected */
		if (!status.equals(Status.STATUS_DISCONNECTED)) {
			notification.setLatestEventInfo(getApplicationContext(),
					contentTitle, status2text(), contentIntent);
			mNM.notify(1, notification);
		}
		
		Log.d("sendStatusNotification", "Status to activity: " + status);
		if (client != null) {
			int notification = MSG_DISCONNECTED;
			switch (status) {
			case STATUS_CONNECTED: notification = MSG_CONNECTED; break;
			case STATUS_CONNECTING: notification = MSG_CONNECTING; break;
			case STATUS_DISCONNECTED: notification = MSG_DISCONNECTED; break;
			}

			Message msg = Message.obtain();
			msg.what = notification;
			try {
				client.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	protected boolean isConnected() {
		return status == Status.STATUS_CONNECTED;
	}
	protected boolean isDisConnected() {
		return status == Status.STATUS_DISCONNECTED;
	}
	protected boolean isConnecting() {
		return status == Status.STATUS_CONNECTING;
	}
	
	/**
	 * called from Connector on various events (status updates)
	 */
	protected void connecting() {
		status = Status.STATUS_CONNECTING;
		sendStatusToActivity();
	}
	protected void connected() {
		status = Status.STATUS_CONNECTED;
		sendStatusToActivity();
	}
	protected void disconnected() {
		status = Status.STATUS_DISCONNECTED;
		sendStatusToActivity();
		stop();
		sendLog("\nRestarting after 5 seconds...\n");
		if (!terminate) mHandler.postDelayed(restarter, 5000);
	}
	private Runnable restarter = new Runnable() {
		public void run() {
			if (!terminate)
				start(setting);
		}
	};
	
	private String status2text() {
		String ret = "unknown";
		switch (status) {
		case STATUS_CONNECTED: ret = "Connected"; break;
		case STATUS_CONNECTING: ret = "Connecting"; break;
		case STATUS_DISCONNECTED: ret = "Disconnected"; break;
		}
		return ret;
	}
}