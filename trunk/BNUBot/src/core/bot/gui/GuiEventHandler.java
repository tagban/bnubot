package core.bot.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import core.Connection;
import core.EventHandler;

public class GuiEventHandler implements EventHandler {
	private JFrame frame = null;
	private Connection c = null;
	private JTextArea mainTextArea = null;
	private JTextArea chatTextArea = null;
	private JLabel channelLabel = null;
	private JButton four = null;
	
	public void initialize(Connection c) {
		if(c != null) {
			this.c = c;
			initializeGui(c.toString());
		} else {
			initializeGui("BNU`Bot");
		}
	}

	@SuppressWarnings("serial")
	private void initializeGui(String title) {
		//Create and set up the window
		frame = new JFrame(title);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Create the menu bar.
		JMenuBar menuBar = new JMenuBar();
		menuBar.setOpaque(true);
		menuBar.setPreferredSize(new Dimension(200, 20));
		
		//Create the File menu.
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
		
		//Set the menu bar and the label to the cotent pane.
		frame.setJMenuBar(menuBar);
		
		//Create a label to put in the content pane.
		/*JLabel label = new JLabel();
		label.setOpaque(true);
		label.setPreferredSize(new Dimension(200, 180));
		frame.getContentPane().add(label, BorderLayout.CENTER);*/
		
		//Create a panel to organize the gui
		frame.setLayout(new BotLayoutManager());
		mainTextArea = new JTextArea();
		mainTextArea.setBackground(Color.BLACK);
		mainTextArea.setForeground(Color.GRAY);
		chatTextArea = new JTextArea();
		chatTextArea.setBackground(Color.BLACK);
		chatTextArea.setForeground(Color.GRAY);
		chatTextArea.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) { keyEvent(e); }
		});
		channelLabel = new JLabel("blah");
		channelLabel.setHorizontalAlignment(SwingConstants.CENTER);
		channelLabel.setVerticalAlignment(SwingConstants.CENTER);
		four = new JButton("4");
		frame.add(mainTextArea);
		frame.add(chatTextArea);
		frame.add(channelLabel);
		frame.add(four);
		
		//Display the window
		frame.pack();
		frame.setVisible(true);
	}
	
	public String safeString(String input) {
		String output = "";
		byte[] data = input.getBytes();
		for(int i = 0; i < data.length; i++) {
			if(data[i] >= 0x20)
				output += (char)data[i];
		}
		return output;
	}
	
	public void keyEvent(KeyEvent e) {
		//System.out.println("KeyEvent: " + e.toString());
		if(e.getKeyChar() == '\n') {
			c.sendChat(safeString(chatTextArea.getText()));
			chatTextArea.setText(null);
		}
	}

	public void channelJoin(String user, int flags, int ping, String statstr) {
		// TODO Auto-generated method stub
		
	}

	public void channelLeave(String user, int flags, int ping, String statstr) {
		// TODO Auto-generated method stub
		
	}

	public void channelUser(String user, int flags, int ping, String statstr) {
		// TODO Auto-generated method stub
		
	}

	public void joinedChannel(String channel) {
		mainTextArea.append("Joining channel " + channel + ".\n");
		channelLabel.setText(channel);
	}

	public void recieveChat(String user, String text) {
		mainTextArea.append("<" + user + "> " + text + "\n");
	}

	public void recieveEmote(String user, String text) {
		mainTextArea.append("<" + user + " " + text + ">\n");
	}

}
