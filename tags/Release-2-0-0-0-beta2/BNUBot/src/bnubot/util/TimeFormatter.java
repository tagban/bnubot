package bnubot.util;

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
}
