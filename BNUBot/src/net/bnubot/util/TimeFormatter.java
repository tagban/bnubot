/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

public class TimeFormatter {

	public static String formatTime(long time) {
		String text = "";
		time /= 1000;
		if(time < 60*60)
			text = Long.toString(time % 60) + "s";
		time /= 60;
		if(time > 0) {
			if(time < 60*24)
				text = Long.toString(time % 60) + "m " + text;
			time /= 60;
			if(time > 0) {
				if(time < 24*7)
					text = Long.toString(time % 24) + "h " + text;
				time /= 24;
				if(time > 0)
					text = Long.toString(time) + "d " + text;
			}
		}
		
		return text.trim();
	}
	
	public static Date fileTime(long ft) {
		// Contains a 64-bit value representing the number of 100-nanosecond intervals since January 1, 1601 (UTC).
		//time /= 10000;
		//time -= 11644455600000L; //Date.parse("1/1/1601");
		return new Date(ft / 10000 - 11644455600000L);
	}

	private static DateFormat df = DateFormat.getDateInstance();
	private static DateFormat dtf = DateFormat.getDateTimeInstance();
	
	public static long parseDate(String d) throws ParseException {
		return df.parse(d).getTime();
	}
	
	public static String formatDate(Date d) {
		return df.format(d);
	}
	
	public static long parseDateTime(String dt) throws ParseException {
		return dtf.parse(dt).getTime();
	}
	
	public static String formatDateTime(Date d) {
		return dtf.format(d);
	}
}
