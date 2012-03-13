package org.airportinternet.ui;

import org.airportinternet.R;
import org.airportinternet.Setting;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.view.View;
import android.view.View.OnClickListener;

public class AirportInternetActivity extends Activity {
	public static final int RELOAD_SETTINGS_LIST = 1;
	
	Button btnConnect, btnEditSetting;
	Spinner sp;
	ArrayAdapter<Setting> spinnerAdapter;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        btnConnect = (Button) findViewById(R.id.connectButton);
        btnEditSetting = (Button) findViewById(R.id.editSettingButton);
        
        sp = ((Spinner)findViewById(R.id.settingsList));
        spinnerAdapter = new ArrayAdapter<Setting>(this,
        		android.R.layout.simple_spinner_item,
                Setting.getSettings(getApplicationContext()));

        spinnerAdapter.setDropDownViewResource(
        		android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(spinnerAdapter);

        btnConnect.setOnClickListener(btnConnectListener);
        btnEditSetting.setOnClickListener(btnEditSettingListener);
    }
    
    private OnClickListener btnConnectListener = new OnClickListener() {
        public void onClick(View v){
        	Intent st = new Intent(getApplicationContext(),
    				ConnectionActivity.class);
        	st.putExtra("setting", sp.getSelectedItem().toString());
    		startActivity(st);
        }
    };

    private OnClickListener btnEditSettingListener = new OnClickListener() {
        public void onClick(View v){
        	Intent st = new Intent(getApplicationContext(),
    				EditSettingActivity.class);
        	st.putExtra("setting", sp.getSelectedItem().toString());
    		startActivityForResult(st, 1);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	spinnerAdapter.notifyDataSetChanged();
    }

}