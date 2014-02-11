/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.commands;

import net.bnubot.core.Connection;
import net.bnubot.core.commands.CommandRunnable;
import net.bnubot.db.Account;
import net.bnubot.util.BNetUser;

/**
 * @author scotta
 */
public final class CommandTrigger implements CommandRunnable {
	@Override
	public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
	throws Exception {
		char trigger = source.getTrigger();
		String output = "0000" + Integer.toString(trigger);
		output = output.substring(output.length() - 4);
		output = "Current trigger: " + trigger + " (alt+" + output + ")";
		user.sendChat(output, whisperBack);
	}
}