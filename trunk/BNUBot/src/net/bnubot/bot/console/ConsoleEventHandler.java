/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.console;

import net.bnubot.core.Connection;
import net.bnubot.core.EventHandler;
import net.bnubot.core.clan.ClanMember;
import net.bnubot.core.friend.FriendEntry;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetUser;

public class ConsoleEventHandler implements EventHandler {
	public synchronized void initialize(Connection source) {
		new CLIThread(source).start();
	}

	public void joinedChannel(Connection source, String channel) {
		System.out.println("Joining channel " + channel);
	}

	public void channelUser(Connection source, BNetUser user) {
		if(GlobalSettings.displayChannelUsers)
			System.out.println(user.getShortPrettyName() + " (" + user.getPing() + "ms)" + user.getStatString().toString());
	}
	
	public void channelJoin(Connection source, BNetUser user) {
		if(GlobalSettings.displayJoinParts)
			System.out.println(user + " has joined the channel" + user.getStatString().toString() + ".");
	}
	public void channelLeave(Connection source, BNetUser user) {
		if(GlobalSettings.displayJoinParts)
			System.out.println(user + " has left the channel.");
	}

	public void recieveChat(Connection source, BNetUser user, String text) {
		System.out.println("<" + user.getShortPrettyName() + "> " + text);
	}

	public void recieveEmote(Connection source, BNetUser user, String text) {
		System.out.println("<" + user.getShortPrettyName() + " " + text + ">");
	}

	public void whisperRecieved(Connection source, BNetUser user, String text) {
		System.out.println("<From: " + user.getShortPrettyName() + "> " + text);
	}

	public void whisperSent(Connection source, BNetUser user, String text) {
		System.out.println("<To: " + user.getShortPrettyName() + "> " + text);
	}
	
	public void recieveDebug(Connection source, String text) {
		System.out.println("(DEBUG) " + text);
	}

	public void recieveInfo(Connection source, String text) {
		System.out.println("(INFO) " + text);
	}

	public void recieveError(Connection source, String text) {
		System.out.println("(ERROR) " + text);
	}

	public void bnetConnected(Connection source) {}
	public void bnetDisconnected(Connection source) {}
	public void titleChanged(Connection source) {}

	public boolean parseCommand(Connection source, BNetUser user, String command, String param, boolean whisperBack) {return false;}

	public void friendsList(Connection source, FriendEntry[] entries) {}
	public void friendsUpdate(Connection source, FriendEntry friend) {}
	public void friendsAdd(Connection source, FriendEntry friend) {}
	public void friendsPosition(Connection source, byte oldPosition, byte newPosition) {}
	public void friendsRemove(Connection source, byte entry) {}

	public void clanMOTD(Connection source, Object cookie, String text) {}
	public void clanMemberList(Connection source, ClanMember[] members) {}
	public void clanMemberRemoved(Connection source, String username) {}
	public void clanMemberStatusChange(Connection source, ClanMember member) {}
	public void clanMemberRankChange(Connection source, byte oldRank, byte newRank, String user) {}

	public void logonRealmEx(Connection source, int[] MCPChunk1, int ip, int port, int[] MCPChunk2, String uniqueName) {}
	public void queryRealms2(Connection source, String[] realms) {}
}
