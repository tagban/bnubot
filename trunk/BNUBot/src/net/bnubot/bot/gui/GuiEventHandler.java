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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu.Separator;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import net.bnubot.bot.gui.components.BotNetList;
import net.bnubot.bot.gui.components.ClanList;
import net.bnubot.bot.gui.components.ColoredTextField;
import net.bnubot.bot.gui.components.FriendList;
import net.bnubot.bot.gui.components.HistoryTextField;
import net.bnubot.bot.gui.components.TextWindow;
import net.bnubot.bot.gui.components.UserList;
import net.bnubot.bot.gui.notifications.Growl;
import net.bnubot.bot.gui.settings.ConfigurationFrame;
import net.bnubot.bot.gui.settings.OperationCancelledException;
import net.bnubot.core.Connection;
import net.bnubot.core.Connection.ConnectionState;
import net.bnubot.core.EventHandler;
import net.bnubot.core.Profile;
import net.bnubot.core.bncs.BNCSConnection;
import net.bnubot.core.botnet.BotNetConnection;
import net.bnubot.core.botnet.BotNetUser;
import net.bnubot.core.clan.ClanMember;
import net.bnubot.core.friend.FriendEntry;
import net.bnubot.logging.Out;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.settings.Settings;
import net.bnubot.util.BNetUser;
import net.bnubot.util.crypto.HexDump;

/**
 * @author scotta
 */
public class GuiEventHandler extends EventHandler {
	private JPanel frame = null;
	private TextWindow mainTextArea = null;
	private JTextField chatTextArea = null;
	private JTextField channelTextPane = null;
	private UserList userList = null;
	private FriendList friendList = null;
	private ClanList clanList = null;
	private BotNetList botNetList = null;
	private String channel = null;
	private final JMenu menuBar = new JMenu();
	private BNetUser lastWhisperFrom = null;
	private BNetUser lastWhisperTo = null;
	private JSplitPane jsp = null;
	private boolean tabComplete = false;
	private JDialog tcPopupWindow;
	private final JList<String> tcList = new JList<String>();
	private boolean tcUserSearch = true;
	private String tcBefore = null;
	private String tcSearch = null;
	private String tcAfter = null;
	private static final int textHeight = 23;
	private static final int paddingHeight = 4;

	private int clanTag = 0;

	private static final Set<? extends AWTKeyStroke> EMPTY_SET = Collections.emptySet();

	public GuiEventHandler(Profile profile) {
		super(profile);
		initializeGui();
	}

	private static Map<Connection, JMenuItem> settingsMenuItems = new HashMap<Connection, JMenuItem>();
	@Override
	public void initialize(final Connection source) {
		JMenuItem settings = new JMenuItem("Settings");
		settings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					new ConfigurationFrame(source.getConnectionSettings());
				} catch(OperationCancelledException e) {}
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

