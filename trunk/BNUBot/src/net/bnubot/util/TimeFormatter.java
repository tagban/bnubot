/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeFormatter {
	public static TimeZone timeZone = TimeZone.getDefault();
	public static String tsFormat = "%1$tH:%1$tM:%1$tS.%1$tL";

	/**
	 * Simply displays a nicely formatted timestamp.
	 */
	public static String getTimestamp() {
		try {
			return String.format(tsFormat, Calendar.getInstance(timeZone));
		} catch(NoSuchMethodError e) {
			return "";
		}
	}

	/**
	 * Display a formatted length of time
	 * @param time The time, in milliseconds
	 * @return A String with the formatted length of time
	 */
	public static String formatTime(long time) {
		return formatTime(time, true);
	}

	/**
	 * Display a formatted length of time
	 * @param time The time, in milliseconds
	 * @param ms Whether to display milliseconds
	 * @return A String with the formatted length of time
	 */
	public static String formatTime(long time, boolean ms) {
		if(time < 0)
			throw new IllegalArgumentException("formatTime does not format negative numbers");

		if(time == 0)
			return ms ? "0ms" : "0s";

		String text = "";
		if(ms) {
			if(time < 1000*60) // 60 seconds
				text = Long.toString(time % 1000) + "ms";
			time /= 1000;
		} else {
			time--;
			time /= 1000;
			time++;
		}

		if(time > 0) {
			if(time < 60*60) // 60 minutes
				text = Long.toString(time % 60) + "s " + text;
			time /= 60;
			if(time > 0) {
				if(time < 60*24) // 24 hours
					text = Long.toString(time % 60) + "m " + text;
				time /= 60;
				if(time > 0) {
					if(time < 24*7)	// 7 days
						text = Long.toString(time % 24) + "h " + text;
					time /= 24;
					if(time > 0)
						text = Long.toString(time) + "d " + text;
				}
			}
		}

		return text.trim();
	}

	/**
	 * Converts a Windows FileTime structure to a Date
	 * @param ft contains a 64-bit value representing the number of 100-nanosecond intervals since January 1, 1601 (UTC).
	 * @return a Date with the correct time
	 */
	public static Date fileTime(long ft) {
		// Date.parse("1/1/1601") == 11644455600000L
		long date = ft / 10000 - 11644455600000L;
		date += timeZone.getOffset(date);
		return new Date(date);
	}

	private static DateFormat df = DateFormat.getDateInstance();
	private static DateFormat dtf = DateFormat.getDateTimeInstance();

	public static long parseDate(String d) throws ParseException {
		df.setCalendar(Calendar.getInstance(timeZone));
		return df.parse(d).getTime();
	}

	public static String formatDate(Date d) {
		df.setCalendar(Calendar.getInstance(timeZone));
		return df.format(d);
	}

	public static long parseDateTime(String dt) throws ParseException {
		dtf.setCalendar(Calendar.getInstance(timeZone));
		return dtf.parse(dt).getTime();
	}

	public static String formatDateTime(Date d) {
		dtf.setCalendar(Calendar.getInstance(timeZone));
		return dtf.format(d);
	}
}
