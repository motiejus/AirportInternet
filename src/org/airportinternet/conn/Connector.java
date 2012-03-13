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
	public static final int MSG_ACTION_DISCONNECT = 6;
	public static final int MSG_CONNECTING = 5;
    public static final int MSG_DISCONNECTED = 4;
	public static final int MSG_CONNECTED = 3;
    public static final int MSG_SET_LOG = 2;
    public static final int MSG_REGISTER_CLIENT = 1;
    
	private Messenger client;
	private Setting setting;
	
	/*
	 * This is called when communication with Activity is started
	 * and it's safe to send messages to the activity
	 */
	protected abstract void start(Setting setting);
	protected abstract void stop();

	/* If activity is/should be running */
	protected boolean running = false,
			/* If we are actually connected to server */
			connected = false;
	
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
    					Log.d("handleMessage", "registering new client");
    					client = msg.replyTo;
    					if (!running)
    						start(setting);
    					break;
    				case MSG_ACTION_DISCONNECT:
    					Log.d("handleMessage", "Got disconnect request");
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
    	setting = Setting.getSettingByName(settingName,
    			getApplicationContext());
    	return START_FLAG_REDELIVERY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
    	return mMessenger.getBinder();
    }

	protected void sendLog(String message) { // Called from worker thread
		Message msg = Message.obtain();
		msg.obj = message;
		msg.what = MSG_SET_LOG;
		try { // Could not send Object to UI.. UI crashed?
			client.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	protected void connecting() {
		sendStatusNotification(MSG_CONNECTING);
	}
	
	protected void connected() {
		sendStatusNotification(MSG_CONNECTED);
	}
	
	protected void disconnected() {
		sendStatusNotification(MSG_DISCONNECTED);
	}
	private void sendStatusNotification(int notification) {
		Message msg = Message.obtain();
		msg.what = notification;
		try {
			client.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}