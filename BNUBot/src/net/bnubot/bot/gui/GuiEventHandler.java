/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.bot.gui;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import net.bnubot.bot.database.Database;
import net.bnubot.bot.gui.ColorScheme.ColorScheme;
import net.bnubot.bot.gui.components.ClanList;
import net.bnubot.bot.gui.components.FriendList;
import net.bnubot.bot.gui.components.TextWindow;
import net.bnubot.bot.gui.components.UserList;
import net.bnubot.bot.gui.database.DatabaseAccountEditor;
import net.bnubot.bot.gui.database.DatabaseRankEditor;
import net.bnubot.bot.gui.icons.IconsDotBniReader;
import net.bnubot.core.Connection;
import net.bnubot.core.ConnectionSettings;
import net.bnubot.core.EventHandler;
import net.bnubot.core.clan.ClanMember;
import net.bnubot.core.friend.FriendEntry;
import net.bnubot.util.BNetUser;
import net.bnubot.util.Out;
import net.bnubot.util.StatString;
import net.bnubot.vercheck.CurrentVersion;
import net.bnubot.vercheck.VersionCheck;

public class GuiEventHandler implements EventHandler {
	private JFrame frame = null;
	private Connection c = null;
	private TextWindow mainTextArea = null;
	private JTextArea chatTextArea = null;
	private JTextArea channelTextArea = null;
	private UserList userList = null;
	private FriendList friendList = null;
	private ClanList clanList = null;
	private RealmWindow w = null;
	private String channel = null;
	private TrayIcon ti = null;
	
	public void initialize(Connection c) {
		ColorScheme cs = ColorScheme.createColorScheme(ConnectionSettings.colorScheme);
		
		if(c != null) {
			this.c = c;
			initializeGui(c.toString(), cs);
		} else {
			initializeGui("BNU`Bot", cs);
		}
		
		initializeSystemTray();
	}

