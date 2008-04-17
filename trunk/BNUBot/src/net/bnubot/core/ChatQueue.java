/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.bnubot.core.botnet.BotNetConnection;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.Out;

public class ChatQueue extends Thread {
	private final List<Connection> cons = new ArrayList<Connection>();
	private final List<String> queue = new LinkedList<String>();
	private int lastCon = 0;
	private boolean disposed = false;

	public ChatQueue(String profileName) {
		super(ChatQueue.class.getSimpleName() + "-" + profileName);
		setDaemon(true);
	}

	public boolean add(Connection c) {
		if(c instanceof BotNetConnection)
			return false;
		synchronized(cons) {
			return cons.add(c);
		}
	}

	private Connection getNextConnection() {
		if(lastCon >= cons.size())
			lastCon = 0;
		return cons.get(lastCon++);
	}

	public boolean enqueue(Connection source, String text) {
		if(GlobalSettings.enableFloodProtect) synchronized(queue) {
			// Flood protection is enabled
			return queue.add(text);
		}

		if(requiresOps(text)) {
			if(sendTextOp(text))
				return true;
			Out.error(getClass(), "Failed to add command to the queue: " + text);
			return false;
		}

		// Flood protection disabled; send in round-robin pattern
		getNextConnection().sendChatCommand(text);
		return true;
	}

	public void clear() {
		int qs = queue.size();
		if(qs > 0)
			Out.info(getClass(), "Removing " + qs + " commands from the ChatQueue.");
		queue.clear();
	}

	@Override
	public void run() {
		while(!disposed) {
			try {
				yield();
				sleep(100);
			} catch (InterruptedException e) {}

			// If there's text in the queue to send
			while(queue.size() > 0) {
				Connection con = getNextConnection();

				// Check if the con can send text now
				if(!con.canSendChat()) {
					lastCon--;
					break;
				}

				String text = queue.remove(0);

				if(con.isOp() || !requiresOps(text)) {
					// Write the text out
					con.sendChatCommand(text);
					continue;
				}

				if(!sendTextOp(text))
					Out.error(getClass(), "Failed to send chat, no available operators: " + text);
			}
		}
	}

	/**
	 * Determine if Channel Operator status is required to send text
	 * @param text The text to check
	 * @return True if Operator status is required
	 */
	private boolean requiresOps(String text) {
		if(!text.startsWith("/"))
			return false;
		if(text.startsWith("/kick ")
		|| text.startsWith("/ban ")
		|| text.startsWith("/unban ")
		|| text.startsWith("/c ")
		|| text.startsWith("/clan "))
			return true;
		return false;
	}

	private boolean sendTextOp(String text) {
		for(int i = 0; i < cons.size(); i++) {
			Connection c = getNextConnection();
			if(!c.isOp())
				continue;
			while(!c.canSendChat())
				try {
					yield();
					sleep(100);
				} catch (InterruptedException e) {}
			return true;
		}
		return false;
	}

	public void dispose() {
		disposed = true;
	}
}
