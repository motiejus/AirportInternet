package org.airportinternet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.util.Log;

public class Setting implements Serializable {
	private static final long serialVersionUID = -6098662810516025032L;

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

	private static List<Setting> settingsStrictlyPrivateArray = null; 
	
    public String debug() {
    	String ret[] = {
    			"name: " + name,
    			"nameserv_addr: " + nameserv_addr,
    			"topdomain: " + topdomain,
    			"username: " + username,
    			"password: " + password,
    			"autodetect_frag_size: " + autodetect_frag_size.toString(),
    			"max_downstream_frag_size: " + max_downstream_frag_size
    				.toString(),
    			"raw_mode: " + raw_mode.toString(),
    			"lazy_mode: " + lazymode.toString(),
    			"selecttimeout: " + selecttimeout.toString(),
    			"hostname_maxlen: " + hostname_maxlen.toString()
    	};
    	return H.join(ret, ", ");
    }

	
    public List<String> cmdarray() {
		List<String> arr = new LinkedList<String>();
		if (password != null) arr.add("-P" + password);
		if (autodetect_frag_size)
			arr.add("-m" + max_downstream_frag_size);
		if (!raw_mode) arr.add("-r");
		if (!lazymode) arr.add("-L");
		arr.add("-I" + selecttimeout);
		arr.add("-M" + hostname_maxlen);
		arr.add("-f");
		arr.add(topdomain);
		if (nameserv_addr != null && !nameserv_addr.equals(""))
			arr.add(nameserv_addr);
		
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
    	if (settingsStrictlyPrivateArray == null) {
    		/* Silently fail if we cannot read previous settings */
    		try {
    			FileInputStream fis = context.openFileInput(settingsFILENAME);
    			try {
    				ObjectInputStream deserializer = new ObjectInputStream(fis);
    				try {
    					settingsStrictlyPrivateArray =
    							(List<Setting>)deserializer.readObject();
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

    		if (settingsStrictlyPrivateArray == null) {
    			settingsStrictlyPrivateArray = new ArrayList<Setting>();
    			settingsStrictlyPrivateArray.add(Setting.default1());
    		}
    	}
    	
    	return settingsStrictlyPrivateArray;
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

    /**
     * Save `this` setting permanently
     * 
     * if setting with this name already exists, overwrite the current one
     * if it doesn't, create a new setting
     * @param c Context
     * @return boolean if save was successful
     */
    public boolean save(Context c, boolean save_as) {
    	if (save_as)
    		settingsStrictlyPrivateArray.add(this);

    	return Setting.save_settings_arr(c);
    }
    
    /**
     * Remove current setting
     * @param c
     * @return if setting was successfully deleted
     */
    public boolean delete(Context c) {
    	boolean ret = settingsStrictlyPrivateArray.remove(this);
    	return ret && Setting.save_settings_arr(c);
    }
    
    /**
     * Save settingsStrictlyPrivateArray to file
     * 
     * @return boolean if save succeeded
     */
    private static boolean save_settings_arr(Context c) {
		FileOutputStream fos;
		ObjectOutputStream serializer;
		
		try {
			fos = c.openFileOutput(settingsFILENAME, Context.MODE_PRIVATE);
		} catch (FileNotFoundException e) {
			Log.e("save_settings_arr", "Could not open objfile for writing");
			e.printStackTrace();
			return false;
		}
		
		try {
			serializer = new ObjectOutputStream(fos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e("save_settings_arr", "Could not create serializer");
			e.printStackTrace();
			return false;
		}
		
		try {
			serializer.writeObject(settingsStrictlyPrivateArray);
		} catch (IOException e) {
			Log.e("save_settings_arr", "Failed to write settings array");
			e.printStackTrace();
			return false;
		}
    	return true;
    }
    
}
