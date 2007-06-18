package bnubot.bot.gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.*;

import bnubot.bot.EventHandler;
import bnubot.bot.RealmEventHandler;
import bnubot.core.BNetUser;
import bnubot.core.Connection;
import bnubot.core.RealmConnection;
import bnubot.core.StatString;
import bnubot.core.clan.ClanMember;
import bnubot.core.friend.FriendEntry;
import bnubot.core.mcp.MCPConnection;

@SuppressWarnings("serial")
public class RealmWindow extends JFrame implements EventHandler, RealmEventHandler {
	protected Connection c = null;
	protected RealmConnection rc = null;
	protected String[] realms = null;

	protected Box b;
	protected JList lstRealms;
	protected JList lstCharactorTypes;
	
	public RealmWindow(String[] realms) {
		super("Realms");
		this.realms = realms;
		
		initializeGUI();
		
		setBounds(0, 0, 300, 150);
	}
	
	public void initializeGUI() {
		DefaultListModel lm = new DefaultListModel();
		for(String s : realms)
			lm.addElement(s);
		lstRealms = new JList(lm);
		lstRealms.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent arg0) {
				String s = (String)lstRealms.getSelectedValue();
				try {
					c.sendLogonRealmEx(s);
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
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
	
	public void initialize(Connection c) {
		this.c = c;
	}

	public void initialize(RealmConnection rc) {
		this.rc = rc;
	}

	public void bnetConnected() {}
	public void bnetDisconnected() {}
	public void titleChanged() {}

	public void parseCommand(BNetUser user, String command, String param) {}
	
	public void realmConnected() {}
	public void realmDisconnected() {}

	public void channelJoin(BNetUser user, StatString statstr) {}
	public void channelLeave(BNetUser user, StatString statstr) {}
	public void channelUser(BNetUser user, StatString statstr) {}
	public void clanMOTD(Object cookie, String text) {}
	public void clanMemberList(ClanMember[] members) {}
	public void clanMemberRankChange(byte oldRank, byte newRank, String user) {}
	public void friendsList(FriendEntry[] entries) {}
	public void friendsUpdate(byte entry, byte location, byte status, int product, String locationName) {}

	public void joinedChannel(String channel) {}
	public void recieveChat(BNetUser user, String text) {}
	public void recieveEmote(BNetUser user, String text) {}
	public void recieveError(String text) {}
	public void recieveInfo(String text) {}
	public void whisperRecieved(BNetUser user, String text) {}
	public void whisperSent(BNetUser user, String text) {}

	public void queryRealms2(String[] realms) {}

	public void logonRealmEx(int[] MCPChunk1, int ip, int port, int[] MCPChunk2, String uniqueName) {
		MCPConnection mcpc = new MCPConnection(MCPChunk1, ip, port, MCPChunk2, uniqueName);
		mcpc.addRealmEventHandler(this);
		mcpc.start();
		
		remove(lstRealms);
		add(b);
		validate();
	}

}
