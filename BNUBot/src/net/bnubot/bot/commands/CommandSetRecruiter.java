/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.commands;

import net.bnubot.core.Connection;
import net.bnubot.core.commands.AccountDoesNotExistException;
import net.bnubot.core.commands.CommandRunnable;
import net.bnubot.db.Account;
import net.bnubot.util.BNetUser;

/**
 * @author scotta
 */
public final class CommandSetRecruiter implements CommandRunnable {
	@Override
	public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
	throws Exception {
		if((params == null) || (params.length != 2)) {
			user.sendChat("Use: %trigger%setrecruiter <account> <account>", whisperBack);
			return;
		}

		Account rsSubject = Account.get(params[0]);
		if(rsSubject == null)
			throw new AccountDoesNotExistException(params[0]);
		params[0] = rsSubject.getName();

		Account rsTarget = Account.get(params[1]);
		if(rsTarget == null)
			throw new AccountDoesNotExistException(params[1]);
		params[1] = rsTarget.getName();

		String recursive = params[0];

		Account cb = rsTarget;
		do {
			recursive += " -> " + cb.getName();
			if(cb == rsSubject) {
				user.sendChat("Recursion detected: " + recursive, whisperBack);
				break;
			}

			cb = cb.getRecruiter();

			if(cb == null) {
				rsSubject.setRecruiter(rsTarget);
				rsSubject.updateRow();
				user.sendChat("Successfully updated recruiter for [ " + params[0] + " ] to [ " + params[1] + " ]" , whisperBack);
				break;
			}
		} while(true);
	}
}