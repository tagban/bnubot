/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core;

import java.util.Collection;
import java.util.List;

import net.bnubot.core.bncs.BNCSConnection;
import net.bnubot.core.botnet.BotNetConnection;
import net.bnubot.core.botnet.BotNetUser;
import net.bnubot.core.clan.ClanCreationInvitationCookie;
import net.bnubot.core.clan.ClanInvitationCookie;
import net.bnubot.core.clan.ClanMember;
import net.bnubot.core.friend.FriendEntry;
import net.bnubot.util.BNetUser;

/**
 * @author scotta
 */
public abstract class EventHandler {
	/**
	 * Helper function to commands
	 * @param <T> The type of <code>EventHandler</code> to look for
	 * @param source The connection to search EventHandlers of
	 * @param clazz The <code>Class<T></code> to look for
	 * @return The EventHandler of type T
	 */
	@SuppressWarnings("unchecked")
	public static <T extends EventHandler> T findThis(Connection source, Class<T> clazz) {
		Collection<EventHandler> eventHandlers = source.getEventHandlers();
		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers) {
				if(eh.getClass() == clazz)
					return (T)eh;
			}
		}
		return null;
	}

	protected final Profile profile;
	public EventHandler(Profile profile) {
		this.profile = profile;
	}

	//Initialization
	public void initialize(Connection source) {}
	public void disable(Connection source) {}

	//Connection
	public void bnetConnected(Connection source) {}
	public void bnetDisconnected(Connection source) {}
	public void enterChat(Connection source, BNetUser user) {
		recieveInfo(source, "Logged in as " + user.getFullLogonName() + ".");
	}
	public void titleChanged(Connection source) {}

	//General output
	public void recieveDebug(Connection source, String text) {}
	public void recieveInfo(Connection source, String text) {}
	public void recieveError(Connection source, String text) {}

	//Channel events
	public void joinedChannel(Connection source, String channel) {}
	public void channelUser(Connection source, BNetUser user) {}
	public void channelJoin(Connection source, BNetUser user) {}
	public void channelLeave(Connection source, BNetUser user) {}
	public void recieveChat(Connection source, BNetUser user, String text) {}
	public void recieveBroadcast(Connection source, String username, int flags, String text) {}
	public void recieveEmote(Connection source, BNetUser user, String text) {}
	public void recieveServerInfo(Connection source, String text) {}
	public void recieveServerError(Connection source, String text) {}
	public void whisperSent(Connection source, BNetUser user, String text) {}
	public void whisperRecieved(Connection source, BNetUser user, String text) {}

	//Realms
	public void queryRealms2(BNCSConnection source, String[] realms) {}
	public void logonRealmEx(BNCSConnection source, int[] MCPChunk1, int ip, int port, int[] MCPChunk2, String uniqueName) {}

	//Friends
	public void friendsList(BNCSConnection source, FriendEntry[] entries) {}
	public void friendsUpdate(BNCSConnection source, FriendEntry friend) {}
	public void friendsAdd(BNCSConnection source, FriendEntry friend) {}
	public void friendsRemove(BNCSConnection source, byte entry) {}
	public void friendsPosition(BNCSConnection source, byte oldPosition, byte newPosition) {}

	//Clan
	public void clanMOTD(BNCSConnection source, Object cookie, String text) {}
	public void clanMemberList(BNCSConnection source, ClanMember[] members) {}
	public void clanMemberRemoved(BNCSConnection source, String username) {}
	public void clanMemberStatusChange(BNCSConnection source, ClanMember member) {}
	public void clanMemberRankChange(BNCSConnection source, byte oldRank, byte newRank, String user) {}

	public void clanFindCandidates(BNCSConnection source, Object cookie, List<String> candidates) {}
	public void clanCreationInvitation(BNCSConnection connection, ClanCreationInvitationCookie c) {}
	public void clanInvitation(BNCSConnection connection, ClanInvitationCookie c) {}

	//BotNet
	public void botnetConnected(BotNetConnection source) {}
	public void botnetDisconnected(BotNetConnection source) {}
	public void botnetUserOnline(BotNetConnection source, BotNetUser user) {}
	public void botnetUserStatus(BotNetConnection source, BotNetUser user) {}
	public void botnetUserLogoff(BotNetConnection source, BotNetUser user) {}
}
