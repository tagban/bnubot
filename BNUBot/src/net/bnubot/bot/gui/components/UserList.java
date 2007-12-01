/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui.components;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import net.bnubot.bot.gui.GuiEventHandler;
import net.bnubot.bot.gui.colors.ColorScheme;
import net.bnubot.bot.gui.icons.BNetIcon;
import net.bnubot.bot.gui.icons.IconsDotBniReader;
import net.bnubot.core.ChannelListPriority;
import net.bnubot.core.bncs.ProductIDs;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetUser;
import net.bnubot.util.Out;

public class UserList extends JPanel {
	private static final long serialVersionUID = -6511252902076594213L;

	private class UserInfo {
		BNetUser user;
		int lastFlags;
		int priority;
		JLabel label;
		JPopupMenu menu;
	}
	
	private Hashtable<BNetUser, UserInfo> users = null;
	private Box box = null;
	private ColorScheme colors = null;
	private GuiEventHandler geh = null;
	
	public String[] findUsers(String containing) {
		containing = containing.toLowerCase();
		
		List<String> ret = new ArrayList<String>(users.size());
		Enumeration<UserInfo> en = users.elements();
		while(en.hasMoreElements()) {
			String u = en.nextElement().user.getShortLogonName();
			if(GlobalSettings.tabCompleteMode.beginsWithMode()) {
				if(u.toLowerCase().startsWith(containing))
					ret.add(u);
			} else {
				if(u.toLowerCase().contains(containing))
					ret.add(u);
			}
		}
		return ret.toArray(new String[ret.size()]);
	}
	
