/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.util;

/**
 * @author scotta
 */
public interface OutputLogger {
	void exception(Throwable e);
	void error(Class<?> source, String text);
	void info(Class<?> source, String text);
	void debug(Class<?> source, String text);
}
