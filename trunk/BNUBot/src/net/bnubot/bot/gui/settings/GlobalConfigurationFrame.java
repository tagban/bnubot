/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui.settings;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.bnubot.bot.database.DriverShim;
import net.bnubot.bot.gui.KeyManager;
import net.bnubot.bot.gui.WindowPosition;
import net.bnubot.bot.gui.components.ConfigCheckBox;
import net.bnubot.bot.gui.components.ConfigComboBox;
import net.bnubot.bot.gui.components.ConfigFactory;
import net.bnubot.bot.gui.components.ConfigTextArea;
import net.bnubot.settings.DatabaseSettings;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.settings.GlobalSettings.TabCompleteMode;
import net.bnubot.settings.GlobalSettings.TrayIconMode;
import net.bnubot.util.Out;
import net.bnubot.util.TimeFormatter;
import net.bnubot.vercheck.CurrentVersion;
import net.bnubot.vercheck.ReleaseType;

public class GlobalConfigurationFrame extends JDialog {
	private static final long serialVersionUID = 1308177934480442149L;

	JTabbedPane tabs = null;
	
	boolean keysOnly;
	boolean pressedCancel = false;

	//Settings
	ConfigTextArea txtEmail = null;
	ConfigComboBox cmbTSFormat = null;
	ConfigTextArea txtBNLSServer = null;
	ConfigComboBox cmbBNUserToString = null;
	ConfigComboBox cmbBNUserToStringUserList = null;
	ConfigComboBox cmbReleaseType = null;
	ConfigComboBox cmbColorScheme = null;
	ConfigComboBox cmbLookAndFeel = null;
	ConfigComboBox cmbPlasticTheme = null;
	ConfigCheckBox chkAutoConnect = null;
	ConfigCheckBox chkEnableMirrorSelector = null;
	ConfigCheckBox chkDisplayBattleNetMOTD = null;
	ConfigCheckBox chkDisplayBattleNetChannels = null;
	ConfigCheckBox chkDisplayJoinParts = null;
	ConfigCheckBox chkDisplayChannelUsers = null;
	ConfigCheckBox chkEnableGUI = null;
	ConfigComboBox cmbTrayIconMode = null;
	ConfigCheckBox chkTrayDisplayConnectDisconnect = null;
	ConfigCheckBox chkTrayDisplayChannel = null;
	ConfigCheckBox chkTrayDisplayJoinPart = null;
	ConfigCheckBox chkTrayDisplayChatEmote = null;
	ConfigCheckBox chkTrayDisplayWhisper = null;
	ConfigComboBox cmbTabCompleteMode = null;
	ConfigCheckBox chkEnableLegacyIcons = null;
	ConfigCheckBox chkEnableCLI = null;
	ConfigCheckBox chkEnableTrivia = null;
	ConfigTextArea txtTriviaRoundLength = null;
	ConfigCheckBox chkEnableCommands = null;
	ConfigCheckBox chkEnableHTMLOutput = null;
	ConfigCheckBox chkEnableFloodProtect = null;
	ConfigCheckBox chkPacketLog = null;
	ConfigCheckBox chkWhisperBack = null;

	//Connection
	JButton btnLoad = null;
	JButton btnCancel = null;
	JButton btnOK = null;
	JButton btnApply = null;

	//CDKeys
	ConfigTextArea txtCDKeys = null;
	JButton btnSaveKeys = null;

	//Database
	DatabaseSettings dbSettings = null;
	ConfigComboBox cmbDrivers = null;
	ConfigTextArea txtDriverURL = null;
	ConfigTextArea txtDriverUsername = null;
	ConfigTextArea txtDriverPassword = null;
	ConfigTextArea txtDriverSchema = null;
	JButton btnSaveDatabase = null;
	
	// Debug
	ConfigCheckBox chkEnableDebug = null;
	ConfigCheckBox chkDebugToGui = null;
	List<ConfigCheckBox> chkDebug = null;
	
