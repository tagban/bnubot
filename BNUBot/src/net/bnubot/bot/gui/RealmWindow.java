/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JList;

import net.bnubot.core.Connection;
import net.bnubot.core.EventHandler;
import net.bnubot.core.Profile;
import net.bnubot.core.RealmConnection;
import net.bnubot.core.RealmEventHandler;
import net.bnubot.core.bncs.BNCSConnection;
import net.bnubot.core.mcp.MCPCharacter;
import net.bnubot.core.mcp.MCPConnection;
import net.bnubot.logging.Out;

/**
 * @author scotta
 */
public class RealmWindow extends EventHandler implements RealmEventHandler {
	protected final BNCSConnection con;
	protected RealmConnection realmCon = null;
	protected String[] realms = null;

	protected JDialog jd;
	protected Box b;
	protected JList<String> lstRealms;
	protected JList<String> lstCharacterTypes;
	protected JList<String> lstCharacters;

	public RealmWindow(BNCSConnection con, String[] realms, Profile profile) {
		super(profile);
		this.realms = realms;
		this.con = con;
		jd = new JDialog();
		initializeGUI();
		jd.setTitle("Realms");

		jd.pack();
		jd.setModal(true);
	}

	public void initializeGUI() {
		DefaultListModel<String> lm = new DefaultListModel<String>();
		for(String realm : realms)
			lm.addElement(realm);
		lstRealms = new JList<String>(lm);
		lstRealms.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(con == null) {
					Out.error(RealmWindow.class, "No BNCS connection set.");
					return;
				}
				String realm = lstRealms.getSelectedValue();
				Out.debug(RealmWindow.class, "Logging on to realm " + realm);
				try {
					con.sendLogonRealmEx(realm);
					jd.setVisible(false);
				} catch (Exception e) {
					Out.fatalException(e);
				}
			}});
		jd.add(lstRealms);

		b = new Box(BoxLayout.Y_AXIS);
		{
			lm = new DefaultListModel<String>();
			lm.addElement("Amazon");
			lm.addElement("Sorceress");
			lm.addElement("Necromancer");
			lm.addElement("Paladin");
			lm.addElement("Barbarian");
			lm.addElement("Druid");
			lm.addElement("Assassin");
			lstCharacterTypes = new JList<String>(lm);
			b.add(lstCharacterTypes);

			lstCharacters = new JList<String>(new DefaultListModel<String>());
			lstCharacters.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent arg0) {
					if(realmCon == null) {
						Out.error(RealmWindow.class, "No MCP connection set.");
						return;
					}
					String c = lstCharacters.getSelectedValue();
					Out.debug(RealmWindow.class, "Logging on to character " + c);
					try {
						realmCon.sendLogonCharacter(c);
						jd.setVisible(false);
					} catch (Exception e) {
						Out.fatalException(e);
					}
				}});
			b.add(lstCharacters);
		}
	}

	@Override
	public void initialize(RealmConnection rc) {
		this.realmCon = rc;
	}

	@Override
	public void realmConnected() {}
	@Override
	public void realmDisconnected() {
		for(Connection c : profile.getConnections())
			c.removeEventHandler(this);
		Out.info(getClass(), "Disconnected from MCP");
	}

	@Override
	public void logonRealmEx(BNCSConnection source, int[] MCPChunk1, int ip, int port, int[] MCPChunk2, String uniqueName) {
		if(realmCon == null) {
			realmCon = new MCPConnection(MCPChunk1, ip, port, MCPChunk2, uniqueName);
			realmCon.addRealmEventHandler(this);
			realmCon.start();
		}
	}

	@Override
	public void recieveRealmError(String text) {
		con.dispatchRecieveError("[MCP] " + text);
	}

	@Override
	public void recieveRealmInfo(String text) {
		con.dispatchRecieveInfo("[MCP] " + text);
	}

	@Override
	public void recieveCharacterList(List<MCPCharacter> chars) {
		DefaultListModel<String> lm = (DefaultListModel<String>) lstCharacters.getModel();
		lm.removeAllElements();
		for(MCPCharacter c : chars)
			lm.addElement(c.getName());

		jd.remove(lstRealms);
		jd.add(b);
		jd.validate();
		jd.setVisible(true);
		jd.pack();
	}
}
