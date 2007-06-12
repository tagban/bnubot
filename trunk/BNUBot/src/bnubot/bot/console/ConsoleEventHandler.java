package bnubot.bot.console;

import bnubot.bot.EventHandler;
import bnubot.core.BNetUser;
import bnubot.core.Connection;
import bnubot.core.StatString;
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

	public void channelUser(BNetUser user, int flags, int ping, StatString statstr) {
		System.out.println(user + " (" + ping + "ms) " + statstr.toString());
	}
	
	public void channelJoin(BNetUser user, int flags, int ping, StatString statstr) {
		System.out.println(user + " has joined the channel " + statstr.toString());
	}
	public void channelLeave(BNetUser user, int flags, int ping, StatString statstr) {
		System.out.println(user + " has left the channel " + statstr.toString());
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
	public void titleChanged() {}

	public void friendsList(FriendEntry[] entries) {}
	public void friendsUpdate(byte entry, byte location, byte status, int product, String locationName) {}
	public void clanMemberRankChange(byte oldRank, byte newRank, String user) {}
	public void clanMemberList(ClanMember[] members) {}
	public void clanMOTD(Object cookie, String text) {}

	public void logonRealmEx(int[] MCPChunk1, int ip, int port, int[] MCPChunk2, String uniqueName) {}

	public void queryRealms2(String[] realms) {}
}
