/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.commands;

/**
 * @author scotta
 */
public class CommandDoesNotExistException extends Exception {
	private static final long serialVersionUID = -6255521542883806228L;
	public CommandDoesNotExistException(String string) {
		super(string);
	}
}