package org.airportinternet.conn;

import org.airportinternet.Setting;

import android.app.Service;
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
	 * Activity sends disconnect action to the service
	 */
	public static final int MSG_ACTION_DISCONNECT = 6;
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
	protected boolean running = false;

	protected Status status = Status.STATUS_UNKNOWN;
	
	@Override
	public void onDestroy() {
		stop();
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
    					if (!running)
    						start(setting);
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
    				case MSG_ACTION_DISCONNECT:
    					Log.d("Connector:handleMsg", "Got disconnect request");
    					stop();
    					stopSelf();
    					break;
    				default:
    					Log.d("handleMessage", "passing msg to parent");
    					super.handleMessage(msg);
    				}
    			}
    		}
    );

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
    	return START_FLAG_REDELIVERY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
    	return mMessenger.getBinder();
    }

    /**
     * Called from Connector when message was received from iodine
     */
	protected void sendLog(String message) {
		Message msg = Message.obtain();
		msg.obj = message;
		msg.what = MSG_SET_LOG;
		try { // Could not send Object to UI.. UI crashed?
			client.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void sendStatusToActivity() {
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
	}
}