	public GlobalConfigurationFrame(boolean keysOnly) throws OperationCancelledException {
		super();
		if(keysOnly)
			setTitle("CD Key Editor");
		else
			setTitle("Configuration");

		this.keysOnly = keysOnly;
		initializeGui();

		setModal(true);
		WindowPosition.load(this);
		setVisible(true);
		
		if(pressedCancel)
			throw new OperationCancelledException();
	}

	public GlobalConfigurationFrame() throws OperationCancelledException {
		this(false);
	}

	private void initializeGui() {
		getContentPane().removeAll();
		Integer selecedTab = null;
		if(tabs != null)
			selecedTab = tabs.getSelectedIndex();
		
		Box boxTabsAndButtons = new Box(BoxLayout.Y_AXIS);
		add(boxTabsAndButtons);

		// CD Keys
		Box boxAll = new Box(BoxLayout.Y_AXIS);
		{
			txtCDKeys = new ConfigTextArea(null);
			JScrollPane jsp = new JScrollPane(txtCDKeys);
			jsp.setPreferredSize(new Dimension(350, 350));
			boxAll.add(jsp);
			loadCDKeys();
		}

		if(keysOnly) {
			boxTabsAndButtons.add(boxAll);
		} else {
			tabs = new JTabbedPane();
			boxTabsAndButtons.add(tabs);
			
			tabs.addTab("CD Keys", boxAll);
			
			boxAll = new Box(BoxLayout.Y_AXIS);
			{
				txtBNLSServer = ConfigFactory.makeText("BNLS Server", GlobalSettings.bnlsServer, boxAll);
				txtEmail = ConfigFactory.makeText("Email", GlobalSettings.email, boxAll);
				
				Object[] values;
				if(CurrentVersion.fromJar())
					values = new ReleaseType[] {
						ReleaseType.Stable,
						ReleaseType.ReleaseCandidate,
						ReleaseType.Beta,
						ReleaseType.Alpha };
				else
					values = new ReleaseType[] {
						ReleaseType.Development };
				cmbReleaseType = ConfigFactory.makeCombo("Version Check", values, false, boxAll);
				cmbReleaseType.setSelectedItem(GlobalSettings.releaseType);
	
				boxAll.add(chkAutoConnect = new ConfigCheckBox("Auto Connect", GlobalSettings.autoConnect));
				boxAll.add(chkEnableMirrorSelector = new ConfigCheckBox("Enable Mirror Selector", GlobalSettings.enableMirrorSelector));
				boxAll.add(chkEnableFloodProtect = new ConfigCheckBox("Enable Flood Protect", GlobalSettings.enableFloodProtect));
				boxAll.add(chkPacketLog = new ConfigCheckBox("Packet Log", GlobalSettings.packetLog));
				boxAll.add(chkWhisperBack = new ConfigCheckBox("Whisper Commands", GlobalSettings.whisperBack));
				boxAll.add(Box.createVerticalGlue());
			}
			tabs.addTab("Settings", boxAll);
	
			boxAll = new Box(BoxLayout.Y_AXIS);
			{
				boxAll.add(chkEnableTrivia = new ConfigCheckBox("Enable Trivia (requires restart)", GlobalSettings.enableTrivia));
				txtTriviaRoundLength = ConfigFactory.makeText("Trivia Round Length", Long.toString(GlobalSettings.triviaRoundLength), boxAll);
				boxAll.add(chkEnableCLI = new ConfigCheckBox("Enable CLI (requires restart)", GlobalSettings.enableCLI));
				boxAll.add(chkEnableCommands = new ConfigCheckBox("Enable Commands (requires restart)", GlobalSettings.enableCommands));
				boxAll.add(chkEnableHTMLOutput = new ConfigCheckBox("Enable HTML Output (requires restart)", GlobalSettings.enableHTMLOutput));
			}
			tabs.addTab("Plugins", boxAll);
	
			boxAll = new Box(BoxLayout.Y_AXIS);
			{
				boxAll.add(chkEnableGUI = new ConfigCheckBox("Enable GUI (requires restart)", GlobalSettings.enableGUI));
				chkEnableGUI.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent arg0) {
						if(!chkEnableGUI.isSelected())
							cmbTrayIconMode.setSelectedItem(TrayIconMode.DISABLED);
					}});
				
