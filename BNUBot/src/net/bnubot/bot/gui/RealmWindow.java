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
import net.bnubot.core.Profile;
import net.bnubot.core.RealmConnection;
import net.bnubot.core.RealmEventHandler;
import net.bnubot.core.bncs.BNCSConnection;
import net.bnubot.core.mcp.MCPConnection;
import net.bnubot.util.Out;

/**
 * @author scotta
 */
public class RealmWindow extends EventHandler implements RealmEventHandler {
	private static final long serialVersionUID = 3965057306231374646L;

	protected Connection con = null;
	protected RealmConnection realmCon = null;
	protected String[] realms = null;

	protected JDialog jd;
	protected Box b;
	protected JList lstRealms;
	protected JList lstCharactorTypes;

	public RealmWindow(String[] realms, Profile profile) {
		super(profile);
		this.realms = realms;
		jd = new JDialog();
		initializeGUI();
		jd.setTitle("Realms");

		jd.pack();
		jd.setModal(true);
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
					jd.setVisible(false);
				} catch (Exception e) {
					Out.fatalException(e);
				}
			}
			public void mouseEntered(MouseEvent arg0) {}
			public void mouseExited(MouseEvent arg0) {}
			public void mousePressed(MouseEvent arg0) {}
			public void mouseReleased(MouseEvent arg0) {}

		});
		jd.add(lstRealms);

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

	public void initialize(RealmConnection rc) {
		this.realmCon = rc;
	}

	public void realmConnected() {}
	public void realmDisconnected() {}

	@Override
	public void logonRealmEx(BNCSConnection source, int[] MCPChunk1, int ip, int port, int[] MCPChunk2, String uniqueName) {
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
