package org.airportinternet.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class ConnectionActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	String stnName = getIntent().getExtras().getString("setting");
    	Log.d("Setting name", stnName);
    }
}
