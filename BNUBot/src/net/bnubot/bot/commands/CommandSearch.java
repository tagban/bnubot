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

/**
 * @author scotta
 */
public final class CommandSearch implements CommandRunnable {
	@Override
	public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
	throws Exception {
		if((params == null) || (params.length != 1)) {
			user.sendChat("Use: %trigger%search <pattern>", whisperBack);
			return;
		}

		String out = "Users found: ";
		int num = 0;
		for(BNLogin login : BNLogin.search(params[0])) {
			if(num >= 10) {
				out += ", <more>";
				break;
			}

			if(num++ > 0)
				out += ", ";
			out += new BNetUser(login.getLogin()).getShortLogonName(user);
		}
		if(num == 0)
			out = "No users found!";
		user.sendChat(out, whisperBack);
	}
}