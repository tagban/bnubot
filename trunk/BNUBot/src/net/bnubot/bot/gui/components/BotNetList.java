/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import net.bnubot.bot.gui.GuiEventHandler;
import net.bnubot.bot.gui.colors.ColorScheme;
import net.bnubot.bot.gui.icons.BNetIcon;
import net.bnubot.bot.gui.icons.IconsDotBniReader;
import net.bnubot.core.Connection;
import net.bnubot.core.botnet.BotNetUser;
import net.bnubot.util.Out;

public class BotNetList extends JPanel {
	private static final long serialVersionUID = 825097004919144106L;

	private class UserInfo {
		BotNetUser user;
		JLabel label;
		JLabel database;
		JPopupMenu menu;
	}
	
	private final Hashtable<BotNetUser, UserInfo> users = new Hashtable<BotNetUser, UserInfo>();
	private final Box box = new Box(BoxLayout.Y_AXIS);
	private final ColorScheme colors = ColorScheme.getColors();
	private final GuiEventHandler geh;
	
	/**
	 * Get UserInfo from JLabel
	 * @param lbl The JLabel to look for
	 * @return The UserInfo, or null if not found
	 */
	private UserInfo getUI(JLabel lbl) {
		Enumeration<UserInfo> en = users.elements();
		while(en.hasMoreElements()) {
			UserInfo ui = en.nextElement();
			if(ui.label == lbl)
				return ui;
		}
		return null;
	}
	
	private UserInfo getUI(BotNetUser user) {
		UserInfo ui = users.get(user);
		if(ui != null)
			return ui;
		
		Enumeration<UserInfo> en = users.elements();
		while(en.hasMoreElements()) {
			ui = en.nextElement();
			if(ui.user.equals(user))
				return ui;
		}
		
		return null;
	}
	
	public BotNetList(GuiEventHandler geh) {
		super(new BorderLayout());
		this.geh = geh;
		setBackground(colors.getBackgroundColor());
		
		add(box, BorderLayout.NORTH);
	}
	
	public void clear() {
		box.removeAll();
		users.clear();
		validate();
	}
	
	public int count() {
		return users.size();
	}
	
	private UserInfo getUserInfo(ActionEvent arg0) {
		JMenuItem jmi = (JMenuItem) arg0.getSource();
		JPopupMenu jp = (JPopupMenu) jmi.getParent();
		
		Enumeration<UserInfo> en = users.elements();
		while(en.hasMoreElements()) {
			UserInfo ui = en.nextElement();
			if(ui.menu == jp) {
				//Found them
				return ui;
			}
		}
		return null;
	}
	
	public void showUser(final Connection source, BotNetUser user) {
		UserInfo ui = getUI(user);
		if(ui == null) {
			ui = new UserInfo();
			ui.user = user;
			
			ui.menu = new JPopupMenu();
			ui.menu.add(new JLabel(user.toString() + user.toStringEx()));
			JMenuItem menuItem = new JMenuItem("Whisper");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					UserInfo ui = getUserInfo(arg0);
					if(ui != null)
						geh.setChatText("/botnet whisper " + ui.user.getHandle() + " ");
				}});
			ui.menu.add(menuItem);
			menuItem = new JMenuItem("Whois");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					UserInfo ui = getUserInfo(arg0);
					if(ui != null)
						source.sendChat("/botnet whois " + ui.user.getHandle(), false);
				}});
			ui.menu.add(menuItem);
			ui.menu.add(Box.createHorizontalGlue());
		}
		if(ui.label == null) {
			//TODO: Color fg = colors.getUserNameListColor(user.getFlags(), user.equals(source.getMyUser()));
			Color fg = colors.getUserNameListColor(0, false);
			
			ui.label = new JLabel(user.toString());
			ui.label.setForeground(fg);
			
			ui.database = new JLabel(user.getDatabase());
			ui.database.setForeground(fg);
			
			JPanel lbl = new JPanel(new BorderLayout());
			lbl.setBackground(colors.getBackgroundColor());
			lbl.add(ui.label, BorderLayout.WEST);
			lbl.add(Box.createHorizontalGlue(), BorderLayout.CENTER);
			lbl.add(ui.database, BorderLayout.EAST);
			
			box.add(lbl);
			
			ui.label.addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent arg0) {
					UserInfo ui = getUI((JLabel) arg0.getSource());
					switch(arg0.getButton()) {
					case MouseEvent.BUTTON1:
						// "Left clicked on " + ui.label.getText()
						break;
					case MouseEvent.BUTTON3:
						ui.menu.show(arg0.getComponent(), arg0.getX(), arg0.getY());
						break;
					}
				}
				public void mouseEntered(MouseEvent arg0) {}
				public void mouseExited(MouseEvent arg0) {}
				public void mousePressed(MouseEvent arg0) {}
				public void mouseReleased(MouseEvent arg0) {}
			});
		}
		
		//Check if the user's flags updated
		/*if(ui.lastFlags != ui.user.getFlags()) {
			//They did; order the list appropriately
			ui.lastFlags = ui.user.getFlags();

			int newPriority = ChannelListPriority.getPrioByFlags(ui.lastFlags);
			if(ui.priority != newPriority) {
				ui.priority = newPriority;
				Container x = ui.label.getParent();
				box.remove(x);
				box.add(x, getInsertPosition(newPriority));
			}
			
			Color fg = colors.getUserNameListColor(ui.lastFlags, ui.user.equals(source.getMyUser()));
			ui.label.setForeground(fg);
			ui.ping.setForeground(fg);
		}*/
		
		BNetIcon[] icons = IconsDotBniReader.getIcons();
		if(icons != null) {
			BNetIcon icon = icons[4];
			
			/* A = superuser, can perform any administrative action
			 * B = broadcast, may use talk-to-all
			 * C = connection, may administer botnet connectivity
			 * D = database, may create and maintain databases
			 * I = ID control, may create and modify hub IDs
			 * S = botnet service
			 */
			String flags = user.getZTFF();
			if(flags.contains("B"))
				icon = icons[3]; // Speaker
			if(flags.contains("S"))
				icon = icons[1]; // B.net
			if(flags.contains("C"))
				icon = icons[2]; // Oper
			if(flags.contains("A"))
				icon = icons[0]; // Blizzard
			
			if(icon != null)
				ui.label.setIcon(icon.getIcon());
		}
		
		users.put(user, ui);
		validate();
	}
	
	public void removeUser(BotNetUser user) {
		UserInfo ui = getUI(user);
		
		if(ui != null) {
			box.remove(ui.label.getParent());
			ui.label = null;
			users.remove(user);
			validate();
		} else {
			Out.error(getClass(), "Attempted to remove a user that was not in the UserList: " + user);
		}
	}

}
