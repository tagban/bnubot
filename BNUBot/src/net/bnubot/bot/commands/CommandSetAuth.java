/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.commands;

import net.bnubot.core.Connection;
import net.bnubot.core.commands.CommandFailedWithDetailsException;
import net.bnubot.core.commands.CommandRunnable;
import net.bnubot.core.commands.InvalidUseException;
import net.bnubot.db.Account;
import net.bnubot.db.Command;
import net.bnubot.db.Rank;
import net.bnubot.util.BNetUser;

/**
 * @author scotta
 */
public final class CommandSetAuth implements CommandRunnable {
	@Override
	public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
	throws Exception {
		try {
			if((params == null) || (params.length != 2))
				throw new InvalidUseException();

			Command rsCommand = Command.get(params[0]);
			if(rsCommand == null)
				throw new CommandFailedWithDetailsException("That command does not exist!");

			try {
				int oldAccess = rsCommand.getAccess();
				int access = Integer.parseInt(params[1]);
				Rank rank = Rank.get(access);
				if(rank == null)
					throw new CommandFailedWithDetailsException("That access level does not exist!");
				rsCommand.setRank(rank);
				rsCommand.updateRow();

				user.sendChat("Successfully changed the authorization required for command [" + rsCommand.getName() + "] from [" + oldAccess + "] to [" + access + "]", whisperBack);
			} catch(NumberFormatException e) {
				throw new InvalidUseException();
			}
		} catch(InvalidUseException e) {
			user.sendChat("Use: %trigger%setauth <command> <access>", whisperBack);
		}
	}
}