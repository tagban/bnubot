/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.commands;

import java.util.List;

import net.bnubot.core.Connection;
import net.bnubot.core.commands.CommandRunnable;
import net.bnubot.core.commands.InvalidUseException;
import net.bnubot.db.Account;
import net.bnubot.db.Mail;
import net.bnubot.util.BNetUser;

/**
 * @author scotta
 */
public final class CommandMailAll implements CommandRunnable {
	@Override
	public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
	throws Exception {
		try {
			//<rank> <message>
			if(param == null)
				throw new InvalidUseException();
			params = param.split(" ", 2);
			if((params.length < 2) || (params[1].length() == 0))
				throw new InvalidUseException();

			int rank = 0;
			try {
				rank = Integer.parseInt(params[0]);
			} catch(Exception e) {
				throw new InvalidUseException();
			}

			String message = "[Sent to ranks " + rank + "+] " + params[1];

			List<Account> rsAccounts = Account.getRanked(rank);
			for(Account a : rsAccounts)
				Mail.send(commanderAccount, a, message);
			user.sendChat("Mail queued for delivery to " + rsAccounts.size() + " accounts", whisperBack);
		} catch(InvalidUseException e) {
			user.sendChat("Use: %trigger%mailall <minimum rank> <message>", whisperBack);
		}
	}
}