/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.commands;

import net.bnubot.bot.CommandEventHandler;
import net.bnubot.core.Connection;
import net.bnubot.core.commands.AccountDoesNotExistException;
import net.bnubot.core.commands.CommandRunnable;
import net.bnubot.db.Account;
import net.bnubot.db.BNLogin;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetUser;

/**
 * @author scotta
 */
public final class CommandRecruit extends CommandRunnable {
	@Override
	public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
	throws Exception {
		if((params == null) || (params.length != 2)) {
			user.sendChat("Use: %trigger%recruit <user>[@<realm>] <account>", whisperBack);
			return;
		}

		if(commanderAccount == null) {
			user.sendChat("You must have an account to use recruit.", whisperBack);
			return;
		}

		BNetUser bnSubject = source.getCreateBNetUser(params[0], user);
		BNLogin rsSubject = BNLogin.get(bnSubject);
		if(rsSubject == null) {
			user.sendChat("I have never seen [" + bnSubject.getFullLogonName() + "] in the channel", whisperBack);
			return;
		}

		if(rsSubject.getAccount() != null) {
			user.sendChat("That user already has an account!", whisperBack);
			return;
		}

		String requiredTagPrefix = GlobalSettings.recruitTagPrefix;
		String requiredTagSuffix = GlobalSettings.recruitTagSuffix;

		if(requiredTagPrefix != null) {
			if(bnSubject.getFullAccountName().substring(0, requiredTagPrefix.length()).compareToIgnoreCase(requiredTagPrefix) != 0) {
				user.sendChat("That user must have the " + requiredTagPrefix + " tag!", whisperBack);
				return;
			}
		}

		if(requiredTagSuffix != null) {
			String s = bnSubject.getFullAccountName();
			int i = s.indexOf("@");
			if(i != -1)
				s = s.substring(0, i);
			s = s.substring(s.length() - requiredTagSuffix.length());
			if(s.compareToIgnoreCase(requiredTagSuffix) != 0) {
				user.sendChat("That user must have the " + requiredTagSuffix + " tag!", whisperBack);
				return;
			}
		}

		try {
			CommandEventHandler.createAccount(params[1], commanderAccount, rsSubject);
		} catch(AccountDoesNotExistException e) {
			user.sendChat(e.getMessage(), whisperBack);
			return;
		}

		bnSubject.resetPrettyName();
		source.sendChat("Welcome to the clan, " + bnSubject.toString() + "!");
	}
}