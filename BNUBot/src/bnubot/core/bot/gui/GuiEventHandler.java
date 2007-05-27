package bnubot.core.bot.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import bnubot.core.Connection;
import bnubot.core.EventHandler;
import bnubot.core.bot.gui.icons.IconsDotBniReader;


public class GuiEventHandler implements EventHandler {
	private JFrame frame = null;
	private Connection c = null;
	private JTextArea mainTextArea = null;
	private JTextArea chatTextArea = null;
	private JTextArea channelTextArea = null;
	private UserList userList = null;
	
	public void initialize(Connection c) {
		if(c != null) {
			this.c = c;
			initializeGui(c.toString());
		} else {
			initializeGui("BNU`Bot");
		}
	}

	private void initializeGui(String title) {
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
				menuItem.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { c.connect(); } });
				menu.add(menuItem);
				
				menuItem = new JMenuItem("Disconnect");
				menuItem.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { c.disconnect(); } });
				menu.add(menuItem);
				
				menu.addSeparator();
				
				menuItem = new JMenuItem("Exit");
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						JFrame frame = (JFrame) SwingUtilities.getRoot((Component) e.getSource());
						frame.dispose();
					}
				});
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
		
		//Create a panel to organize the gui
		frame.setLayout(new BotLayoutManager());
		mainTextArea = new JTextArea();
		mainTextArea.setBackground(Color.BLACK);
		mainTextArea.setForeground(Color.LIGHT_GRAY);
		chatTextArea = new JTextArea();
		chatTextArea.setBackground(Color.BLACK);
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
		channelTextArea = new JTextArea();
		channelTextArea.setAlignmentX(SwingConstants.CENTER);
		channelTextArea.setAlignmentY(SwingConstants.CENTER);
		channelTextArea.setBackground(Color.BLACK);
		channelTextArea.setForeground(Color.LIGHT_GRAY);
		userList = new UserList(IconsDotBniReader.readIconsDotBni(c.downloadFile("Icons.bni")));
		userList.setBackground(Color.BLACK);
		userList.setForeground(Color.LIGHT_GRAY);
		frame.add(mainTextArea);
		frame.add(chatTextArea);
		frame.add(channelTextArea);
		frame.add(userList);
		
		//Display the window
		frame.pack();
		frame.setVisible(true);
	}

	public void channelJoin(String user, int flags, int ping, String statstr) {
		userList.showUser(user, flags, ping, statstr);
	}

	public void channelLeave(String user, int flags, int ping, String statstr) {
		userList.removeUser(user);
	}

	public void channelUser(String user, int flags, int ping, String statstr) {
		userList.showUser(user, flags, ping, statstr);
	}

	public void joinedChannel(String channel) {
		userList.clear();
		mainTextArea.append("Joining channel " + channel + ".\n");
		channelTextArea.setText(channel);
	}

	public void recieveChat(String user, String text) {
		mainTextArea.append("<" + user + "> " + text + "\n");
	}

	public void recieveEmote(String user, String text) {
		mainTextArea.append("<" + user + " " + text + ">\n");
	}

	public void recieveInfo(String text) {
		mainTextArea.append(text + "\n");
	}
}
