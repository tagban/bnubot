/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui;

import java.awt.AWTKeyStroke;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.JPopupMenu.Separator;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import net.bnubot.bot.gui.colors.ColorScheme;
import net.bnubot.bot.gui.components.ClanList;
import net.bnubot.bot.gui.components.ColoredTextField;
import net.bnubot.bot.gui.components.FriendList;
import net.bnubot.bot.gui.components.TextWindow;
import net.bnubot.bot.gui.components.UserList;
import net.bnubot.bot.gui.notifications.Growl;
import net.bnubot.bot.gui.settings.ConfigurationFrame;
import net.bnubot.core.Connection;
import net.bnubot.core.EventHandler;
import net.bnubot.core.clan.ClanMember;
import net.bnubot.core.friend.FriendEntry;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.settings.Settings;
import net.bnubot.util.BNetUser;
import net.bnubot.util.Out;

public class GuiEventHandler implements EventHandler {
	private Connection firstConnection = null;
	private JPanel frame = null;
	private TextWindow mainTextArea = null;
	private JTextField chatTextArea = null;
	private JTextField channelTextPane = null;
	private UserList userList = null;
	private FriendList friendList = null;
	private ClanList clanList = null;
	private String channel = null;
	private final JMenu menuBar = new JMenu();
	private BNetUser lastWhisperFrom = null;
	private JSplitPane jsp = null;
	private boolean tabComplete = false;
	private JDialog tcPopupWindow;
	private final JList tcList = new JList();
	private String tcBefore = null;
	private String tcUser = null;
	private String tcAfter = null;
	private static final int textHeight = 23;
	private static final int paddingHeight = 4;
	
	private static final Set<? extends AWTKeyStroke> EMPTY_SET = Collections.emptySet();
	
	public GuiEventHandler(Connection firstConnection) {
		this.firstConnection = firstConnection;
		initializeGui(ColorScheme.createColorScheme(GlobalSettings.colorScheme));
	}
	
