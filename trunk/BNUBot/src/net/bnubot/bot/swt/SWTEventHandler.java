/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.swt;

import net.bnubot.core.Connection;
import net.bnubot.core.EventHandler;
import net.bnubot.core.Profile;
import net.bnubot.core.clan.ClanMember;
import net.bnubot.core.friend.FriendEntry;
import net.bnubot.util.BNetUser;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * @author sanderson
 *
 */
public class SWTEventHandler implements EventHandler {
	private Connection firstConnection = null;
	private Composite frame;
	
	public SWTEventHandler(Composite parent) {
		frame = parent;
		final Text text = new Text(parent, SWT.MULTI);
		text.setText("asdf");
		parent.pack();
	}
	
	public Composite getFrame() {
		return frame;
	}
	
	public void initialize(Connection source) {
		if(firstConnection == null)
			firstConnection = source;
	}
	
	public void disable(Connection source) {
		//if(source == firstConnection)
		//	SWTDesktop.remove(this);
		//menuBar.remove(settingsMenuItems.remove(source));
		titleChanged(source);
	}

	public Connection getFirstConnection() {
		return this.firstConnection;
	}
	
	public void bnetConnected(Connection source) {}
	public void bnetDisconnected(Connection source) {}
	public void channelJoin(Connection source, BNetUser user) {}
	public void channelLeave(Connection source, BNetUser user) {}
	public void channelUser(Connection source, BNetUser user) {}
	public void clanMOTD(Connection source, Object cookie, String text) {}
	public void clanMemberList(Connection source, ClanMember[] members) {}
	public void clanMemberRankChange(Connection source, byte oldRank, byte newRank, String user) {}
	public void clanMemberRemoved(Connection source, String username) {}
	public void clanMemberStatusChange(Connection source, ClanMember member) {}
	public void friendsAdd(Connection source, FriendEntry friend) {}
	public void friendsList(Connection source, FriendEntry[] entries) {}
	public void friendsPosition(Connection source, byte oldPosition, byte newPosition) {}
	public void friendsRemove(Connection source, byte entry) {}
	public void friendsUpdate(Connection source, FriendEntry friend) {}
	public void joinedChannel(Connection source, String channel) {}
	public void logonRealmEx(Connection source, int[] MCPChunk1, int ip, int port, int[] MCPChunk2, String uniqueName) {}
	public boolean parseCommand(Connection source, BNetUser user, String command, boolean whisperBack) {
		return false;
	}
	public void queryRealms2(Connection source, String[] realms) {}
	public void recieveChat(Connection source, BNetUser user, String text) {}
	public void recieveDebug(Connection source, String text) {}
	public void recieveEmote(Connection source, BNetUser user, String text) {}
	public void recieveError(Connection source, String text) {}
	public void recieveInfo(Connection source, String text) {}
	public void titleChanged(Connection source) {
		SWTDesktop.setTitle(this, source.getProductID());
	}
	public void whisperRecieved(Connection source, BNetUser user, String text) {}
	public void whisperSent(Connection source, BNetUser user, String text) {}

	public String toString() {
		if(firstConnection == null)
			return null;
		Profile p = firstConnection.getProfile();
		if((p == null) || (p.getConnections().size() == 1))
			return firstConnection.toString();
		return p.getName();
	}
}
