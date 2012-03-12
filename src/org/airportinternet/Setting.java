package org.airportinternet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.util.Log;

public class Setting {
	private static final String settingsFILENAME = "settingsMap.obj"; 
	
	public String name; // Unique
	public String nameserv_addr = null;
	public String topdomain = null;
	public String username = null; // NOT username in iodine sense, UNUSED
	public String password = "";
    
	public Boolean autodetect_frag_size = true;
	public Integer max_downstream_frag_size = 3072;
	public Boolean raw_mode = true;
	public Boolean lazymode = true;
	public Integer selecttimeout = 4;
	public Integer hostname_maxlen = 0xFF;
    
    public List<String> cmdarray() {
		List<String> arr = new LinkedList<String>();
		if (password != null) arr.add("-P " + password);
		if (autodetect_frag_size)
			arr.add("-m " + max_downstream_frag_size);
		if (!raw_mode) arr.add("-r");
		if (!lazymode) arr.add("-L");
		arr.add("-I " + selecttimeout);
		arr.add("-M " + hostname_maxlen);

		arr.add(topdomain);
		if (nameserv_addr != null) arr.add(nameserv_addr);
		
		return arr;
    }

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
    
    /**
     * 
     * @param settingName
     * @param c acquired from getApplicationContext()
     * @return full setting
     */
    public static Setting getSettingByName(String settingName, Context c) {
    	Setting setting = null;
    	for(Setting s : Setting.getSettings(c)) {
    		if (s.toString().equals(settingName)) {
    			setting = s;
    			break;
    		}
    	}
    	return setting;
    }
}
