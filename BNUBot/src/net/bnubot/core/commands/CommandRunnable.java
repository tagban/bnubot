/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.commands;

import net.bnubot.core.Connection;
import net.bnubot.util.BNetUser;

public abstract class CommandRunnable {
	public abstract void run(
			Connection source,
			BNetUser user,
			String param,
			String[] params,
			boolean whisperBack,
			long commanderAccess,
			String commanderAccount,
			Long commanderAccountID,
			boolean superUser)
	throws Exception;
}
