/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui;

import java.awt.AWTException;
import java.awt.Container;
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
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowStateListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.bnubot.bot.gui.icons.BNetIcon;
import net.bnubot.bot.gui.icons.IconsDotBniReader;
import net.bnubot.bot.gui.notifications.Growl;
import net.bnubot.bot.gui.settings.GlobalConfigurationFrame;
import net.bnubot.bot.gui.settings.OperationCancelledException;
import net.bnubot.core.Profile;
import net.bnubot.core.bncs.ProductIDs;
import net.bnubot.db.Account;
import net.bnubot.db.BNLogin;
import net.bnubot.db.Command;
import net.bnubot.db.CommandAlias;
import net.bnubot.db.CustomDataObject;
import net.bnubot.db.Mail;
import net.bnubot.db.Rank;
import net.bnubot.logging.Out;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.settings.GlobalSettings.TrayIconMode;
import net.bnubot.settings.Settings;
import net.bnubot.util.OperatingSystem;
import net.bnubot.util.task.TaskManager;
import net.bnubot.vercheck.CurrentVersion;
import net.bnubot.vercheck.VersionCheck;

/**
 * @author scotta
 */
public class GuiDesktop extends JFrame {
	private static final long serialVersionUID = -7144648179041514994L;
	private static final List<GuiEventHandler> guis = new ArrayList<GuiEventHandler>();
	private static GuiEventHandler selectedGui = null;
	private static final Box boxTasks = new Box(BoxLayout.Y_AXIS);
	private static final JTabbedPane tabs = new JTabbedPane();
	private static final JMenuBar menuBar = new JMenuBar();
	private static TrayIcon tray = null;
	private static Growl growl = null;
	private static final int KEYMASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
	private static final JCheckBoxMenuItem mnuDebug = new JCheckBoxMenuItem("Enable debug logging");
	private static final JCheckBoxMenuItem mnuDisplayJoinParts = new JCheckBoxMenuItem("Display join/parts", GlobalSettings.getDisplayJoinParts());

	// This must be the last thing to initialize
	private static final GuiDesktop instance = new GuiDesktop();

	private GuiDesktop() {
		super();

		TaskManager.setTaskLocation(boxTasks);

		setTitle();
		initializeGui();
		initializeSystemTray();
		WindowPosition.load(this);
		setVisible(true);
	}

	private void initializeGui() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Box b = new Box(BoxLayout.Y_AXIS);
		b.setBorder(null);
		b.add(boxTasks);
		b.add(tabs);
		getContentPane().add(b);
		setSize(800, 500);

		// Make sure the window gets laid out when maximized/restored
        addWindowStateListener(new WindowStateListener() {
			@Override
			public void windowStateChanged(WindowEvent e) {
				if(e.getID() == WindowEvent.WINDOW_STATE_CHANGED)
					((Window)e.getSource()).validate();
			}});

        // When recieving focus, select the chat box
        addWindowFocusListener(new WindowFocusListener() {
			@Override
			public void windowGainedFocus(WindowEvent e) {
				if(selectedGui != null)
					selectedGui.getFrame().requestFocus();
			}
			@Override
			public void windowLostFocus(WindowEvent e) {}
		});

