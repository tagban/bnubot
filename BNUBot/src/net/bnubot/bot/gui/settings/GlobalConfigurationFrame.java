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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.bnubot.bot.gui.GuiEventHandler;
import net.bnubot.bot.gui.KeyManager;
import net.bnubot.bot.gui.WindowPosition;
import net.bnubot.bot.gui.components.ConfigPanel;
import net.bnubot.bot.gui.components.ConfigSpinner;
import net.bnubot.bot.gui.components.ConfigTextArea;
import net.bnubot.bot.gui.components.ConfigTextField;
import net.bnubot.bot.gui.components.TextWindow;
import net.bnubot.core.EventHandler;
import net.bnubot.core.PluginManager;
import net.bnubot.logging.Out;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.settings.GlobalSettings.SOCKSType;
import net.bnubot.settings.GlobalSettings.TabCompleteMode;
import net.bnubot.settings.GlobalSettings.TrayIconMode;
import net.bnubot.settings.Settings;
import net.bnubot.util.TimeFormatter;
import net.bnubot.vercheck.CurrentVersion;
import net.bnubot.vercheck.ReleaseType;

/**
 * @author scotta
 */
public class GlobalConfigurationFrame extends JDialog {
	private static final long serialVersionUID = 1308177934480442149L;

	private JTabbedPane tabs = null;
	private boolean keysOnly;
	private boolean pressedCancel = false;

	// CDKeys
	private ConfigTextArea txtCDKeys = null;

	// Settings
	private ConfigTextField txtEmail = null;
	private ConfigTextField txtBNLSServer = null;
	private JComboBox<ReleaseType> cmbReleaseType = null;
	private JComboBox<String> cmbTimeZone = null;
	private JCheckBox chkAutoConnect = null;
	private JCheckBox chkAutoRejoin = null;
	private JCheckBox chkEnableMirrorSelector = null;
	private JCheckBox chkEnableFloodProtect = null;
	private JCheckBox chkPacketLog = null;

	// Proxy
	private JCheckBox chkProxyEnabled = null;
	private JComboBox<SOCKSType> cmbProxyType = null;
	private ConfigTextField txtProxyHost = null;
	private ConfigSpinner spnProxyPort = null;

	// Plugins
	private ConfigSpinner spnTriviaRoundLength = null;
	private JCheckBox chkWhisperBack = null;
	private List<Class<? extends EventHandler>> plugins = null;
	private List<JCheckBox> chkEnabledPlugins = null;

	// Display 1
	private JComboBox<String> cmbBNUserToString = null;
	private JComboBox<String> cmbBNUserToStringUserList = null;
	private JComboBox<String> cmbBNUserToStringCommandResponse = null;
	private JComboBox<TrayIconMode> cmbTrayIconMode = null;
	private JCheckBox chkTrayMinimizeTo = null;
	private JCheckBox chkTrayDisplayConnectDisconnect = null;
	private JCheckBox chkTrayDisplayChannel = null;
	private JCheckBox chkTrayDisplayJoinPart = null;
	private JCheckBox chkTrayDisplayChatEmote = null;
	private JCheckBox chkTrayDisplayWhisper = null;
	private JComboBox<TabCompleteMode> cmbTabCompleteMode = null;
	private JCheckBox chkEnableTabCompleteUser = null;
	private JCheckBox chkEnableTabCompleteCommand = null;

	// Display 2
	private JCheckBox chkEnableLegacyIcons = null;
	private JComboBox<String> cmbTSFormat = null;
	private JComboBox<String> cmbColorScheme = null;
	private JComboBox<Object> cmbLookAndFeel = null;
	private JComboBox<String> cmbPlasticTheme = null;
	private JCheckBox chkDisplayBattleNetMOTD = null;
	private JCheckBox chkDisplayBattleNetChannels = null;
	private JCheckBox chkDisplayJoinParts = null;
	private JCheckBox chkDisplayChannelUsers = null;
	private JCheckBox chkDisplaySlashCommands = null;
	private ConfigTextField txtGuiFontFamily = null;
	private ConfigSpinner spnGuiFontSize = null;

