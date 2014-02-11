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
public final class CommandUnban implements CommandRunnable {
	@Override
	public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
	throws Exception {
		// TODO: Wildcard unbanning (requires keeping track of banned users)
		if((params == null) || (params.length != 1)) {
			user.sendChat("Use: %trigger%unban <user>[@<realm>]", whisperBack);
			return;
		}

		BNetUser target = new BNetUser(source, params[0], user);
		source.sendChat("/unban " + target.getFullLogonName());
		CommandEventHandler.setInfoForwarding(source, user, whisperBack);

		synchronized(CommandEventHandler.timeBannedUsers) {
			TimeBan targetTimeBan = null;
			for(TimeBan tb : CommandEventHandler.timeBannedUsers) {
				if(tb.getSubject().equals(target)) {
					targetTimeBan = tb;
					break;
				}
			}
			if(targetTimeBan != null) {
				CommandEventHandler.timeBannedUsers.remove(targetTimeBan);
				user.sendChat("Ending timeban on " + target.getFullLogonName(), whisperBack);
			}
		}
	}
}