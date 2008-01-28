/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui;

import java.awt.AWTException;
import java.awt.Frame;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowStateListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.bnubot.bot.database.Database;
import net.bnubot.bot.gui.database.DatabaseAccountEditor;
import net.bnubot.bot.gui.database.DatabaseRankEditor;
import net.bnubot.bot.gui.icons.BNetIcon;
import net.bnubot.bot.gui.icons.IconsDotBniReader;
import net.bnubot.bot.gui.notifications.Growl;
import net.bnubot.bot.gui.settings.GlobalConfigurationFrame;
import net.bnubot.bot.gui.settings.OperationCancelledException;
import net.bnubot.core.Profile;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.settings.Settings;
import net.bnubot.settings.GlobalSettings.TrayIconMode;
import net.bnubot.util.Out;
import net.bnubot.vercheck.CurrentVersion;
import net.bnubot.vercheck.VersionCheck;

public class GuiDesktop extends JFrame {
	private static final long serialVersionUID = -7144648179041514994L;
	private static final List<GuiEventHandler> guis = new ArrayList<GuiEventHandler>();
	private static GuiEventHandler selectedGui = null;
	private static final JTabbedPane tabs = new JTabbedPane();
	private static final JMenuBar menuBar = new JMenuBar();
	private static TrayIcon tray = null;
	private static Growl growl = null;
	private static final int KEYMASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
	private static final JMenuItem mnuDebug = new JMenuItem();
	
	// This must be the last thing to initialize
	private static final GuiDesktop instance = new GuiDesktop();

	private GuiDesktop() {
		super();

		if(CurrentVersion.fromJar()) {
			// If we're launching a new version, pop up the what's new window
			long currentVersionBuilt = CurrentVersion.version().getBuildDate().getTime();
			long lastWhatsNewWindow = Settings.readLong(null, "whatsNewTime", 0);
			if(lastWhatsNewWindow != currentVersionBuilt) {
				Settings.writeLong(null, "whatsNewTime", currentVersionBuilt);
				Settings.store();
				new WhatsNewWindow();
			}
		}
		
		setTitle();
		initializeGui();
		initializeSystemTray();
		WindowPosition.load(this);
		setVisible(true);
	}

	private void initializeGui() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		getContentPane().add(tabs);
		setSize(800, 500);
		
		// Make sure the window gets laid out when maximized/restored
        addWindowStateListener(new WindowStateListener() {
			public void windowStateChanged(WindowEvent e) {
				if(e.getID() == WindowEvent.WINDOW_STATE_CHANGED)
					((Window)e.getSource()).validate();
			}});
        
        // When recieving focus, select the chat box
        addWindowFocusListener(new WindowFocusListener() {
			public void windowGainedFocus(WindowEvent e) {
				if(selectedGui != null)
					selectedGui.getFrame().requestFocus();
			}
			public void windowLostFocus(WindowEvent e) {}
		});
		
