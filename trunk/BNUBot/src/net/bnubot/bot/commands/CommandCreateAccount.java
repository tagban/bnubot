/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.commands;

import net.bnubot.core.Connection;
import net.bnubot.core.commands.CommandFailedWithDetailsException;
import net.bnubot.core.commands.CommandRunnable;
import net.bnubot.db.Account;
import net.bnubot.db.Rank;
import net.bnubot.util.BNetUser;

/**
 * @author scotta
 */
public final class CommandCreateAccount implements CommandRunnable {
	@Override
	public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
	throws Exception {
		if((params == null) || (params.length != 1)) {
			user.sendChat("Use: %trigger%createaccount <account>", whisperBack);
			return;
		}

		Account rsAccount = Account.get(params[0]);
		if(rsAccount != null)
			throw new CommandFailedWithDetailsException("The account [" + rsAccount.getName() + "] already exists");

		rsAccount = Account.create(params[0], Rank.get(0), commanderAccount);
		if(rsAccount == null)
			throw new CommandFailedWithDetailsException("Failed to create account [" + params[0] + "] for an unknown reason");
		user.sendChat("The account [" + params[0] + "] has been created", whisperBack);
	}
}
