package org.airportinternet.conn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

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
	
	private boolean running = true;
	
	@Override
	public void onDestroy() {
		running = false;
	}
	
	@Override
	protected void start(Setting setting) {
		s = setting;

		List<String> cmdc = s.cmdarray();
		cmdc.add(0, IODINE_PATH);
		Log.d("CommandLine", cmdc.toString());

		try {
			proc = new ProcessBuilder(cmdc).redirectErrorStream(true).start();
			connecting();
			in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			mHandler.post(poller);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Toast.makeText(getApplicationContext(),
					"Failed to start iodine", Toast.LENGTH_SHORT);
			e.printStackTrace();
			sendLog("Failed to start iodine");
		}
	}

	private Runnable poller = new Runnable() {
		public void run() {
			char buf[] = new char[1024];
			StringBuilder ret = new StringBuilder();
			int cnt = 0;
			
			try {
				while (in.ready())
					while((cnt = in.read(buf)) != -1) {
						ret.append(buf, 0, cnt);
					}
			} catch (IOException e) {
				ret.append("Read interrupted");
				e.printStackTrace();
			}
			if (ret.length() > 0) sendLog(ret.toString());
    		if (running) mHandler.postDelayed(this, 1000);
		}
	};
}
