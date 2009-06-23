/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.commands;

import net.bnubot.bot.CommandEventHandler;

/**
 * @author scotta
 */
final class TimeBanThread extends Thread {
	@Override
	public void run() {
		while(true) {
			synchronized(CommandEventHandler.timeBannedUsers) {
				for(TimeBan tb : CommandEventHandler.timeBannedUsers)
					if(tb.getTimeLeft() <= 0) {
						tb.getSource().sendChat("/unban " + tb.getSubject().getFullLogonName());
						CommandEventHandler.timeBannedUsers.remove(tb);
					}
			}
			try {
				sleep(5000);
			} catch (InterruptedException e) {}
			yield();
		}
	}
}