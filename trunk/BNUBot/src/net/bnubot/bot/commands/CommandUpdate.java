/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.commands;

import net.bnubot.core.Connection;
import net.bnubot.core.commands.CommandRunnable;
import net.bnubot.db.Account;
import net.bnubot.util.BNetUser;
import net.bnubot.vercheck.VersionCheck;

/**
 * @author scotta
 */
public final class CommandUpdate implements CommandRunnable {
	@Override
	public void run(Connection source, final BNetUser user, String param, String[] params, final boolean whisperBack, Account commanderAccount, boolean superUser)
	throws Exception {
		VersionCheck.checkVersion(true, user, whisperBack);
	}
}