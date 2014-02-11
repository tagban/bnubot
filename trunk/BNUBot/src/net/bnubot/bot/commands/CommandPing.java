/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.commands;

import net.bnubot.core.Connection;
import net.bnubot.core.commands.CommandFailedWithDetailsException;
import net.bnubot.core.commands.CommandRunnable;
import net.bnubot.db.Account;
import net.bnubot.util.BNetUser;

/**
 * @author scotta
 */
public final class CommandPing implements CommandRunnable {
	@Override
	public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
	throws Exception {
		if((params == null) || (params.length != 1)) {
			user.sendChat("Use: %trigger%ping <user>[@<realm>]", whisperBack);
			return;
		}

		BNetUser bnSubject = source.getCreateBNetUser(params[0], user);
		Integer ping = bnSubject.getPing();
		if(ping == null)
			throw new CommandFailedWithDetailsException("I do not know the ping for " + bnSubject.getFullLogonName());
		user.sendChat("Ping for " + bnSubject.getFullLogonName() + ": " + ping, whisperBack);
	}
}