/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.commands;

import net.bnubot.core.Connection;
import net.bnubot.core.commands.AccountDoesNotExistException;
import net.bnubot.core.commands.CommandRunnable;
import net.bnubot.core.commands.InvalidUseException;
import net.bnubot.db.Account;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetUser;

/**
 * @author scotta
 */
public final class CommandRecruits implements CommandRunnable {
	@Override
	public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
	throws Exception {
		try {
			if((params != null) && (params.length != 1))
				throw new InvalidUseException();


			Account subjectAccount = null;
			String output = null;
			if(params == null) {
				subjectAccount = commanderAccount;
				output = "You have recruited: ";
			} else {
				subjectAccount = Account.get(params[0]);
				if(subjectAccount == null)
					throw new AccountDoesNotExistException(params[0]);
				output = subjectAccount.getName();
				output += " has recruited: ";
			}

			if(subjectAccount.getRecruits().size() == 0)
				output += "no one";
			for(Account recruit : subjectAccount.getRecruits()) {
				// Remove accounts below the threshold
				if(recruit.getAccess() >= GlobalSettings.recruitAccess)
					output += recruit.getName() + "(" + recruit.getAccess() + ") ";
			}


			output = output.trim();
			user.sendChat(output, whisperBack);
		} catch(InvalidUseException e) {
			user.sendChat("Use: %trigger%recruits [account]", whisperBack);
		}
		return;
	}
}