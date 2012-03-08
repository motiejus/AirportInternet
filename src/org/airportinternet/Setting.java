package org.airportinternet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;


public class Setting {
	private static final String settingsFILENAME = "settingsMap.obj"; 
	
	String name; // Unique
	String nameserv_addr = null;
    String topdomain = null;
    String username = null;
    String password = "";
    
    Integer autodetect_frag_size = 1;
    Integer max_downstream_frag_size = 3072;
    Boolean raw_mode = true;
    Boolean lazymode = true;
    Integer selecttimeout = 4;
    Integer hostname_maxlen = 0xFF;

    public static Setting default1() {
    	Setting s = new Setting();
    	s.name = "Free Test";
    	s.password = "vienas";
    	s.topdomain = "p.daro.lt";
    	return s;
    }
    
    public String toString() {
    	return name;
    }
    
    @SuppressWarnings("unchecked")
	public static List<Setting> getSettings(Context context) {
    	List<Setting> ret = null;
    	
    	/* Silently fail if we cannot read previous settings */
    	try {
			FileInputStream fis = context.openFileInput(settingsFILENAME);
			try {
				ObjectInputStream deserializer = new ObjectInputStream(fis);
				try {
					 ret = (List<Setting>)deserializer.readObject();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			} catch (StreamCorruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			Log.i("getSettings", "Settings file not found, using default");
		}

		if (ret == null) {
			ret = new ArrayList<Setting>();
			ret.add(Setting.default1());
		}
    	return ret;
    }
}