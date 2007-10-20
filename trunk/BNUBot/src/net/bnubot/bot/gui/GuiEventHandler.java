/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.bot.gui;

import java.awt.Color;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import net.bnubot.bot.gui.ColorScheme.ColorScheme;
import net.bnubot.bot.gui.components.ClanList;
import net.bnubot.bot.gui.components.FriendList;
import net.bnubot.bot.gui.components.TextWindow;
import net.bnubot.bot.gui.components.UserList;
import net.bnubot.bot.gui.icons.IconsDotBniReader;
import net.bnubot.bot.gui.main.GuiDesktop;
import net.bnubot.core.Connection;
import net.bnubot.core.EventHandler;
import net.bnubot.core.clan.ClanMember;
import net.bnubot.core.friend.FriendEntry;
import net.bnubot.settings.ConnectionSettings;
import net.bnubot.util.BNetUser;
import net.bnubot.util.Out;

public class GuiEventHandler implements EventHandler {
	private JPanel frame = null;
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
	private JMenu menuBar = new JMenu();
	private BNetUser lastWhisperFrom = null;
	
	public void initialize(Connection c) {
		ColorScheme cs = ColorScheme.createColorScheme(ConnectionSettings.colorScheme);
		
		if(c != null) {
			this.c = c;
			initializeGui(c.toString(), cs);
		} else {
			initializeGui("BNU-Bot", cs);
		}
		Out.setOutputConnection(this);
		
		initializeSystemTray();
	}

	private void initializeSystemTray() {
	/*	try {
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
		
		/*frame.addWindowStateListener(new WindowStateListener() {
			public void windowStateChanged(WindowEvent e) {
				if((e.getNewState() & java.awt.Frame.ICONIFIED) != 0) {
					frame.setVisible(false);
					frame.setState(e.getNewState() & ~java.awt.Frame.ICONIFIED);
				}
			}
		});*/
	}
	
	private void initializeGui(String title, ColorScheme cs) {
		//Create the panel
		frame = new JPanel(true);
		
		//Create the menu bar.
		menuBar.setOpaque(true);
		menuBar.setText(c.getConnectionSettings().bncsServer);
		{
			JMenu menu;
			JMenuItem menuItem;
			
			menuItem = new JMenuItem("Settings");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					new ConfigurationFrame(c.getConnectionSettings()).setVisible(true);
				} });
			menuBar.add(menuItem);
			
			menuBar.addSeparator();

			menu = new JMenu("Battle.net");
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
			}
			menuBar.add(menu);
			
			menuItem = new JMenuItem("Realms");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					try {
						c.sendQueryRealms();
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
							c.sendClanMOTD(new ClanMOTDEditor(c));
						} catch(Exception e) {
							Out.exception(e);
						}
					} });
				menu.add(menuItem);
			}
			menuBar.add(menu);
		}
		//frame.setJMenuBar(menuBar);
		
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
					return;
				}
				
				if(lastWhisperFrom != null) {
					String txt = chatTextArea.getText();
					if("/r ".equals(txt) || "/rw ".equals(txt))
						chatTextArea.setText("/w " + lastWhisperFrom.getShortLogonName() + " ");
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
		
		//Display the window
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
		mainTextArea.channelInfo(user + " has joined the channel" + user.getStatString().toString() + ".");
		channelTextArea.setText(channel + " (" + userList.count() + ")");
	}

	public void channelLeave(BNetUser user) {
		userList.removeUser(user);
		mainTextArea.channelInfo(user + " has left the channel.");
		channelTextArea.setText(channel + " (" + userList.count() + ")");
	}

	public void channelUser(BNetUser user) {
		mainTextArea.channelInfo(user + user.getStatString().toString() + ".");
		userList.showUser(user);
		channelTextArea.setText(channel + " (" + userList.count() + ")");
	}

	public void joinedChannel(String channel) {
		this.channel = channel;
		userList.clear();
		mainTextArea.addSeparator();
		mainTextArea.channelInfo("Joining channel " + channel + ".");
		channelTextArea.setText(channel);
		
		GuiDesktop.setTitle(this, c.getProductID());
	}

	public void recieveChat(BNetUser user, String text) {
		mainTextArea.userChat(user, text, user.equals(c.getMyUser()));

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
		lastWhisperFrom = user;
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
		BNetUser user = c.getMyUser();
		String name;
		
		if(user != null)
			name = c.getMyUser().getFullAccountName();
		else
			name = c.getConnectionSettings().bncsServer;
		
		menuBar.setText(name);
		
		GuiDesktop.setTitle(this, c.getProductID());
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

	@Override
	public String toString() {
		return c.toString();
	}
}
