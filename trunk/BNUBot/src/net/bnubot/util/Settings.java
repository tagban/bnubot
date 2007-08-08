/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import net.bnubot.vercheck.CurrentVersion;

public class Settings {
	private static File propsFile = null;
	private static Properties props = null;
	private static Boolean anythingChanged = false;
	
	private static void init() {
		if(props != null)
			return;
		
		props = new SortedProperties();
		propsFile = new File("settings.ini");
		
		if(propsFile.exists()) try {
			props.load(new FileInputStream(propsFile));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static String getKey(String Header, String Setting) {
		return Header + "_" + Setting;
	}
	
	public static String read(String Header, String Setting, String Default) {
		init();
		
		String s = props.getProperty(getKey(Header, Setting));
		if(s != null)
			return s;
		return Default;
	}
	
	public static void write(String Header, String Setting, String Value) {
		init();
		
		String key = getKey(Header, Setting);
		if(Value == null)
			Value = new String();
		
		// Don't allow modification of keys unless they haven't changed
		if(props.containsKey(key) && props.getProperty(key).equals(Value))
			return;
		
		anythingChanged = true;
		System.out.println("Setting " + key + "=" + Value);
		props.setProperty(key, Value);
	}

	public static void store() {
		if(!anythingChanged)
			return;
		
		init();
		
		System.out.println("Writing settings.ini");
		
		try {
			// Generate the comment first, because the settings.ini file could be lost if CurrentVersion.version() fails
			String comment = CurrentVersion.version().toString();
			props.store(new FileOutputStream(propsFile), comment);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
