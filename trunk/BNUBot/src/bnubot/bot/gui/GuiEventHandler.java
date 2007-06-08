package bnubot.bot.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.Date;

import javax.swing.*;

import bnubot.bot.EventHandler;
import bnubot.bot.gui.ColorScheme.ColorScheme;
import bnubot.bot.gui.components.FriendList;
import bnubot.bot.gui.components.TextWindow;
import bnubot.bot.gui.components.UserList;
import bnubot.bot.gui.icons.IconsDotBniReader;
import bnubot.core.BNetUser;
import bnubot.core.Connection;
import bnubot.core.StatString;
import bnubot.core.clan.ClanMember;
import bnubot.core.friend.FriendEntry;

public class GuiEventHandler implements EventHandler {
	private JFrame frame = null;
	private Connection c = null;
	private TextWindow mainTextArea = null;
	private JTextArea chatTextArea = null;
	private JTextArea channelTextArea = null;
	private UserList userList = null;
	private FriendList friendList = null;
	
	public void initialize(Connection c) {
		ColorScheme cs = ColorScheme.createColorScheme(c.getConnectionSettings().colorScheme);
		
		if(c != null) {
			this.c = c;
			initializeGui(c.toString(), cs);
		} else {
			initializeGui("BNU`Bot", cs);
		}
	}

	private void initializeGui(String title, ColorScheme cs) {
		//Create and set up the window
		frame = new JFrame(title);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Create the menu bar.
		JMenuBar menuBar = new JMenuBar();
		menuBar.setOpaque(true);
		menuBar.setPreferredSize(new Dimension(200, 20));
		{
			JMenu menu;
			JMenuItem menuItem;

			menu = new JMenu("File");
			{	
				menuItem = new JMenuItem("Connect");
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(!c.isConnected())
							c.setConnected(true);
					} });
				menu.add(menuItem);
				
