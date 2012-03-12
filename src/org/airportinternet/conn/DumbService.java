package org.airportinternet.conn;

import org.airportinternet.Setting;

import android.os.Handler;
import android.util.Log;

public class DumbService extends Connector {

	private Handler mHandler = new Handler();
	private int tick = 0;
	private boolean running = true;
	
	@Override
	public void onDestroy() {
		running = false;
	}

	@Override
	public void start(Setting setting) {
		Log.d("start", "Starting connection to " + setting);
		connecting();
		mHandler.postDelayed(poller, 2000);
	}
	
	private Runnable poller = new Runnable() {
		public void run() {
			if (tick % 6 == 4)
				disconnected();
			else if (tick % 6 == 5)
				connecting();
			else
				connected();
			sendLog("Tick " + ++tick + "\n");
    		if (running) mHandler.postDelayed(this, 1000);
		}
	};

}
