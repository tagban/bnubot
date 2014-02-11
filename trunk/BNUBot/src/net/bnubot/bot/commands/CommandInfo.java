/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.commands;

import net.bnubot.core.Connection;
import net.bnubot.core.commands.CommandRunnable;
import net.bnubot.db.Account;
import net.bnubot.util.BNetUser;
import net.bnubot.util.OperatingSystem;
import net.bnubot.vercheck.CurrentVersion;

/**
 * @author scotta
 */
public final class CommandInfo implements CommandRunnable {
	@Override
	public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
	throws Exception {
		user.sendChat("BNU-Bot " + CurrentVersion.version() + " running on " + OperatingSystem.osVersion() + " with " + OperatingSystem.javaVersion(), whisperBack);
	}
}