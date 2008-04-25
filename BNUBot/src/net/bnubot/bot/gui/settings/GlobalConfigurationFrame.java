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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.bnubot.bot.gui.KeyManager;
import net.bnubot.bot.gui.WindowPosition;
import net.bnubot.bot.gui.components.ConfigCheckBox;
import net.bnubot.bot.gui.components.ConfigFactory;
import net.bnubot.bot.gui.components.ConfigSpinner;
import net.bnubot.bot.gui.components.ConfigTextArea;
import net.bnubot.bot.gui.components.TextWindow;
import net.bnubot.core.EventHandler;
import net.bnubot.core.PluginManager;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.settings.GlobalSettings.TabCompleteMode;
import net.bnubot.settings.GlobalSettings.TrayIconMode;
import net.bnubot.util.Out;
import net.bnubot.util.TimeFormatter;
import net.bnubot.vercheck.CurrentVersion;
import net.bnubot.vercheck.ReleaseType;

public class GlobalConfigurationFrame extends JDialog {
	private static final long serialVersionUID = 1308177934480442149L;

	private JTabbedPane tabs = null;
	private boolean keysOnly;
	private boolean pressedCancel = false;

	// CDKeys
	private ConfigTextArea txtCDKeys = null;

	// Settings
	private ConfigTextArea txtEmail = null;
	private ConfigTextArea txtBNLSServer = null;
	private JComboBox cmbReleaseType = null;
	private ConfigCheckBox chkAutoConnect = null;
	private ConfigCheckBox chkAutoRejoin = null;
	private ConfigCheckBox chkEnableMirrorSelector = null;
	private ConfigCheckBox chkEnableFloodProtect = null;
	private ConfigCheckBox chkPacketLog = null;

	// Plugins
	private ConfigSpinner spnTriviaRoundLength = null;
	private ConfigCheckBox chkEnableCLI = null;
	private ConfigCheckBox chkEnableCommands = null;
	private ConfigCheckBox chkWhisperBack = null;
	private List<Class<? extends EventHandler>> plugins = null;
	private List<ConfigCheckBox> chkEnabledPlugins = null;

	// Display 1
	private JComboBox cmbBNUserToString = null;
	private JComboBox cmbBNUserToStringUserList = null;
	private JComboBox cmbBNUserToStringCommandResponse = null;
	private JComboBox cmbTrayIconMode = null;
	private ConfigCheckBox chkTrayMinimizeTo = null;
	private ConfigCheckBox chkTrayDisplayConnectDisconnect = null;
	private ConfigCheckBox chkTrayDisplayChannel = null;
	private ConfigCheckBox chkTrayDisplayJoinPart = null;
	private ConfigCheckBox chkTrayDisplayChatEmote = null;
	private ConfigCheckBox chkTrayDisplayWhisper = null;
	private JComboBox cmbTabCompleteMode = null;
	private ConfigCheckBox chkEnableTabCompleteUser = null;
	private ConfigCheckBox chkEnableTabCompleteCommand = null;

	// Display 2
	private ConfigCheckBox chkEnableLegacyIcons = null;
	private JComboBox cmbTSFormat = null;
	private JComboBox cmbColorScheme = null;
	private JComboBox cmbLookAndFeel = null;
	private JComboBox cmbPlasticTheme = null;
	private ConfigCheckBox chkDisplayBattleNetMOTD = null;
	private ConfigCheckBox chkDisplayBattleNetChannels = null;
	private ConfigCheckBox chkDisplayJoinParts = null;
	private ConfigCheckBox chkDisplayChannelUsers = null;
	private ConfigTextArea txtGuiFontFamily = null;
	private ConfigSpinner spnGuiFontSize = null;

	// Debug
	private ConfigCheckBox chkEnableDebug = null;
	private ConfigCheckBox chkDebugToGui = null;
	private List<ConfigCheckBox> chkDebug = null;

	// Buttons
	private JButton btnUndo = null;
	private JButton btnCancel = null;
	private JButton btnOK = null;
	private JButton btnApply = null;

