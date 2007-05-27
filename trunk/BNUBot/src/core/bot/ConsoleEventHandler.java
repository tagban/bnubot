package core.bot;

import core.Connection;
import core.EventHandler;

public class ConsoleEventHandler implements EventHandler {
	Connection c = null;
	
	public void initialize(Connection c) {
		this.c = c;
	}

	public void joinedChannel(String channel) {
		System.out.println("Joining channel " + channel);
		//c.sendChat("it begins again...");
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

}
