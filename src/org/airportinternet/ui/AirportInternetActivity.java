package org.airportinternet.ui;

import org.airportinternet.R;
import org.airportinternet.Setting;
import org.airportinternet.conn.Connector;
import org.airportinternet.conn.DumbService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.view.View;
import android.view.View.OnClickListener;

public class AirportInternetActivity extends Activity {
	
	Button btnConnect;
	Spinner sp;
	
    Messenger mService = null;
    final Messenger mMessenger = new Messenger(new IncomingHandler());
	
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder serv) {
            mService = new Messenger(serv);
            try {
                Message msg = Message.obtain(null,
                		Connector.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
            	Log.w("RegisterClient", "Failed");
            	// In this case the service has crashed
            	// before we could even do anything with it
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
        	//unexpectedly disconnected - process crashed.
            mService = null;
            Log.w("disconnect", "Service unexpectedly disconnected");
        }
    };

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        btnConnect = (Button)findViewById(R.id.connectButton);
        
        sp = ((Spinner)findViewById(R.id.settingsList));
        ArrayAdapter<Setting> adapter = new ArrayAdapter<Setting>(this,
        		android.R.layout.simple_spinner_item,
                Setting.getSettings(getApplicationContext()));

        adapter.setDropDownViewResource(
        		android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);

        
        btnConnect.setOnClickListener(btnConnectListener);
        
        bindService(new Intent(this, DumbService.class), mConnection,
        		Context.BIND_AUTO_CREATE);
    }
    
    
    private OnClickListener btnConnectListener = new OnClickListener() {
        public void onClick(View v){
        	Intent st = new Intent(getApplicationContext(),
    				ConnectionActivity.class);
        	st.putExtra("setting", sp.getSelectedItem().toString());
    		startActivity(st);
        }
    };
    
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case Connector.MSG_SET_LOG:
            	Log.d("handleMessage", "append to TextArea: " + msg.obj);
                break;
            default:
            	Log.d("handleMessage", "got unknown message: " + msg);
                super.handleMessage(msg);
            }
        }
    }

}