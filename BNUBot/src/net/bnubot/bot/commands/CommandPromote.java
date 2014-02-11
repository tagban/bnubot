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
public final class CommandPromote implements CommandRunnable {
	@Override
	public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
	throws Exception {
		try {
			if(params == null)
				throw new InvalidUseException();
			if(params.length != 1)
				throw new InvalidUseException();

			Account subjectAccount = CommandEventHandler.findOrCreateAccount(source, user, commanderAccount, params[0], 0);

			int targetAccess = subjectAccount.getAccess() + 1;
			CommandEventHandler.setAccountAccess(user, commanderAccount, subjectAccount, targetAccess, superUser, whisperBack);
		} catch(InvalidUseException e) {
			user.sendChat("Use: %trigger%promote <account>", whisperBack);
		}
	}
}
