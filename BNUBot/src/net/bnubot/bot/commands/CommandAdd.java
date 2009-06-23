/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.commands;

import java.util.Date;

import net.bnubot.bot.CommandEventHandler;
import net.bnubot.core.Connection;
import net.bnubot.core.commands.AccountDoesNotExistException;
import net.bnubot.core.commands.CommandRunnable;
import net.bnubot.core.commands.InsufficientAccessException;
import net.bnubot.core.commands.InvalidUseException;
import net.bnubot.db.Account;
import net.bnubot.db.BNLogin;
import net.bnubot.db.Rank;
import net.bnubot.logging.Out;
import net.bnubot.util.BNetUser;

/**
 * @author scotta
 */
public final class CommandAdd extends CommandRunnable {
	@Override
	public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
	throws Exception {
		try {
			if(params == null)
				throw new InvalidUseException();
			if(params.length != 2)
				throw new InvalidUseException();

			Account rsSubjectAccount = Account.get(params[0]);
			if(rsSubjectAccount == null) {
				// They don't have an account by that name, check if it's a user
				BNetUser bnSubject = source.getCreateBNetUser(params[0], user);

				rsSubjectAccount = Account.get(bnSubject);
				if(rsSubjectAccount == null) {
					// The account does not exist
					BNLogin rsSubject = BNLogin.get(bnSubject);
					if(rsSubject == null) {
						user.sendChat("I have never seen [" + bnSubject.getFullAccountName() + "]", whisperBack);
						return;
					}

					try {
						rsSubjectAccount = CommandEventHandler.createAccount(params[0], commanderAccount, rsSubject);
					} catch(AccountDoesNotExistException e) {
						user.sendChat(e.getMessage(), whisperBack);
						return;
					}

					// Re-load the account
					if(rsSubjectAccount == null) {
						user.sendChat("Failed to create account for an unknown reason", whisperBack);
						return;
					}
				}
			}

			int targetAccess = Integer.parseInt(params[1]);
			Rank originalRank = rsSubjectAccount.getRank();
			int originalAccess = originalRank.getAccess();
			if(targetAccess == originalAccess) {
				user.sendChat("That would have no effect", whisperBack);
				return;
			}

			Rank targetRank = Rank.get(targetAccess);
			if(targetRank == null) {
				user.sendChat("Invalid rank: " + targetAccess, whisperBack);
				return;
			}

			if(!superUser) {
				if(rsSubjectAccount.equals(commanderAccount))
					throw new InsufficientAccessException("to modify your self", true);

				int commanderAccess = 0;
				if(commanderAccount != null)
					commanderAccess = commanderAccount.getAccess();
				if(targetAccess >= commanderAccess)
					throw new InsufficientAccessException("to add users beyond " + (commanderAccess - 1), true);
			}

			rsSubjectAccount.setRank(targetRank);
			rsSubjectAccount.setLastRankChange(new Date(System.currentTimeMillis()));
			try {
				rsSubjectAccount.updateRow();
				user.sendChat(rsSubjectAccount.getName() + "'s rank has changed from "
						+ originalRank.getPrefix() + " (" + originalAccess + ") to "
						+ targetRank.getPrefix() + " (" + targetAccess + ")", whisperBack);
			} catch(Exception e) {
				Out.exception(e);
				user.sendChat("Failed: " + e.getMessage(), whisperBack);
			}
		} catch(InvalidUseException e) {
			user.sendChat("Use: %trigger%add <account> <access>", whisperBack);
		}
	}
}
