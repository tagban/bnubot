/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.commands;

import net.bnubot.core.Connection;
import net.bnubot.util.BNetUser;

/**
 * @author scotta
 */
public class TimeBan {
	Connection source;
	BNetUser subject;
	long endTime;
	public TimeBan(Thread timeBanThread, Connection source, BNetUser subject, long endTime) {
		this.source = source;
		this.subject = subject;
		this.endTime = endTime;
		if(!timeBanThread.isAlive())
			timeBanThread.start();
	}

	public Connection getSource() {
		return source;
	}

	public BNetUser getSubject() {
		return subject;
	}

	public long getTimeLeft() {
		return endTime - System.currentTimeMillis();
	}
}