	public GlobalConfigurationFrame(boolean keysOnly) throws OperationCancelledException {
		super();
		if(keysOnly)
			setTitle("CD Key Editor");
		else
			setTitle("Configuration");

		this.keysOnly = keysOnly;
		initializeGui();

		setModal(true);
		setResizable(false);
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
						ReleaseType.Alpha,
						ReleaseType.Nightly };
				else
					values = new ReleaseType[] {
						ReleaseType.Development };
				cmbReleaseType = ConfigFactory.makeCombo("Version Check", values, false, boxAll);
				cmbReleaseType.setSelectedItem(GlobalSettings.releaseType);

				boxAll.add(chkAutoConnect = new ConfigCheckBox("Auto Connect", GlobalSettings.autoConnect));
				boxAll.add(chkAutoRejoin = new ConfigCheckBox("Auto Rejoin After Kicked", GlobalSettings.autoRejoin));
				boxAll.add(chkEnableMirrorSelector = new ConfigCheckBox("Enable Mirror Selector", GlobalSettings.enableMirrorSelector));
				boxAll.add(chkEnableFloodProtect = new ConfigCheckBox("Enable Flood Protect", GlobalSettings.enableFloodProtect));
				boxAll.add(chkPacketLog = new ConfigCheckBox("Packet Log", GlobalSettings.packetLog));
			}
			tabs.addTab("Settings", boxAll);

			boxAll = new Box(BoxLayout.Y_AXIS);
			{
				boxAll.add(new JLabel("You must reopen profiles for these changes to take effect."));
				boxAll.add(chkEnableCLI = new ConfigCheckBox("Enable Command Line Interface", GlobalSettings.enableCLI));
				boxAll.add(chkEnableCommands = new ConfigCheckBox("Enable Commands", GlobalSettings.enableCommands));
				boxAll.add(chkWhisperBack = new ConfigCheckBox("Whisper Command Responses", GlobalSettings.whisperBack));

				plugins = PluginManager.getPlugins();
				chkEnabledPlugins = new ArrayList<ConfigCheckBox>(plugins.size());
				for(int i = 0; i < plugins.size(); i++) {
					Class<? extends EventHandler> plugin = plugins.get(i);

					ConfigCheckBox chkEnablePlugin = new ConfigCheckBox(plugin.getSimpleName(), PluginManager.isEnabled(plugin));
					boxAll.add(chkEnablePlugin);
					chkEnabledPlugins.add(chkEnablePlugin);
				}
				spnTriviaRoundLength = ConfigFactory.makeSpinner("Trivia Round Length", new Integer(GlobalSettings.triviaRoundLength), boxAll);
			}
			tabs.addTab("Plugins", boxAll);

			boxAll = new Box(BoxLayout.Y_AXIS);
			{
				Object[] values = new String[] {
					"BNLogin@Gateway",
					"BNLogin",
					"Prefix Account",
					"Prefix Account (BNLogin)",
					"Account",
					"Account (BNLogin)" };
				cmbBNUserToString = ConfigFactory.makeCombo("BNetUser.toString()", values, false, boxAll);
				cmbBNUserToString.setSelectedIndex(GlobalSettings.bnUserToString);
				cmbBNUserToStringUserList = ConfigFactory.makeCombo("User List", values, false, boxAll);
				cmbBNUserToStringUserList.setSelectedIndex(GlobalSettings.bnUserToStringUserList);
				cmbBNUserToStringCommandResponse = ConfigFactory.makeCombo("Command Response", values, false, boxAll);
				cmbBNUserToStringCommandResponse.setSelectedIndex(GlobalSettings.bnUserToStringCommandResponse);

				cmbTrayIconMode = ConfigFactory.makeCombo("Tray Icon", TrayIconMode.values(), false, boxAll);
				cmbTrayIconMode.setSelectedItem(GlobalSettings.trayIconMode);

				boxAll.add(chkTrayMinimizeTo = new ConfigCheckBox("Minimize To System Tray (Java 6+)", GlobalSettings.trayMinimizeTo));
				boxAll.add(chkTrayDisplayConnectDisconnect = new ConfigCheckBox("Tray: Connect/Disconnect", GlobalSettings.trayDisplayConnectDisconnect));
				boxAll.add(chkTrayDisplayChannel = new ConfigCheckBox("Tray: Channel", GlobalSettings.trayDisplayChannel));
				boxAll.add(chkTrayDisplayJoinPart = new ConfigCheckBox("Tray: Join/Part", GlobalSettings.trayDisplayJoinPart));
				boxAll.add(chkTrayDisplayChatEmote = new ConfigCheckBox("Tray: Chat/Emote", GlobalSettings.trayDisplayChatEmote));
				boxAll.add(chkTrayDisplayWhisper = new ConfigCheckBox("Tray: Whisper", GlobalSettings.trayDisplayWhisper));

				cmbTabCompleteMode = ConfigFactory.makeCombo("Tab Complete Mode", TabCompleteMode.values(), false, boxAll);
				cmbTabCompleteMode.setSelectedItem(GlobalSettings.tabCompleteMode);

				boxAll.add(chkEnableTabCompleteUser = new ConfigCheckBox("User Tab Completion", GlobalSettings.enableTabCompleteUser));
				boxAll.add(chkEnableTabCompleteCommand = new ConfigCheckBox("Command Tab Completion", GlobalSettings.enableTabCompleteCommand));
			}
			tabs.addTab("Display 1", boxAll);

			boxAll = new Box(BoxLayout.Y_AXIS);
			{
				boxAll.add(chkEnableLegacyIcons = new ConfigCheckBox("Enable Legacy Icons", GlobalSettings.enableLegacyIcons));

				Object[] values = new String[] { TimeFormatter.tsFormat, "%1$tH:%1$tM:%1$tS.%1$tL", "%1$tH:%1$tM:%1$tS", "%1$tH:%1$tM" };
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
				txtGuiFontFamily = ConfigFactory.makeText("GUI Font Family", GlobalSettings.guiFontFamily, boxAll);
				spnGuiFontSize = ConfigFactory.makeSpinner("GUI Font Size", new Integer(GlobalSettings.guiFontSize), boxAll);
			}
			tabs.addTab("Display 2", boxAll);

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
			btnUndo = new JButton("Undo");
			btnUndo.addActionListener(new ActionListener() {
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
			boxButtons.add(btnUndo);
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
			// Save global settings
			GlobalSettings.bnlsServer = txtBNLSServer.getText();
			GlobalSettings.email = txtEmail.getText();
			GlobalSettings.colorScheme = (byte)(cmbColorScheme.getSelectedIndex() + 1);
			TimeFormatter.tsFormat = (String)cmbTSFormat.getSelectedItem();
			GlobalSettings.bnUserToString = cmbBNUserToString.getSelectedIndex();
			GlobalSettings.bnUserToStringUserList = cmbBNUserToStringUserList.getSelectedIndex();
			GlobalSettings.bnUserToStringCommandResponse = cmbBNUserToStringCommandResponse.getSelectedIndex();
			GlobalSettings.releaseType = (ReleaseType)cmbReleaseType.getSelectedItem();
			GlobalSettings.autoConnect = chkAutoConnect.isSelected();
			GlobalSettings.autoRejoin = chkAutoRejoin.isSelected();
			GlobalSettings.enableMirrorSelector = chkEnableMirrorSelector.isSelected();
			GlobalSettings.displayBattleNetMOTD = chkDisplayBattleNetMOTD.isSelected();
			GlobalSettings.displayBattleNetChannels = chkDisplayBattleNetChannels.isSelected();
			GlobalSettings.displayJoinParts = chkDisplayJoinParts.isSelected();
			GlobalSettings.displayChannelUsers = chkDisplayChannelUsers.isSelected();
			GlobalSettings.guiFontFamily = txtGuiFontFamily.getText();
			GlobalSettings.guiFontSize = spnGuiFontSize.getValue().intValue();
			TextWindow.resetHead();
			GlobalSettings.trayIconMode = (TrayIconMode)cmbTrayIconMode.getSelectedItem();
			GlobalSettings.trayMinimizeTo = chkTrayMinimizeTo.isSelected();
			GlobalSettings.trayDisplayConnectDisconnect = chkTrayDisplayConnectDisconnect.isSelected();
			GlobalSettings.trayDisplayChannel = chkTrayDisplayChannel.isSelected();
			GlobalSettings.trayDisplayJoinPart = chkTrayDisplayJoinPart.isSelected();
			GlobalSettings.trayDisplayChatEmote = chkTrayDisplayChatEmote.isSelected();
			GlobalSettings.trayDisplayWhisper = chkTrayDisplayWhisper.isSelected();
			GlobalSettings.tabCompleteMode = (TabCompleteMode)cmbTabCompleteMode.getSelectedItem();
			GlobalSettings.enableLegacyIcons = chkEnableLegacyIcons.isSelected();
			GlobalSettings.enableTabCompleteUser = chkEnableTabCompleteUser.isSelected();
			GlobalSettings.enableTabCompleteCommand = chkEnableTabCompleteCommand.isSelected();
			GlobalSettings.enableCLI = chkEnableCLI.isSelected();
			GlobalSettings.triviaRoundLength = spnTriviaRoundLength.getValue().intValue();
			GlobalSettings.enableCommands = chkEnableCommands.isSelected();
			for(int i = 0; i < plugins.size(); i++) {
				Class<? extends EventHandler> plugin = plugins.get(i);
				ConfigCheckBox chkEnablePlugin = chkEnabledPlugins.get(i);
				PluginManager.setEnabled(plugin, chkEnablePlugin.isSelected());
			}
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
			// Load global settings
			GlobalSettings.load();
			txtBNLSServer.setText(GlobalSettings.bnlsServer);
			txtEmail.setText(GlobalSettings.email);
			cmbColorScheme.setSelectedIndex(GlobalSettings.colorScheme - 1);
			cmbTSFormat.setSelectedItem(TimeFormatter.tsFormat);
			cmbBNUserToString.setSelectedIndex(GlobalSettings.bnUserToString);
			cmbBNUserToStringUserList.setSelectedIndex(GlobalSettings.bnUserToStringUserList);
			cmbBNUserToStringCommandResponse.setSelectedIndex(GlobalSettings.bnUserToStringCommandResponse);
			cmbReleaseType.setSelectedItem(GlobalSettings.releaseType);
			chkAutoConnect.setSelected(GlobalSettings.autoConnect);
			chkAutoRejoin.setSelected(GlobalSettings.autoRejoin);
			chkEnableMirrorSelector.setSelected(GlobalSettings.enableMirrorSelector);
			chkDisplayBattleNetMOTD.setSelected(GlobalSettings.displayBattleNetMOTD);
			chkDisplayBattleNetChannels.setSelected(GlobalSettings.displayBattleNetChannels);
			chkDisplayJoinParts.setSelected(GlobalSettings.displayJoinParts);
			chkDisplayChannelUsers.setSelected(GlobalSettings.displayChannelUsers);
			txtGuiFontFamily.setText(GlobalSettings.guiFontFamily);
			spnGuiFontSize.setValue(new Integer(GlobalSettings.guiFontSize));
			chkEnableLegacyIcons.setSelected(GlobalSettings.enableLegacyIcons);
			chkEnableTabCompleteUser.setSelected(GlobalSettings.enableTabCompleteUser);
			chkEnableTabCompleteCommand.setSelected(GlobalSettings.enableTabCompleteCommand);
			chkEnableCLI.setSelected(GlobalSettings.enableCLI);
			cmbTrayIconMode.setSelectedItem(GlobalSettings.trayIconMode);
			chkTrayMinimizeTo.setSelected(GlobalSettings.trayMinimizeTo);
			chkTrayDisplayConnectDisconnect.setSelected(GlobalSettings.trayDisplayConnectDisconnect);
			chkTrayDisplayChannel.setSelected(GlobalSettings.trayDisplayChannel);
			chkTrayDisplayJoinPart.setSelected(GlobalSettings.trayDisplayJoinPart);
			chkTrayDisplayChatEmote.setSelected(GlobalSettings.trayDisplayChatEmote);
			chkTrayDisplayWhisper.setSelected(GlobalSettings.trayDisplayWhisper);
			cmbTabCompleteMode.setSelectedItem(GlobalSettings.tabCompleteMode);
			spnTriviaRoundLength.setValue(new Integer(GlobalSettings.triviaRoundLength));
			chkEnableCommands.setSelected(GlobalSettings.enableCommands);
			for(int i = 0; i < plugins.size(); i++) {
				Class<? extends EventHandler> plugin = plugins.get(i);
				ConfigCheckBox chkEnablePlugin = chkEnabledPlugins.get(i);
				chkEnablePlugin.setSelected(PluginManager.isEnabled(plugin));
			}
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
