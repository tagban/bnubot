/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.util;

/**
 * @author scotta
 */
public interface OutputHandler {
	void dispatchRecieveDebug(String error);
	void dispatchRecieveInfo(String error);
	void dispatchRecieveError(String error);
}
