/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.commands;

import net.bnubot.bot.CommandEventHandler;
import net.bnubot.core.Connection;
import net.bnubot.core.commands.CommandRunnable;
import net.bnubot.db.Account;
import net.bnubot.util.BNetUser;

/**
 * @author scotta
 */
public final class CommandVoteCancel extends CommandRunnable {
	@Override
	public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
	throws Exception {
		Vote vote = CommandEventHandler.votes.get(source);
		if(vote == null)
			user.sendChat("There is no vote in progress", whisperBack);
		else
			vote.cancel();
	}
}