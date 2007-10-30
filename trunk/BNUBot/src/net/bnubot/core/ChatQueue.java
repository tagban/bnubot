/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.core;

import java.util.LinkedList;

public class ChatQueue extends Thread {
	private LinkedList<Connection> cons = new LinkedList<Connection>();
	private LinkedList<String> queue = new LinkedList<String>();
	private int lastCon = 0;

	public ChatQueue() {
		super(ChatQueue.class.getSimpleName());
		setDaemon(true);
	}

	public boolean add(Connection c) {
		synchronized(cons) {
			return cons.add(c);
		}
	}

	public boolean enqueue(String text, boolean fp) {
		if(fp) synchronized(queue) {
			// Flood protection is enabled
			return queue.add(text);
		}
		// Flood protection disabled; send in round-robin pattern
		if(lastCon >= cons.size())
			lastCon = 0;
		cons.get(lastCon++).sendChatCommand(text);
		return true;
	}

	public void run() {
		while(true) {
			yield();
			try { sleep(10); } catch(Exception e) {}
			
			// If there's text in the queue to send
			if(queue.size() > 0) {
				// Iterate through the connecitons
				for(Connection con : cons) {
					// Check if the con can send text now
					if(con.canSendChat()) {
						if(con.isOp()) {
							// Write the text out
							con.sendChatCommand(queue.remove());
						} else {
							//Find a string we can send
							sendTextNonOp(con);
						}

						// If the queue is empty, stop
						if(queue.size() == 0)
							break;
					}
				}
			}
		}
	}

	private boolean sendTextNonOp(Connection con) {
		for(String text : queue) {
			// Check if ops is required
			try {
				// Only consider strings beginning with a slash
				if(text.charAt(0) == '/') {
					String cmd = text.substring(1);
					int i = cmd.indexOf(' ');
					if(i != -1) {
						// Split the text from the slash to the first space
						cmd = cmd.substring(0, i).toLowerCase();

						// The commands /kick and /ban require ops
						if(cmd.equals("kick")
						|| cmd.equals("ban"))
							continue;
					}
				}
			} catch(Exception e) {}

			// Write the text out
			con.sendChatCommand(text);
			return queue.remove(text);
		}
		return false;
	}
}
