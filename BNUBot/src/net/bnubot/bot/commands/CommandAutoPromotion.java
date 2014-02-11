/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.commands;

import java.util.Date;

import net.bnubot.core.Connection;
import net.bnubot.core.commands.AccountDoesNotExistException;
import net.bnubot.core.commands.CommandFailedWithDetailsException;
import net.bnubot.core.commands.CommandRunnable;
import net.bnubot.core.commands.InvalidUseException;
import net.bnubot.db.Account;
import net.bnubot.db.Rank;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetUser;

/**
 * @author scotta
 */
public final class CommandAutoPromotion implements CommandRunnable {
	@Override
	public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
	throws Exception {
		try {
			if((params != null) && (params.length != 1))
				throw new InvalidUseException();

			Account subject;
			if(params == null)
				subject = Account.get(user);
			else
				subject = Account.get(params[0]);

			if(subject == null)
				throw new AccountDoesNotExistException(params[0]);

			long wins[] = subject.getWinsLevels(GlobalSettings.recruitTagPrefix, GlobalSettings.recruitTagSuffix);
			long recruitScore = subject.getRecruitScore(GlobalSettings.recruitAccess);
			Date ts = subject.getLastRankChange();
			String timeElapsed;
			if(ts != null) {
				double te = System.currentTimeMillis() - ts.getTime();
				te /= 1000 * 60 * 60 * 24;
				//Round to 2 decimal places
				timeElapsed = ("00" + ((long)Math.floor(te * 100) % 100));
				timeElapsed = timeElapsed.substring(timeElapsed.length()-2);
				timeElapsed = (long)Math.floor(te) + "." + timeElapsed;
			} else {
				timeElapsed = "?";
			}

			Rank rsRank = subject.getRank();
			if(rsRank == null) {
				String result = "Rank does not exist! ";
				result += subject.getName() + "'s current status is: ";
				result += timeElapsed + " days, ";
				result += wins[0] + " wins, ";
				result += wins[1] + " D2 level, ";
				result += wins[2] + " W3 level";

				throw new CommandFailedWithDetailsException(result);
			}

			Integer apDays = rsRank.getApDays();
			Integer apWins = rsRank.getApWins();
			Integer apD2Level = rsRank.getApD2Level();
			Integer apW3Level = rsRank.getApW3Level();
			Integer apRecruitScore = rsRank.getApRecruitScore();

			// Check if any fields are null
			boolean condition = false;
			condition |= (apDays == null);
			condition |= (apWins == null);
			condition |= (apD2Level == null);
			condition |= (apW3Level == null);
			if(condition == false)
				// No nulls; check if all zeroes
				condition = (apDays == 0) && (apWins == 0) && (apD2Level == 0) && (apW3Level == 0);
			if(condition) {
				String result = "AutoPromotions are not enabled for rank " + subject.getAccess() + ". ";
				result += subject.getName() + "'s current status is: ";
				result += "Days: " + timeElapsed;
				result += ", Wins: " + wins[0];
				result += ", D2 Level: " + wins[1];
				result += ", W3 level: " + wins[2];
				if(recruitScore > 0)
					result += ", Recruit Score: " + recruitScore;

				user.sendChat(result, whisperBack);
			} else {
				String result = "AutoPromotion Info for [" + subject.getName() + "]: ";
				result += "Days: " + timeElapsed + "/" + apDays;
				result += ", Wins: " + wins[0] + "/" + apWins;
				result += ", D2 Level: " + wins[1] + "/" + apD2Level;
				result += ", W3 level: " + wins[2] + "/" + apW3Level;
				if(((apRecruitScore != null) && (apRecruitScore > 0)) || (recruitScore > 0)) {
					result += ", Recruit Score: " + recruitScore;
					if(apRecruitScore != null)
						result += "/" + apRecruitScore;
				}

				user.sendChat(result, whisperBack);
			}
		} catch(InvalidUseException e) {
			user.sendChat("Use: %trigger%automaticpromotion [account]", whisperBack);
		}
	}
}
