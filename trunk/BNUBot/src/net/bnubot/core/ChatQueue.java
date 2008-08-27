/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import net.bnubot.core.botnet.BotNetConnection;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.ByteArray;
import net.bnubot.util.Out;

/**
 * @author scotta
 */
public class ChatQueue extends Thread {
	private class QueueEntry implements Comparable<QueueEntry> {
		public final ByteArray text;
		public final Integer priority;

		public QueueEntry(ByteArray text, int priority) {
			this.text = text;
			this.priority = new Integer(priority);
		}

		public int compareTo(QueueEntry o) {
			return priority.compareTo(o.priority);
		}
	}

	private static final Comparator<QueueEntry> queueComparator = new Comparator<QueueEntry>() {
		public int compare(QueueEntry o1, QueueEntry o2) {
			return o1.priority.compareTo(o2.priority);
		}};

	private final List<Connection> cons = new ArrayList<Connection>();
	private final List<QueueEntry> queue = new LinkedList<QueueEntry>();
	private int lastCon = 0;
	private boolean disposed = false;

	public ChatQueue(String profileName) {
		super(ChatQueue.class.getSimpleName() + "-" + profileName);
		setDaemon(true);
	}

	public int size() {
		return queue.size();
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

	public boolean enqueue(Connection source, ByteArray text, int priority) {
		if(GlobalSettings.enableFloodProtect) synchronized(queue) {
			// Flood protection is enabled
			if(queue.add(new QueueEntry(text, priority))) {
				Collections.sort(queue, queueComparator);
				return true;
			}
			return false;
		}

		if(requiresOps(text.toString())) {
			if(sendTextOp(text))
				return true;
			Out.error(getClass(), "Failed send command: " + text);
			return false;
		}

		// Flood protection disabled; send in round-robin pattern
		getNextConnection().sendChatCommand(text);
		return true;
	}

	public void clear() {
		int qs = size();
		if(qs > 0)
			Out.info(getClass(), "Removing " + qs + " commands from the ChatQueue.");
		synchronized(queue) {
			queue.clear();
		}
	}

	@Override
	public void run() {
		while(!disposed) {
			try {
				yield();
				sleep(100);
			} catch (InterruptedException e) {}

			// If there's text in the queue to send
			while(size() > 0) synchronized(queue) {
				Connection con = getNextConnection();

				// Check if the con can send text now
				if(!con.canSendChat()) {
					lastCon--;
					break;
				}

				ByteArray text = queue.remove(0).text;

				if(con.isOp() || !requiresOps(text.toString())) {
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

	private boolean sendTextOp(ByteArray text) {
		for(int i = 0; i < cons.size(); i++) {
			Connection c = getNextConnection();
			if(!c.isOp())
				continue;
			while(!c.canSendChat())
				try {
					yield();
					sleep(100);
				} catch (InterruptedException e) {}
			c.sendChatCommand(text);
			return true;
		}
		return false;
	}

	public void dispose() {
		disposed = true;
	}
}
