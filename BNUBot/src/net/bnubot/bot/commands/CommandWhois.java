/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.commands;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import net.bnubot.core.Connection;
import net.bnubot.core.commands.CommandRunnable;
import net.bnubot.core.commands.InvalidUseException;
import net.bnubot.db.Account;
import net.bnubot.db.BNLogin;
import net.bnubot.db.Rank;
import net.bnubot.util.BNetUser;
import net.bnubot.util.TimeFormatter;

/**
 * @author scotta
 */
public final class CommandWhois extends CommandRunnable {
	@Override
	public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
	throws Exception {
		try {
			if((params == null) || (params.length != 1))
				throw new InvalidUseException();

			Account rsSubjectAccount = Account.get(params[0]);
			if(rsSubjectAccount == null) {
				BNetUser bnSubject = source.getCreateBNetUser(params[0], user);

				BNLogin rsSubject = BNLogin.get(bnSubject);
				if(rsSubject == null) {
					user.sendChat("I have never seen [" + bnSubject.getFullLogonName() + "] in the channel", whisperBack);
					return;
				}

				rsSubjectAccount = rsSubject.getAccount();
				if(rsSubjectAccount == null) {
					user.sendChat("User [" + params[0] + "] has no account", whisperBack);
					return;
				}
			}

			List<String> clauses = new LinkedList<String>();

			// Access
			Rank rsSubjectRank = rsSubjectAccount.getRank();
			if(rsSubjectRank != null) {
				String prefix = rsSubjectRank.getShortPrefix();
				if(prefix == null)
					prefix = rsSubjectRank.getPrefix();

				if(prefix == null)
					prefix = "";
				else
					prefix += " ";

				String result = prefix + rsSubjectAccount.getName();
				if(rsSubjectRank.getVerbstr() != null)
					result += " " + rsSubjectRank.getVerbstr();
				else
					result += " is a " + rsSubjectRank.getPrefix();
				result += " (" + rsSubjectAccount.getAccess() + ")";

				clauses.add(result);
			} else {
				clauses.add(rsSubjectAccount.getName() + " has access " + rsSubjectAccount.getAccess());
			}

			// Birthday
			Date subjectBirthday = rsSubjectAccount.getBirthday();
			if(subjectBirthday != null) {
				double age = System.currentTimeMillis() - subjectBirthday.getTime();
				age /= 1000 * 60 * 60 * 24 * 365.24;
				age = Math.floor(age * 100) / 100;
				clauses.add("is " + Double.toString(age) + " years old");
			}

			// Last seen
			Date lastSeen = null;
			for(BNLogin rsSubject : rsSubjectAccount.getBnLogins()) {
				if(lastSeen == null)
					lastSeen = rsSubject.getLastSeen();
				else {
					Date nt = rsSubject.getLastSeen();
					if((nt != null) && (nt.compareTo(lastSeen) > 0))
						lastSeen = nt;
				}
			}

			if(lastSeen != null) {
				clauses.add("was last seen [ " + TimeFormatter.formatTime(System.currentTimeMillis() - lastSeen.getTime()) + " ] ago");
			}

			// Recruiter
			Account rsCreatorAccount = rsSubjectAccount.getRecruiter();
			if(rsCreatorAccount != null) {
				clauses.add("was recruited by " + rsCreatorAccount.getName());
			}

			String aliases = null;
			for(BNLogin alias : rsSubjectAccount.getBnLogins()) {
				if(aliases == null)
					aliases = "has aliases ";
				else
					aliases += ", ";
				aliases += alias.getLogin();
			}
			if(aliases != null)
				clauses.add(aliases);

			String result = clauses.remove(0);
			while(clauses.size() > 1)
				result += ", " + clauses.remove(0);
			while(clauses.size() > 0)
				result += ", and " + clauses.remove(0);

			user.sendChat(result, whisperBack);
		} catch(InvalidUseException e) {
			user.sendChat("Use: %trigger%whois ( <account> | <user>[@realm] )", whisperBack);
		}
	}
}