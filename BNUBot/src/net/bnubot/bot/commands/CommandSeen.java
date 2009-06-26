/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.commands;

import java.util.Date;

import net.bnubot.core.Connection;
import net.bnubot.core.commands.CommandRunnable;
import net.bnubot.db.Account;
import net.bnubot.db.BNLogin;
import net.bnubot.util.BNetUser;
import net.bnubot.util.TimeFormatter;

/**
 * @author scotta
 */
public final class CommandSeen implements CommandRunnable {
	public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
	throws Exception {
		if((params == null) || (params.length != 1)) {
			user.sendChat("Use: %trigger%seen <account>", whisperBack);
			return;
		}

		Date mostRecent = null;
		String mostRecentAction = null;

		Account rsSubjectAccount = Account.get(params[0]);
		if(rsSubjectAccount == null) {
			//They don't have an account by that name, check if it's a user
			BNetUser bnSubject = source.getCreateBNetUser(params[0], user);
			BNLogin rsSubject = BNLogin.get(bnSubject);
			if(rsSubject == null) {
				user.sendChat("I have never seen [" + bnSubject.getFullAccountName() + "]", whisperBack);
				return;
			}

			mostRecent = rsSubject.getLastSeen();
			mostRecentAction = rsSubject.getLastAction();
			params[0] = rsSubject.getLogin();
		} else {
			params[0] = rsSubjectAccount.getName();
			for(BNLogin rsSubjectUsers : rsSubjectAccount.getBnLogins()) {
				Date nt = rsSubjectUsers.getLastSeen();
				if(mostRecent == null) {
					mostRecent = nt;
					mostRecentAction = rsSubjectUsers.getLastAction();
				} else {
					if((nt != null) && (nt.compareTo(mostRecent) > 0)) {
						mostRecent = nt;
						mostRecentAction = rsSubjectUsers.getLastAction();
					}
				}
			}
		}

		if(mostRecent == null) {
			user.sendChat("I have never seen [" + params[0] + "]", whisperBack);
			return;
		}

		String diff = TimeFormatter.formatTime(System.currentTimeMillis() - mostRecent.getTime());
		diff = "User [" + params[0] + "] was last seen " + diff + " ago";
		if(mostRecentAction != null)
			diff += " " + mostRecentAction;
		user.sendChat(diff, whisperBack);
	}
}