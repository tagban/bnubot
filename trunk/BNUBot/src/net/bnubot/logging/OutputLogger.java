/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.logging;

/**
 * An {@link OutputLogger} is a device that displays information that
 * might be useful for debugging purposes. This interface is used by
 * the logging package when it wants to write to a log file.
 * @author scotta
 */
public interface OutputLogger {
	void exception(Throwable e);
	void error(Class<?> source, String text);
	void info(Class<?> source, String text);
	void debug(Class<?> source, String text);
}