	private static Map<Connection, JMenuItem> settingsMenuItems = new HashMap<Connection, JMenuItem>();
	public void initialize(final Connection source) {
		Out.setThreadOutputConnection(source);
		
		JMenuItem settings = new JMenuItem("Settings");
		settings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new ConfigurationFrame(source.getConnectionSettings());
			}});
		settingsMenuItems.put(source, settings);
		
		for(int i = 0; i < menuBar.getMenuComponentCount(); i++) {
			Object x = menuBar.getMenuComponent(i);
			if(x instanceof Separator) {
				menuBar.add(settings, i);
				break;
			}
		}
		titleChanged(source);
	}
	
	public void disable(final Connection source) {
		if(source == firstConnection) {
			GuiDesktop.remove(this);
		}
	}
	
	/**
	 * Update the list of the TC popup
	 * @param source TODO
	 */
	private void tcUpdate(Connection source) {
		List<String> users = source.findUsersForTabComplete(tcUser);
		if(users.size() == 1) {
			tcSelect(users.get(0));
		} else if(users.size() == 0) {
			tcCancel();
		} else {
			tcList.setModel(new DefaultComboBoxModel(users.toArray()));
			tcPopupWindow.pack();
			tcPopupWindow.setVisible(true);
			tcList.setSelectedIndex(0);
			tcList.requestFocus();
		}
	}
	
	/**
	 * Exit TC mode
	 */
	private void tcCancel() {
		// Exit TC mode
		tcPopupWindow.setVisible(tabComplete = false);
	}
	
	/**
	 * Exit TC mode by selecting a username
	 * @param selection The selected username
	 */
	private void tcSelect(String selection) {
		if((tcBefore.length() == 0) && (tcAfter.length() == 0)) {
			chatTextArea.setText(selection + ": ");
			chatTextArea.setCaretPosition(chatTextArea.getText().length());
		} else {
			chatTextArea.setText(tcBefore + selection + tcAfter);
			chatTextArea.setCaretPosition(tcBefore.length() + selection.length());
		}

		// Exit TC mode
		tcPopupWindow.setVisible(tabComplete = false);
	}
	
	private void initializeGui(ColorScheme colors) {
		// Create the panel
		frame = new JPanel(new BorderLayout());
		
		// When gained focus, select the chat box
		frame.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				chatTextArea.requestFocus();
			}
			public void focusLost(FocusEvent e) {}
		});
		
		// Create the menu bar.
		menuBar.setOpaque(true);
		{
			JMenu menu;
			JMenuItem menuItem;
			
			menuBar.addSeparator();

			menu = new JMenu("Battle.net");
			{
				menuItem = new JMenuItem("Connect");
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(!firstConnection.isConnected())
							firstConnection.setConnected(true);
					} });
				menu.add(menuItem);
				
				menuItem = new JMenuItem("Reconnect");
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(firstConnection.isConnected())
							firstConnection.reconnect();
					} });
				menu.add(menuItem);
				
				menuItem = new JMenuItem("Disconnect");
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(firstConnection.isConnected())
							firstConnection.setConnected(false);
					} });
				menu.add(menuItem);
			}
			menuBar.add(menu);
			
			menuItem = new JMenuItem("Realms");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					try {
						firstConnection.sendQueryRealms2();
					} catch (Exception e) {
						Out.exception(e);
					}
				} });
			menuBar.add(menuItem);

			menu = new JMenu("Clan");
			{
				menuItem = new JMenuItem("Edit MOTD");
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						try {
							firstConnection.sendClanMOTD(new ClanMOTDEditor(firstConnection));
						} catch(Exception e) {
							Out.exception(e);
						}
					} });
				menu.add(menuItem);
			}
			menuBar.add(menu);
		}
		//frame.setJMenuBar(menuBar);
		
		// Main text area
		mainTextArea = new TextWindow(colors);
		// Send chat textbox
		chatTextArea = new ColoredTextField(colors);
		chatTextArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, textHeight));
		// Enable tab character
		chatTextArea.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, EMPTY_SET);
		chatTextArea.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, EMPTY_SET);
		chatTextArea.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {
				switch(e.getKeyChar()) {
				case '\n': {
					e.consume();
					try {
						String text[] = chatTextArea.getText().split("\n");
						for(String element : text) {
							if(element.trim().length() > 0)
								firstConnection.queueChatHelper(element, true);
						}
						chatTextArea.setText(null);
						return;
					} catch(Exception ex) {
						Out.exception(ex);
					}
					break;
				}
				case '\t': {
					e.consume();
					if(GlobalSettings.tabCompleteMode.enableTC())
						tabComplete = true;
					break;
				}
				case ' ': {
					if(lastWhisperFrom != null) {
						String txt = chatTextArea.getText().trim();
						if("/r".equals(txt) || "/rw".equals(txt))
							chatTextArea.setText("/w " + lastWhisperFrom.getShortLogonName());
					}
					break;
				}
				default:
					break;
				}
			}
			public void keyReleased(KeyEvent e) {
				if(!tabComplete)
					return;

				try {
					int end = chatTextArea.getCaretPosition();
					tcUser = chatTextArea.getText(0, end);
					int start = tcUser.lastIndexOf(' ') + 1;
					if(start != 0)
						tcUser = tcUser.substring(start);

					tcBefore = chatTextArea.getText(0, start);
					tcAfter = chatTextArea.getText(end, chatTextArea.getText().length() - end);

					tcUpdate(firstConnection);
				} catch(Exception ex) {
					Out.exception(ex);
				}
			}
		});
		// Channel text box (above userlist)
		channelTextPane = new ColoredTextField(colors);
		channelTextPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, textHeight));
		channelTextPane.setHorizontalAlignment(JTextField.CENTER);
		channelTextPane.setEditable(false);
		
		// The userlist
		userList = new UserList(colors, this);
		// Friends list
		friendList = new FriendList(colors);
		// Clan list
		clanList = new ClanList(colors);

		JTabbedPane allLists = new JTabbedPane(JTabbedPane.BOTTOM);
		allLists.addTab("Channel", new JScrollPane(userList));
		allLists.addTab("Friends", new JScrollPane(friendList));
		allLists.addTab("Clan", new JScrollPane(clanList));
		
		Box leftSide = new Box(BoxLayout.Y_AXIS);
		leftSide.add(mainTextArea);
		leftSide.add(Box.createVerticalStrut(paddingHeight));
		leftSide.add(chatTextArea);
		
		Box rightSide = new Box(BoxLayout.Y_AXIS);
		rightSide.add(channelTextPane);
		rightSide.add(Box.createVerticalStrut(paddingHeight));
		rightSide.add(allLists);

		jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, leftSide, rightSide);
		jsp.setDividerSize(8);
		jsp.setResizeWeight(1); // Make the left side expand when resizing
		// Add a listener for when the divider moves
		((BasicSplitPaneUI)jsp.getUI()).getDivider().addComponentListener(new ComponentAdapter() {
			public void componentMoved(ComponentEvent e) {
				// If the window is maximized, skip this
				if(GuiDesktop.getInstance().getExtendedState() == Frame.MAXIMIZED_BOTH)
					return;
				// Save the divider location
				Settings.writeInt("GuiDesktop", "dividerLocation", jsp.getDividerLocation());
				Settings.store();
			}
		});
		
		// Add them to the frame
		frame.add(jsp);

		// Initialize the TC popup window
		tcPopupWindow = new JDialog();
		tcPopupWindow.setUndecorated(true);
		tcPopupWindow.addWindowFocusListener(new WindowFocusListener() {
			public void windowGainedFocus(WindowEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						// Set location relative to chatTextArea
						Point location = chatTextArea.getLocation();
						SwingUtilities.convertPointToScreen(location, chatTextArea.getParent());
						location.translate(0, chatTextArea.getHeight()
								+ (chatTextArea.getBorder() == null ? 0
										: chatTextArea.getBorder().getBorderInsets(chatTextArea).bottom));
						tcPopupWindow.setLocation(location);
					}
				});
			}
			public void windowLostFocus(WindowEvent e) {
				tcPopupWindow.setVisible(tabComplete = false);
			}
		});
		tcPopupWindow.getContentPane().setLayout(new BorderLayout());
		((JComponent)tcPopupWindow.getContentPane()).setBorder(BorderFactory.createEtchedBorder());
		tcPopupWindow.getContentPane().add(tcList);
		
		// Initialize TC list
		tcList.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {
				switch(e.getKeyCode()) {
				case KeyEvent.VK_SHIFT:
				case KeyEvent.VK_ALT:
				case KeyEvent.VK_CONTROL:
				case KeyEvent.VK_META:
					// Throw away modifiers
				case KeyEvent.VK_UP:
				case KeyEvent.VK_DOWN:
				case KeyEvent.VK_LEFT:
				case KeyEvent.VK_RIGHT:
					// Throw away arrow keys
					break;
				case KeyEvent.VK_TAB:
				case KeyEvent.VK_ENTER:
					tcSelect(tcList.getSelectedValue().toString());
					break;
				case KeyEvent.VK_ESCAPE:
					tcCancel();
					break;
				default:
					// Pass the keystroke to the TC strings
					if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
						// If there's nothing left in the user string, cancel TC mode
						if(tcUser.length() == 0) {
							tcCancel();
							break;
						}
						// Remove the last char from the string
						tcUser = tcUser.substring(0, tcUser.length() - 1);
					} else {
						// Append the char to the string
						tcUser += e.getKeyChar();
					}
					
					// Update the TextArea with the new strings
					chatTextArea.setText(tcBefore + tcUser + tcAfter);
					chatTextArea.setCaretPosition(tcBefore.length() + tcUser.length());
					
					// Redraw the TC list
					tcUpdate(firstConnection);
					break;
				}
			}
		});
		// Enable tab character
		tcList.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, EMPTY_SET);
		tcList.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, EMPTY_SET);
		
		// Display the window
		GuiDesktop.add(this);
	}

	public JPanel getFrame() {
		return frame;
	}
	
	public JMenu getMenuBar() {
		return menuBar;
	}

	private void notifySystemTray(String gt, String headline, String text) {
		// Require that enableTrayPopups is set
		if(!GlobalSettings.trayIconMode.enableTray())
			return;
		
		// If popups are not always enabled, require that the window is defocused
		if(!GlobalSettings.trayIconMode.alwaysDisplayPopups())
			if(GuiDesktop.getInstance().isFocused())
				return;
		
		TrayIcon tray = GuiDesktop.getTray();
		if(tray != null)
			tray.displayMessage(headline, text, TrayIcon.MessageType.INFO);
		
		Growl growl = GuiDesktop.getGrowl();
		if(growl != null)
			try {
				growl.notifyGrowlOf(gt, headline, text);
			} catch (Exception e) {
				Out.exception(e);
			}
	}
	
	public void channelJoin(Connection source, BNetUser user) {
		userList.showUser(source, user);
		if(GlobalSettings.displayJoinParts)
			mainTextArea.channelInfo(user + " has joined the channel" + user.getStatString().toString() + ".");
		notifySystemTray(
				Growl.CHANNEL_USER_JOIN,
				source.getChannel(),
				user.getShortPrettyName() + " joined");
		channelTextPane.setText(channel + " (" + userList.count() + ")");
	}

	public void channelLeave(Connection source, BNetUser user) {
		userList.removeUser(user);
		if(GlobalSettings.displayJoinParts)
			mainTextArea.channelInfo(user + " has left the channel.");
		notifySystemTray(
				Growl.CHANNEL_USER_PART,
				source.getChannel(),
				user.getShortPrettyName() + " left");
		channelTextPane.setText(channel + " (" + userList.count() + ")");
	}

	public void channelUser(Connection source, BNetUser user) {
		if(GlobalSettings.displayChannelUsers)
			mainTextArea.channelInfo(user + user.getStatString().toString() + ".");
		userList.showUser(source, user);
		channelTextPane.setText(channel + " (" + userList.count() + ")");
	}

	public void joinedChannel(Connection source, String channel) {
		this.channel = channel;
		userList.clear();
		mainTextArea.addSeparator();
		mainTextArea.channelInfo("Joining channel " + channel + ".");
		channelTextPane.setText(channel);
		notifySystemTray(
				Growl.CHANNEL,
				"Channel",
				channel);
		
		GuiDesktop.setTitle(this, source.getProductID());
	}

	public void recieveChat(Connection source, BNetUser user, String text) {
		mainTextArea.userChat(user, text, user.equals(source.getMyUser()));
		notifySystemTray(
				Growl.CHANNEL_USER_CHAT,
				source.getChannel(),
				"<" + user.getShortPrettyName() + "> " + text);
	}

	public void recieveEmote(Connection source, BNetUser user, String text) {
		mainTextArea.userEmote(user, text);
		notifySystemTray(
				Growl.CHANNEL_USER_EMOTE,
				source.getChannel(),
				"<" + user.getShortPrettyName() + " " + text + ">");
	}

	private static long lastInfoRecieved = 0;
	private static String lastInfo = null;
	public void recieveInfo(Connection source, String text) {
		long now = System.currentTimeMillis();
		// Do not allow duplicate info strings unless there's a 50ms delay
		if((now - lastInfoRecieved < 50)
		&& text.equals(lastInfo)) {
			lastInfoRecieved = now;
			return;
		}
		
		lastInfo = text;
		lastInfoRecieved = now;
		mainTextArea.recieveInfo(text);
	}

	public void recieveError(Connection source, String text) {
		mainTextArea.recieveError(text);
	}

	public void recieveDebug(Connection source, String text) {
		mainTextArea.recieveDebug(text);
	}

	public void whisperRecieved(Connection source, BNetUser user, String text) {
		lastWhisperFrom = user;
		mainTextArea.whisperRecieved(user, text);
		notifySystemTray(
				Growl.CHANNEL_WHISPER_RECIEVED,
				source.getChannel(),
				"<From: " + user.getShortPrettyName() + "> " + text);
	}

	public void whisperSent(Connection source, BNetUser user, String text) {
		mainTextArea.whisperSent(user, text);
		notifySystemTray(
				Growl.CHANNEL_WHISPER_SENT,
				source.getChannel(),
				"<To: " + user.getShortPrettyName() + "> " + text);
	}

	public void bnetConnected(Connection source) {
		userList.clear();
		channelTextPane.setText(null);
		notifySystemTray(
				Growl.BNET_CONNECT,
				"Connected",
				source.toString());
	}

	public void bnetDisconnected(Connection source) {
		userList.clear();
		channelTextPane.setText(null);
		mainTextArea.recieveError("Disconnected from battle.net.");
		mainTextArea.addSeparator();
		notifySystemTray(
				Growl.BNET_DISCONNECT,
				"Disconnected",
				source.toString());
	}

	public void titleChanged(Connection source) {
		// Set the menu text to profile name or logon name
		if(source.getMyUser() == null)
			menuBar.setText(source.getProfile().getName());
		else
			menuBar.setText(source.getMyUser().getFullLogonName());
		
		// Update the settings menu items
		for (Entry<Connection, JMenuItem> item : settingsMenuItems.entrySet()) {
			Connection key = item.getKey();
			JMenuItem value = item.getValue();
			String description = (key.getMyUser() == null)
					? key.getProfile().getName() 
					: key.getMyUser().getFullLogonName();
			value.setText("Settings (" + description + ")");
		}
		
		// Update the desktop window
		GuiDesktop.setTitle(this, source.getProductID());
		
		// Update the tray icon
		TrayIcon tray = GuiDesktop.getTray();
		if(tray != null)
			tray.setToolTip(source.toString());
	}

	public void friendsList(Connection source, FriendEntry[] entries) {
		friendList.showFriends(entries);
	}
	
	public void friendsUpdate(Connection source, FriendEntry friend) {
		friendList.update(friend);
	}
	
	public void friendsAdd(Connection source, FriendEntry friend) {
		friendList.add(friend);
	}
	
	public void friendsPosition(Connection source, byte oldPosition, byte newPosition) {
		friendList.position(oldPosition, newPosition);
	}
	
	public void friendsRemove(Connection source, byte entry) {
		friendList.remove(entry);
	}

	public void clanMOTD(Connection source, Object cookie, String text) {
		if(cookie instanceof ClanMOTDEditor) {
			ClanMOTDEditor motd = (ClanMOTDEditor)cookie;
			motd.setMOTD(text);
			motd.setVisible(true);
		}
	}

	public void clanMemberList(Connection source, ClanMember[] members) {
		clanList.showMembers(members);
	}
	
	public void clanMemberRemoved(Connection source, String username) {
		clanList.remove(username);
	}
	
	public void clanMemberStatusChange(Connection source, ClanMember member) {
		clanList.statusChange(member);
	}
	
	public void clanMemberRankChange(Connection source, byte oldRank, byte newRank, String user) {
		clanList.rankChange(oldRank, newRank, user);
	}
	
	public void queryRealms2(final Connection source, String[] realms) {
		if(realms.length == 0)
			return;
		
		final RealmWindow realmWindow = new RealmWindow(realms);
		final boolean showWindow = (realms.length > 1);
		final String autoRealm = realms[0];
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				source.addEventHandler(realmWindow);
				if(showWindow)
					realmWindow.setVisible(true);
				else
					try {
						source.sendLogonRealmEx(autoRealm);
					} catch (Exception e) {
						Out.exception(e);
					}
			} });
	}

	public void logonRealmEx(Connection source, int[] MCPChunk1, int ip, int port, int[] MCPChunk2, String uniqueName) {}

	public void setChatText(String chatText) {
		chatTextArea.setText(chatText);
		if(chatText != null)
			chatTextArea.setSelectionStart(chatText.length());
		focusChat();
	}

	public void focusChat() {
		chatTextArea.requestFocus();
	}

	public boolean parseCommand(Connection source, BNetUser user, String command, String param, boolean whisperBack) {
		//mainTextArea.recieveInfo(String.format("parseCommand(\"%1$s\", \"%2$s\", \"%3$s\")", user.getShortLogonName(), command, param));
		return false;
	}

	public String toString() {
		if(firstConnection == null)
			return null;
		return firstConnection.toString();
	}
	
	public void setDividerLocation(int arg0) {
		jsp.setDividerLocation(arg0);
	}
	
	public int getDividerLocation() {
		return jsp.getDividerLocation();
	}

	public Connection getFirstConnection() {
		return firstConnection;
	}
}
