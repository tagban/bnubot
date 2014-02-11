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
import net.bnubot.db.CommandAlias;
import net.bnubot.util.BNetUser;

/**
 * @author scotta
 */
public final class CommandAddAlias implements CommandRunnable {
	@Override
	public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
	throws Exception {
		try {
			if(params == null)
				throw new InvalidUseException();
			if(params.length < 2)
				throw new InvalidUseException();

			Command rsSubjectCommand = Command.get(params[0]);
			if(rsSubjectCommand == null)
				throw new CommandFailedWithDetailsException("The command [" + params[0] + "] does not exist!");

			for(int i = 1; i < params.length; i++) {
				if(Command.get(params[i]) != null)
					throw new CommandFailedWithDetailsException("The command [" + params[0] + "] already exists!");
				if(CommandAlias.get(params[i]) != null)
					throw new CommandFailedWithDetailsException("The command alias [" + params[0] + "] already exists!");
			}

			String out = "Successfully created aliases for " + params[0] + ": ";
			for(int i = 1; i < params.length; i++) {
				if(i != 1)
					out += ", ";
				out += params[i];

				if(CommandAlias.create(rsSubjectCommand, params[i]) == null)
					throw new CommandFailedWithDetailsException("Error creating alias " + params[i]);
			}
			user.sendChat(out, whisperBack);
		} catch(InvalidUseException e) {
			user.sendChat("Use: %trigger%addalias <command> <alias1> [alias2] [alias3...]", whisperBack);
		}
	}
}