		tabs.addChangeListener(new ChangeListener() {
			@Override
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
					Out.setDefaultOutputHandler(gui.getFirstConnection());

					// Store the selected GUI
					selectedGui = gui;

					// Set the title to the title for the selected GUI
					setTitle();

					// Move cursor focus to the chat box
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							gui.setChatText(null);
						}
					});

					break;
				}
			}
		});

		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
			@Override
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
					@Override
					public void actionPerformed(ActionEvent e) {
						Profile.newConnection();
					} });
				menu.add(menuItem);

				menuItem = new JMenuItem("Close Profile");
				menuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if(selectedGui != null)
							selectedGui.getFirstConnection().getProfile().dispose();
					} });
				menu.add(menuItem);

				menuItem = new JMenuItem("Settings");
				menuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							new GlobalConfigurationFrame();
						} catch (OperationCancelledException e1) {}
					} });
				menu.add(menuItem);

				menuItem = new JMenuItem("Exit");
				menuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						System.exit(0);
					} });
				menu.add(menuItem);
			}
			menuBar.add(menu);

			menu = new JMenu("Display");
			{
				updateDisplayJoinPartsMenuChecked();
				mnuDisplayJoinParts.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						final boolean b = !GlobalSettings.getDisplayJoinParts();
						GlobalSettings.setDisplayJoinParts(b);
						for(GuiEventHandler geh : guis)
							geh.channelInfo("Join/part notifications " + (b ? "en" : "dis") + "abled.");
					} });
				// Alt+V
				mnuDisplayJoinParts.setAccelerator(
						KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.ALT_DOWN_MASK));
				menu.add(mnuDisplayJoinParts);
			}
			menuBar.add(menu);

			menu = new JMenu("Database");
			{
				addDatabaseEditor(menu, Account.class);
				addDatabaseEditor(menu, BNLogin.class);
				addDatabaseEditor(menu, Command.class);
				addDatabaseEditor(menu, CommandAlias.class);
				addDatabaseEditor(menu, Mail.class);
				addDatabaseEditor(menu, Rank.class);
			}
			menuBar.add(menu);

			menu = new JMenu("Debug");
			{
				JMenuItem menuItem = new JMenuItem("Show Icons");
				menuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent event) {
						IconsDotBniReader.showWindow();
					} });
				menu.add(menuItem);

				updateDebugMenuChecked();
				mnuDebug.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent event) {
						Out.setDebug(!Out.isDebug());
					}});
				mnuDebug.setText("Enable debug logging");
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
					@Override
					public void actionPerformed(ActionEvent event) {
						new Thread() {
							@Override
							public void run() {
								try {
									VersionCheck.checkVersion(true);
								} catch (Exception e) {
									Out.exception(e);
								}
							}
						}.start();
					} });
				menu.add(menuItem);

				menuItem = new JMenuItem("View Change Log");
				menuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent event) {
						new WhatsNewWindow();
					} });
				menu.add(menuItem);

				menuItem = new JMenuItem("About");
				menuItem.addActionListener(new ActionListener() {
					@Override
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

	/**
	 * @param menu
	 */
	private <S extends Comparable<S>, T extends CustomDataObject<S>> void addDatabaseEditor(JMenu menu, final Class<T> clazz) {
		JMenuItem menuItem;
		menuItem = new JMenuItem(clazz.getSimpleName() + " Editor");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					new DatabaseEditor<T,S>(clazz);
				} catch(Exception e) {
					Out.exception(e);
				}
			} });
		menu.add(menuItem);
	}

	private void initializeSystemTray() {
		if((tray != null) || (growl != null) || !GlobalSettings.trayIconMode.enableTray())
			return;

		try {
			if(OperatingSystem.userOS == OperatingSystem.OSX)
				growl = new Growl("BNU-Bot", "Contents/Resources/Icon.icns");
		} catch(Exception ex) {
			Out.exception(ex);
			Out.error(GuiEventHandler.class, "Growl is not supported");
			GlobalSettings.trayIconMode = TrayIconMode.DISABLED;
		}

		try {
			if(!SystemTray.isSupported())
				throw new NoClassDefFoundError();
		} catch(NoClassDefFoundError e) {
			Out.error(GuiEventHandler.class, "SystemTray is not supported! This requires Java 6+");
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
				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(!isVisible());
					if(isVisible())
						toFront();
				}
			});
			pm.add(mi);

			mi = new MenuItem("Exit");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					System.exit(0);
				}
			});
			pm.add(mi);
		}

		tray = new TrayIcon(image);
		tray.setPopupMenu(pm);
		if(OperatingSystem.userOS != OperatingSystem.OSX)
			tray.addMouseListener(new MouseListener() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if(e.getButton() == MouseEvent.BUTTON1) {
						setVisible(!isVisible());
						if(isVisible())
							toFront();
					}
				}
				@Override
				public void mouseEntered(MouseEvent e) {}
				@Override
				public void mouseExited(MouseEvent e) {}
				@Override
				public void mousePressed(MouseEvent e) {}
				@Override
				public void mouseReleased(MouseEvent e) {}
			});
		tray.setImageAutoSize(true);

		try {
			SystemTray.getSystemTray().add(tray);
		} catch(AWTException e) {
			Out.exception(e);
		}

		addWindowStateListener(new WindowStateListener() {
			@Override
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

	public static void setTitle(GuiEventHandler geh, ProductIDs product) {
		instance.setTitle();

		Icon icon = null;
		BNetIcon[] icons_bni = IconsDotBniReader.getIcons();
		if((icons_bni != null) && (product != null)) {
			for(BNetIcon element : icons_bni) {
				if(element.useFor(0, product.getDword())) {
					icon = element.getIcon();
					break;
				}
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
	public static void updateDebugMenuChecked() {
		mnuDebug.setState(Out.isDebug());
	}

	public static void updateDisplayJoinPartsMenuChecked() {
		boolean djp = GlobalSettings.getDisplayJoinParts();
		if(djp == mnuDisplayJoinParts.getState())
			return;
		mnuDisplayJoinParts.setState(djp);
		if(selectedGui != null)
			selectedGui.channelInfo("Join/part notifications " + (djp ? "en" : "dis") + "abled.");
	}

	/**
	 * @return the location of the divider
	 */
	public static int getDividerLocation() {
		if(selectedGui != null)
			return selectedGui.getDividerLocation();
		return Settings.getSection("GuiDesktop").read("dividerLocation", (instance.getWidth() - 200));
	}

	public static Container getTasksLocation() {
		return boxTasks;
	}
}
