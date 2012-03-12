package org.airportinternet.conn;

import android.os.Handler;

public class DumbService extends Connector {

	private Handler mHandler = new Handler();
	private int tick = 0;
	private boolean running = true;
	
	@Override
	public void onDestroy() {
		running = false;
	}

	@Override
	public void start() {
		mHandler.post(poller);
	}
	
	private Runnable poller = new Runnable() {
		public void run() {
			sendObject("Tick " + ++tick + "\n");
    		if (running) mHandler.postDelayed(this, 1000);
		}
	};

}
