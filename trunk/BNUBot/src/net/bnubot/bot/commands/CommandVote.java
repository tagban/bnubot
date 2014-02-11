/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.commands;

import net.bnubot.bot.CommandEventHandler;
import net.bnubot.core.Connection;
import net.bnubot.core.commands.CommandFailedWithDetailsException;
import net.bnubot.core.commands.CommandRunnable;
import net.bnubot.db.Account;
import net.bnubot.util.BNetUser;

/**
 * @author scotta
 */
public final class CommandVote implements CommandRunnable {
	@Override
	public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
	throws Exception {
		Vote vote = CommandEventHandler.votes.get(source);
		if(vote == null)
			throw new CommandFailedWithDetailsException("There is no vote in progress");
		else if(commanderAccount == null)
			throw new CommandFailedWithDetailsException("You must have an account to use vote.");
		else if(param.equals("yes"))
			vote.castVote(commanderAccount, true);
		else if(param.equals("no"))
			vote.castVote(commanderAccount, false);
		else
			user.sendChat("Use: %trigger%vote (yes | no)", whisperBack);
	}
}