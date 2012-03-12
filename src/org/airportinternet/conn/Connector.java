package org.airportinternet.conn;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public abstract class Connector extends Service {	
    public static final int MSG_SET_LOG = 2;
    public static final int MSG_REGISTER_CLIENT = 1;
    
	private Messenger client;
	
	/*
	 * This is called when communication with Activity is started
	 * and it's safe to send messages to the activity
	 */
	protected abstract void start();
	
	// Target we publish for clients to send messages to IncomingHandler
    private final Messenger mMessenger = new Messenger(
			// Handler of incoming messages from clients
    		new Handler() {
    			@Override
    			public void handleMessage(Message msg) {
    				switch (msg.what) {
    				case MSG_REGISTER_CLIENT:
    					Log.d("handleMessage", "registering new client");
    					client = msg.replyTo;
    					start();
    					break;
    				default:
    					Log.d("handleMessage", "passing msg to parent");
    					super.handleMessage(msg);
    				}
    			}
    		}
    );
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

	protected void sendObject(Object message) { // Called from worker thread
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