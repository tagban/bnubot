/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core;

import net.bnubot.core.clan.ClanMember;
import net.bnubot.core.friend.FriendEntry;
import net.bnubot.util.BNetUser;

public interface EventHandler {
	//Initialization
	public void initialize(Connection source);
	
	//Connection
	public void bnetConnected(Connection source);
	public void bnetDisconnected(Connection source);
	public void titleChanged(Connection source);
	
	//Workarounds
	public boolean parseCommand(Connection source, BNetUser user, String command, String param, boolean whisperBack);

	//Channel events
	public void joinedChannel(Connection source, String channel);
	public void channelUser(Connection source, BNetUser user);
	public void channelJoin(Connection source, BNetUser user);
	public void channelLeave(Connection source, BNetUser user);
	public void recieveChat(Connection source, BNetUser user, String text);
	public void recieveEmote(Connection source, BNetUser user, String text);
	public void recieveDebug(Connection source, String text);
	public void recieveInfo(Connection source, String text);
	public void recieveError(Connection source, String text);
	public void whisperSent(Connection source, BNetUser user, String text);
	public void whisperRecieved(Connection source, BNetUser user, String text);
	
	//Realms
	public void queryRealms2(Connection source, String[] realms);
	public void logonRealmEx(Connection source, int[] MCPChunk1, int ip, int port, int[] MCPChunk2, String uniqueName);
	
	//Friends
	public void friendsList(Connection source, FriendEntry[] entries);
	public void friendsUpdate(Connection source, FriendEntry friend);
	public void friendsAdd(Connection source, FriendEntry friend);
	public void friendsRemove(Connection source, byte entry);
	public void friendsPosition(Connection source, byte oldPosition, byte newPosition);
	
	//Clan
	public void clanMOTD(Connection source, Object cookie, String text);
	public void clanMemberList(Connection source, ClanMember[] members);
	public void clanMemberRemoved(Connection source, String username);
	public void clanMemberStatusChange(Connection source, ClanMember member);
	public void clanMemberRankChange(Connection source, byte oldRank, byte newRank, String user);
	//CLANMEMBERINFORMATION
}
