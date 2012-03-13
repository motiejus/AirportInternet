package org.airportinternet.ui;

import org.airportinternet.R;
import org.airportinternet.Setting;

import android.app.Activity;
import android.content.Context;
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
	
	private Button saveSettingBtn, deleteSettingBtn; 
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.editsetting);
    	saveSettingBtn = (Button) findViewById(R.id.saveSettingButton);
    	deleteSettingBtn = (Button) findViewById(R.id.deleteSettingButton);
    	
    	map_fields();
    	
    	String stnName = getIntent().getExtras().getString("setting");
    	Log.d("Modifying setting: ", stnName);
    	setting = Setting.getSettingByName(stnName, getApplicationContext());
    	
    	fill_fields();
    	saveSettingBtn.setOnClickListener(btnSaveSettingListener);
    	deleteSettingBtn.setOnClickListener(btnDeleteSettingListener);
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
    	max_downstream_frag_size.setText(
    			String.valueOf(setting.max_downstream_frag_size));
    	selecttimeout.setText(String.valueOf(setting.selecttimeout));
    	hostname_maxlen.setText(String.valueOf(setting.hostname_maxlen));
    	
    	autodetect_frag_size.setChecked(setting.autodetect_frag_size);
    	raw_mode.setChecked(setting.raw_mode);
    	lazymode.setChecked(setting.lazymode);
    }
    
    /**
     * Fill this.setting with values from fields
     * @return true if it's a new setting (save as) or old one
     */
    private boolean fill_setting() {
    	boolean save_as = !setting.name.equals(name.getText().toString());
    	if (save_as)
    		setting = new Setting();
    	setting.name = name.getText().toString();
    	setting.nameserv_addr = nameserv_addr.getText().toString();
    	setting.topdomain = topdomain.getText().toString();
    	setting.password = password.getText().toString();
    	
    	setting.max_downstream_frag_size = Integer.valueOf(
    			max_downstream_frag_size.getText().toString());
    	setting.selecttimeout = Integer.valueOf(
    			selecttimeout.getText().toString());
    	setting.hostname_maxlen = Integer.valueOf(
    			hostname_maxlen.getText().toString());
    	
    	setting.autodetect_frag_size = autodetect_frag_size.isChecked();
    	setting.raw_mode = raw_mode.isChecked();
    	setting.lazymode = lazymode.isChecked();
    	
    	Log.d("Debug setting", setting.debug());
    	
    	return save_as;
    }
    
    private OnClickListener btnSaveSettingListener = new OnClickListener() {
        public void onClick(View v) {
        	boolean save_as = fill_setting();
        	if (setting.save(getApplicationContext(), save_as)) {
        		Log.d("saveSettings", "Saved new settings");
        		Toast.makeText(getApplicationContext(), "Saved \"" + setting +
        				"\" ("+setting.topdomain+")", Toast.LENGTH_LONG).show();
        		finish();
        	} else {
        		Log.w("saveSettings", "Failed to save settings");
        		Toast.makeText(getApplicationContext(),
        				"Failed to save setting", Toast.LENGTH_LONG).show();
        	}
        }
    };
    
    private OnClickListener btnDeleteSettingListener = new OnClickListener() {
    	public void onClick(View v) {
    		String notif = "Deleted \""+setting+" \" ("+setting.topdomain+")";
    		if (setting.delete(getApplicationContext())) {
    			Toast.makeText(getApplicationContext(), notif,
    					Toast.LENGTH_LONG).show();
    			finish();
    		} else {
    			Toast.makeText(getApplicationContext(), "Failed to delete",
    					Toast.LENGTH_LONG);
    		}
    	}
    };

}
