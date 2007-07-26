/**
 * This file is distributed under the GPL 
 * $Id$
 */

package bnubot.util;

import java.util.Date;

public class TimeFormatter {

	public static String formatTime(long time) {
		time /= 1000;
		String text = Long.toString(time % 60) + "s";
		time /= 60;
		if(time > 0) {
			text = Long.toString(time % 60) + "m " + text;
			time /= 60;
			if(time > 0) {
				text = Long.toString(time % 24) + "h " + text;
				time /= 24;
				if(time > 0)
					text = Long.toString(time) + "d " + text;
			}
		}
		
		return text;
	}
	
	public static Date fileTime(long ft) {
		//time /= 10000;
		//time -= 11644455600000L; //Date.parse("1/1/1601");
		return new Date(ft / 10000 - 11644455600000L);
	}
}
