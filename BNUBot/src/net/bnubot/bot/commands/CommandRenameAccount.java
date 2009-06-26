/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.commands;

import net.bnubot.core.Connection;
import net.bnubot.core.commands.AccountDoesNotExistException;
import net.bnubot.core.commands.CommandRunnable;
import net.bnubot.db.Account;
import net.bnubot.logging.Out;
import net.bnubot.util.BNetUser;

/**
 * @author scotta
 */
public final class CommandRenameAccount implements CommandRunnable {
	public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
	throws Exception {
		if((params == null) || (params.length != 2)) {
			user.sendChat("Use: %trigger%renameaccount <old account> <new account>", whisperBack);
			return;
		}

		Account rsSubjectAccount = Account.get(params[0]);
		Account targetAccount = Account.get(params[1]);

		if((targetAccount != null) && !targetAccount.equals(rsSubjectAccount)) {
			user.sendChat("The Account [" + targetAccount.getName() + "] already exists!", whisperBack);
			return;
		}

		if(rsSubjectAccount == null)
			throw new AccountDoesNotExistException(params[0]);

		params[0] = rsSubjectAccount.getName();

		try {
			rsSubjectAccount.setName(params[1]);
			rsSubjectAccount.updateRow();
		} catch(Exception e) {
			Out.exception(e);
			user.sendChat("Rename failed for an unknown reason.", whisperBack);
			return;
		}

		user.sendChat("The account [" + params[0] + "] was successfully renamed to [" + params[1] + "]", whisperBack);
	}
}