	// Debug
	private JCheckBox chkEnableDebug = null;
	private List<JCheckBox> chkDebug = null;

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
		ConfigPanel boxAll = new ConfigPanel();
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

			boxAll = new ConfigPanel();
			{
				txtBNLSServer = boxAll.makeText("BNLS Server", GlobalSettings.bnlsServer);
				txtEmail = boxAll.makeText("Email", GlobalSettings.email);

				ReleaseType[] releaseTypes;
				if(CurrentVersion.fromJar())
					releaseTypes = new ReleaseType[] {
						ReleaseType.Stable,
						ReleaseType.ReleaseCandidate,
						ReleaseType.Beta,
						ReleaseType.Alpha,
						ReleaseType.Nightly };
				else
					releaseTypes = new ReleaseType[] {
						ReleaseType.Development };
				cmbReleaseType = boxAll.makeCombo("Version Check", releaseTypes, false);
				cmbReleaseType.setSelectedItem(GlobalSettings.releaseType);

				String[] values = TimeZone.getAvailableIDs();
				Arrays.sort(values);
				cmbTimeZone = boxAll.makeCombo("TimeZone", values, false);
				cmbTimeZone.setSelectedItem(TimeFormatter.timeZone.getID());

				chkAutoConnect = boxAll.makeCheck("Auto Connect", GlobalSettings.autoConnect);
				chkAutoRejoin = boxAll.makeCheck("Auto Rejoin After Kicked", GlobalSettings.autoRejoin);
				chkEnableMirrorSelector = boxAll.makeCheck("Enable Mirror Selector", GlobalSettings.enableMirrorSelector);
				chkEnableFloodProtect = boxAll.makeCheck("Enable Flood Protect", GlobalSettings.enableFloodProtect);
				chkPacketLog = boxAll.makeCheck("Packet Log", GlobalSettings.packetLog);
			}
			tabs.addTab("Settings", boxAll);

			boxAll = new ConfigPanel();
			{
				chkProxyEnabled = boxAll.makeCheck("Enabled", GlobalSettings.socksEnabled);
				cmbProxyType = boxAll.makeCombo("Type", GlobalSettings.SOCKSType.values(), false);
				txtProxyHost = boxAll.makeText("Host", GlobalSettings.socksHost);
				spnProxyPort = boxAll.makeSpinner("Port", GlobalSettings.socksPort);
			}
			tabs.addTab("Proxy", boxAll);

			boxAll = new ConfigPanel();
			{
				boxAll.add(new JLabel("You must reopen profiles for these changes to take effect."));
				chkWhisperBack = boxAll.makeCheck("Whisper Command Responses", GlobalSettings.whisperBack);

				plugins = new ArrayList<Class<? extends EventHandler>>();
				chkEnabledPlugins = new ArrayList<JCheckBox>();
				for(Class<? extends EventHandler> plugin : PluginManager.getPlugins()) {
					// Do not allow the user to disable the GUI in this way
					if(plugin == GuiEventHandler.class)
						continue;

					JCheckBox chkEnablePlugin = boxAll.makeCheck(plugin.getSimpleName(), PluginManager.isEnabled(plugin));
					chkEnabledPlugins.add(chkEnablePlugin);
					plugins.add(plugin);
				}
				spnTriviaRoundLength = boxAll.makeSpinner("Trivia Round Length", Integer.valueOf(GlobalSettings.triviaRoundLength));
			}
			tabs.addTab("Plugins", boxAll);

