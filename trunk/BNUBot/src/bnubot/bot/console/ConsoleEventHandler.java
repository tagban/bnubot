package bnubot.bot.console;

import bnubot.bot.EventHandler;
import bnubot.core.BNetUser;
import bnubot.core.Connection;
import bnubot.core.clan.ClanMember;
import bnubot.core.friend.FriendEntry;

public class ConsoleEventHandler implements EventHandler {
	Connection c = null;
	
	public synchronized void initialize(Connection c) {
		this.c = c;
		new CLIThread(c).start();
	}

	public void joinedChannel(String channel) {
		System.out.println("Joining channel " + channel);
	}

	public void channelUser(BNetUser user, int flags, int ping, String statstr) {
		if(statstr.length() > 4)
			statstr = statstr.substring(0, 4);
		System.out.println(user + " (" + ping + "ms) " + statstr);
	}
	
	public void channelJoin(BNetUser user, int flags, int ping, String statstr) {
		System.out.println(user + " has joined the channel.");
	}
	public void channelLeave(BNetUser user, int flags, int ping, String statstr) {
		System.out.println(user + " has left the channel.");
	}

	public void recieveChat(BNetUser user, int flags, int ping, String text) {
		System.out.println("<" + user + "> " + text);
	}

	public void recieveEmote(BNetUser user, int flags, int ping, String text) {
		System.out.println("<" + user + " " + text + ">");
	}

	public void whisperRecieved(BNetUser user, int flags, int ping, String text) {
		System.out.println("<From: " + user + "> " + text);
	}

	public void whisperSent(BNetUser user, int flags, int ping, String text) {
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

	public void friendsList(FriendEntry[] entries) {}
	public void friendsUpdate(byte entry, byte location, byte status, int product, String locationName) {}
	public void clanMemberRankChange(byte oldRank, byte newRank, String user) {}
	public void clanMemberList(ClanMember[] members) {}
}
