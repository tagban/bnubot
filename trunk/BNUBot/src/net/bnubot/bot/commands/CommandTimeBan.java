/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.commands;

import net.bnubot.bot.CommandEventHandler;
import net.bnubot.core.Connection;
import net.bnubot.core.commands.CommandFailedWithDetailsException;
import net.bnubot.core.commands.CommandRunnable;
import net.bnubot.core.commands.InvalidUseException;
import net.bnubot.core.commands.NeverSeenUserException;
import net.bnubot.db.Account;
import net.bnubot.util.BNetUser;
import net.bnubot.util.TimeFormatter;

/**
 * @author scotta
 */
public final class CommandTimeBan implements CommandRunnable {
	private final Thread timeBanThread = new TimeBanThread();

	@Override
	public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
	throws Exception {
		try {
			if((params == null) || (params.length < 2))
				throw new InvalidUseException();
			params = param.split(" ", 2);

			BNetUser bnSubject = source.findUser(params[0], user);
			if(bnSubject == null)
				throw new NeverSeenUserException(params[0]);

			long duration;
			try {
				duration = TimeFormatter.parseDuration(params[1]);
			} catch(NumberFormatException e) {
				throw new InvalidUseException();
			}
			if(duration < 30 * TimeFormatter.SECOND)
				throw new CommandFailedWithDetailsException("You may not timeban for less than 30 seconds");

			doTimeBan(source, user, bnSubject, duration);
		} catch(InvalidUseException e) {
			user.sendChat("Use: %trigger%timeban <user>[@realm] [#days] [#hours] ... -- example: %trigger%timeban c0ke@USEast 100days", whisperBack);
		}
	}

	private void doTimeBan(Connection source, BNetUser user, BNetUser bnSubject, long duration) {
		source.sendChat("/ban " + bnSubject.getFullLogonName() + " TimeBan from " + user.toString() + " " + TimeFormatter.formatTime(duration, false));

		synchronized(CommandEventHandler.timeBannedUsers) {
			CommandEventHandler.timeBannedUsers.add(new TimeBan(
					timeBanThread,
					source,
					bnSubject,
					System.currentTimeMillis() + duration));
		}
	}
}