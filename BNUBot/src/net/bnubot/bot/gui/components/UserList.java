/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
import net.bnubot.core.Connection;
import net.bnubot.core.bncs.ProductIDs;
import net.bnubot.logging.Out;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetUser;

/**
 * @author scotta
 */
public class UserList extends JPanel {
	private static final long serialVersionUID = -6511252902076594213L;

	private class UserInfo {
		BNetUser user;
		int lastFlags;
		int priority;
		JLabel label;
		JLabel ping;
		JPopupMenu menu;
	}

	private final Map<BNetUser, UserInfo> users = new HashMap<BNetUser, UserInfo>();
	private final Box box = new Box(BoxLayout.Y_AXIS);
	private final ColorScheme colors = ColorScheme.getColors();
	private final GuiEventHandler geh;

	/**
	 * Get UserInfo from JLabel
	 * @param lbl The JLabel to look for
	 * @return The UserInfo, or null if not found
	 */
	private UserInfo getUI(JLabel lbl) {
		for(Entry<BNetUser, UserInfo> en : users.entrySet()) {
			UserInfo ui = en.getValue();
			if(ui.label == lbl)
				return ui;
		}
		return null;
	}

	private UserInfo getUI(BNetUser user) {
		UserInfo ui = users.get(user);
		if(ui != null)
			return ui;

		for(Entry<BNetUser, UserInfo> en : users.entrySet()) {
			ui = en.getValue();
			if(ui.user.equals(user))
				return ui;
		}

		return null;
	}

