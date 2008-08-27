/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.commands;

/**
 * @author scotta
 */
public class AccountDoesNotExistException extends Exception {
	private static final long serialVersionUID = 8234222521214115822L;
	public AccountDoesNotExistException(String string) {
		super(string);
	}
}