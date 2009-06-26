/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.core.commands;

/**
 * @author scotta
 */
public class CommandFailedWithDetailsException extends Exception {
	private static final long serialVersionUID = 7154697122440166638L;

	public CommandFailedWithDetailsException(String string) {
		super(string);
	}

}
