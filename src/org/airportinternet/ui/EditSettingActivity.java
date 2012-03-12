package org.airportinternet.ui;

import org.airportinternet.R;
import org.airportinternet.Setting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

public class EditSettingActivity extends Activity {

	private Setting setting;
	
	private EditText name, nameserv_addr, topdomain, password,
		max_downstream_frag_size, selecttimeout, hostname_maxlen;
	private ToggleButton autodetect_frag_size, raw_mode, lazymode;
	
	private Button saveSettingBtn; 
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.editsetting);

    	map_fields();
    	
    	String stnName = getIntent().getExtras().getString("setting");
    	Log.d("Modifying setting: ", stnName);
    	setting = Setting.getSettingByName(stnName, getApplicationContext());
    	
    	fill_fields();
    }
    
    private void map_fields() {
    	name = (EditText) findViewById(R.id.name);
    	nameserv_addr = (EditText) findViewById(R.id.nameserv_addr);
    	topdomain = (EditText) findViewById(R.id.topdomain);
    	password = (EditText) findViewById(R.id.password);
    	max_downstream_frag_size = (EditText) findViewById(
    			R.id.max_downstream_frag_size);

    	selecttimeout = (EditText) findViewById(R.id.selecttimeout);
    	hostname_maxlen = (EditText) findViewById(R.id.hostname_maxlen);

    	autodetect_frag_size = (ToggleButton) findViewById(
    			R.id.autodetect_frag_size);
    	raw_mode = (ToggleButton) findViewById(R.id.raw_mode);
    	lazymode = (ToggleButton) findViewById(R.id.lazymode);
    }

    private void fill_fields() {
    	name.setText(setting.name);
    	nameserv_addr.setText(setting.nameserv_addr);
    	topdomain.setText(setting.topdomain);
    	password.setText(setting.password);
    	//max_downstream_frag_size.setText(setting.max_downstream_frag_size);
    	//selecttimeout.setText(setting.selecttimeout);
    	//hostname_maxlen.setText(setting.hostname_maxlen);
    	
    	autodetect_frag_size.setChecked(setting.autodetect_frag_size);
    	raw_mode.setChecked(setting.raw_mode);
    	lazymode.setChecked(setting.lazymode);
    }
    
    private void fill_setting() {
    	setting.name = name.getText().toString();
    }
    
    private OnClickListener btnSaveSettingListener = new OnClickListener() {
        public void onClick(View v) {
        	Context c = getApplicationContext();
        	
        	fill_setting();
        	if (setting.save(c)) {
        		Toast.makeText(c, "Failed to save setting", Toast.LENGTH_LONG);
        	} else {
        		Toast.makeText(c, "Saved.\nTODO: back", Toast.LENGTH_LONG);
        	}
        }
    };

}