	private void initializeSystemTray() {
		try {
			if(!SystemTray.isSupported())
				throw new NoClassDefFoundError();
		} catch(NoClassDefFoundError e) {
			Out.info(getClass(), "System tray is not supported");
			return;
		}
		
		Image image = Toolkit.getDefaultToolkit().getImage("tray.gif");
			
		PopupMenu pm = new PopupMenu("title");
		{
			MenuItem mi = new MenuItem("BNU-Bot " + CurrentVersion.version().toString());
			mi.setEnabled(false);
			pm.add(mi);
			
			pm.addSeparator();
			
			mi = new MenuItem("Hide/show");
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frame.setVisible(!frame.isVisible());
					if(frame.isVisible())
						frame.toFront();
				}
			});
			pm.add(mi);
			
			mi = new MenuItem("Exit");
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.exit(0);
				}
			});
			pm.add(mi);
		}
		
		ti = new TrayIcon(image, c.toString(), pm);
		ti.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == 1) {
					frame.setVisible(!frame.isVisible());
					if(frame.isVisible())
						frame.toFront();
				}
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
		});
		ti.setImageAutoSize(true);
		
		try {
			SystemTray.getSystemTray().add(ti);
		} catch(AWTException e) {
			Out.exception(e);
		}
		
		frame.addWindowStateListener(new WindowStateListener() {
			public void windowStateChanged(WindowEvent e) {
				if((e.getNewState() & java.awt.Frame.ICONIFIED) != 0) {
					frame.setVisible(false);
					frame.setState(e.getNewState() & ~java.awt.Frame.ICONIFIED);
				}
			}
		});
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
				
				menuItem = new JMenuItem("Reconnect");
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(c.isConnected())
							c.reconnect();
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
				
				menuItem = new JMenuItem("Realms");
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						try {
							c.sendQueryRealms();
						} catch (Exception e) {
							Out.exception(e);
						}
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
				JMenu subMenu = new JMenu("Clan");
				{
					menuItem = new JMenuItem("Edit MOTD");
					menuItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent event) {
							try {
								c.sendClanMOTD(new ClanMOTDEditor(c));
							} catch(Exception e) {
								Out.exception(e);
							}
						} });
					subMenu.add(menuItem);
				}
				menu.add(subMenu);
				
				subMenu = new JMenu("Database");
				{
					menuItem = new JMenuItem("Rank editor");
					menuItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent event) {
							Database d = Database.getInstance();
							if(d != null)
								new DatabaseRankEditor(d);
							else
								c.recieveError("There is no database initialized.");
						} });
					subMenu.add(menuItem);
					
					menuItem = new JMenuItem("Account editor");
					menuItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent event) {
							Database d = Database.getInstance();
							if(d != null)
								new DatabaseAccountEditor(d);
							else
								c.recieveError("There is no database initialized.");
						} });
					subMenu.add(menuItem);
				}
				menu.add(subMenu);
			
				subMenu = new JMenu("Debug");
				{
					menuItem = new JMenuItem("Show Icons");
					menuItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent event) {
							IconsDotBniReader.showWindow();
						} });
					subMenu.add(menuItem);
					
					menuItem = new JMenuItem((Out.isDebug() ? "Dis" : "En") + "able debug logging");
					menuItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent event) {
							Out.setDebug(!Out.isDebug());
							
							JMenuItem jmi = (JMenuItem)event.getSource();
							jmi.setText((Out.isDebug() ? "Dis" : "En") + "able debug logging");
						} });
					subMenu.add(menuItem);
				}
				menu.add(subMenu);
			}
			menuBar.add(menu);
			
			menu = new JMenu("Help");
			{
				menuItem = new JMenuItem("Complain about scrollbars");
				menu.add(menuItem);
				
				menu.addSeparator();
				
				menuItem = new JMenuItem("Check for updates");
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						try {
							VersionCheck.checkVersion();
						} catch (Exception e) {
							Out.exception(e);
						}
					} });
				menu.add(menuItem);
				
				menuItem = new JMenuItem("About");
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						new AboutWindow();
					} });
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
					for(String element : text) {
						if(element.trim().length() > 0)
							c.sendChat(element);
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
		userList = new UserList(cs, c, this);
		//Friends list
		friendList = new FriendList(cs);
		//Clan list
		clanList = new ClanList(cs);

		JTabbedPane allLists = new JTabbedPane();
		allLists.addTab("Channel", new JScrollPane(userList));
		allLists.addTab("Friends", new JScrollPane(friendList));
		allLists.addTab("Clan", new JScrollPane(clanList));
		
		//Add them to the frame
		frame.add(mainTextArea);
		frame.add(chatTextArea);
		frame.add(channelTextArea);
		frame.add(allLists);
		/*JSplitPane leftPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, mainTextArea, chatTextArea);
		leftPane.setResizeWeight(1);
		JSplitPane rightPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, channelTextArea, allLists);
		JSplitPane mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPane, rightPane);
		mainPane.setResizeWeight(1);
		frame.add(mainPane);*/
		
		//Display the window
		frame.pack();
		frame.setVisible(true);
	}

	public void channelJoin(BNetUser user, StatString statstr) {
		userList.showUser(user, statstr);
		mainTextArea.channelInfo(user + " has joined" + statstr.toString() + ".");
		channelTextArea.setText(channel + " (" + userList.count() + ")");
	}

	public void channelLeave(BNetUser user) {
		userList.removeUser(user);
		mainTextArea.channelInfo(user + " has left.");
		channelTextArea.setText(channel + " (" + userList.count() + ")");
	}

	public void channelUser(BNetUser user, StatString statstr) {
		mainTextArea.channelInfo(user + statstr.toString() + ".");
		userList.showUser(user, statstr);
		channelTextArea.setText(channel + " (" + userList.count() + ")");
	}

	public void joinedChannel(String channel) {
		this.channel = channel;
		userList.clear();
		mainTextArea.addSeparator();
		mainTextArea.channelInfo("Joining channel " + channel + ".");
		channelTextArea.setText(channel);
		frame.setTitle(c.toString());
	}

	public void recieveChat(BNetUser user, String text) {
		mainTextArea.userChat(user, text);

		if((ti != null) && !frame.isVisible()) {
	        ti.displayMessage("User is chatting: " + user.getShortLogonName(), 
	            text,
	            TrayIcon.MessageType.INFO);
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
		mainTextArea.whisperRecieved(user, text);

		if((ti != null) && !frame.isVisible()) {
	        ti.displayMessage("Whisper from " + user.getShortLogonName(), 
	            text,
	            TrayIcon.MessageType.INFO);
		}
	}

	public void whisperSent(BNetUser user, String text) {
		mainTextArea.whisperSent(user, text);
	}

	public void bnetConnected() {
		userList.clear();
		channelTextArea.setText(null);
	}

	public void bnetDisconnected() {
		userList.clear();
		channelTextArea.setText(null);
		mainTextArea.recieveError("Disconnected from battle.net.");
		mainTextArea.addSeparator();
	}

	public void titleChanged() {
		frame.setTitle(c.toString());
		if(ti != null)
			ti.setToolTip(c.toString());
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
		if(w == null)
			w = new RealmWindow(realms);
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				c.addEventHandler(w);
				w.setVisible(true);
			} });
	}

	public void logonRealmEx(int[] MCPChunk1, int ip, int port, int[] MCPChunk2, String uniqueName) {}

	public void setChatText(String chatText) {
		chatTextArea.setText(chatText);
		chatTextArea.setSelectionStart(chatText.length());
		chatTextArea.requestFocus();
	}

	public void parseCommand(BNetUser user, String command, String param, boolean wasWhispered) {
		mainTextArea.recieveInfo(String.format("parseCommand(\"%1$s\", \"%2$s\", \"%3$s\")", user.getShortLogonName(), command, param));
	}
}
