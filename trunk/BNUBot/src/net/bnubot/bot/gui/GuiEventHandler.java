/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.bot.gui;

import java.awt.AWTKeyStroke;
import java.awt.Color;
import java.awt.KeyboardFocusManager;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collections;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import net.bnubot.bot.gui.ColorScheme.ColorScheme;
import net.bnubot.bot.gui.components.ClanList;
import net.bnubot.bot.gui.components.FriendList;
import net.bnubot.bot.gui.components.TextWindow;
import net.bnubot.bot.gui.components.UserList;
import net.bnubot.core.Connection;
import net.bnubot.core.EventHandler;
import net.bnubot.core.clan.ClanMember;
import net.bnubot.core.friend.FriendEntry;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetUser;
import net.bnubot.util.Out;

public class GuiEventHandler implements EventHandler {
	private JPanel frame = null;
	private Connection con = null;
	private TextWindow mainTextArea = null;
	private JTextField chatTextArea = null;
	private JTextField channelTextPane = null;
	private UserList userList = null;
	private FriendList friendList = null;
	private ClanList clanList = null;
	private String channel = null;
	private JMenu menuBar = new JMenu();
	private BNetUser lastWhisperFrom = null;
	private JSplitPane jsp = null;
	
	private static final Set<? extends AWTKeyStroke> EMPTY_SET = Collections.emptySet();
	
	public GuiEventHandler() {
		initializeGui(ColorScheme.createColorScheme(GlobalSettings.colorScheme));
	}
	
	public void initialize(Connection con) {
		this.con = con;
		Out.setThreadOutputConnection(this);
		titleChanged();
	}
	
