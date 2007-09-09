/**
 * This file is a modified version of the Out class from JBLS
 * $Id$
 */

package net.bnubot.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import javax.swing.JOptionPane;

import net.bnubot.bot.gui.GuiEventHandler;

public class Out {
	private static PrintStream outStream = System.out;
	private static GuiEventHandler outConnection = null;
	private static boolean globalDebug = false;
	private static Properties debug = null;
	private static File debugFile = new File("debug.properties");
	static {
		debug = new SortedProperties();
		try {
			if(!debugFile.exists())
				debugFile.createNewFile();
			debug.load(new FileReader(debugFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void exception(Exception e) {
		if(outConnection != null) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			outConnection.recieveError(sw.toString());
		} else if(outStream != null)
			e.printStackTrace(outStream);
		else
			e.printStackTrace();
	}
	
	public static void fatalException(Exception e) {
		try {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			JOptionPane.showMessageDialog(null, sw.toString(), e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
		} catch(Exception e1) {}
		e.printStackTrace();
		System.exit(1);
	}

	/**
	 * Displays error messages
	 * @param source source of the info
	 * @param text text to show
	 */
	public static void error(Class<?> source, String text) {
		if(outConnection != null)
			outConnection.recieveError("(" + source.getSimpleName() + ") " + text);
		else if(outStream != null)
			outStream.println("[" + TimeFormatter.getTimestamp() + "] (" + source.getSimpleName() + ") ERROR " + text);
	}

	/**
	 * Displays debugging information if debug has been set
	 * @param source source of the info
	 * @param text text to show
	 */
	public static void debug(Class<?> source, String text) {
		if(isDebug(source))
			debugAlways(source, text);
	}

	/**
	 * Displays debugging information
	 * @param source source of the info
	 * @param text text to show
	 */
	public static void debugAlways(Class<?> source, String text) {
		if(outConnection != null)
			outConnection.recieveDebug("(" + source.getSimpleName() + ") " + text);
		else if(outStream != null)
			outStream.println("[" + TimeFormatter.getTimestamp() + "] (" + source.getSimpleName() + ") DEBUG " + text);
	}
	
	/**
	 * Displays information
	 * @param source source of the info
	 * @param text text to show
	 */
	public static void info(Class<?> source, String text) {
		if(outConnection != null)
			outConnection.recieveInfo("(" + source.getSimpleName() + ") " + text);
		else if(outStream != null)
			outStream.println("[" + TimeFormatter.getTimestamp() + "] (" + source.getSimpleName() + ") INFO " + text);
	}

	/**
	 * Sets the output stream for the information to be displayed to. Can be set
	 * to asdf, admin output stream, file logging, etc..
	 * @param s PrintStream to send information to.
	 */
	public static void setOutputStream(PrintStream s) {
		outStream = s;
	}
	
	/**
	 * Sets the GuiEventHandler for the information to be displayed to.
	 * @param g GuiEventHandler to send messages to
	 */
	public static void setOutputConnection(GuiEventHandler g) {
		outConnection = g;
	}

	/**
	 * Sets whether debugging messages should be shown
	 * @param debug true means debugging messages will be shown
	 */
	public static void setDebug(boolean debug) {
		globalDebug = debug;
		info(Out.class, "Debug logging " + (debug ? "en" : "dis") + "abled");
	}

	/**
	 * Sets whether debugging messages should be shown for a given class
	 * @param debug true means debugging messages will be shown
	 */
	public static void setDebug(Class<?> c, boolean debug) {
		Out.debug.setProperty(c.getName(), Boolean.toString(debug));
		try {
			Out.debug.store(new FileWriter(debugFile), null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		info(Out.class, "Debug logging {" + c.getName() + "} " + (debug ? "en" : "dis") + "abled");
	}

	/**
	 * Gets whether debugging messages should be shown
	 * @return true when debugging messages will be shown
	 */
	public static boolean isDebug() {
		return globalDebug;
	}

	/**
	 * Gets whether debugging messages should be shown for a given class
	 * @return true when debugging messages will be shown
	 */
	public static boolean isDebug(Class<?> c) {
		if(!globalDebug)
			return false;
		if(debug.containsKey(c.getName()))
			return Boolean.parseBoolean(debug.getProperty(c.getName()));
		setDebug(c, true);
		return true;
	}

}