			boxAll = new ConfigPanel();
			{
				String[] values = new String[] {
					"BNLogin@Gateway",
					"BNLogin",
					"ShortPrefix Account",
					"Prefix Account (BNLogin)",
					"Account",
					"Account (BNLogin)" };
				cmbBNUserToString = boxAll.makeCombo("BNetUser.toString()", values, false);
				cmbBNUserToString.setSelectedIndex(GlobalSettings.bnUserToString);
				cmbBNUserToStringUserList = boxAll.makeCombo("User List", values, false);
				cmbBNUserToStringUserList.setSelectedIndex(GlobalSettings.bnUserToStringUserList);
				cmbBNUserToStringCommandResponse = boxAll.makeCombo("Command Response", values, false);
				cmbBNUserToStringCommandResponse.setSelectedIndex(GlobalSettings.bnUserToStringCommandResponse);

				cmbTrayIconMode = boxAll.makeCombo("Tray Icon", TrayIconMode.values(), false);
				cmbTrayIconMode.setSelectedItem(GlobalSettings.trayIconMode);

				chkTrayMinimizeTo = boxAll.makeCheck("Minimize To System Tray (Java 6+)", GlobalSettings.trayMinimizeTo);
				chkTrayDisplayConnectDisconnect = boxAll.makeCheck("Tray: Connect/Disconnect", GlobalSettings.trayDisplayConnectDisconnect);
				chkTrayDisplayChannel = boxAll.makeCheck("Tray: Channel", GlobalSettings.trayDisplayChannel);
				chkTrayDisplayJoinPart = boxAll.makeCheck("Tray: Join/Part", GlobalSettings.trayDisplayJoinPart);
				chkTrayDisplayChatEmote = boxAll.makeCheck("Tray: Chat/Emote", GlobalSettings.trayDisplayChatEmote);
				chkTrayDisplayWhisper = boxAll.makeCheck("Tray: Whisper", GlobalSettings.trayDisplayWhisper);

				cmbTabCompleteMode = boxAll.makeCombo("Tab Complete Mode", TabCompleteMode.values(), false);
				cmbTabCompleteMode.setSelectedItem(GlobalSettings.tabCompleteMode);

				chkEnableTabCompleteUser = boxAll.makeCheck("User Tab Completion", GlobalSettings.enableTabCompleteUser);
				chkEnableTabCompleteCommand = boxAll.makeCheck("Command Tab Completion", GlobalSettings.enableTabCompleteCommand);
			}
			tabs.addTab("Display 1", boxAll);