	public UserList(GuiEventHandler geh) {
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

	private int getInsertPosition(int priority) {
		for(int i = 0; i < box.getComponentCount(); i++) {
			Container box2 = (Container)box.getComponent(i);
			JLabel lbl = (JLabel)box2.getComponent(0);
			UserInfo ui = getUI(lbl);
			if(ui == null)
				Out.error(UserList.class, "Couldn't find UserInfo for " + lbl.getText());
			else {
				int pCurrent = ChannelListPriority.getPrioByFlags(ui.user.getFlags());
				if(priority > pCurrent)
					return i;
			}
		}
		return box.getComponentCount();
	}

	private UserInfo getUserInfo(ActionEvent arg0) {
		JMenuItem jmi = (JMenuItem) arg0.getSource();
		JPopupMenu jp = (JPopupMenu) jmi.getParent();

		for(Entry<BNetUser, UserInfo> en : users.entrySet()) {
			UserInfo ui = en.getValue();
			if(ui.menu == jp) {
				//Found them
				return ui;
			}
		}
		return null;
	}

	public void showUser(final Connection source, BNetUser user) {
		UserInfo ui = getUI(user);
		if(ui == null) {
			ui = new UserInfo();
			ui.user = user;
			ui.priority = ChannelListPriority.getPrioByFlags(user.getFlags());

			ui.menu = new JPopupMenu();
			ui.menu.add(new JLabel(user.toString() + user.getStatString().toString()));
			JMenuItem menuItem = new JMenuItem("Whisper");
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					UserInfo ui = getUserInfo(arg0);
					if(ui != null)
						geh.setChatText(ui.user.getWhisperCommand());
				}});
			ui.menu.add(menuItem);
			menuItem = new JMenuItem("Whois");
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					UserInfo ui = getUserInfo(arg0);
					if(ui != null)
						source.sendChatInternal("/whois " + ui.user.getShortLogonName());
				}});
			ui.menu.add(menuItem);
			menuItem = new JMenuItem("Profile");
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					UserInfo ui = getUserInfo(arg0);
					if(ui != null)
						try { source.sendProfile(ui.user); } catch(Exception e) { Out.exception(e); }
				}});
			ui.menu.add(menuItem);
			ui.menu.add(Box.createHorizontalGlue());
		}
		if(ui.label == null) {
			Color fg = colors.getUserNameListColor(user.getFlags(), source.getProfile().isOneOfMyUsers(user));

			ui.label = new JLabel(user.toString(GlobalSettings.bnUserToStringUserList));
			ui.label.setForeground(fg);

			ui.ping = new JLabel();
			ui.ping.setForeground(fg);

			JPanel lbl = new JPanel(new BorderLayout());
			lbl.setBackground(colors.getBackgroundColor());
			lbl.add(ui.label, BorderLayout.WEST);
			lbl.add(Box.createHorizontalGlue(), BorderLayout.CENTER);
			lbl.add(ui.ping, BorderLayout.EAST);

			box.add(lbl, getInsertPosition(ui.priority));

			ui.label.addMouseListener(new MouseListener() {
				@Override
				public void mouseClicked(MouseEvent arg0) {
					JLabel jl = (JLabel) arg0.getSource();
					for(Entry<BNetUser, UserInfo> en : users.entrySet()) {
						UserInfo ui = en.getValue();
						if(ui.label == jl) {
							switch(arg0.getButton()) {
							case MouseEvent.BUTTON1:
								// "Left clicked on " + ui.label.getText()
								break;
							case MouseEvent.BUTTON2:
								try { source.sendProfile(ui.user); } catch(Exception e) { Out.exception(e); }
								break;
							case MouseEvent.BUTTON3:
								ui.menu.show(arg0.getComponent(), arg0.getX(), arg0.getY());
								break;
							}
							break;
						}
					}
				}
				@Override
				public void mouseEntered(MouseEvent arg0) {}
				@Override
				public void mouseExited(MouseEvent arg0) {}
				@Override
				public void mousePressed(MouseEvent arg0) {}
				@Override
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
				Container x = ui.label.getParent();
				box.remove(x);
				box.add(x, getInsertPosition(newPriority));
			}

			Color fg = colors.getUserNameListColor(ui.lastFlags, source.getProfile().isOneOfMyUsers(ui.user));
			ui.label.setForeground(fg);
			ui.ping.setForeground(fg);
		}

		Icon icon = null;
		ProductIDs product = user.getStatString().getProduct();
		int specialIcon = user.getStatString().getIcon();
		if(specialIcon == product.getDword())
			specialIcon = 0;

		BNetIcon[] icons = IconsDotBniReader.getIcons();
		boolean keepThisIcon = false;
		if(icons != null)
			for(BNetIcon element : icons) {
				//Look for
				if(element.useFor(ui.user.getFlags(), specialIcon)) {
					keepThisIcon = true;
					icon = element.getIcon();
					break;
				}
				if(element.useFor(ui.user.getFlags(), product.getDword())) {
					icon = element.getIcon();
				}
			}

		if(!keepThisIcon) {
			if(GlobalSettings.enableLegacyIcons) {
				switch(product) {
				case STAR:
				case SEXP:
				case W2BN:
					icons = IconsDotBniReader.getLegacyIcons();
					break;
				case WAR3:
					icons = IconsDotBniReader.getIconsWAR3();
					break;
				case W3XP:
					icons = IconsDotBniReader.getIconsW3XP();
					break;
				default:
					icons = null;
					break;
				}

				if(icons != null) {
					switch(product) {
					case STAR:
					case SEXP:
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

					case W2BN:
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

		icons = IconsDotBniReader.getIconsLag();
		if((icons != null) && (user.getPing() != null)) {
			int ping = user.getPing().intValue();

			if((user.getFlags() & 0x10) != 0)
				ui.ping.setIcon(icons[7].getIcon());
			else if(ping < 0)
				ui.ping.setIcon(icons[6].getIcon());
			else if(ping < 10)
				ui.ping.setIcon(icons[0].getIcon());
			else if(ping < 200)
				ui.ping.setIcon(icons[1].getIcon());
			else if(ping < 300)
				ui.ping.setIcon(icons[2].getIcon());
			else if(ping < 400)
				ui.ping.setIcon(icons[3].getIcon());
			else if(ping < 500)
				ui.ping.setIcon(icons[4].getIcon());
			else if(ping < 600)
				ui.ping.setIcon(icons[5].getIcon());
			else
				ui.ping.setIcon(icons[6].getIcon());
		}

		users.put(user, ui);
		validate();
	}

	public void removeUser(BNetUser user) {
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
