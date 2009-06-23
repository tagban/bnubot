/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.commands;

import java.util.HashMap;
import java.util.Map;

import net.bnubot.bot.CommandEventHandler;
import net.bnubot.core.Connection;
import net.bnubot.db.Account;
import net.bnubot.util.BNetUser;

/**
 * @author scotta
 */
public class Vote extends Thread {
	private long startTime;
	private Connection connection;
	private BNetUser subject;
	private boolean isBan;
	private Map<String, Boolean> votes = new HashMap<String, Boolean>();

	private boolean voteCancelled = false;

	public Vote(Connection connection, BNetUser subject, boolean isBan) {
		startTime = System.currentTimeMillis();
		this.connection = connection;
		this.subject = subject;
		this.isBan = isBan;
		start();
	}

	public BNetUser getSubject() {
		return subject;
	}

	public void cancel() {
		voteCancelled = true;
		send("Vote cancelled.");
	}

	protected void castVote(Account user, boolean vote) {
		votes.put(user.getName(), new Boolean(vote));
	}

	private void send(String text) {
		connection.sendChat(text);
	}

	@Override
	public void run() {
		send("A vote to " + (isBan ? "ban " : "kick ") + subject.toString() + " has started. Type \"%trigger%vote yes\" or \"%trigger%vote no\" to vote. Vote lasts 30 seconds.");

		// Wait 30 seconds for voters to vote
		while(!voteCancelled) {
			if(System.currentTimeMillis() - startTime > 30000)
				break;

			yield();
			try {
				sleep(1000);
			} catch (InterruptedException e) {}
		}

		if(!voteCancelled) {
			// Tally up the votes
			int yay = 0, nay = 0;
			for(String voter : votes.keySet()) {
				if(votes.get(voter).booleanValue())
					yay++;
				else
					nay++;
			}

			if(yay + nay >= 5) {
				float ratio = ((float)yay) / (yay + nay);
				// Check for 2/3 ratio
				if(ratio * 3 >= 2)
					send((isBan ? "/ban " : "/kick ") + subject.getFullLogonName() + " " + yay + " to " + nay);
				else
					send("Vote failed, " + yay + " to " + nay + ", needed 2/3 ratio.");
			} else {
				send("Not enough votes: " + yay + " to " + nay + ", needed 5 votes.");
			}
		}

		CommandEventHandler.votes.remove(connection);
	}
}