			boxAll = new ConfigPanel();
			{
				chkEnableLegacyIcons = boxAll.makeCheck("Enable Legacy Icons", GlobalSettings.enableLegacyIcons);

				String[] values = new String[] { TimeFormatter.tsFormat, "%1$tH:%1$tM:%1$tS.%1$tL", "%1$tH:%1$tM:%1$tS", "%1$tH:%1$tM" };
				cmbTSFormat = boxAll.makeCombo("TimeStamp", values, true);
				cmbTSFormat.setSelectedItem(TimeFormatter.tsFormat);

				values = new String[] { "Starcraft", "Diablo 2", "Invigoration" };
				cmbColorScheme = boxAll.makeCombo("Color Scheme", values, false);
				cmbColorScheme.setSelectedIndex(GlobalSettings.colorScheme - 1);

				ArrayList<String> lafs = new ArrayList<String>();
				for(LookAndFeelInfo lafi : UIManager.getInstalledLookAndFeels())
					lafs.add(lafi.getName());
				cmbLookAndFeel = boxAll.makeCombo("Look and Feel", lafs.toArray(), false);
				cmbLookAndFeel.setSelectedItem(GlobalSettings.getLookAndFeel());
				cmbLookAndFeel.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						for(LookAndFeelInfo lafi : UIManager.getInstalledLookAndFeels())
							if(lafi.getName().equals(cmbLookAndFeel.getSelectedItem()))
								GlobalSettings.setLookAndFeel(lafi);
					}
				});

				cmbPlasticTheme = boxAll.makeCombo("Plastic Theme", GlobalSettings.getLookAndFeelThemes(), false);
				cmbPlasticTheme.setSelectedItem(GlobalSettings.getLookAndFeelTheme());
				cmbPlasticTheme.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						GlobalSettings.setLookAndFeelTheme(cmbPlasticTheme.getSelectedItem().toString());
					}
				});

				chkDisplayBattleNetMOTD = boxAll.makeCheck("Display Battle.net MOTD", GlobalSettings.displayBattleNetMOTD);
				chkDisplayBattleNetChannels = boxAll.makeCheck("Display Battle.net Channels", GlobalSettings.displayBattleNetChannels);
				chkDisplayJoinParts = boxAll.makeCheck("Display Join/Part Messages", GlobalSettings.getDisplayJoinParts());
				chkDisplayChannelUsers = boxAll.makeCheck("Display Channel Users On Join", GlobalSettings.displayChannelUsers);
				chkDisplaySlashCommands = boxAll.makeCheck("Display / Commands", GlobalSettings.displaySlashCommands);
				txtGuiFontFamily = boxAll.makeText("GUI Font Family", GlobalSettings.guiFontFamily);
				spnGuiFontSize = boxAll.makeSpinner("GUI Font Size", Integer.valueOf(GlobalSettings.guiFontSize));
			}
			tabs.addTab("Display 2", boxAll);

			boxAll = new ConfigPanel();
			{
				chkEnableDebug = boxAll.makeCheck("Enable debug logging", Out.isDebug());

				Properties props = Out.getProperties();
				chkDebug = new ArrayList<JCheckBox>(props.size());
				for (Enumeration<Object> en = props.keys(); en.hasMoreElements();) {
					String clazz = en.nextElement().toString();
					boolean chkEnabled = Boolean.parseBoolean(props.getProperty(clazz));
					JCheckBox chk = boxAll.makeCheck(clazz, chkEnabled);
					chkDebug.add(chk);
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
				@Override
				public void actionPerformed(ActionEvent act) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							load();
						}
					});
				}
			});

			btnOK = new JButton("OK");
			btnOK.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent act) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
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
				@Override
				public void actionPerformed(ActionEvent act) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							cancel();
						}
					});
				}
			});

			btnApply = new JButton("Apply");
			btnApply.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent act) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
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
			TimeFormatter.timeZone = TimeZone.getTimeZone((String)cmbTimeZone.getSelectedItem());
			GlobalSettings.autoConnect = chkAutoConnect.isSelected();
			GlobalSettings.autoRejoin = chkAutoRejoin.isSelected();
			GlobalSettings.enableMirrorSelector = chkEnableMirrorSelector.isSelected();
			GlobalSettings.displayBattleNetMOTD = chkDisplayBattleNetMOTD.isSelected();
			GlobalSettings.displayBattleNetChannels = chkDisplayBattleNetChannels.isSelected();
			GlobalSettings.setDisplayJoinParts(chkDisplayJoinParts.isSelected());
			GlobalSettings.displayChannelUsers = chkDisplayChannelUsers.isSelected();
			GlobalSettings.displaySlashCommands = chkDisplaySlashCommands.isSelected();
			GlobalSettings.guiFontFamily = txtGuiFontFamily.getText();
			GlobalSettings.guiFontSize = spnGuiFontSize.getValue().intValue();
			TextWindow.resetHead();
			GlobalSettings.socksEnabled = chkProxyEnabled.isSelected();
			GlobalSettings.socksType = (SOCKSType)cmbProxyType.getSelectedItem();
			GlobalSettings.socksHost = txtProxyHost.getText();
			GlobalSettings.socksPort = spnProxyPort.getValue().intValue();
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
			GlobalSettings.triviaRoundLength = spnTriviaRoundLength.getValue().intValue();

			for(int i = 0; i < plugins.size(); i++) {
				Class<? extends EventHandler> plugin = plugins.get(i);
				JCheckBox chkEnablePlugin = chkEnabledPlugins.get(i);
				PluginManager.setEnabled(plugin, chkEnablePlugin.isSelected());
			}
			GlobalSettings.enableFloodProtect = chkEnableFloodProtect.isSelected();
			GlobalSettings.packetLog = chkPacketLog.isSelected();
			GlobalSettings.whisperBack = chkWhisperBack.isSelected();

			// Save debug
			Out.setDebug(chkEnableDebug.isSelected());
			for(JCheckBox chk : chkDebug)
				Out.setDebug(chk.getText(), chk.isSelected());

			// Save
			GlobalSettings.save();
		}

		// Save CD keys
		try {
			FileWriter fw = new FileWriter(Settings.keysFile);
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
		try (BufferedReader br = new BufferedReader(new FileReader(Settings.keysFile))) {
			KeyManager.initialize();
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
			cmbTimeZone.setSelectedItem(TimeFormatter.timeZone.getID());
			chkAutoConnect.setSelected(GlobalSettings.autoConnect);
			chkAutoRejoin.setSelected(GlobalSettings.autoRejoin);
			chkEnableMirrorSelector.setSelected(GlobalSettings.enableMirrorSelector);
			chkDisplayBattleNetMOTD.setSelected(GlobalSettings.displayBattleNetMOTD);
			chkDisplayBattleNetChannels.setSelected(GlobalSettings.displayBattleNetChannels);
			chkDisplayJoinParts.setSelected(GlobalSettings.getDisplayJoinParts());
			chkDisplayChannelUsers.setSelected(GlobalSettings.displayChannelUsers);
			chkDisplaySlashCommands.setSelected(GlobalSettings.displaySlashCommands);
			txtGuiFontFamily.setText(GlobalSettings.guiFontFamily);
			spnGuiFontSize.setValue(Integer.valueOf(GlobalSettings.guiFontSize));
			chkEnableLegacyIcons.setSelected(GlobalSettings.enableLegacyIcons);
			chkEnableTabCompleteUser.setSelected(GlobalSettings.enableTabCompleteUser);
			chkEnableTabCompleteCommand.setSelected(GlobalSettings.enableTabCompleteCommand);
			chkProxyEnabled.setSelected(GlobalSettings.socksEnabled);
			cmbProxyType.setSelectedItem(GlobalSettings.socksType);
			txtProxyHost.setText(GlobalSettings.socksHost);
			spnProxyPort.setValue(GlobalSettings.socksPort);
			cmbTrayIconMode.setSelectedItem(GlobalSettings.trayIconMode);
			chkTrayMinimizeTo.setSelected(GlobalSettings.trayMinimizeTo);
			chkTrayDisplayConnectDisconnect.setSelected(GlobalSettings.trayDisplayConnectDisconnect);
			chkTrayDisplayChannel.setSelected(GlobalSettings.trayDisplayChannel);
			chkTrayDisplayJoinPart.setSelected(GlobalSettings.trayDisplayJoinPart);
			chkTrayDisplayChatEmote.setSelected(GlobalSettings.trayDisplayChatEmote);
			chkTrayDisplayWhisper.setSelected(GlobalSettings.trayDisplayWhisper);
			cmbTabCompleteMode.setSelectedItem(GlobalSettings.tabCompleteMode);
			spnTriviaRoundLength.setValue(Integer.valueOf(GlobalSettings.triviaRoundLength));
			for(int i = 0; i < plugins.size(); i++) {
				Class<? extends EventHandler> plugin = plugins.get(i);
				JCheckBox chkEnablePlugin = chkEnabledPlugins.get(i);
				chkEnablePlugin.setSelected(PluginManager.isEnabled(plugin));
			}
			chkEnableFloodProtect.setSelected(GlobalSettings.enableFloodProtect);
			chkPacketLog.setSelected(GlobalSettings.packetLog);
			chkWhisperBack.setSelected(GlobalSettings.whisperBack);

			// Load debug
			chkEnableDebug.setSelected(Out.isDebug());
			for(JCheckBox chk : chkDebug)
				chk.setSelected(Out.isDebug(chk.getText()));
		}
	}

	private void cancel() {
		pressedCancel = true;
		load();
		dispose();
	}
}
