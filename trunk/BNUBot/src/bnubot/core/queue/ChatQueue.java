package bnubot.core.queue;

import java.util.Iterator;
import java.util.LinkedList;

import bnubot.core.Connection;

public class ChatQueue extends Thread {
	LinkedList<Connection> cons = new LinkedList<Connection>();
	LinkedList<String> queue = new LinkedList<String>();
	
	public ChatQueue() {
		setDaemon(true);
	}
	
	public boolean add(Connection c) {
		return cons.add(c);
	}
	
	public void enqueue(String text) {
		queue.add(text);
	}
	
	public void run() {
		while(true) {
			// If there's text in the queue to send
			if(queue.size() > 0) {
				// Iterate through the connecitons
				Iterator<Connection> it = cons.iterator();
				while(it.hasNext()) {
					Connection con = it.next();
					// Check if the con can send text now
					if(con.canSendChat()) {
						// Write the text out
						con.sendChatNow(queue.remove());
						// If the queue is empty, stop
						if(queue.size() == 0)
							break;
					}
				}
				
			}
			
			yield();
			try { sleep(10); } catch(Exception e) {}
		}
	}
}
