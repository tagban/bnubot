package bnubot.bot.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import bnubot.bot.EventHandler;
import bnubot.bot.gui.ColorScheme.ColorScheme;
import bnubot.bot.gui.ColorScheme.Diablo2ColorScheme;
import bnubot.bot.gui.textwindow.TextWindow;
import bnubot.bot.gui.userlist.IconsDotBniReader;
import bnubot.bot.gui.userlist.UserList;
import bnubot.core.BNetUser;
import bnubot.core.Connection;


public class GuiEventHandler implements EventHandler {
	private JFrame frame = null;
	private Connection c = null;
	private TextWindow mainTextArea = null;
	private JTextArea chatTextArea = null;
	private JTextArea channelTextArea = null;
	private UserList userList = null;
	
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
					c.sendChat(chatTextArea.getText());
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
		//The userlist
		userList = new UserList(IconsDotBniReader.readIconsDotBni(c.downloadFile("Icons.bni")), cs);
		
		//Add them to the frame
		frame.add(mainTextArea);
		frame.add(chatTextArea);
		frame.add(channelTextArea);
		frame.add(userList);
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

	public void channelJoin(BNetUser user, int flags, int ping, String statstr) {
		userList.showUser(user.toString(), flags, ping, statstr);
		mainTextArea.channelInfo(user + " has joined.");
	}

	public void channelLeave(BNetUser user, int flags, int ping, String statstr) {
		userList.removeUser(user.toString());
		mainTextArea.channelInfo(user + " has left.");
	}

	public void channelUser(BNetUser user, int flags, int ping, String statstr) {
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

	public void recieveInfo(String text) {
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
}
