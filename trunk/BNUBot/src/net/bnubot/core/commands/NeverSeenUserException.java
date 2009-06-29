/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.core.commands;

import net.bnubot.db.Account;
import net.bnubot.util.BNetUser;

/**
 * @author scotta
 */
public class NeverSeenUserException extends CommandFailedWithDetailsException {
	private static final long serialVersionUID = 7382526151395525439L;

	public NeverSeenUserException(String user) {
		super("I have never seen " + user + " in the channel");
	}

	public NeverSeenUserException(BNetUser user) {
		this(user.getFullLogonName());
	}

	public NeverSeenUserException(Account user) {
		this(user.getName());
	}

}
