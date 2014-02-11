/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.commands;

import net.bnubot.bot.CommandEventHandler;
import net.bnubot.core.Connection;
import net.bnubot.core.commands.CommandRunnable;
import net.bnubot.core.commands.InvalidUseException;
import net.bnubot.db.Account;
import net.bnubot.util.BNetUser;

/**
 * @author scotta
 */
public final class CommandAdd implements CommandRunnable {
	@Override
	public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
	throws Exception {
		try {
			if(params == null)
				throw new InvalidUseException();
			if(params.length != 2)
				throw new InvalidUseException();

			int targetAccess;
			try {
				targetAccess = Integer.parseInt(params[1]);
			} catch(NumberFormatException e) {
				throw new InvalidUseException();
			}

			Account subjectAccount = CommandEventHandler.findOrCreateAccount(source, user, commanderAccount, params[0], targetAccess);
			CommandEventHandler.setAccountAccess(user, commanderAccount, subjectAccount, targetAccess, superUser, whisperBack);
		} catch(InvalidUseException e) {
			user.sendChat("Use: %trigger%add <account> <access>", whisperBack);
		}
	}
}