				menuItem = new JMenuItem("Disconnect");
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(c.isConnected())
							c.setConnected(false);
					} });
				menu.add(menuItem);
				
				menu.addSeparator();
				
				menuItem = new JMenuItem("Settings");
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						new ConfigurationFrame(c.getConnectionSettings()).setVisible(true);
					} });
				menu.add(menuItem);
				
				menu.addSeparator();
				
				menuItem = new JMenuItem("Exit");
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						System.exit(0);
					} });
				menu.add(menuItem);
			}
			menuBar.add(menu);	
			
			menu = new JMenu("Edit");
			{
				menuItem = new JMenuItem("Cut");
				menu.add(menuItem);
				
				menuItem = new JMenuItem("Copy");
				menu.add(menuItem);
				
				menuItem = new JMenuItem("Paste");
				menu.add(menuItem);
				
				menuItem = new JMenuItem("Select All");
				menu.add(menuItem);
				
			}
			menuBar.add(menu);
			
			menu = new JMenu("Friends");
			{
				menuItem = new JMenuItem("...");
				menu.add(menuItem);
			}
			menuBar.add(menu);
			
			menu = new JMenu("Clan");
			{
				menuItem = new JMenuItem("Edit MOTD");
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						try {
							c.sendClanMOTD(new ClanMOTDEditor(c));
						} catch(Exception e) {
							e.printStackTrace();
							System.exit(1);
						}
					} });
				menu.add(menuItem);
			}
			menuBar.add(menu);
			
			menu = new JMenu("Help");
			{
				menuItem = new JMenuItem("Complain about scrollbars");
				menu.add(menuItem);
			}
			menuBar.add(menu);
		}
		frame.setJMenuBar(menuBar);
		
		//Create a LayoutManager to organize the frame
		frame.setLayout(new BotLayoutManager());
		
		//Main text area
		mainTextArea = new TextWindow(cs);
		//Send chat textbox
		chatTextArea = new JTextArea();
		chatTextArea.setBackground(cs.getBackgroundColor());
		chatTextArea.setForeground(Color.LIGHT_GRAY);
		chatTextArea.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {
				if(e.getKeyChar() == '\n') {
					String text[] = chatTextArea.getText().split("\n");
					for(int i = 0; i < text.length; i++) {
						if(text[i].trim().length() > 0)
							c.sendChat(text[i]);
					}
					chatTextArea.setText(null);
				}
			}
		});
		//Channel text box (above userlist)
		channelTextArea = new JTextArea();
		channelTextArea.setAlignmentX(SwingConstants.CENTER);
		channelTextArea.setAlignmentY(SwingConstants.CENTER);
		channelTextArea.setBackground(cs.getBackgroundColor());
		channelTextArea.setForeground(Color.LIGHT_GRAY);
		
		IconsDotBniReader.initialize(c.getConnectionSettings());
		
		//The userlist
		userList = new UserList(cs, c);
		//Friends list
		friendList = new FriendList(cs);
		
		
		JTabbedPane allLists = new JTabbedPane();
		allLists.addTab("Channel", userList);
		allLists.addTab("Friends", friendList);
		allLists.addTab("Clan", null);
		
		//Add them to the frame
		frame.add(mainTextArea);
		frame.add(chatTextArea);
		frame.add(channelTextArea);
		frame.add(allLists);
		/*JSplitPane leftPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, mainTextScroll, chatTextArea);
		leftPane.setResizeWeight(1);
		JSplitPane rightPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, channelTextArea, userList);
		JSplitPane mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPane, rightPane);
		mainPane.setResizeWeight(1);
		frame.add(mainPane);*/
		
		//Display the window
		frame.pack();
		frame.setVisible(true);
	}

	public void channelJoin(BNetUser user, int flags, int ping, StatString statstr) {
		userList.showUser(user.toString(), flags, ping, statstr);
		mainTextArea.channelInfo(user + " has joined " + statstr.toString());
	}

	public void channelLeave(BNetUser user, int flags, int ping, StatString statstr) {
		userList.removeUser(user.toString());
		mainTextArea.channelInfo(user + " has left " + statstr.toString());
	}

	public void channelUser(BNetUser user, int flags, int ping, StatString statstr) {
		mainTextArea.channelInfo(user + " " + statstr.toString());
		userList.showUser(user.toString(), flags, ping, statstr);
	}

	public void joinedChannel(String channel) {
		userList.clear();
		mainTextArea.channelInfo("Joining channel " + channel + ".");
		channelTextArea.setText(channel);
		frame.setTitle(c.toString());
	}

	public void recieveChat(BNetUser user, int flags, int ping, String text) {
		mainTextArea.userChat(user, flags, text);
	}

	public void recieveEmote(BNetUser user, int flags, int ping, String text) {
		mainTextArea.userEmote(user, flags, text);
	}

	private static long lastInfoRecieved = 0;
	private static String lastInfo = null;
	public void recieveInfo(String text) {
		long now = new Date().getTime();
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

	public void whisperRecieved(BNetUser user, int flags, int ping, String text) {
		mainTextArea.whisperRecieved(user, flags, text);
	}

	public void whisperSent(BNetUser user, int flags, int ping, String text) {
		mainTextArea.whisperSent(user, flags, text);
	}

	public void bnetConnected() {
		userList.clear();
		channelTextArea.setText(null);
	}

	public void bnetDisconnected() {
		userList.clear();
		channelTextArea.setText(null);
	}

	public void friendsList(FriendEntry[] entries) {
		friendList.showFriends(entries);
	}
	
	public void friendsUpdate(byte entry, byte location, byte status, int product, String locationName) {}
	public void clanMemberRankChange(byte oldRank, byte newRank, String user) {}
	public void clanMemberList(ClanMember[] members) {}

	public void clanMOTD(Object cookie, String text) {
		if(cookie instanceof ClanMOTDEditor) {
			ClanMOTDEditor motd = (ClanMOTDEditor)cookie;
			motd.setMOTD(text);
			motd.setVisible(true);
		}
	}
}
