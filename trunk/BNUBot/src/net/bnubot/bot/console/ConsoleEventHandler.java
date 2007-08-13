/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.bot.console;

import net.bnubot.bot.EventHandler;
import net.bnubot.core.BNetUser;
import net.bnubot.core.Connection;
import net.bnubot.core.StatString;
import net.bnubot.core.clan.ClanMember;
import net.bnubot.core.friend.FriendEntry;
import net.bnubot.util.Out;

public class ConsoleEventHandler implements EventHandler {
	Connection c = null;
	
	public synchronized void initialize(Connection c) {
		this.c = c;
		new CLIThread(c).start();
	}

	public void joinedChannel(String channel) {
		Out.info(this.getClass(), "Joining channel " + channel);
	}

	public void channelUser(BNetUser user, StatString statstr) {
		Out.info(this.getClass(), user.getShortPrettyName() + " (" + user.getPing() + "ms)" + statstr.toString());
	}
	
	public void channelJoin(BNetUser user, StatString statstr) {
		Out.info(this.getClass(), user + " has joined the channel" + statstr.toString() + ".");
	}
	public void channelLeave(BNetUser user) {
		Out.info(this.getClass(), user + " has left the channel.");
	}

	public void recieveChat(BNetUser user, String text) {
		Out.info(this.getClass(), "<" + user.getShortPrettyName() + "> " + text);
	}

	public void recieveEmote(BNetUser user, String text) {
		Out.info(this.getClass(), "<" + user.getShortPrettyName() + " " + text + ">");
	}

	public void whisperRecieved(BNetUser user, String text) {
		Out.info(this.getClass(), "<From: " + user.getShortPrettyName() + "> " + text);
	}

	public void whisperSent(BNetUser user, String text) {
		Out.info(this.getClass(), "<To: " + user.getShortPrettyName() + "> " + text);
	}

	public void recieveInfo(String text) {
		Out.info(this.getClass(), text);
	}

	public void recieveError(String text) {
		Out.error(this.getClass(), text);
	}

	public void bnetConnected() {}
	public void bnetDisconnected() {}
	public void titleChanged() {}

	public void parseCommand(BNetUser user, String command, String param, boolean wasWhispered) {}

	public void friendsList(FriendEntry[] entries) {}
	public void friendsUpdate(FriendEntry friend) {}
	public void friendsAdd(FriendEntry friend) {}
	public void friendsPosition(byte oldPosition, byte newPosition) {}
	public void friendsRemove(byte entry) {}

	public void clanMOTD(Object cookie, String text) {}
	public void clanMemberList(ClanMember[] members) {}
	public void clanMemberRemoved(String username) {}
	public void clanMemberStatusChange(ClanMember member) {}
	public void clanMemberRankChange(byte oldRank, byte newRank, String user) {}

	public void logonRealmEx(int[] MCPChunk1, int ip, int port, int[] MCPChunk2, String uniqueName) {}
	public void queryRealms2(String[] realms) {}
}
