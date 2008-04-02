/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.commands;

import net.bnubot.core.Connection;
import net.bnubot.db.Account;
import net.bnubot.util.BNetUser;

public abstract class CommandRunnable {
	public abstract void run(
			Connection source,
			BNetUser user,
			String param,
			String[] params,
			boolean whisperBack,
			int commanderAccess,
			Account commanderAccount,
			boolean superUser)
	throws Exception;
}