				Object[] values = new String[] {
					"BNLogin@Gateway",
					"BNLogin",
					"Prefix Account",
					"Prefix Account (BNLogin)",
					"Account (BNLogin)" };
				cmbBNUserToString = ConfigFactory.makeCombo("BNetUser.toString()", values, false, boxAll);
				cmbBNUserToString.setSelectedIndex(GlobalSettings.bnUserToString);
				cmbBNUserToStringUserList = ConfigFactory.makeCombo("User List", values, false, boxAll);
				cmbBNUserToStringUserList.setSelectedIndex(GlobalSettings.bnUserToStringUserList);
				
				cmbTrayIconMode = ConfigFactory.makeCombo("Tray Icon", TrayIconMode.values(), false, boxAll);
				cmbTrayIconMode.setSelectedItem(GlobalSettings.trayIconMode);
				cmbTrayIconMode.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent e) {
						TrayIconMode selection = (TrayIconMode)cmbTrayIconMode.getSelectedItem();
						if(selection.enableTray())
							chkEnableGUI.setSelected(true);
					}});
	
				boxAll.add(chkTrayDisplayConnectDisconnect = new ConfigCheckBox("Tray: Connect/Disconnect", GlobalSettings.trayDisplayConnectDisconnect));
				boxAll.add(chkTrayDisplayChannel = new ConfigCheckBox("Tray: Channel", GlobalSettings.trayDisplayChannel));
				boxAll.add(chkTrayDisplayJoinPart = new ConfigCheckBox("Tray: Join/Part", GlobalSettings.trayDisplayJoinPart));
				boxAll.add(chkTrayDisplayChatEmote = new ConfigCheckBox("Tray: Chat/Emote", GlobalSettings.trayDisplayChatEmote));
				boxAll.add(chkTrayDisplayWhisper = new ConfigCheckBox("Tray: Whisper", GlobalSettings.trayDisplayWhisper));
				
				cmbTabCompleteMode = ConfigFactory.makeCombo("Tab Complete", TabCompleteMode.values(), false, boxAll);
				cmbTabCompleteMode.setSelectedItem(GlobalSettings.tabCompleteMode);
				
				values = new String[] { TimeFormatter.tsFormat, "%1$tH:%1$tM:%1$tS.%1$tL", "%1$tH:%1$tM:%1$tS", "%1$tH:%1$tM" };
				cmbTSFormat = ConfigFactory.makeCombo("TimeStamp", values, true, boxAll);
				cmbTSFormat.setSelectedItem(TimeFormatter.tsFormat);
	
				values = new String[] { "Starcraft", "Diablo 2" };
				cmbColorScheme = ConfigFactory.makeCombo("Color Scheme", values, false, boxAll);
				cmbColorScheme.setSelectedIndex(GlobalSettings.colorScheme - 1);
	
				ArrayList<String> lafs = new ArrayList<String>();
				for(LookAndFeelInfo lafi : UIManager.getInstalledLookAndFeels())
					lafs.add(lafi.getName());
				cmbLookAndFeel = ConfigFactory.makeCombo("Look and Feel", lafs.toArray(), false, boxAll);
				cmbLookAndFeel.setSelectedItem(GlobalSettings.getLookAndFeel());
				cmbLookAndFeel.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent e) {
						for(LookAndFeelInfo lafi : UIManager.getInstalledLookAndFeels())
							if(lafi.getName().equals(cmbLookAndFeel.getSelectedItem()))
								GlobalSettings.setLookAndFeel(lafi);
					}
				});
				
				cmbPlasticTheme = ConfigFactory.makeCombo("Plastic Theme", GlobalSettings.getLookAndFeelThemes(), false, boxAll);
				cmbPlasticTheme.setSelectedItem(GlobalSettings.getLookAndFeelTheme());
				cmbPlasticTheme.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent e) {
						GlobalSettings.setLookAndFeelTheme(cmbPlasticTheme.getSelectedItem().toString());
					}
				});
	
				boxAll.add(chkDisplayBattleNetMOTD = new ConfigCheckBox("Display Battle.net MOTD", GlobalSettings.displayBattleNetMOTD));
				boxAll.add(chkDisplayBattleNetChannels = new ConfigCheckBox("Display Battle.net Channels", GlobalSettings.displayBattleNetChannels));
				boxAll.add(chkDisplayJoinParts = new ConfigCheckBox("Display Join/Part Messages", GlobalSettings.displayJoinParts));
				boxAll.add(chkDisplayChannelUsers = new ConfigCheckBox("Display Channel Users On Join", GlobalSettings.displayChannelUsers));
	
				boxAll.add(chkEnableLegacyIcons = new ConfigCheckBox("Enable Legacy Icons", GlobalSettings.enableLegacyIcons));
			}
			tabs.addTab("Display", boxAll);
			
			boxAll = new Box(BoxLayout.Y_AXIS);
			{
				dbSettings = new DatabaseSettings();
				dbSettings.load();
	
				// Get a list of the avalable drivers
				List<Object> driverList = new ArrayList<Object>();
				Enumeration<Driver> drivers = DriverManager.getDrivers();
				while(drivers.hasMoreElements()) {
					Driver d = drivers.nextElement();
					if(d instanceof DriverShim)
						driverList.add(((DriverShim)d).getDriverClass().getName());
					else
						driverList.add(d.getClass().getName());
				}
				
				cmbDrivers = ConfigFactory.makeCombo("Driver", driverList.toArray(), false, boxAll);
				cmbDrivers.setSelectedItem(dbSettings.driver);
				txtDriverURL = ConfigFactory.makeText("URL", dbSettings.url, boxAll);
				txtDriverUsername = ConfigFactory.makeText("Username", dbSettings.username, boxAll);
				txtDriverPassword = ConfigFactory.makeText("Password", dbSettings.password, boxAll);
				txtDriverSchema = ConfigFactory.makeText("Schema", dbSettings.schema, boxAll);
			}
			tabs.addTab("Database", boxAll);
	
			boxAll = new Box(BoxLayout.Y_AXIS);
			{
				boxAll.add(chkEnableDebug = new ConfigCheckBox("Enable debug logging", Out.isDebug()));
				boxAll.add(chkDebugToGui = new ConfigCheckBox("Log debug messages on the GUI", Out.isDebugToGui()));
				
				Properties props = Out.getProperties();
				chkDebug = new ArrayList<ConfigCheckBox>(props.size());
				for (Enumeration<Object> en = props.keys(); en.hasMoreElements();) {
					String clazz = en.nextElement().toString();
					boolean chkEnabled = Boolean.parseBoolean(props.getProperty(clazz));
					ConfigCheckBox chk = new ConfigCheckBox(clazz, chkEnabled);
					chkDebug.add(chk);
					boxAll.add(chk);
				}
			}
			tabs.addTab("Debug", boxAll);
		}
		
		if(selecedTab != null)
			tabs.setSelectedIndex(selecedTab);
		
		Box boxButtons = new Box(BoxLayout.X_AXIS);
		{
			btnLoad = new JButton("Undo");
			btnLoad.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent act) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							load();
						}
					});
				}
			});

			btnOK = new JButton("OK");
			btnOK.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent act) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							try {
								save();
								dispose();
							} catch(Exception e) {
								JOptionPane.showMessageDialog(
										null,
										e.getClass().getName() + "\n" + e.getMessage(),
										"The configuration is invalid",
										JOptionPane.ERROR_MESSAGE);
							}
						}
					});
				}
			});

			btnCancel = new JButton("Cancel");
			btnCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent act) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							cancel();
						}
					});
				}
			});

			btnApply = new JButton("Apply");
			btnApply.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent act) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							save();
						}
					});
				}
			});

			boxButtons.add(Box.createHorizontalGlue());
			boxButtons.add(btnLoad);
			boxButtons.add(Box.createHorizontalStrut(50));
			boxButtons.add(btnCancel);
			boxButtons.add(btnOK);
			boxButtons.add(btnApply);
		}
		boxTabsAndButtons.add(boxButtons);
		
		pack();
	}

	private void save() {
		if(!keysOnly) {
			// Save database info
			dbSettings.driver = (String)cmbDrivers.getSelectedItem();
			dbSettings.url = txtDriverURL.getText();
			dbSettings.username = txtDriverUsername.getText();
			dbSettings.password = txtDriverPassword.getText();
			dbSettings.schema = txtDriverSchema.getText();
			dbSettings.save();
			
			// Save global settings
			GlobalSettings.bnlsServer = txtBNLSServer.getText();
			GlobalSettings.email = txtEmail.getText();
			GlobalSettings.colorScheme = (byte)(cmbColorScheme.getSelectedIndex() + 1);
			TimeFormatter.tsFormat = (String)cmbTSFormat.getSelectedItem();
			GlobalSettings.bnUserToString = cmbBNUserToString.getSelectedIndex();
			GlobalSettings.bnUserToStringUserList = cmbBNUserToStringUserList.getSelectedIndex();
			GlobalSettings.releaseType = (ReleaseType)cmbReleaseType.getSelectedItem();
			GlobalSettings.autoConnect = chkAutoConnect.isSelected();
			GlobalSettings.enableMirrorSelector = chkEnableMirrorSelector.isSelected();
			GlobalSettings.displayBattleNetMOTD = chkDisplayBattleNetMOTD.isSelected();
			GlobalSettings.displayBattleNetChannels = chkDisplayBattleNetChannels.isSelected();
			GlobalSettings.displayJoinParts = chkDisplayJoinParts.isSelected();
			GlobalSettings.displayChannelUsers = chkDisplayChannelUsers.isSelected();
			GlobalSettings.enableGUI = chkEnableGUI.isSelected();
			GlobalSettings.trayIconMode = (TrayIconMode)cmbTrayIconMode.getSelectedItem();
			GlobalSettings.trayDisplayConnectDisconnect = chkTrayDisplayConnectDisconnect.isSelected();
			GlobalSettings.trayDisplayChannel = chkTrayDisplayChannel.isSelected();
			GlobalSettings.trayDisplayJoinPart = chkTrayDisplayJoinPart.isSelected();
			GlobalSettings.trayDisplayChatEmote = chkTrayDisplayChatEmote.isSelected();
			GlobalSettings.trayDisplayWhisper = chkTrayDisplayWhisper.isSelected();
			GlobalSettings.tabCompleteMode = (TabCompleteMode)cmbTabCompleteMode.getSelectedItem();
			GlobalSettings.enableLegacyIcons = chkEnableLegacyIcons.isSelected();
			GlobalSettings.enableCLI = chkEnableCLI.isSelected();
			GlobalSettings.enableTrivia = chkEnableTrivia.isSelected();
			GlobalSettings.triviaRoundLength = Integer.parseInt(txtTriviaRoundLength.getText());
			GlobalSettings.enableCommands = chkEnableCommands.isSelected();
			GlobalSettings.enableHTMLOutput = chkEnableHTMLOutput.isSelected();
			GlobalSettings.enableFloodProtect = chkEnableFloodProtect.isSelected();
			GlobalSettings.packetLog = chkPacketLog.isSelected();
			GlobalSettings.whisperBack = chkWhisperBack.isSelected();
			
			// Save debug
			Out.setDebug(chkEnableDebug.isSelected());
			Out.setDebugToGui(chkDebugToGui.isSelected());
			for(ConfigCheckBox chk : chkDebug)
				Out.setDebug(chk.getText(), chk.isSelected());
	
			// Save
			GlobalSettings.save();
		}
		
		// Save CD keys
		try {
			FileWriter fw = new FileWriter(new File("cdkeys.txt"));
			fw.write(txtCDKeys.getText());
			fw.close();

			KeyManager.resetInitialized();
			initializeGui();
			validate();
		} catch (IOException e) {
			Out.exception(e);
		}
	}

	private void loadCDKeys() {
		String keys = null;
		try {
			File f = new File("cdkeys.txt");
			BufferedReader br = new BufferedReader(new FileReader(f));
			while(true) {
				String l = br.readLine();
				if(l == null)
					break;
				if(keys == null)
					keys = l;
				else
					keys += "\n" + l;
			}
		} catch (Exception e) {
			Out.exception(e);
		}
		txtCDKeys.setText(keys);
	}

	private void load() {
		loadCDKeys();
		
		if(!keysOnly) {
			// Load database info
			dbSettings.load();
			cmbDrivers.setSelectedItem(dbSettings.driver);
			txtDriverURL.setText(dbSettings.url);
			txtDriverUsername.setText(dbSettings.username);
			txtDriverPassword.setText(dbSettings.password);
			txtDriverSchema.setText(dbSettings.schema);
			
			// Load global settings
			GlobalSettings.load();
			txtBNLSServer.setText(GlobalSettings.bnlsServer);
			txtEmail.setText(GlobalSettings.email);
			cmbColorScheme.setSelectedIndex(GlobalSettings.colorScheme - 1);
			cmbTSFormat.setSelectedItem(TimeFormatter.tsFormat);
			cmbBNUserToString.setSelectedIndex(GlobalSettings.bnUserToString);
			cmbBNUserToStringUserList.setSelectedIndex(GlobalSettings.bnUserToStringUserList);
			cmbReleaseType.setSelectedItem(GlobalSettings.releaseType);
			chkAutoConnect.setSelected(GlobalSettings.autoConnect);
			chkEnableMirrorSelector.setSelected(GlobalSettings.enableMirrorSelector);
			chkEnableGUI.setSelected(GlobalSettings.enableGUI);
			chkEnableLegacyIcons.setSelected(GlobalSettings.enableLegacyIcons);
			chkEnableCLI.setSelected(GlobalSettings.enableCLI);
			cmbTrayIconMode.setSelectedItem(GlobalSettings.trayIconMode);
			chkTrayDisplayConnectDisconnect.setSelected(GlobalSettings.trayDisplayConnectDisconnect);
			chkTrayDisplayChannel.setSelected(GlobalSettings.trayDisplayChannel);
			chkTrayDisplayJoinPart.setSelected(GlobalSettings.trayDisplayJoinPart);
			chkTrayDisplayChatEmote.setSelected(GlobalSettings.trayDisplayChatEmote);
			chkTrayDisplayWhisper.setSelected(GlobalSettings.trayDisplayWhisper);
			cmbTabCompleteMode.setSelectedItem(GlobalSettings.tabCompleteMode);
			chkEnableTrivia.setSelected(GlobalSettings.enableTrivia);
			txtTriviaRoundLength.setText(Long.toString(GlobalSettings.triviaRoundLength));
			chkEnableCommands.setSelected(GlobalSettings.enableCommands);
			chkEnableHTMLOutput.setSelected(GlobalSettings.enableHTMLOutput);
			chkEnableFloodProtect.setSelected(GlobalSettings.enableFloodProtect);
			chkPacketLog.setSelected(GlobalSettings.packetLog);
			chkWhisperBack.setSelected(GlobalSettings.whisperBack);
			
			// Load debug
			chkDebugToGui.setSelected(Out.isDebugToGui());
			chkEnableDebug.setSelected(Out.isDebug());
			for(ConfigCheckBox chk : chkDebug)
				chk.setSelected(Out.isDebug(chk.getText()));
		}
	}

	private void cancel() {
		pressedCancel = true;
		load();
		dispose();
	}
}
