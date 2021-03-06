/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.commands;

import net.bnubot.core.Connection;
import net.bnubot.core.commands.CommandRunnable;
import net.bnubot.db.Account;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetUser;

/**
 * @author scotta
 */
public final class CommandWhisperBack implements CommandRunnable {
	@Override
	public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
	throws Exception {
		boolean wb = !GlobalSettings.whisperBack;
		GlobalSettings.whisperBack = wb;
		user.sendChat("WhisperBack is now " + (wb ? "on" : "off"), wb);
	}
}