/**
 * This file is a modified version of the Out class from JBLS
 * $Id$
 */

package net.bnubot.util;

import java.io.PrintStream;
import java.util.Calendar;

public class Out {
	private static PrintStream outStream = System.out;
	private static boolean debug = false;

	public static void print(String out) {
		outStream.print(out);
	}

	/**
	 * Displays errors
	 * 
	 * @param source:
	 *            source of the info
	 * @param text:
	 *            text to show
	 */
	public static void error(String source, String text) {
		outStream.println(getTimestamp() + "{" + source + " - Error} " + text);
	}

	/**
	 * Displays debug information, if wanted
	 * 
	 * @param source -
	 *            source of the info
	 * @param text
	 *            -text to show
	 */
	public static void debug(String source, String text) {
		if (debug)
			outStream.println(getTimestamp() + "{" + source + " - Debug} "
					+ text);
	}

	/**
	 * Displays "info"
	 * 
	 * @param source:
	 *            source of the info
	 * @param text:
	 *            text to show
	 */
	public static void info(String source, String text) {
		outStream.println(getTimestamp() + "{" + source + "} " + text);
	}

	/**
	 * Sets the output stream for the information to be displayed to. Can be set
	 * to asdf, admin output stream, file logging, etc..
	 * 
	 * @param s
	 *            PrintStream to send information to.
	 */
	public static void setOutputStream(PrintStream s) {
		outStream = s;
	}

	static public String padString(String str, int length, char c) {
		while (str.length() < length)
			str = c + str;
		return str;
	}

	static public String padNumber(int number, int length) {
		return padString("" + number, length, '0');
	}

	/**
	 * Simply displays a nicely formatted timestamp.
	 */
	public static String getTimestamp() {
		Calendar c = Calendar.getInstance();
		return new StringBuffer()
		.append('[')
		.append(padNumber(c.get(Calendar.HOUR_OF_DAY), 2)).append(':')
		.append(padNumber(c.get(Calendar.MINUTE), 2)).append(':')
		.append(padNumber(c.get(Calendar.SECOND), 2)).append('.')
		.append(padNumber(c.get(Calendar.MILLISECOND), 3))
		.append("] ")
		.toString();
	}

}
