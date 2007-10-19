/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.bot.gui.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.bnubot.bot.database.Database;
import net.bnubot.bot.gui.AboutWindow;
import net.bnubot.bot.gui.GlobalConfigurationFrame;
import net.bnubot.bot.gui.GuiEventHandler;
import net.bnubot.bot.gui.database.DatabaseAccountEditor;
import net.bnubot.bot.gui.database.DatabaseRankEditor;
import net.bnubot.bot.gui.icons.BNetIcon;
import net.bnubot.bot.gui.icons.IconsDotBniReader;
import net.bnubot.core.Profile;
import net.bnubot.settings.ConnectionSettings;
import net.bnubot.util.Out;
import net.bnubot.vercheck.CurrentVersion;
import net.bnubot.vercheck.VersionCheck;

public class GuiDesktop extends JFrame {
	private static final long serialVersionUID = -7144648179041514994L;
	private static final ArrayList<GuiEventHandler> guis = new ArrayList<GuiEventHandler>();
	private static GuiEventHandler selectedGui = null;
	private static final JTabbedPane desktop = new JTabbedPane();
	private static final JMenuBar menuBar = new JMenuBar();
	private static final GuiDesktop instance = new GuiDesktop();
	
	private GuiDesktop() {
		super();
		setTitle();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setContentPane(desktop);
		setSize(800, 500);
		
		desktop.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JTabbedPane jtp = (JTabbedPane)e.getSource();
				JPanel jp = (JPanel)jtp.getSelectedComponent();
				for(GuiEventHandler gui : guis) {
					if(jp != gui.getFrame())
						continue;
					
					// Swap out the menus
					if(selectedGui != null)
						selectedGui.getMenuBar().setVisible(false);
					gui.getMenuBar().setVisible(true);
					
					// Store the selected GUI
					selectedGui = gui;
					
					// Tell Out to direct info to the selected gui
					Out.setOutputConnection(gui);
					
					// Set the title to the title for the selected GUI
					setTitle();
					
					break;
				}
			}
		});
        
		menuBar.setOpaque(true);
		{
			JMenu menu = new JMenu("File");
			{
				JMenuItem menuItem = new JMenuItem("New Profile");
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							ConnectionSettings cs = new ConnectionSettings();
							ConnectionSettings.numBots++;
							cs.load(ConnectionSettings.numBots);
							Thread.yield();
							Profile.add(cs);
						} catch(Exception ex) {
							Out.exception(ex);
						}
					} });
				menu.add(menuItem);
				
				menuItem = new JMenuItem("Settings");
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						new GlobalConfigurationFrame().setVisible(true);
					} });
				menu.add(menuItem);
				
				menuItem = new JMenuItem("Exit");
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						System.exit(0);
					} });
				menu.add(menuItem);
			}
			menuBar.add(menu);
			
			menu = new JMenu("Database");
			{
				JMenuItem menuItem = new JMenuItem("Rank editor");
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						Database d = Database.getInstance();
						if(d != null)
							new DatabaseRankEditor(d);
						else
							Out.error(GuiDesktop.class, "There is no database initialized.");
					} });
				menu.add(menuItem);
				
				menuItem = new JMenuItem("Account editor");
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						Database d = Database.getInstance();
						if(d != null)
							new DatabaseAccountEditor(d);
						else
							Out.error(GuiDesktop.class, "There is no database initialized.");
					} });
				menu.add(menuItem);
			}
			menuBar.add(menu);
		
			menu = new JMenu("Debug");
			{
				JMenuItem menuItem = new JMenuItem("Show Icons");
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						IconsDotBniReader.showWindow();
					} });
				menu.add(menuItem);
				
				menuItem = new JMenuItem((Out.isDebug() ? "Dis" : "En") + "able debug logging");
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						Out.setDebug(!Out.isDebug());
						
						JMenuItem jmi = (JMenuItem)event.getSource();
						jmi.setText((Out.isDebug() ? "Dis" : "En") + "able debug logging");
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
			
			menu = new JMenu();
			menu.setVisible(false);
			menuBar.add(menu);
		}
		setJMenuBar(menuBar);
		
        setVisible(true);
	}
	
	public static GuiDesktop getInstance() {
		return instance;
	}
	
	public static void add(GuiEventHandler geh) {
		geh.getMenuBar().setVisible(false);
		menuBar.add(geh.getMenuBar());
		
		guis.add(geh);
		desktop.addTab(geh.toString(), geh.getFrame());
	}
	
	private void setTitle() {
		String title = "BNU-Bot " + CurrentVersion.version();
		if(selectedGui != null) {
			title += " - ";
			title += selectedGui.toString();
		}
		setTitle(title);
	}
	
	public static void setTitle(GuiEventHandler geh, int product) {
		instance.setTitle();
		
		Icon icon = null;
		for(BNetIcon element : IconsDotBniReader.getIcons()) {
			if(element.useFor(0, product)) {
				icon = element.getIcon();
				break;
			}
		}
		
		JPanel component = geh.getFrame();
		for(int i = 0; i < desktop.getTabCount(); i++) {
			if(component == desktop.getComponentAt(i)) {
				desktop.setTitleAt(i, geh.toString());
				desktop.setIconAt(i, icon);
				geh.getMenuBar().setIcon(icon);
				break;
			}
		}
	}
}