	/**
	 * Get UserInfo from JLabel
	 * @param lbl The JLabel to look fo
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
	
	private UserInfo getUI(BNetUser user) {
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
	
	public UserList(ColorScheme colors, GuiEventHandler geh) {
		super(new FlowLayout(FlowLayout.LEFT));
		this.users = new Hashtable<BNetUser, UserInfo>();
		this.colors = colors;
		this.geh = geh;
		setBackground(colors.getBackgroundColor());
		box = new Box(BoxLayout.Y_AXIS);
		add(box);
	}
	
	public void clear() {
		box.removeAll();
		users.clear();
		validate();
	}
	
	public int count() {
		return users.size();
	}
	
	private int getInsertPosition(int priority) {
		for(int i = 0; i < box.getComponentCount(); i++) {
			JLabel lbl = (JLabel)box.getComponent(i);
			UserInfo ui = getUI(lbl);
			int pCurrent = ChannelListPriority.getPrioByFlags(ui.user.getFlags());
			
			if(priority > pCurrent)
				return i;
		}
		return box.getComponentCount();
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
	
	public void showUser(BNetUser user) {
		UserInfo ui = getUI(user);
		if(ui == null) {
			ui = new UserInfo();
			ui.user = user;
			ui.priority = ChannelListPriority.getPrioByFlags(user.getFlags());
			
			ui.menu = new JPopupMenu();
			ui.menu.add(new JLabel(user.getShortPrettyName() + user.getStatString().toString()));
			JMenuItem menuItem = new JMenuItem("Whisper");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					UserInfo ui = getUserInfo(arg0);
					if(ui != null)
						geh.setChatText("/w " + ui.user.getShortLogonName() + " ");
				}});
			ui.menu.add(menuItem);
			menuItem = new JMenuItem("Whois");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					UserInfo ui = getUserInfo(arg0);
					if(ui != null)
						geh.getConnection().queueChatHelper("/whois " + ui.user.getShortLogonName(), false);
				}});
			ui.menu.add(menuItem);
			menuItem = new JMenuItem("Profile");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					UserInfo ui = getUserInfo(arg0);
					if(ui != null)
						try { geh.getConnection().sendProfile(ui.user); } catch(Exception e) { Out.exception(e); }
				}});
			ui.menu.add(menuItem);
			ui.menu.add(Box.createHorizontalGlue());
		}
		if(ui.label == null) {
			ui.label = new JLabel(user.getFullLogonName());
			ui.label.setForeground(colors.getUserNameListColor(user.getFlags()));
			box.add(ui.label, getInsertPosition(ui.priority));
			
			ui.label.addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent arg0) {
					JLabel jl = (JLabel) arg0.getSource();
					Enumeration<UserInfo> en = users.elements();
					while(en.hasMoreElements()) {
						UserInfo ui = en.nextElement();
						if(ui.label == jl) {
							switch(arg0.getButton()) {
							case MouseEvent.BUTTON1:
								// "Left clicked on " + ui.label.getText()
								break;
							case MouseEvent.BUTTON2:
								try { geh.getConnection().sendProfile(ui.user); } catch(Exception e) { Out.exception(e); }
								break;
							case MouseEvent.BUTTON3:
								ui.menu.show(arg0.getComponent(), arg0.getX(), arg0.getY());
								break;
							}
							break;
						}
					}
				}
				public void mouseEntered(MouseEvent arg0) {}
				public void mouseExited(MouseEvent arg0) {}
				public void mousePressed(MouseEvent arg0) {}
				public void mouseReleased(MouseEvent arg0) {}
			});
		}
		
		//Check if the user's flags updated
		if(ui.lastFlags != ui.user.getFlags()) {
			//They did; order the list appropriately
			ui.lastFlags = ui.user.getFlags();

			int newPriority = ChannelListPriority.getPrioByFlags(ui.lastFlags);
			if(ui.priority != newPriority) {
				ui.priority = newPriority;
				box.remove(ui.label);
				box.add(ui.label, getInsertPosition(newPriority));
			}
			
			ui.label.setForeground(colors.getUserNameListColor(ui.lastFlags));
		}
				
		Icon icon = null;
		int product = user.getStatString().getProduct();
		int specialIcon = user.getStatString().getIcon();
		if(specialIcon == product)
			specialIcon = 0;
		
		BNetIcon[] icons = IconsDotBniReader.getIcons();
		boolean keepThisIcon = false;
		for(BNetIcon element : icons) {
			//Look for
			if(element.useFor(ui.user.getFlags(), specialIcon)) {
				keepThisIcon = true;
				icon = element.getIcon();
				break;
			}
			if(element.useFor(ui.user.getFlags(), product)) {
				icon = element.getIcon();
			}
		}
		
		if(!keepThisIcon) {
			if(GlobalSettings.enableLegacyIcons) {
				switch(product) {
				case ProductIDs.PRODUCT_STAR:
				case ProductIDs.PRODUCT_SEXP:
				case ProductIDs.PRODUCT_W2BN:
					icons = IconsDotBniReader.getLegacyIcons();
					break;
				case ProductIDs.PRODUCT_WAR3:
					icons = IconsDotBniReader.getIconsWAR3();
					break;
				case ProductIDs.PRODUCT_W3XP:
					icons = IconsDotBniReader.getIconsW3XP();
					break;
				default:
					icons = null;
					break;
				}
				
				if(icons != null) {
					switch(product) {
					case ProductIDs.PRODUCT_STAR:
					case ProductIDs.PRODUCT_SEXP:
						int w = Math.max(Math.min(user.getStatString().getWins(), 10), 0);
						icon = icons[w].getIcon();
						int r = user.getStatString().getLadderRank();
						if(r > 0) {
							if(r == 1)
								icon = icons[IconsDotBniReader.LEGACY_LADDERNUM1].getIcon();
							else
								icon = icons[IconsDotBniReader.LEGACY_LADDER].getIcon();
						}
						break;

					case ProductIDs.PRODUCT_W2BN:
						w = Math.max(Math.min(user.getStatString().getWins(), 10), 0);
						icon = icons[IconsDotBniReader.LEGACY_W2BNWIN + w].getIcon();
						
						r = user.getStatString().getLadderRank();
						if(r > 0) {
							if(r == 1)
								icon = icons[IconsDotBniReader.LEGACY_LADDERNUM1].getIcon();
							else
								icon = icons[IconsDotBniReader.LEGACY_LADDER].getIcon();
						}
						break;
						
					default:
						if(icons != null)
							for(BNetIcon element : icons) {
								if(element.useFor(ui.user.getFlags(), specialIcon)) {
									keepThisIcon = true;
									icon = element.getIcon();
									break;
								}
							}
						break;
					}
				}
			}
		}
		
		if(icon != null)
			ui.label.setIcon(icon);
		
		users.put(user, ui);
		validate();
	}
	
	public void removeUser(BNetUser user) {
		UserInfo ui = getUI(user);
		
		if(ui != null) {
			box.remove(ui.label);
			ui.label = null;
			users.remove(user);
			validate();
		} else {
			Out.error(getClass(), "Attempted to remove a user that was not in the UserList: " + user);
		}
	}

}
