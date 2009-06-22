/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.settings;

import java.util.TimeZone;

import net.bnubot.logging.Out;

/**
 * @author scotta
 */
public class SettingsSection {
	private final String header;

	public SettingsSection(String header) {
		this.header = header;
	}

	private String getKey(String setting) {
		if(header == null)
			return "general_" + setting;
		return header + "_" + setting;
	}

	public String read(String setting, String defaultValue) {
		return Settings.read(getKey(setting), defaultValue);
	}

	public boolean read(String setting, boolean defaultValue) {
		return Boolean.parseBoolean(read(setting, Boolean.toString(defaultValue)));
	}

	public int read(String setting, int defaultValue) {
		return Integer.parseInt(read(setting, Integer.toString(defaultValue)));
	}

	public long read(String setting, long defaultValue) {
		return Long.parseLong(read(setting, Long.toString(defaultValue)));
	}

	public TimeZone read(String setting, TimeZone defaultValue) {
		return TimeZone.getTimeZone(read(setting, defaultValue.getID()));
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
	public <T extends Enum<T>> T read(String setting, T defaultValue) {
		String readValue = read(setting, defaultValue.name());
		try {
			return Enum.valueOf(defaultValue.getDeclaringClass(), readValue);
		} catch(Exception e) {
			Out.error(Settings.class, "Invalid " + defaultValue.getDeclaringClass().getSimpleName() + ": " + readValue);
			return defaultValue;
		}
	}

	public void write(String setting, String value) {
		Settings.write(getKey(setting), value);
	}

	public void write(String setting, boolean value) {
		write(setting, Boolean.toString(value));
	}

	public void write(String setting, int value) {
		write(setting, Integer.toString(value));
	}

	public void write(String setting, long value) {
		write(setting, Long.toString(value));
	}

	public void write(String setting, TimeZone value) {
		write(setting, value.getID());
	}

	public <T extends Enum<T>> void write(String setting, T value) {
		write(setting, value.name());
	}

}
