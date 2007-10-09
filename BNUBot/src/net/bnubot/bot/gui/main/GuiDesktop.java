/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.bot.gui.main;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import net.bnubot.bot.gui.AboutWindow;
import net.bnubot.util.Out;
import net.bnubot.vercheck.CurrentVersion;
import net.bnubot.vercheck.VersionCheck;

public class GuiDesktop extends JFrame {
	private static final long serialVersionUID = -7144648179041514994L;
	private static final GuiDesktop instance = new GuiDesktop();
	
	private GuiDesktop() {
		super("BNU-Bot " + CurrentVersion.version());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JDesktopPane desktop = new JDesktopPane();
		setContentPane(desktop);
		
		// TODO: get these colors from a ColorScheme
        desktop.setBackground(Color.BLACK);
        desktop.setForeground(Color.WHITE);
		
        setSize(800, 500);
        
		JMenuBar menuBar = new JMenuBar();
		menuBar.setOpaque(true);
		{
			JMenu menu = new JMenu("File");
			{
				JMenuItem menuItem = new JMenuItem("Exit");
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						System.exit(0);
					} });
				menu.add(menuItem);
			}
			menuBar.add(menu);
			
			menu = new JMenu("Help");
			{
				JMenuItem menuItem = new JMenuItem("Complain about scrollbars");
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
		setJMenuBar(menuBar);
		
        setVisible(true);
	}
	
	public static GuiDesktop getInstance() {
		return instance;
	}
	
	public static void add(JInternalFrame gui) {
		instance.getContentPane().add(gui);
	}
}
