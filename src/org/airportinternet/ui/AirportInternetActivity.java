package org.airportinternet.ui;

import org.airportinternet.R;
import org.airportinternet.Setting;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class AirportInternetActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Spinner sp = ((Spinner)findViewById(R.id.settingsList));
        ArrayAdapter<Setting> adapter = new ArrayAdapter<Setting>(this,
        		android.R.layout.simple_spinner_item,
                Setting.getSettings(getApplicationContext()));

        adapter.setDropDownViewResource(
        		android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);
    }
}