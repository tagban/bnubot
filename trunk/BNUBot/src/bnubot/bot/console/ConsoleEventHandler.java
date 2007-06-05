package bnubot.bot.console;

import bnubot.bot.EventHandler;
import bnubot.core.BNetUser;
import bnubot.core.Connection;

public class ConsoleEventHandler implements EventHandler {
	Connection c = null;
	
	public synchronized void initialize(Connection c) {
		this.c = c;
		new CLIThread(c).start();
	}

	public synchronized void joinedChannel(String channel) {
		System.out.println("Joining channel " + channel);
	}

	public synchronized void channelUser(BNetUser user, int flags, int ping, String statstr) {
		if(statstr.length() > 4)
			statstr = statstr.substring(0, 4);
		System.out.println(user + " (" + ping + "ms) " + statstr);
	}
	
	public synchronized void channelJoin(BNetUser user, int flags, int ping, String statstr) {
		System.out.println(user + " has joined the channel.");
	}
	public synchronized void channelLeave(BNetUser user, int flags, int ping, String statstr) {
		System.out.println(user + " has left the channel.");
	}

	public synchronized void recieveChat(BNetUser user, int flags, int ping, String text) {
		System.out.println("<" + user + "> " + text);
	}

	public synchronized void recieveEmote(BNetUser user, int flags, int ping, String text) {
		System.out.println("<" + user + " " + text + ">");
	}

	public synchronized void whisperRecieved(BNetUser user, int flags, int ping, String text) {
		System.out.println("<From: " + user + "> " + text);
	}

	public synchronized void whisperSent(BNetUser user, int flags, int ping, String text) {
		System.out.println("<To: " + user + "> " + text);
	}

	public synchronized void recieveInfo(String text) {
		System.out.println(text);
	}

	public synchronized void recieveError(String text) {
		System.err.println(text);
	}

	public synchronized void bnetConnected() {}
	public synchronized void bnetDisconnected() {}
}
