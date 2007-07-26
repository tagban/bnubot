/**
 * This file is distributed under the GPL 
 * $Id$
 */

package bnubot.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import bnubot.Version;

public class Settings {
	private static File propsFile = null;
	private static Properties props = null;
	private static final String comments = "BNUBot " + Version.version();
	
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
		
		props.setProperty(key, Value);
	}

	public static void store() {
		init();
		
		try {
			FileOutputStream fos = new FileOutputStream(propsFile);
			props.store(fos, comments);
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
