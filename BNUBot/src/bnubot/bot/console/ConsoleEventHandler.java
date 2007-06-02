package bnubot.bot.console;

import bnubot.bot.EventHandler;
import bnubot.core.Connection;

public class ConsoleEventHandler implements EventHandler {
	Connection c = null;
	
	public void initialize(Connection c) {
		this.c = c;
		new CLIThread(c).start();
	}

	public void joinedChannel(String channel) {
		System.out.println("Joining channel " + channel);
	}

	public void channelUser(String user, int flags, int ping, String statstr) {
		if(statstr.length() > 4)
			statstr = statstr.substring(0, 4);
		System.out.println(user + " (" + ping + "ms) " + statstr);
	}
	
	public void channelJoin(String user, int flags, int ping, String statstr) {
		System.out.println(user + " has joined the channel.");
	}
	public void channelLeave(String user, int flags, int ping, String statstr) {
		System.out.println(user + " has left the channel.");
	}

	public void recieveChat(String user, String text) {
		System.out.println("<" + user + "> " + text);
	}

	public void recieveEmote(String user, String text) {
		System.out.println("<" + user + " " + text + ">");
	}

	public void whisperRecieved(String user, String text) {
		System.out.println("<From: " + user + "> " + text);
	}

	public void whisperSent(String user, String text) {
		System.out.println("<To: " + user + "> " + text);
	}

	public void recieveInfo(String text) {
		System.out.println(text);
	}

	public void recieveError(String text) {
		System.err.println(text);
	}

	public void bnetConnected() {}

	public void bnetDisconnected() {}
}
