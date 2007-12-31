/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JList;

import net.bnubot.core.Connection;
import net.bnubot.core.EventHandler;
import net.bnubot.core.RealmConnection;
import net.bnubot.core.RealmEventHandler;
import net.bnubot.core.clan.ClanMember;
import net.bnubot.core.friend.FriendEntry;
import net.bnubot.core.mcp.MCPConnection;
import net.bnubot.util.BNetUser;
import net.bnubot.util.Out;

public class RealmWindow extends JDialog implements EventHandler, RealmEventHandler {
	private static final long serialVersionUID = 3965057306231374646L;
	
	protected Connection con = null;
	protected RealmConnection realmCon = null;
	protected String[] realms = null;

	protected Box b;
	protected JList lstRealms;
	protected JList lstCharactorTypes;
	
	public RealmWindow(String[] realms) {
		this.realms = realms;
		initializeGUI();
		setTitle("Realms");
		
		pack();
		setModal(true);
	}
	
	public void initializeGUI() {
		DefaultListModel lm = new DefaultListModel();
		for(String realm : realms)
			lm.addElement(realm);
		lstRealms = new JList(lm);
		lstRealms.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent arg0) {
				if(con == null) {
					Out.error(RealmWindow.class, "No BNCS connection set.");
					return;
				}
				String realm = (String)lstRealms.getSelectedValue();
				Out.debug(RealmWindow.class, "Logging on to realm " + realm);
				try {
					con.sendLogonRealmEx(realm);
					setVisible(false);
				} catch (Exception e) {
					Out.fatalException(e);
				}
			}
			public void mouseEntered(MouseEvent arg0) {}
			public void mouseExited(MouseEvent arg0) {}
			public void mousePressed(MouseEvent arg0) {}
			public void mouseReleased(MouseEvent arg0) {}
			
		});
		add(lstRealms);

		b = new Box(BoxLayout.Y_AXIS);
		{
			lm = new DefaultListModel();
			lm.addElement("Amazon");
			lm.addElement("Sorceress");
			lm.addElement("Necromancer");
			lm.addElement("Paladin");
			lm.addElement("Barbarian");
			lm.addElement("Druid");
			lm.addElement("Assassin");
			lstCharactorTypes = new JList(lm);
			b.add(lstCharactorTypes);
			
		}
	}

	public void initialize(Connection source) {}
	public void disable(Connection source) {}

	public void initialize(RealmConnection rc) {
		this.realmCon = rc;
	}

	public void bnetConnected(Connection source) {}
	public void bnetDisconnected(Connection source) {}
	public void titleChanged(Connection source) {}

	public boolean parseCommand(Connection source, BNetUser user, String command, boolean wasWhispered) {return false;}
	
	public void realmConnected() {}
	public void realmDisconnected() {}

	public void channelJoin(Connection source, BNetUser user) {}
	public void channelUser(Connection source, BNetUser user) {}
	public void channelLeave(Connection source, BNetUser user) {}
	
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

	public void joinedChannel(Connection source, String channel) {}
	public void recieveChat(Connection source, BNetUser user, String text) {}
	public void recieveEmote(Connection source, BNetUser user, String text) {}
	public void recieveError(Connection source, String text) {}
	public void recieveInfo(Connection source, String text) {}
	public void recieveDebug(Connection source, String text) {}
	public void whisperRecieved(Connection source, BNetUser user, String text) {}
	public void whisperSent(Connection source, BNetUser user, String text) {}

	public void queryRealms2(Connection source, String[] realms) {}

	public void logonRealmEx(Connection source, int[] MCPChunk1, int ip, int port, int[] MCPChunk2, String uniqueName) {
		MCPConnection mcpc = new MCPConnection(MCPChunk1, ip, port, MCPChunk2, uniqueName);
		mcpc.addRealmEventHandler(this);
		mcpc.start();
		
		//remove(lstRealms);
		//add(b);
		//validate();
	}

	public void recieveRealmError(String text) {
		Out.error(getClass(), text);
	}

	public void recieveRealmInfo(String text) {
		Out.info(getClass(), text);
	}

}
