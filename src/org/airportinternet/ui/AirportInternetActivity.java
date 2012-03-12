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
    }
    
    
    private OnClickListener btnConnectListener = new OnClickListener() {
        public void onClick(View v){
        	Intent st = new Intent(getApplicationContext(),
    				ConnectionActivity.class);
        	st.putExtra("setting", sp.getSelectedItem().toString());
    		startActivity(st);
        }
    };
}