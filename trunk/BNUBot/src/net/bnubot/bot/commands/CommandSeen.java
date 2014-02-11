/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.commands;

import java.util.Date;

import net.bnubot.core.Connection;
import net.bnubot.core.commands.CommandRunnable;
import net.bnubot.core.commands.NeverSeenUserException;
import net.bnubot.db.Account;
import net.bnubot.db.BNLogin;
import net.bnubot.util.BNetUser;
import net.bnubot.util.TimeFormatter;

/**
 * @author scotta
 */
public final class CommandSeen implements CommandRunnable {
	@Override
	public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
	throws Exception {
		if((params == null) || (params.length != 1)) {
			user.sendChat("Use: %trigger%seen <account>", whisperBack);
			return;
		}

		Account rsSubjectAccount = Account.get(params[0]);
		BNLogin rsSubject = null;
		if(rsSubjectAccount == null) {
			//They don't have an account by that name, check if it's a user
			BNetUser bnSubject = source.getCreateBNetUser(params[0], user);
			rsSubject = BNLogin.get(bnSubject);
			if(rsSubject == null)
				throw new NeverSeenUserException(bnSubject);
		} else {
			for(BNLogin login : rsSubjectAccount.getBnLogins()) {
				Date nt = login.getLastSeen();
				if((rsSubject == null)
				|| ((nt != null) && (nt.compareTo(rsSubject.getLastSeen()) > 0)))
					rsSubject = login;
			}
			if(rsSubject == null)
				throw new NeverSeenUserException(rsSubjectAccount);
		}

		Date mostRecent = rsSubject.getLastSeen();
		String mostRecentAction = rsSubject.getLastAction();
		params[0] = new BNetUser(rsSubject.getLogin()).getShortLogonName(user);

		if(mostRecent == null)
			throw new NeverSeenUserException(params[0]);

		String diff = TimeFormatter.formatTime(System.currentTimeMillis() - mostRecent.getTime());
		diff = "User [" + params[0] + "] was last seen " + diff + " ago";
		if(mostRecentAction != null)
			diff += " " + mostRecentAction;
		user.sendChat(diff, whisperBack);
	}
}