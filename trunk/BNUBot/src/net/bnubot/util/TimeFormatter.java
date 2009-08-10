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

/**
 * @author scotta
 */
public class TimeFormatter {
	public static final long SECOND = 1000;
	public static final long MINUTE = 60 * SECOND;
	public static final long HOUR = 60 * MINUTE;
	public static final long DAY = 24 * HOUR;
	public static final long WEEK = 7 * DAY;

	public static TimeZone timeZone = TimeZone.getDefault();
	public static String tsFormat = "%1$tH:%1$tM:%1$tS.%1$tL";

	public static Calendar getCalendar() {
		return Calendar.getInstance(timeZone);
	}

	/**
	 * @return a nicely formatted timestamp
	 */
	public static String getTimestamp() {
		try {
			return String.format(tsFormat, getCalendar());
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
			return "(neg) " + formatTime(time * -1, ms);

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

	public static long parseDuration(String duration)
	throws NumberFormatException {
		long out = 0;
		for(String part : duration.split(" ")) {
			if(part.length() == 0)
				continue;

			int i = 0;
			for(; i < part.length(); i++) {
				char c = part.charAt(i);
				if((c < '0') || (c > '9'))
					break;
			}
			long base = Long.parseLong(part.substring(0, i));
			String unit = part.substring(i).toLowerCase();

			switch(unit.charAt(0)) {
			case 's':
				if("s".equals(unit)
				|| "second".equals(unit)
				|| "seconds".equals(unit)) {
					out += base * SECOND;
					continue;
				}
				break;
			case 'm':
				if("m".equals(unit)
				|| "min".equals(unit)
				|| "mins".equals(unit)
				|| "minute".equals(unit)
				|| "minutes".equals(unit)) {
					out += base * MINUTE;
					continue;
				}
				break;
			case 'h':
				if("h".equals(unit)
				|| "hour".equals(unit)
				|| "hours".equals(unit)) {
					out += base * HOUR;
					continue;
				}
				break;
			case 'd':
				if("d".equals(unit)
				|| "day".equals(unit)
				|| "days".equals(unit)) {
					out += base * DAY;
					continue;
				}
				break;
			case 'w':
				if("w".equals(unit)
				|| "week".equals(unit)
				|| "weeks".equals(unit)) {
					out += base * WEEK;
					continue;
				}
				break;
			}
			throw new NumberFormatException("Invalid unit: " + unit);
		}
		return out;
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
		df.setCalendar(getCalendar());
		return df.parse(d).getTime();
	}

	public static String formatDate(Date d) {
		df.setCalendar(getCalendar());
		return df.format(d);
	}

	public static long parseDateTime(String dt) throws ParseException {
		dtf.setCalendar(getCalendar());
		return dtf.parse(dt).getTime();
	}

	public static String formatDateTime(Date d) {
		dtf.setCalendar(getCalendar());
		return dtf.format(d);
	}
}
