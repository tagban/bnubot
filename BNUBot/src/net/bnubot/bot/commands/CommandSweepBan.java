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
public final class CommandSweepBan implements CommandRunnable {
	@Override
	public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
	throws Exception {
		if((params == null) || (params.length < 1)) {
			user.sendChat("Use: %trigger%sweepban <channel>", whisperBack);
			return;
		}
		CommandEventHandler.sweepBanInProgress.put(source, true);
		CommandEventHandler.sweepBannedUsers.put(source, 0);
		source.sendChat("/who " + param);
	}
}