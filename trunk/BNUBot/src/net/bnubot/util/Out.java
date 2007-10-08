/**
 * This file is a modified version of the Out class from JBLS
 * $Id$
 */

package net.bnubot.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import javax.swing.JOptionPane;

import net.bnubot.bot.gui.GuiEventHandler;
import net.bnubot.settings.Settings;

/**
 * An output class, modified from JBLS
 * @author hdx
 * @author scotta
 */
public class Out {
	private static PrintStream outStream = System.out;
	private static GuiEventHandler outConnection = null;
	private static boolean globalDebug = Boolean.parseBoolean(Settings.read(null, "debug", "false"));
	private static Properties debug = new SortedProperties();
	private static File debugFile = new File("debug.properties");
	static {
		try {
			if(!debugFile.exists())
				debugFile.createNewFile();
			debug.load(new FileInputStream(debugFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get the lines of the stack trace relevant to the project
	 * @param e The exception source
	 * @return Each line of the exception starting with net.bnubot, and ellipsies where lines were trimmed
	 */
	private static String getRelevantStack(Exception e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		String lines[] = sw.toString().trim().split("\n");
		
		String out = lines[0];
		boolean ellipsis = false;
		for(String line : lines) {
			line = line.trim();
			if(line.startsWith("at net.bnubot.")) {
				out += "\n" + line;
				ellipsis = false;
			} else if(!ellipsis) {
				ellipsis = true;
				out += "\n...";
			}
		}	
		return out;
	}
	
	/**
	 * Display the stack trace in an appropriate location
	 * @param e The exception source
	 */
	public static void exception(Exception e) {
		if(outConnection != null)
			outConnection.recieveError(getRelevantStack(e));
		if(outStream != null)
			e.printStackTrace(outStream);
		else
			e.printStackTrace();
	}
	
	/**
	 * Attempt to popup a window with a stack trace, and exit with code 1
	 * @param e The exception source
	 */
	public static void fatalException(Exception e) {
		try {
			JOptionPane.showMessageDialog(null, getRelevantStack(e), e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
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
		Settings.write(null, "debug", Boolean.toString(debug));
		Settings.store();
	}

	/**
	 * Sets whether debugging messages should be shown for a given class
	 * @param debug true means debugging messages will be shown
	 */
	public static void setDebug(Class<?> c, boolean debug) {
		Out.debug.setProperty(c.getName(), Boolean.toString(debug));
		try {
			Out.debug.store(new FileOutputStream(debugFile), null);
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

	public static Properties getProperties() {
		return debug;
	}
}
