/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.TimeZone;

import net.bnubot.util.Out;
import net.bnubot.util.SortedProperties;
import net.bnubot.vercheck.CurrentVersion;

/**
 * @author scotta
 */
public class Settings {
	public static final File keysFile = new File(getRootPath() + "cdkeys.txt");
	public static final File propsFile = new File(getRootPath() + "settings.ini");
	private static final Properties props = new SortedProperties();
	private static boolean anythingChanged = false;

	static {
		if(propsFile.exists()) try {
			props.load(new FileInputStream(propsFile));
		} catch(Exception e) {
			Out.exception(e);
		}
	}

	private static String getKey(String header, String setting) {
		if(header == null)
			return "general_" + setting;
		return header + "_" + setting;
	}

	public static String getRootPath() {
		String out = System.getProperty("net.bnubot.rootpath");
		if(out != null)
			return out;
		return "";
	}

	public static String read(String header, String setting, String defaultValue) {
		String key = getKey(header, setting);
		if(props.containsKey(key))
			return props.getProperty(key);

		write(key, defaultValue);
		return defaultValue;
	}

	public static boolean read(String header, String setting, boolean defaultValue) {
		return Boolean.parseBoolean(read(header, setting, Boolean.toString(defaultValue)));
	}

	public static int read(String header, String setting, int defaultValue) {
		return Integer.parseInt(read(header, setting, Integer.toString(defaultValue)));
	}

	public static long read(String header, String setting, long defaultValue) {
		return Long.parseLong(read(header, setting, Long.toString(defaultValue)));
	}

	public static TimeZone read(String header, String setting, TimeZone defaultValue) {
		return TimeZone.getTimeZone(read(header, setting, defaultValue.getID()));
	}

	/**
	 * This method will not handle a NULL value for defaultValue
	 * @param header the header name
	 * @param setting the setting name
	 * @param defaultValue the default value
	 * @param <T> the <code>Enum</code> type
	 * @return the value if it exists in settings, or defaultValue if none exists
	 * @throws NullPointerException if <code>defaultValue == null</code>
	 */
	public static <T extends Enum<T>> T read(String header, String setting, T defaultValue) {
		String readValue = read(header, setting, defaultValue.name());
		try {
			return Enum.valueOf(defaultValue.getDeclaringClass(), readValue);
		} catch(Exception e) {
			Out.error(Settings.class, "Invalid " + defaultValue.getDeclaringClass().getSimpleName() + ": " + readValue);
			return defaultValue;
		}
	}

	public static void write(String header, String setting, String value) {
		write(getKey(header, setting), value);
	}

	private static void write(String key, String value) {
		if(value == null)
			value = new String();

		// Don't allow modification of keys unless they haven't changed
		if(props.containsKey(key) && props.getProperty(key).equals(value))
			return;

		anythingChanged = true;
		if(Out.isDebug(Settings.class))
			Out.debugAlways(Settings.class, "setting " + key + "=" + value);
		props.setProperty(key, value);
	}

	public static void write(String header, String setting, boolean value) {
		write(header, setting, Boolean.toString(value));
	}

	public static void write(String header, String setting, int value) {
		write(header, setting, Integer.toString(value));
	}

	public static void write(String header, String setting, long value) {
		write(header, setting, Long.toString(value));
	}

	public static void write(String header, String setting, TimeZone value) {
		write(header, setting, value.getID());
	}

	public static <T extends Enum<T>> void write(String header, String setting, T value) {
		write(header, setting, value.name());
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
