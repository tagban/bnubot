/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import net.bnubot.util.Out;
import net.bnubot.util.SortedProperties;
import net.bnubot.vercheck.CurrentVersion;

public class Settings {
	private static final File propsFile = new File("settings.ini");
	private static final Properties props = new SortedProperties();
	private static Boolean anythingChanged = false;
	
	static {
		if(propsFile.exists()) try {
			props.load(new FileInputStream(propsFile));
		} catch(Exception e) {
			Out.exception(e);
		}
	}
	
	private static String getKey(String Header, String Setting) {
		if(Header == null)
			return "general_" + Setting;
		return Header + "_" + Setting;
	}
	
	public static String read(String Header, String Setting, String Default) {
		String s = props.getProperty(getKey(Header, Setting));
		if(s != null)
			return s;
		return Default;
	}
	
	public static boolean readBoolean(String Header, String Setting, boolean Default) {
		return Boolean.parseBoolean(read(Header, Setting, Boolean.toString(Default)));
	}
	
	public static int readInt(String Header, String Setting, int Default) {
		return Integer.parseInt(read(Header, Setting, Integer.toString(Default)));
	}
	
	public static long readLong(String Header, String Setting, long Default) {
		return Long.parseLong(read(Header, Setting, Long.toString(Default)));
	}
	
	public static <T extends Enum<T>> T readEnum(Class<T> enumType, String Header, String Setting, T Default) {
		return Enum.valueOf(enumType, read(Header, Setting, Default.name()));
	}
	
	public static void write(String Header, String Setting, String Value) {
		String key = getKey(Header, Setting);
		if(Value == null)
			Value = new String();
		
		// Don't allow modification of keys unless they haven't changed
		if(props.containsKey(key) && props.getProperty(key).equals(Value))
			return;
		
		anythingChanged = true;
		Out.debug(Settings.class, "Setting " + key + "=" + Value);
		props.setProperty(key, Value);
	}
	
	public static void writeBoolean(String Header, String Setting, boolean Value) {
		write(Header, Setting, Boolean.toString(Value));
	}
	
	public static void writeInt(String Header, String Setting, int Value) {
		write(Header, Setting, Integer.toString(Value));
	}
	
	public static void writeLong(String Header, String Setting, long Value) {
		write(Header, Setting, Long.toString(Value));
	}
	
	public static <T extends Enum<T>> void writeEnum(String Header, String Setting, T Value) {
		write(Header, Setting, Value.name());
	}

	public static void store() {
		if(!anythingChanged)
			return;
		
		Out.debug(Settings.class, "Writing settings.ini");
		
		try {
			// Generate the comment first, because the settings.ini file could be lost if CurrentVersion.version() fails
			String comment = CurrentVersion.version().toString();
			props.store(new FileOutputStream(propsFile), comment);
			anythingChanged = false;
		} catch (Exception e) {
			Out.fatalException(e);
		}
	}

}