	private void initializeGui(ColorScheme colors) {
		// Create the panel
		frame = new JPanel(new BotLayoutManager(), true);
		
		// Create the menu bar.
		menuBar.setOpaque(true);
		{
			JMenu menu;
			JMenuItem menuItem;
			
			menuItem = new JMenuItem("Settings");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					new ConfigurationFrame(con.getConnectionSettings()).setVisible(true);
				} });
			menuBar.add(menuItem);
			
			menuBar.addSeparator();

			menu = new JMenu("Battle.net");
			{	
				menuItem = new JMenuItem("Connect");
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(!con.isConnected())
							con.setConnected(true);
					} });
				menu.add(menuItem);
				
				menuItem = new JMenuItem("Reconnect");
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(con.isConnected())
							con.reconnect();
					} });
				menu.add(menuItem);
				
				menuItem = new JMenuItem("Disconnect");
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(con.isConnected())
							con.setConnected(false);
					} });
				menu.add(menuItem);
			}
			menuBar.add(menu);
			
			menuItem = new JMenuItem("Realms");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					try {
						con.sendQueryRealms2();
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
							con.sendClanMOTD(new ClanMOTDEditor(con));
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
		chatTextArea = new JTextField();
		// Enable tab character
		chatTextArea.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, EMPTY_SET);
		chatTextArea.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, EMPTY_SET);
		chatTextArea.setBorder(null);
		chatTextArea.setBackground(colors.getBackgroundColor());
		chatTextArea.setForeground(Color.LIGHT_GRAY);
		chatTextArea.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {
				if(e.getKeyChar() == '\n') {
					try {
						String text[] = chatTextArea.getText().split("\n");
						for(String element : text) {
							if(element.trim().length() > 0)
								con.queueChatHelper(element);
						}
						chatTextArea.setText(null);
						return;
					} catch(Exception ex) {
						Out.exception(ex);
					}
				}
				
				if(e.getKeyChar() == '\t') {
					e.consume();
					try {
						int end = chatTextArea.getCaretPosition();
						String text = chatTextArea.getText(0, end);
						int start = text.lastIndexOf(' ') + 1;
						if(start != 0)
							text = text.substring(start);
						
						String before = chatTextArea.getText(0, start);
						String after = chatTextArea.getText(end, chatTextArea.getText().length() - end);
						
						String[] users = userList.findUsers(text);
						if(users.length == 1) {
							chatTextArea.setText(before + users[0] + after);
							chatTextArea.setCaretPosition(before.length() + users[0].length());
						} else {
							// TODO: Add assumed characters
							
							chatTextArea.setText(before + text + after);
							chatTextArea.setCaretPosition(before.length() + text.length());
							
							for(String user : users)
								recieveDebug(user);
						}
					} catch(Exception ex) {
						Out.exception(ex);
					}
				}
				
				if(lastWhisperFrom != null) {
					String txt = chatTextArea.getText();
					if("/r ".equals(txt) || "/rw ".equals(txt))
						chatTextArea.setText("/w " + lastWhisperFrom.getShortLogonName() + " ");
				}
			}
		});
		// Channel text box (above userlist)
		channelTextPane = new JTextField();
		channelTextPane.setHorizontalAlignment(JTextField.CENTER);
		channelTextPane.setEditable(false);
		channelTextPane.setBackground(colors.getBackgroundColor());
		channelTextPane.setForeground(Color.LIGHT_GRAY);
		
		// The userlist
		userList = new UserList(colors, this);
		// Friends list
		friendList = new FriendList(colors);
		// Clan list
		clanList = new ClanList(colors);

		JTabbedPane allLists = new JTabbedPane();
		allLists.addTab("Channel", new JScrollPane(userList));
		allLists.addTab("Friends", new JScrollPane(friendList));
		allLists.addTab("Clan", new JScrollPane(clanList));
		allLists.setTabPlacement(JTabbedPane.BOTTOM);
		
		JPanel leftSide = new JPanel();
		leftSide.add(mainTextArea);
		leftSide.add(chatTextArea);
		
		JPanel rightSide = new JPanel();
		rightSide.add(channelTextPane);
		rightSide.add(allLists);
		
		final Runnable redraw = new Runnable() {
			public void run() {
				// Tell the LayoutManager to reposition the components in the frame
				frame.validate();
			}
		};
		
		// This listener can detect when the JSplitPane divider is moved
		rightSide.addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent event) {}
			public void ancestorMoved(AncestorEvent event) {
				// Delay so the containers can resize
				SwingUtilities.invokeLater(redraw);
			}
			public void ancestorRemoved(AncestorEvent event) {}
		});
		
		jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, leftSide, rightSide);
		jsp.setDividerSize(8);
		jsp.setResizeWeight(1); // Make the left side expand when resizing
		
		// Add them to the frame
		frame.add(jsp);
		
		// Display the window
		GuiDesktop.add(this);
	}

	public JPanel getFrame() {
		return frame;
	}
	
	public JMenu getMenuBar() {
		return menuBar;
	}
	
	public void channelJoin(BNetUser user) {
		userList.showUser(user);
		if(GlobalSettings.displayJoinParts)
			mainTextArea.channelInfo(user + " has joined the channel" + user.getStatString().toString() + ".");
		channelTextPane.setText(channel + " (" + userList.count() + ")");
	}

	public void channelLeave(BNetUser user) {
		userList.removeUser(user);
		if(GlobalSettings.displayJoinParts)
			mainTextArea.channelInfo(user + " has left the channel.");
		channelTextPane.setText(channel + " (" + userList.count() + ")");
	}

	public void channelUser(BNetUser user) {
		if(GlobalSettings.displayChannelUsers)
			mainTextArea.channelInfo(user + user.getStatString().toString() + ".");
		userList.showUser(user);
		channelTextPane.setText(channel + " (" + userList.count() + ")");
	}

	public void joinedChannel(String channel) {
		this.channel = channel;
		userList.clear();
		mainTextArea.addSeparator();
		mainTextArea.channelInfo("Joining channel " + channel + ".");
		channelTextPane.setText(channel);
		
		GuiDesktop.setTitle(this, con.getProductID());
	}

	public void recieveChat(BNetUser user, String text) {
		mainTextArea.userChat(user, text, user.equals(con.getMyUser()));

		if(GlobalSettings.enableTrayPopups) {
			TrayIcon tray = GuiDesktop.getTray();
			if((tray != null) && !frame.isVisible()) {
		        tray.displayMessage("User is chatting: " + user.getShortLogonName(), 
		            text,
		            TrayIcon.MessageType.INFO);
			}
		}
	}

	public void recieveEmote(BNetUser user, String text) {
		mainTextArea.userEmote(user, text);
	}

	private static long lastInfoRecieved = 0;
	private static String lastInfo = null;
	public void recieveInfo(String text) {
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

	public void recieveError(String text) {
		mainTextArea.recieveError(text);
	}

	public void recieveDebug(String text) {
		mainTextArea.recieveDebug(text);
	}

	public void whisperRecieved(BNetUser user, String text) {
		lastWhisperFrom = user;
		mainTextArea.whisperRecieved(user, text);

		if(GlobalSettings.enableTrayPopups) {
			TrayIcon tray = GuiDesktop.getTray();
			if((tray != null) && !frame.isVisible()) {
		        tray.displayMessage("Whisper from " + user.getShortLogonName(), 
		            text,
		            TrayIcon.MessageType.INFO);
			}
		}
	}

	public void whisperSent(BNetUser user, String text) {
		mainTextArea.whisperSent(user, text);
	}

	public void bnetConnected() {
		userList.clear();
		channelTextPane.setText(null);
	}

	public void bnetDisconnected() {
		userList.clear();
		channelTextPane.setText(null);
		mainTextArea.recieveError("Disconnected from battle.net.");
		mainTextArea.addSeparator();
	}

	public void titleChanged() {
		// Set the menu text to profile name or logon name
		if(con.getMyUser() == null)
			menuBar.setText(con.getProfile().getName());
		else
			menuBar.setText(con.getMyUser().getFullLogonName());
		
		// Update the desktop window
		GuiDesktop.setTitle(this, con.getProductID());
		
		// Update the tray icon
		TrayIcon tray = GuiDesktop.getTray();
		if(tray != null)
			tray.setToolTip(con.toString());
	}

	public void friendsList(FriendEntry[] entries) {
		friendList.showFriends(entries);
	}
	
	public void friendsUpdate(FriendEntry friend) {
		friendList.update(friend);
	}
	
	public void friendsAdd(FriendEntry friend) {
		friendList.add(friend);
	}
	
	public void friendsPosition(byte oldPosition, byte newPosition) {
		friendList.position(oldPosition, newPosition);
	}
	
	public void friendsRemove(byte entry) {
		friendList.remove(entry);
	}

	public void clanMOTD(Object cookie, String text) {
		if(cookie instanceof ClanMOTDEditor) {
			ClanMOTDEditor motd = (ClanMOTDEditor)cookie;
			motd.setMOTD(text);
			motd.setVisible(true);
		}
	}

	public void clanMemberList(ClanMember[] members) {
		clanList.showMembers(members);
	}
	
	public void clanMemberRemoved(String username) {
		clanList.remove(username);
	}
	
	public void clanMemberStatusChange(ClanMember member) {
		clanList.statusChange(member);
	}
	
	public void clanMemberRankChange(byte oldRank, byte newRank, String user) {
		clanList.rankChange(oldRank, newRank, user);
	}
	
	public void queryRealms2(String[] realms) {
		if(realms.length == 0)
			return;
		
		final RealmWindow realmWindow = new RealmWindow(realms);
		final boolean showWindow = (realms.length > 1);
		final String autoRealm = realms[0];
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				con.addEventHandler(realmWindow);
				if(showWindow)
					realmWindow.setVisible(true);
				else
					try {
						con.sendLogonRealmEx(autoRealm);
					} catch (Exception e) {
						Out.exception(e);
					}
			} });
	}

	public void logonRealmEx(int[] MCPChunk1, int ip, int port, int[] MCPChunk2, String uniqueName) {}

	public void setChatText(String chatText) {
		chatTextArea.setText(chatText);
		if(chatText != null)
			chatTextArea.setSelectionStart(chatText.length());
		chatTextArea.requestFocus();
	}

	public void parseCommand(BNetUser user, String command, String param, boolean whisperBack) {
		//mainTextArea.recieveInfo(String.format("parseCommand(\"%1$s\", \"%2$s\", \"%3$s\")", user.getShortLogonName(), command, param));
	}

	public String toString() {
		if(con == null)
			return null;
		return con.toString();
	}
	
	public void setDividerLocation(int arg0) {
		jsp.setDividerLocation(arg0);
	}
	
	public int getDividerLocation() {
		return jsp.getDividerLocation();
	}

	public Connection getConnection() {
		return this.con;
	}
}
