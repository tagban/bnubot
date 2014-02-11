/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.commands;

import net.bnubot.core.CommandResponseCookie;
import net.bnubot.core.Connection;
import net.bnubot.core.commands.CommandRunnable;
import net.bnubot.core.commands.InvalidUseException;
import net.bnubot.db.Account;
import net.bnubot.util.BNetUser;
import net.bnubot.util.CookieUtility;

/**
 * @author scotta
 */
public final class CommandSetRank implements CommandRunnable {
	@Override
	public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
	throws Exception {
		try {
			if(params == null)
				throw new InvalidUseException();
			if(params.length != 2)
				throw new InvalidUseException();

			int newRank = 0;
			if(params[1].compareToIgnoreCase("peon") == 0)
				newRank = 1;
			else if(params[1].compareToIgnoreCase("grunt") == 0)
				newRank = 2;
			else if(params[1].compareToIgnoreCase("shaman") == 0)
				newRank = 3;
			else
				try {
					newRank = Integer.valueOf(params[1]);
				} catch(Exception e) {}

			if((newRank < 1) || (newRank > 3))
				throw new InvalidUseException();

			// TODO: validate that params[0] is in the clan
			source.sendClanRankChange(
					CookieUtility.createCookie(new CommandResponseCookie(user, whisperBack)),
					params[0],
					newRank);
		} catch(InvalidUseException e) {
			user.sendChat("Use: %trigger%setrank <user> <rank(peon|grunt|shaman|1-3)>", whisperBack);
		}
	}
}