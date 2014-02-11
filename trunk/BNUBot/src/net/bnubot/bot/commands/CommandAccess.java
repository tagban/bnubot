/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.commands;

import java.util.List;

import net.bnubot.core.Connection;
import net.bnubot.core.commands.CommandFailedWithDetailsException;
import net.bnubot.core.commands.CommandRunnable;
import net.bnubot.core.commands.InvalidUseException;
import net.bnubot.db.Account;
import net.bnubot.db.Command;
import net.bnubot.util.BNetUser;

/**
 * @author scotta
 */
public final class CommandAccess implements CommandRunnable {
	@Override
	public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
	throws Exception {
		int commanderAccess = 0;
		if(commanderAccount != null)
			commanderAccess = commanderAccount.getAccess();

		try {
			if(params == null)
				throw new InvalidUseException();
			if(params.length != 1)
				throw new InvalidUseException();

			List<Command> commands;
			if(params[0].equals("all"))
				commands = Command.getCommands(commanderAccess);
			else
				commands = Command.getCommands(params[0], commanderAccess);

			if((commands == null) || (commands.size() == 0))
				throw new CommandFailedWithDetailsException("The category [" + params[0] + "] does not exist!");

			StringBuilder result = new StringBuilder("Available commands for rank ");
			result.append(commanderAccess).append(" in cagegory ");
			result.append(params[0]).append(": ");
			boolean first = true;
			for(Command c : commands) {
				if(first)
					first = false;
				else
					result.append(", ");
				result.append(c.getName()).append(" (");
				result.append(c.getAccess()).append(")");
			}

			user.sendChat(result.toString(), whisperBack);
		} catch(InvalidUseException e) {
			StringBuilder use = new StringBuilder("Use: %trigger%access <category> -- Available categories for rank ");
			use.append(commanderAccess).append(": all");
			for(String group : Command.getGroups(commanderAccess))
				use.append(", ").append(group);
			user.sendChat(use.toString(), whisperBack);
		}
	}
}
