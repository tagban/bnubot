/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.commands;

import net.bnubot.core.Connection;
import net.bnubot.core.commands.CommandRunnable;
import net.bnubot.db.Account;
import net.bnubot.db.BNLogin;
import net.bnubot.util.BNetUser;
import net.bnubot.util.TimeFormatter;

/**
 * @author scotta
 */
public final class CommandAllSeen implements CommandRunnable {
	@Override
	public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
	throws Exception {
		String response = "Last 10 users seen: ";
		boolean first = true;
		for(BNLogin login : BNLogin.getLastSeen(10)) {
			if(!first)
				response += ", ";
			first = false;

			response += new BNetUser(login.getLogin()).getShortLogonName(user);
			response += " [";
			long time = System.currentTimeMillis() - login.getLastSeen().getTime();
			response += TimeFormatter.formatTime(time, false);
			response += "]";
		}
		user.sendChat(response, whisperBack);
	}
}
