/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.logging;

/**
 * An {@link OutputHandler} is a device that displays information the
 * average user wants to see, which is less verbose than a
 * logfile, containing no exceptions and limited debug messages
 * @author scotta
 */
public interface OutputHandler {
	void dispatchRecieveDebug(String error);
	void dispatchRecieveInfo(String error);
	void dispatchRecieveError(String error);
}
