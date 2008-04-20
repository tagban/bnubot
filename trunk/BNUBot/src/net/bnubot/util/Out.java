/**
 * This file is a modified version of the Out class from JBLS
 * $Id$
 */

package net.bnubot.util;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import net.bnubot.bot.gui.GuiDesktop;
import net.bnubot.core.Connection;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.settings.Settings;

/**
 * An output class, modified from JBLS
 * @author hdx
 * @author scotta
 */
public class Out {
	private static PrintStream outStream = System.out;
	private static final ThreadLocal<Connection> outConnection = new ThreadLocal<Connection>();
	private static Connection outConnectionDefault = null;
	private static boolean globalDebug = Settings.read(null, "debug", false);
	private static boolean debugToGui = Settings.read(null, "debugToGui", true);
	private static final Properties debug = new SortedProperties();
	private static final File debugFile = new File("debug.properties");
	static {
		try {
			if(debugFile.exists())
				debug.load(new FileInputStream(debugFile));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get the lines of the stack trace relevant to the project
	 * @param e The exception source
	 * @return Each line of the exception starting with net.bnubot, and ellipses where lines were trimmed
	 */
	private static String getRelevantStack(Throwable e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		String lines[] = sw.toString().trim().split("\n");

		String out = lines[0];
		boolean ellipsis = false;
		for(String line : lines) {
			line = line.trim();
			if(line.startsWith("at net.bnubot.")
			|| line.startsWith("Caused by:")) {
				out += "\n" + line;
				ellipsis = false;
			} else if(!ellipsis) {
				ellipsis = true;
				out += "\n...";
			}
		}
		return out;
	}

	private static Connection getOutConnection() {
		final Connection oc = outConnection.get();
		if(oc != null)
			return oc;
		return outConnectionDefault;
	}

	/**
	 * Display the stack trace in an appropriate location
	 * @param e The exception source
	 */
	public static void exception(Throwable e) {
		final Connection oc = getOutConnection();
		if(oc != null)
			error(e.getClass(), e.getMessage());
		if(outStream != null)
			e.printStackTrace(outStream);
		else
			e.printStackTrace();
	}

	/**
	 * Display a popup with the exception
	 * @param e
	 */
	public static void popupException(Throwable e, Component parent) {
		JDialog jd = null;
		boolean isModal = false;
		if((parent != null) && (parent instanceof JDialog)) {
			jd = (JDialog)parent;
			isModal = jd.isModal();
			jd.setModal(false);
		}

		JOptionPane.showMessageDialog(
				parent,
				getRelevantStack(e),
				e.getClass().getName(),
				JOptionPane.ERROR_MESSAGE);

		if(isModal)
			jd.setModal(true);

		if(outStream != null)
			e.printStackTrace(outStream);
		else
			e.printStackTrace();
	}

	/**
	 * Attempt to popup a window with a stack trace, and exit with code 1
	 * @param e The exception source
	 */
	public static void fatalException(Throwable e) {
		try {
			JOptionPane.showMessageDialog(null, getRelevantStack(e), e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
		} catch(Exception e1) {}
		if(outStream != null)
			e.printStackTrace(outStream);
		else
			e.printStackTrace();
		System.exit(1);
	}

	/**
	 * Displays error messages
	 * @param source source of the info
	 * @param text text to show
	 */
	public static void error(Class<?> source, String text) {
		final Connection oc = getOutConnection();
		if(oc != null)
			oc.recieveError("(" + source.getSimpleName() + ") " + text);
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
		final Connection oc = getOutConnection();
		if(debugToGui && (oc != null))
			oc.recieveDebug("(" + source.getSimpleName() + ") " + text);
		else if(outStream != null)
			outStream.println("[" + TimeFormatter.getTimestamp() + "] (" + source.getSimpleName() + ") DEBUG " + text);
	}

	/**
	 * Displays information
	 * @param source source of the info
	 * @param text text to show
	 */
	public static void info(Class<?> source, String text) {
		final Connection oc = getOutConnection();
		if(oc != null)
			oc.recieveInfo("(" + source.getSimpleName() + ") " + text);
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
	 * Sets the Connection for the information to be displayed to for this thread.
	 * @param g Connection to send messages to
	 */
	public static void setThreadOutputConnection(Connection g) {
		outConnection.set(g);
	}

	/**
	 * Sets the Connection for the information to be displayed to for this thread if none is already set.
	 * @param g Connection to send messages to
	 */
	public static void setThreadOutputConnectionIfNone(Connection g) {
		if(outConnection.get() == null)
			outConnection.set(g);
	}

	/**
	 * Sets the Connection for the information to be displayed to when none is specified for the thread
	 * @param g Connection to send messages to
	 */
	public static void setDefaultOutputConnection(Connection g) {
		outConnectionDefault = g;
	}

	/**
	 * Sets whether debugging messages should be shown
	 * @param debug true means debugging messages will be shown
	 */
	public static void setDebug(boolean debug) {
		if(globalDebug == debug)
			return;
		globalDebug = debug;
		try {
			if(GlobalSettings.enableGUI)
				GuiDesktop.updateDebugMenuText();
			debug(Out.class, "Debug logging " + (debug ? "en" : "dis") + "abled");
			Settings.write(null, "debug", debug);
			Settings.store();
		} catch(Throwable t) {}
	}

	/**
	 * Sets whether debugging messages should be shown for a given class
	 * @param debug true means debugging messages will be shown
	 */
	public static void setDebug(String clazz, boolean debug) {
		if(Out.debug.containsKey(clazz)) {
			boolean current = Boolean.parseBoolean(Out.debug.getProperty(clazz));
			if(current == debug)
				return;
		}
		Out.debug.setProperty(clazz, Boolean.toString(debug));
		debug(Out.class, "Debug logging {" + clazz + "} " + (debug ? "en" : "dis") + "abled");
		try {
			Out.debug.store(new FileOutputStream(debugFile), null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets whether debugging messages should be shown
	 * @return true when debugging messages will be shown
	 */
	public static boolean isDebug() {
		return globalDebug;
	}

	/**
	 * Sets whether debugging messages should be shown on the GUI
	 * @param debug true means debugging messages will be shown on the GUI
	 */
	public static void setDebugToGui(boolean debug) {
		if(debugToGui == debug)
			return;
		debugToGui = debug;
		Settings.write(null, "debugToGui", debugToGui);
		Settings.store();
	}

	/**
	 * Gets whether debugging messages should be shown on the GUI
	 * @return true when debugging messages will be shown on the GUI
	 */
	public static boolean isDebugToGui() {
		return debugToGui;
	}

	/**
	 * Gets whether debugging messages should be shown for a given class
	 * @return true when debugging messages will be shown
	 */
	public static boolean isDebug(Class<?> clazz) {
		return(isDebug(clazz.getName()));
	}

	/**
	 * Gets whether debugging messages should be shown for a given class
	 * @return true when debugging messages will be shown
	 */
	public static boolean isDebug(String clazz) {
		if(!globalDebug)
			return false;
		if(debug.containsKey(clazz))
			return Boolean.parseBoolean(debug.getProperty(clazz));
		setDebug(clazz, true);
		return true;
	}

	public static Properties getProperties() {
		return debug;
	}
}
