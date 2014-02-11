/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.commands;

import net.bnubot.core.Connection;
import net.bnubot.core.commands.CommandRunnable;
import net.bnubot.core.commands.InvalidUseException;
import net.bnubot.db.Account;
import net.bnubot.db.Rank;
import net.bnubot.util.BNetUser;

/**
 * @author scotta
 */
public final class CommandSearchRank implements CommandRunnable {
	@Override
	public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
	throws Exception {
		try {
			if((params == null) || (params.length < 1) || (params.length > 2))
				throw new InvalidUseException();

			int access;
			try {
				access = Integer.parseInt(params[0]);
			} catch(NumberFormatException e) {
				throw new InvalidUseException();
			}

			int accessUpper = access;
			if(params.length > 1)
				try {
					accessUpper = Integer.parseInt(params[1]);
				} catch(NumberFormatException e) {
					throw new InvalidUseException();
				}

			String out = "Accounts found: ";
			boolean first = true;
			for(int i = access; i <= accessUpper; i++) {
				Rank rank = Rank.get(i);
				if(rank == null)
					continue;
				for(Account account : rank.getAccountArray()) {
					if(!first)
						out += ", ";
					first = false;
					out += account.getName();
					out += " {" + i + "}";
				}
			}
			if(first)
				out = "No users found!";

			user.sendChat(out, whisperBack);
		} catch(InvalidUseException e) {
			user.sendChat("Use: %trigger%searchrank <rank_lowest> [rank_highest]", whisperBack);
		}
	}
}