		String channel = source.getChannel();
		if(channel != null) {
			// Set up the channel state
			joinedChannel(source, channel);
			for(BNetUser user : source.getUsers())
				channelUser(source, user);
		}
	}

	@Override
	public void disable(final Connection source) {
		if(source == getFirstConnection())
			GuiDesktop.remove(this);
		JMenuItem mi = settingsMenuItems.remove(source);
		if(mi != null)
			menuBar.remove(mi);
		titleChanged(source);
	}

	/**
	 * Update the list of the TC popup
	 */
	private void tcUpdate() {
		Collection<String> options;
		if(tcUserSearch)
			options = getFirstConnection().findUsersForTabComplete(tcSearch);
		else
			options = Profile.findCommandsForTabComplete(tcSearch);
		if(options.size() == 1) {
			// Unconfirmed; simply ran out of other options
			tcSelect(options.toArray(new String[1])[0], false);
		} else if(options.size() == 0) {
			tcCancel();
		} else {
			tcList.setModel(new DefaultComboBoxModel<String>(options.toArray(new String[options.size()])));
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
	 * @param confirmed Whether the completed part should be highlighted
	 */
	private void tcSelect(String selection, boolean confirmed) {
		if((tcBefore.length() == 0) && (tcAfter.length() == 0)) {
			chatTextArea.setText(selection + ": ");
		} else {
			chatTextArea.setText(tcBefore + selection + tcAfter);
		}

		// Exit TC mode
		tcPopupWindow.setVisible(tabComplete = false);

		chatTextArea.requestFocus();
		final int start;
		final int end = chatTextArea.getText().length();
		if(confirmed)
			start = end;
		else
			start = tcBefore.length() + tcSearch.length();
		chatTextArea.select(start, end);
	}

	private void initializeGui() {
		// Create the panel
		frame = new JPanel(new BorderLayout());

		// When gained focus, select the chat box
		frame.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				chatTextArea.requestFocus();
			}
			@Override
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
					@Override
					public void actionPerformed(ActionEvent e) {
						for(Connection con : getFirstConnection().getProfile().getConnections())
							con.connect();
					} });
				menu.add(menuItem);

				menuItem = new JMenuItem("Reconnect");
				menuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						for(Connection con : getFirstConnection().getProfile().getConnections())
							con.reconnect();
					} });
				menu.add(menuItem);

				menuItem = new JMenuItem("Disconnect");
				menuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						for(Connection con : getFirstConnection().getProfile().getConnections())
							con.disconnect(ConnectionState.DO_NOT_ALLOW_CONNECT);
					} });
				menu.add(menuItem);
			}
			menuBar.add(menu);

			menuItem = new JMenuItem("Realms");
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					try {
						getFirstConnection().sendQueryRealms2();
					} catch (Exception e) {
						Out.exception(e);
					}
				} });
			menuBar.add(menuItem);

			menu = new JMenu("Clan");
			{
				menuItem = new JMenuItem("Create a clan");
				menuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent event) {
						try {
							String ct = JOptionPane.showInputDialog(frame, "Clan tag (maximum of 4 characters):", "Create a clan", JOptionPane.INFORMATION_MESSAGE);
							clanTag = HexDump.PrettyToDWord(ct);
							getFirstConnection().sendClanFindCandidates(null, clanTag);
						} catch(Exception e) {
							Out.exception(e);
						}
					} });
				menu.add(menuItem);

				menuItem = new JMenuItem("Edit MOTD");
				menuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent event) {
						try {
							getFirstConnection().sendClanMOTD(new ClanMOTDEditor(getFirstConnection()));
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
		mainTextArea = new TextWindow();
		// Send chat textbox
		chatTextArea = new HistoryTextField();
		chatTextArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, textHeight));
		// Enable tab character
		chatTextArea.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, EMPTY_SET);
		chatTextArea.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, EMPTY_SET);
		chatTextArea.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {}
			@Override
			public void keyTyped(KeyEvent e) {
				switch(e.getKeyChar()) {
				case '\n': {
					e.consume();
					try {
						for(String element : chatTextArea.getText().split("\n")) {
							if(element.trim().length() > 0)
								getFirstConnection().sendChatInternal(element);
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
					if(GlobalSettings.enableTabCompleteUser) {
						tabComplete = true;
						tcUserSearch = true;
					}
					break;
				}
				case ' ': {
					if(lastWhisperFrom != null) {
						String txt = chatTextArea.getText().trim();
						if("/r".equals(txt))
							chatTextArea.setText(lastWhisperFrom.getWhisperCommand().trim());
					}
					if(lastWhisperTo != null) {
						String txt = chatTextArea.getText().trim();
						if("/rw".equals(txt))
							chatTextArea.setText(lastWhisperTo.getWhisperCommand().trim());
					}
					if("/cmd".equals(chatTextArea.getText()) && GlobalSettings.enableTabCompleteCommand) {
						tabComplete = true;
						tcUserSearch = false;
					}
					break;
				}
				case '/': {
					if("/".equals(chatTextArea.getText()) && GlobalSettings.enableTabCompleteCommand) {
						tabComplete = true;
						tcUserSearch = false;
					}
					break;
				}
				default:
					break;
				}
			}
			@Override
			public void keyReleased(KeyEvent e) {
				if(!tabComplete)
					return;

				try {
					if(tcUserSearch) {
						int end = chatTextArea.getCaretPosition();
						tcSearch = chatTextArea.getText(0, end);
						int start = tcSearch.lastIndexOf(' ') + 1;
						if(start != 0)
							tcSearch = tcSearch.substring(start);

						tcBefore = chatTextArea.getText(0, start);
						tcAfter = chatTextArea.getText(end, chatTextArea.getText().length() - end);
					} else {
						tcSearch = "";
						tcBefore = chatTextArea.getText();
						tcAfter = "";
					}

					tcUpdate();
				} catch(Exception ex) {
					Out.exception(ex);
				}
			}
		});
		// Channel text box (above userlist)
		channelTextPane = new ColoredTextField();
		channelTextPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, textHeight));
		channelTextPane.setHorizontalAlignment(JTextField.CENTER);
		channelTextPane.setEditable(false);

		// The userlist
		userList = new UserList(this);
		// Friends list
		friendList = new FriendList();
		// Clan list
		clanList = new ClanList();
		// BotNet list
		if(getFirstConnection().getConnectionSettings().enableBotNet)
			botNetList = new BotNetList(this);

		JTabbedPane allLists = new JTabbedPane(JTabbedPane.BOTTOM);
		allLists.addTab("Channel", new JScrollPane(userList));
		allLists.addTab("Friends", new JScrollPane(friendList));
		allLists.addTab("Clan", new JScrollPane(clanList));
		if(botNetList != null)
			allLists.addTab("BotNet", new JScrollPane(botNetList));

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
			@Override
			public void componentMoved(ComponentEvent e) {
				// If the window is maximized, skip this
				if(GuiDesktop.getInstance().getExtendedState() == Frame.MAXIMIZED_BOTH)
					return;
				// Save the divider location
				Settings.getSection("GuiDesktop").write("dividerLocation", jsp.getDividerLocation());
				Settings.store();
			}
		});

		// Add them to the frame
		frame.add(jsp);

		// Initialize the TC popup window
		tcPopupWindow = new JDialog();
		tcPopupWindow.setUndecorated(true);
		tcPopupWindow.addWindowFocusListener(new WindowFocusListener() {
			@Override
			public void windowGainedFocus(WindowEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
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
			@Override
			public void windowLostFocus(WindowEvent e) {
				tcPopupWindow.setVisible(tabComplete = false);
			}
		});
		tcPopupWindow.getContentPane().setLayout(new BorderLayout());
		((JComponent)tcPopupWindow.getContentPane()).setBorder(BorderFactory.createEtchedBorder());
		tcPopupWindow.getContentPane().add(tcList);

		// Initialize TC list
		tcList.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {}
			@Override
			public void keyTyped(KeyEvent e) {}
			@Override
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
					// Confirmed; the user selected with enter or tab
					tcSelect(tcList.getSelectedValue(), true);
					break;
				case KeyEvent.VK_ESCAPE:
					tcCancel();
					break;
				default:
					// Pass the keystroke to the TC strings
					if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
						// If there's nothing left in the user string, cancel TC mode
						if(tcSearch.length() == 0) {
							tcCancel();
							break;
						}
						// Remove the last char from the string
						tcSearch = tcSearch.substring(0, tcSearch.length() - 1);
					} else {
						// Append the char to the string
						tcSearch += e.getKeyChar();
					}

					// Update the TextArea with the new strings
					chatTextArea.setText(tcBefore + tcSearch + tcAfter);
					chatTextArea.setCaretPosition(tcBefore.length() + tcSearch.length());

					// Redraw the TC list
					tcUpdate();
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

	private static long lastSystemTrayTime = 0;
	private void notifySystemTray(String gt, String headline, String text) {
		// Require that enableTrayPopups is set
		if(!GlobalSettings.trayIconMode.enableTray())
			return;

		// If popups are not always enabled, require that the window is defocused
		if(!GlobalSettings.trayIconMode.alwaysDisplayPopups())
			if(GuiDesktop.getInstance().isFocused())
				return;

		// Require 1 second between tray notifications
		long timeNow = System.currentTimeMillis();
		if(timeNow - lastSystemTrayTime < 1000)
			return;
		lastSystemTrayTime = timeNow;

		Growl growl = GuiDesktop.getGrowl();
		if(growl != null) {
			try {
				growl.notifyGrowlOf(gt, headline, text);
			} catch (Exception e) {
				Out.exception(e);
			}
		} else {
			TrayIcon tray = GuiDesktop.getTray();
			if(tray != null)
				tray.displayMessage(headline, text, TrayIcon.MessageType.INFO);
		}
	}

	@Override
	public void channelJoin(Connection source, BNetUser user) {
		userList.showUser(source, user);
		if(GlobalSettings.getDisplayJoinParts())
			mainTextArea.channelInfo(user.toStringEx() + " has joined the channel" + user.getStatString().toString() + ".");
		if(GlobalSettings.trayDisplayJoinPart)
			notifySystemTray(
					Growl.CHANNEL_USER_JOIN,
					source.getChannel(),
					user.toString() + " joined");
		channelTextPane.setText(channel + " (" + userList.count() + ")");
	}

	@Override
	public void channelLeave(Connection source, BNetUser user) {
		userList.removeUser(user);
		if(GlobalSettings.getDisplayJoinParts())
			mainTextArea.channelInfo(user.toStringEx() + " has left the channel.");
		if(GlobalSettings.trayDisplayJoinPart)
			notifySystemTray(
					Growl.CHANNEL_USER_PART,
					source.getChannel(),
					user.toString() + " left");
		channelTextPane.setText(channel + " (" + userList.count() + ")");
	}

	@Override
	public void channelUser(Connection source, BNetUser user) {
		if(GlobalSettings.displayChannelUsers)
			mainTextArea.channelInfo(user.toStringEx() + user.getStatString().toString() + ".");
		userList.showUser(source, user);
		channelTextPane.setText(channel + " (" + userList.count() + ")");
	}

	@Override
	public void joinedChannel(Connection source, String channel) {
		this.channel = channel;
		userList.clear();
		if(channel == null) {
			mainTextArea.channelInfo("Leaving chat");
		} else {
			mainTextArea.addSeparator();
			mainTextArea.channelInfo("Joining channel " + channel + ".");
		}
		channelTextPane.setText(channel);
		if(GlobalSettings.trayDisplayChannel)
			notifySystemTray(
					Growl.CHANNEL,
					"Channel",
					channel);

		GuiDesktop.setTitle(this, getFirstConnection().getProductID());
	}

	@Override
	public void recieveChat(Connection source, BNetUser user, String text) {
		mainTextArea.userChat(source.getDisplayType(), user, text, profile.isOneOfMyUsers(user));
		if(GlobalSettings.trayDisplayChatEmote)
			notifySystemTray(
					Growl.CHANNEL_USER_CHAT,
					source.getChannel(),
					"<" + user.toString() + "> " + text);
	}

	@Override
	public void recieveBroadcast(Connection source, String username, int flags, String text) {
		mainTextArea.broadcast(username, flags, text);
	}

	@Override
	public void recieveEmote(Connection source, BNetUser user, String text) {
		mainTextArea.userEmote(source.getDisplayType(), user, text);
		if(GlobalSettings.trayDisplayChatEmote)
			notifySystemTray(
					Growl.CHANNEL_USER_EMOTE,
					source.getChannel(),
					"<" + user.toString() + " " + text + ">");
	}

	@Override
	public void recieveInfo(Connection source, String text) {
		mainTextArea.recieveInfo(source.getDisplayType(), text);
	}

	private static long lastInfoRecieved = 0;
	private static String lastInfo = null;
	@Override
	public void recieveServerInfo(Connection source, String text) {
		long now = System.currentTimeMillis();
		// Do not allow duplicate info strings unless there's a 50ms delay
		if((now - lastInfoRecieved < 50)
		&& text.equals(lastInfo)) {
			lastInfoRecieved = now;
			return;
		}

		lastInfo = text;
		lastInfoRecieved = now;
		mainTextArea.recieveInfo(source.getServerType(), text);
	}

	@Override
	public void recieveError(Connection source, String text) {
		mainTextArea.recieveError(source.getDisplayType(), text);
	}

	@Override
	public void recieveServerError(Connection source, String text) {
		mainTextArea.recieveError(source.getServerType(), text);
	}

	@Override
	public void recieveDebug(Connection source, String text) {
		mainTextArea.recieveDebug(source.getDisplayType(), text);
	}

	@Override
	public void whisperRecieved(Connection source, BNetUser user, String text) {
		lastWhisperFrom = user;
		mainTextArea.whisperRecieved(source.getDisplayType(), user, text);
		if(GlobalSettings.trayDisplayWhisper)
			notifySystemTray(
					Growl.CHANNEL_WHISPER_RECIEVED,
					source.getChannel(),
					"<From: " + user.toString() + "> " + text);
	}

	@Override
	public void whisperSent(Connection source, BNetUser user, String text) {
		lastWhisperTo = user;
		mainTextArea.whisperSent(source.getDisplayType(), user, text);
		if(GlobalSettings.trayDisplayWhisper)
			notifySystemTray(
					Growl.CHANNEL_WHISPER_SENT,
					source.getChannel(),
					"<To: " + user.toString() + "> " + text);
	}

	@Override
	public void bnetConnected(Connection source) {
		userList.clear();
		channelTextPane.setText(null);
		if(GlobalSettings.trayDisplayConnectDisconnect)
			notifySystemTray(
					Growl.BNET_CONNECT,
					"Connected",
					source.toString());
	}

	@Override
	public void bnetDisconnected(Connection source) {
		userList.clear();
		channelTextPane.setText(null);
		recieveError(source, "Disconnected from battle.net.");
		mainTextArea.addSeparator();
		if(GlobalSettings.trayDisplayConnectDisconnect)
			notifySystemTray(
					Growl.BNET_DISCONNECT,
					"Disconnected",
					source.toString());
	}

	@Override
	public void titleChanged(Connection source) {
		BNetUser myUser = source.getMyUser();
		Profile profile = source.getProfile();

		// Set the menu text to profile name or logon name
		if((myUser == null) || (profile.getConnections().size() != 1))
			menuBar.setText(profile.getName());
		else
			menuBar.setText(myUser.getFullLogonName());

		// Update the settings menu items
		for (Entry<Connection, JMenuItem> item : settingsMenuItems.entrySet())
			item.getValue().setText("Settings (" + item.getKey().toShortString() + ")");

		// Update the desktop window
		GuiDesktop.setTitle(this, getFirstConnection().getProductID());

		// Update the tray icon
		TrayIcon tray = GuiDesktop.getTray();
		if(tray != null)
			tray.setToolTip(source.toString());
	}

	@Override
	public void friendsList(BNCSConnection source, FriendEntry[] entries) {
		friendList.showFriends(entries);
	}

	@Override
	public void friendsUpdate(BNCSConnection source, FriendEntry friend) {
		friendList.update(friend);
	}

	@Override
	public void friendsAdd(BNCSConnection source, FriendEntry friend) {
		friendList.add(friend);
	}

	@Override
	public void friendsPosition(BNCSConnection source, byte oldPosition, byte newPosition) {
		friendList.position(oldPosition, newPosition);
	}

	@Override
	public void friendsRemove(BNCSConnection source, byte entry) {
		friendList.remove(entry);
	}

	@Override
	public void clanMOTD(BNCSConnection source, Object cookie, String text) {
		if(cookie instanceof ClanMOTDEditor) {
			ClanMOTDEditor motd = (ClanMOTDEditor)cookie;
			motd.setMOTD(text);
			motd.setVisible(true);
		}
	}

	@Override
	public void clanMemberList(BNCSConnection source, ClanMember[] members) {
		clanList.showMembers(members);
	}

	@Override
	public void clanMemberRemoved(BNCSConnection source, String username) {
		clanList.remove(username);
	}

	@Override
	public void clanMemberStatusChange(BNCSConnection source, ClanMember member) {
		clanList.statusChange(member);
	}

	@Override
	public void clanMemberRankChange(BNCSConnection source, byte oldRank, byte newRank, String user) {
		clanList.rankChange(oldRank, newRank, user);
	}

	@Override
	public void clanFindCandidates(BNCSConnection source, Object cookie, List<String> candidates) {
		if(clanTag == 0)
			return;
		String clanName = JOptionPane.showInputDialog(frame, "Clan name?", "Create a clan", JOptionPane.INFORMATION_MESSAGE);

		// Just go ahead and invite everyone
		try {
			source.sendClanInviteMultiple(null, clanName, clanTag, candidates);
		} catch(Exception e) {
			Out.exception(e);
		}
	}

	@Override
	public void queryRealms2(final BNCSConnection source, String[] realms) {
		if(realms.length == 0)
			return;

		final RealmWindow realmWindow = new RealmWindow(source, realms, profile);
		final boolean showWindow = (realms.length > 1);
		final String autoRealm = realms[0];

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				source.addEventHandler(realmWindow);
				if(showWindow)
					realmWindow.jd.setVisible(true);
				else
					try {
						source.sendLogonRealmEx(autoRealm);
					} catch (Exception e) {
						Out.exception(e);
					}
			} });
	}

	@Override
	public void botnetConnected(BotNetConnection source) {
		if(botNetList != null)
			botNetList.clear();
	}

	@Override
	public void botnetDisconnected(BotNetConnection source) {
		if(botNetList != null)
			botNetList.clear();
	}

	@Override
	public void botnetUserOnline(BotNetConnection source, BotNetUser user) {
		if(botNetList != null)
			botNetList.showUser(source, user);
	}

	@Override
	public void botnetUserStatus(BotNetConnection source, BotNetUser user) {
		if(botNetList != null)
			botNetList.showUser(source, user);
	}

	@Override
	public void botnetUserLogoff(BotNetConnection source, BotNetUser user) {
		if(botNetList != null)
			botNetList.removeUser(user);
	}

	public void setChatText(String chatText) {
		chatTextArea.setText(chatText);
		if(chatText != null)
			chatTextArea.setSelectionStart(chatText.length());
		focusChat();
	}

	public void focusChat() {
		chatTextArea.requestFocus();
	}

	/**
	 * Get the name for the tab on the GuiDesktop
	 */
	@Override
	public String toString() {
		if(getFirstConnection() == null)
			return null;
		Profile p = getFirstConnection().getProfile();
		if((p == null) || (p.getConnections().size() == 1))
			return getFirstConnection().toString();
		return p.getName();
	}

	public void setDividerLocation(int arg0) {
		jsp.setDividerLocation(arg0);
	}

	public int getDividerLocation() {
		return jsp.getDividerLocation();
	}

	protected Connection getFirstConnection() {
		return profile.getPrimaryConnection();
	}

	public void channelInfo(String text) {
		mainTextArea.channelInfo(text);
	}
}