		tabs.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JTabbedPane jtp = (JTabbedPane)e.getSource();
				JPanel jp = (JPanel)jtp.getSelectedComponent();
				for(final GuiEventHandler gui : guis) {
					if(jp != gui.getFrame())
						continue;
					
					// Swap out the menus
					gui.getMenuBar().setVisible(true);
					if(selectedGui != null) {
						selectedGui.getMenuBar().setVisible(false);
						
						// Set the divider to the same place the last one was
						gui.setDividerLocation(selectedGui.getDividerLocation());
					}
					
					// Set the default output window
					Out.setDefaultOutputConnection(gui.getFirstConnection());
					
					// Store the selected GUI
					selectedGui = gui;
					
					// Set the title to the title for the selected GUI
					setTitle();
					
					// Move cursor focus to the chat box
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							gui.setChatText(null);
						}
					});
					
					break;
				}
			}
		});
		
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
			public boolean dispatchKeyEvent(KeyEvent e) {
				int mod = e.getModifiers();
				if(mod != KEYMASK)
					return false;
				
				if(e.getID() != KeyEvent.KEY_RELEASED)
					return false;
				
				switch(e.getKeyCode()) {
				case KeyEvent.VK_LEFT: {
					int idx = tabs.getSelectedIndex() - 1;
					if(idx < 0)
						idx = tabs.getTabCount() - 1;
					tabs.setSelectedIndex(idx);
					return true;
				}
				case KeyEvent.VK_RIGHT: {
					int idx = tabs.getSelectedIndex() + 1;
					if(idx >= tabs.getTabCount())
						idx = 0;
					tabs.setSelectedIndex(idx);
					return true;
				}
				}
				
				return false;
			}
		});
        
		menuBar.setOpaque(true);
		{
			JMenu menu = new JMenu("File");
			{
				JMenuItem menuItem = new JMenuItem("New Profile");
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						Profile.newConnection();
					} });
				menu.add(menuItem);
				
				menuItem = new JMenuItem("Close Profile");
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(selectedGui != null)
							selectedGui.getFirstConnection().getProfile().dispose();
					} });
				menu.add(menuItem);
				
				menuItem = new JMenuItem("Settings");
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							new GlobalConfigurationFrame();
						} catch (OperationCancelledException e1) {}
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
				
				updateDebugMenuText();
				mnuDebug.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						Out.setDebug(!Out.isDebug());
					}});
				menu.add(mnuDebug);
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
				
				menuItem = new JMenuItem("View Change Log");
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						new WhatsNewWindow();
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
	}

	private void initializeSystemTray() {
		if(tray != null)
			return;
		if(growl != null)
			return;
		
		if(!GlobalSettings.trayIconMode.enableTray())
			return;
		
		try {
			if(!SystemTray.isSupported())
				throw new NoClassDefFoundError();
		} catch(NoClassDefFoundError e) {
			Out.error(GuiEventHandler.class, "System tray is not supported; trying growl");
			
			try {
				growl = new Growl("BNU-Bot", "Contents/Resources/Icon.icns");
			} catch(Exception ex) {
				Out.exception(ex);
				Out.error(GuiEventHandler.class, "Growl is not supported either");
				GlobalSettings.trayIconMode = TrayIconMode.DISABLED;
			}
			return;
		}
		
		Image image = Toolkit.getDefaultToolkit().getImage("tray.gif");
		
		PopupMenu pm = new PopupMenu();
		{
			MenuItem mi = new MenuItem("BNU-Bot " + CurrentVersion.version().toString());
			mi.setEnabled(false);
			pm.add(mi);
			
			pm.addSeparator();
			
			mi = new MenuItem("Hide/show");
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(!isVisible());
					if(isVisible())
						toFront();
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
		
		tray = new TrayIcon(image);
		tray.setPopupMenu(pm);
		tray.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1) {
					setVisible(!isVisible());
					if(isVisible())
						toFront();
				}
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
		});
		tray.setImageAutoSize(true);
		
		try {
			SystemTray.getSystemTray().add(tray);
		} catch(AWTException e) {
			Out.exception(e);
		}
		
		addWindowStateListener(new WindowStateListener() {
			public void windowStateChanged(WindowEvent e) {
				if(GlobalSettings.trayMinimizeTo && ((e.getNewState() & Frame.ICONIFIED) != 0)) {
					setVisible(false);
					setState(e.getNewState() & ~Frame.ICONIFIED);
				}
			}
		});
	}
	
	public static GuiDesktop getInstance() {
		return instance;
	}
	
	public static TrayIcon getTray() {
		return tray;
	}
	
	public static Growl getGrowl() {
		return growl;
	}
	
	public static void add(GuiEventHandler geh) {
		// Set the divider location
		geh.setDividerLocation(getDividerLocation());
		
		// Add the components to the display
		geh.getMenuBar().setVisible(false);
		menuBar.add(geh.getMenuBar());
		guis.add(geh);
		tabs.addTab(geh.toString(), geh.getFrame());
	}
	
	public static void remove(GuiEventHandler geh) {
		menuBar.remove(geh.getMenuBar());
		guis.remove(geh);
		tabs.remove(geh.getFrame());
	}
	
	private void setTitle() {
		String title = "BNU-Bot " + CurrentVersion.version();
		if(selectedGui != null) {
			title += " - ";
			title += selectedGui.toString();
		}
		setTitle(title);
		
		if(tray != null)
			tray.setToolTip(title);
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
		for(int i = 0; i < tabs.getTabCount(); i++) {
			if(component == tabs.getComponentAt(i)) {
				tabs.setTitleAt(i, geh.toString());
				tabs.setIconAt(i, icon);
				geh.getMenuBar().setIcon(icon);
				break;
			}
		}
	}

	/**
	 * Set the debug menu item text
	 */
	public static void updateDebugMenuText() {
		mnuDebug.setText((Out.isDebug() ? "Dis" : "En") + "able debug logging");
	}

	/**
	 * Get the desired divider location
	 */
	public static int getDividerLocation() {
		if(selectedGui != null)
			return selectedGui.getDividerLocation();
		return Settings.readInt("GuiDesktop", "dividerLocation", instance.getWidth() - 200);
	}
}
