/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.commands;

import net.bnubot.core.Connection;
import net.bnubot.core.commands.AccountDoesNotExistException;
import net.bnubot.core.commands.CommandRunnable;
import net.bnubot.db.Account;
import net.bnubot.db.BNLogin;
import net.bnubot.util.BNetUser;

/**
 * @author scotta
 */
public final class CommandSetAccount implements CommandRunnable {
	public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
	throws Exception {
		if((params == null) || (params.length < 1) || (params.length > 2)) {
			user.sendChat("Use: %trigger%setaccount <user>[@<realm>] [<account>]", whisperBack);
			return;
		}

		BNetUser bnSubject = source.getCreateBNetUser(params[0], user);
		BNLogin rsSubject = BNLogin.get(bnSubject);
		if(rsSubject == null) {
			user.sendChat("I have never seen [" + bnSubject.getFullAccountName() + "] in the channel", whisperBack);
			return;
		}
		String subject = rsSubject.getLogin();

		Account newAccount = null;
		if(params.length == 2) {
			newAccount = Account.get(params[1]);
			if(newAccount == null)
				throw new AccountDoesNotExistException(params[1]);
		}

		rsSubject.setAccount(newAccount);
		rsSubject.updateRow();

		// Set params[1] to what the account looks like in the database
		if(newAccount == null)
			params = new String[] { params[0], "NULL" };
		else
			params[1] = newAccount.getName();

		bnSubject.resetPrettyName();
		user.sendChat("User [" + subject + "] was added to account [" + params[1] + "] successfully.", whisperBack);
	}
}