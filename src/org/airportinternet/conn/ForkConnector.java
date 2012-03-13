package org.airportinternet.conn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.airportinternet.Setting;

import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class ForkConnector extends Connector {
	private static final String IODINE_PATH =
			"/data/data/org.airportinternet/iodine";

	private Handler mHandler = new Handler();

	private Setting s;
	private Process proc;
	private BufferedReader in;

	private StringBuilder fullLog = new StringBuilder();
	
	private List<String> cmdc;
	
	/*
	 * We want to assign buffered readers and stuff only after watchdog 
	 * starts the process and starts monitoring it
	 */
	private Lock watchdogLock = new ReentrantLock();
	private Condition watchdogCond = watchdogLock.newCondition();
	
	/*
	 * Refresh UI every 100ms before "connected", every 1000ms when connected 
	 */
	private int refreshEvery = 100;
	
	@Override
	public void stop() {
		proc.destroy();
	}
	
	@Override
	protected void start(Setting setting) {
		s = setting;
		fullLog = new StringBuilder();

		cmdc = s.cmdarray();
		cmdc.add(0, IODINE_PATH);
		Log.d("CommandLine", cmdc.toString() + "; size: " + cmdc.size());
		watchdog.start();
		connecting();
		
		watchdogLock.lock();
		try {
			watchdogCond.await();
		} catch (InterruptedException e) {
			// should never happen
			e.printStackTrace();
		} finally {
			watchdogLock.unlock();
		}
		mHandler.post(poller);
		running = true;
	}

	private Runnable poller = new Runnable() {
		public void run() {
			char buf[] = new char[1024];
			StringBuilder ret = new StringBuilder();
			int cnt = 0;
			
			try {
				while (in.ready())
					if((cnt = in.read(buf)) != -1) {
						ret.append(buf, 0, cnt);
					}
			} catch (IOException e) {
				ret.append("Read interrupted");
				e.printStackTrace();
			}
			if (ret.length() > 0) {
				sendLog(ret.toString());
				fullLog.append(ret);
			}
			if (!connected) {
				connected = fullLog.lastIndexOf("setup complete, ") != -1;
				if (connected) {
					connected();
					// TODO: set up routing
					refreshEvery = 1000;
				}
			}
    		if (running) mHandler.postDelayed(this, refreshEvery);
    		else disconnected();
		}
	};

	
	private Thread watchdog = new Thread() {
		public void run() {
			watchdogLock.lock();
			try {
				proc = new ProcessBuilder(cmdc).redirectErrorStream(
						true).start();
				in = new BufferedReader(new InputStreamReader(
						proc.getInputStream()));
			} catch (IOException e) {
				Toast.makeText(getApplicationContext(),
						"Failed to start iodine", Toast.LENGTH_SHORT);
				e.printStackTrace();
				sendLog("Failed to start iodine");
			}
			watchdogCond.signal();
			watchdogLock.unlock();

			try {
				proc.waitFor();
				running = false;
			} catch (InterruptedException e) {
				e.printStackTrace(); // shouldn't ever happen
			}
			
			Log.d("watchdog", "watchdog stopped");
		}
	};
}