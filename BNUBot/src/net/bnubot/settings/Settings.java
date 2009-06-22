/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import net.bnubot.logging.Out;
import net.bnubot.util.SortedProperties;
import net.bnubot.vercheck.CurrentVersion;

/**
 * @author scotta
 */
public class Settings {
	public static final File keysFile = new File(getRootPath() + "cdkeys.txt");
	private static final File propsFile = new File(getRootPath() + "settings.ini");
	private static final Properties props = new SortedProperties();
	private static boolean anythingChanged = false;

	static {
		synchronized(propsFile) {
			if(propsFile.exists()) try {
				props.load(new FileInputStream(propsFile));
			} catch(Exception e) {
				Out.exception(e);
			}
		}
	}

	public static SettingsSection getSection(String header) {
		return new SettingsSection(header);
	}

	public static String getRootPath() {
		String out = System.getProperty("net.bnubot.rootpath");
		if(out != null)
			return out;
		return "";
	}

	protected static String read(String key, String defaultValue) {
		if(props.containsKey(key))
			return props.getProperty(key);

		write(key, defaultValue);
		return defaultValue;
	}

	protected static void write(String key, String value) {
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

	public static void store() {
		if(!anythingChanged)
			return;

		Out.debug(Settings.class, "Writing settings.ini");

		try {
			// Generate the comment first, because the settings.ini file could be lost if CurrentVersion.version() fails
			String comment = CurrentVersion.version().toString();
			synchronized(propsFile) {
				props.store(new FileOutputStream(propsFile), comment);
			}
			anythingChanged = false;
		} catch (Exception e) {
			Out.fatalException(e);
		}
	}

}
