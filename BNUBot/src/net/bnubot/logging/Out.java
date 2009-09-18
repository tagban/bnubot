/**
 * This file is a modified version of the Out class from JBLS
 * $Id$
 */

package net.bnubot.logging;

import java.awt.Component;
import java.awt.Dialog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import javax.swing.JOptionPane;

import net.bnubot.bot.gui.GuiDesktop;
import net.bnubot.core.PluginManager;
import net.bnubot.settings.Settings;
import net.bnubot.util.SortedProperties;
import net.bnubot.util.UnloggedException;

/**
 * An output class, modified from JBLS
 * @author hdx
 * @author scotta
 */
public class Out {
	private static final OutputLoggerCollection outLogger = new OutputLoggerCollection(System.out);
	private static final ThreadLocal<OutputHandler> outHandler = new ThreadLocal<OutputHandler>();
	private static OutputHandler outHandlerDefault = null;
	private static boolean globalDebug = Settings.getSection(null).read("debug", false);
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

	private static OutputHandler getOutputHandler() {
		Thread t = Thread.currentThread();
		if(t instanceof OutputHandler)
			return (OutputHandler)t;
		final OutputHandler oh = outHandler.get();
		if(oh != null)
			return oh;
		return outHandlerDefault;
	}

	/**
	 * Display the stack trace in an appropriate location
	 * @param e the <code>Throwable</code> source
	 */
	public static void exception(Throwable e) {
		final OutputHandler oh = getOutputHandler();
		if(oh != null)
			error(e.getClass(), getRelevantStack(e));
		// Do not log UnloggedExceptions
		if(!(e instanceof UnloggedException))
			outLogger.exception(e);
	}

	/**
	 * Display a popup with the exception
	 * @param e the <code>Throwable</code> source
	 * @param parent determines the <code>Frame</code>
     *		in which the dialog is displayed; if <code>null</code>,
     *		or if the <code>parentComponent</code> has no
     *		<code>Frame</code>, a default <code>Frame</code> is used
	 */
	public static void popupException(Throwable e, Component parent) {
		Dialog dialog = null;
		boolean isModal = false;
		if((parent != null) && (parent instanceof Dialog)) {
			dialog = (Dialog)parent;
			isModal = dialog.isModal();
			dialog.setModal(false);
		}

		JOptionPane.showMessageDialog(
				parent,
				getRelevantStack(e),
				e.getClass().getName(),
				JOptionPane.ERROR_MESSAGE);

		if(isModal)
			dialog.setModal(true);

		outLogger.exception(e);
	}

	/**
	 * Attempt to popup a window with a stack trace
	 * @param e the <code>Throwable</code> source
	 */
	public static void popupException(Throwable e) {
		outLogger.exception(e);
		try {
			JOptionPane.showMessageDialog(
					null,
					"A fatal error has occurred:\n\n" + getRelevantStack(e),
					e.getClass().getSimpleName(),
					JOptionPane.ERROR_MESSAGE);
		} catch(Exception e1) {}
	}

	/**
	 * Attempt to popup a window with a stack trace, and exit with code 1
	 * @param e the <code>Throwable</code> source
	 */
	public static void fatalException(Throwable e) {
		popupException(e);
		System.exit(1);
	}

	/**
	 * Displays error messages
	 * @param source source of the info
	 * @param text text to show
	 */
	public static void error(Class<?> source, String text) {
		final OutputHandler oh = getOutputHandler();
		if(oh != null)
			oh.dispatchRecieveError("(" + source.getSimpleName() + ") " + text);
		else
			outLogger.error(source, text);
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
		final OutputHandler oh = getOutputHandler();
		if(oh != null)
			oh.dispatchRecieveDebug("(" + source.getSimpleName() + ") " + text);
		else
			outLogger.debug(source, text);
	}

	/**
	 * Displays information
	 * @param source source of the info
	 * @param text text to show
	 */
	public static void info(Class<?> source, String text) {
		final OutputHandler oh = getOutputHandler();
		if(oh != null)
			oh.dispatchRecieveInfo("(" + source.getSimpleName() + ") " + text);
		else
			outLogger.info(source, text);
	}

	/**
	 * Sets the output logger for the information to be displayed to
	 * @param ol OutputLogger to send log data to
	 */
	public static void addOutputLogger(OutputLogger ol) {
		outLogger.addLogger(ol);
	}

	/**
	 * Sets the OutputHandler for the information to be displayed to for this thread.
	 * @param oh OutputHandler to send messages to
	 */
	public static void setThreadOutputHandler(OutputHandler oh) {
		outHandler.set(oh);
	}

	/**
	 * Sets the OutputHandler for the information to be displayed to when none is specified for the thread
	 * @param oh OutputHandler to send messages to
	 */
	public static void setDefaultOutputHandler(OutputHandler oh) {
		outHandlerDefault = oh;
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
			if(PluginManager.getEnableGui())
				GuiDesktop.updateDebugMenuChecked();
			debug(Out.class, "Debug logging " + (debug ? "en" : "dis") + "abled");
			Settings.getSection(null).write("debug", debug);
			Settings.store();
		} catch(Throwable t) {}
	}

	/**
	 * Sets whether debugging messages should be shown for a given class
	 * @param clazz the <code>Class</code> to set debugging for
	 * @param debug <code>true</code> means debugging messages will be shown
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
	 * Gets whether debugging messages should be shown for a given class
	 * @param clazz the given <code>Class</code>
	 * @return true when debugging messages will be shown
	 */
	public static boolean isDebug(Class<?> clazz) {
		return isDebug(clazz.getName());
	}

	/**
	 * Gets whether debugging messages should be shown for a given class
	 * @param clazz the given <code>Class</code>
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
