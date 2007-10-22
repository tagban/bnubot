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
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

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
	private Connection con = null;
	private TextWindow mainTextArea = null;
	private JTextArea chatTextArea = null;
	private JTextArea channelTextArea = null;
	private UserList userList = null;
	private FriendList friendList = null;
	private ClanList clanList = null;
	private RealmWindow w = null;
	private String channel = null;
	private TrayIcon tray = null;
	private JMenu menuBar = new JMenu();
	private BNetUser lastWhisperFrom = null;
	
	public void initialize(Connection con) {
		ColorScheme colors = ColorScheme.createColorScheme(ConnectionSettings.colorScheme);
		
		if(con != null) {
			this.con = con;
			initializeGui(con.toString(), colors);
		} else {
			initializeGui("BNU-Bot", colors);
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
	
	private void initializeGui(String title, ColorScheme colors) {
		// Create the panel
		frame = new JPanel(new BotLayoutManager(), true);
		
		// Create the menu bar.
		menuBar.setOpaque(true);
		menuBar.setText(con.getConnectionSettings().bncsServer);
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
						con.sendQueryRealms();
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
		chatTextArea = new JTextArea();
		chatTextArea.setBackground(colors.getBackgroundColor());
		chatTextArea.setForeground(Color.LIGHT_GRAY);
		chatTextArea.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {
				if(e.getKeyChar() == '\n') {
					try {
						int cp = chatTextArea.getCaretPosition();
						String txt = chatTextArea.getText(0, cp - 1);
						txt += chatTextArea.getText(cp, chatTextArea.getText().length() - cp);
						
						String text[] = txt.split("\n");
						for(String element : text) {
							if(element.trim().length() > 0)
								con.sendChat(element);
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
						String text = chatTextArea.getText(0, end - 1);
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
		channelTextArea = new JTextArea();
		channelTextArea.setEditable(false);
		channelTextArea.setAlignmentX(SwingConstants.CENTER);
		channelTextArea.setAlignmentY(SwingConstants.CENTER);
		channelTextArea.setBackground(colors.getBackgroundColor());
		channelTextArea.setForeground(Color.LIGHT_GRAY);
		
		IconsDotBniReader.initialize(con.getConnectionSettings());
		
		// The userlist
		userList = new UserList(colors, con, this);
		// Friends list
		friendList = new FriendList(colors);
		// Clan list
		clanList = new ClanList(colors);

		JTabbedPane allLists = new JTabbedPane();
		allLists.addTab("Channel", new JScrollPane(userList));
		allLists.addTab("Friends", new JScrollPane(friendList));
		allLists.addTab("Clan", new JScrollPane(clanList));
		
		JPanel leftSide = new JPanel();
		leftSide.add(mainTextArea);
		leftSide.add(chatTextArea);
		
		JPanel rightSide = new JPanel();
		rightSide.add(channelTextArea);
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
		
		JSplitPane jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, leftSide, rightSide);
		jsp.setDividerLocation(550);
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
		
		GuiDesktop.setTitle(this, con.getProductID());
	}

	public void recieveChat(BNetUser user, String text) {
		mainTextArea.userChat(user, text, user.equals(con.getMyUser()));

		if((tray != null) && !frame.isVisible()) {
	        tray.displayMessage("User is chatting: " + user.getShortLogonName(), 
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

		if((tray != null) && !frame.isVisible()) {
	        tray.displayMessage("Whisper from " + user.getShortLogonName(), 
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
		BNetUser user = con.getMyUser();
		String name;
		
		if(user != null)
			name = con.getMyUser().getFullAccountName();
		else
			name = con.getConnectionSettings().bncsServer;
		
		menuBar.setText(name);
		
		GuiDesktop.setTitle(this, con.getProductID());
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
		if(w == null)
			w = new RealmWindow(realms);
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				con.addEventHandler(w);
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
		return con.toString();
	}
}
