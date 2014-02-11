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
import net.bnubot.util.BNetUser;

/**
 * @author scotta
 */
public final class CommandAuth implements CommandRunnable {
	@Override
	public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
	throws Exception {
		try {
			if(params == null)
				throw new InvalidUseException();
			if(params.length != 1)
				throw new InvalidUseException();

			Command rsSubjectCommand = Command.get(params[0]);
			if(rsSubjectCommand == null)
				throw new CommandFailedWithDetailsException("The command [" + params[0] + "] does not exist!");

			params[0] = rsSubjectCommand.getName();
			int access = rsSubjectCommand.getAccess();

			user.sendChat("Authorization required for " + params[0] + " is " + access, whisperBack);
		} catch(InvalidUseException e) {
			user.sendChat("Use: %trigger%auth <command>", whisperBack);
		}
	}
}
