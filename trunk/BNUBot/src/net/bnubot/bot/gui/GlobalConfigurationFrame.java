/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui;

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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.bnubot.bot.database.DriverShim;
import net.bnubot.bot.gui.components.ConfigCheckBox;
import net.bnubot.bot.gui.components.ConfigComboBox;
import net.bnubot.bot.gui.components.ConfigTextArea;
import net.bnubot.settings.DatabaseSettings;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.Out;
import net.bnubot.util.TimeFormatter;
import net.bnubot.vercheck.CurrentVersion;
import net.bnubot.vercheck.ReleaseType;

public class GlobalConfigurationFrame extends JDialog {
	private static final long serialVersionUID = 1308177934480442149L;

	JTabbedPane tabs = null;

	//Settings
	ConfigCheckBox chkAntiIdle = null;
	ConfigTextArea txtAntiIdle = null;
	ConfigTextArea txtAntiIdleTimer = null;
	ConfigTextArea txtTrigger = null;
	ConfigTextArea txtEmail = null;
	ConfigComboBox cmbTSFormat = null;
	ConfigTextArea txtBNLSServer = null;
	ConfigComboBox cmbReleaseType = null;
	ConfigComboBox cmbColorScheme = null;
	ConfigComboBox cmbLookAndFeel = null;
	ConfigComboBox cmbPlasticTheme = null;
	ConfigCheckBox chkAutoConnect = null;
	ConfigCheckBox chkDisplayBattleNetMOTD = null;
	ConfigCheckBox chkDisplayBattleNetChannels = null;
	ConfigCheckBox chkDisplayJoinParts = null;
	ConfigCheckBox chkDisplayChannelUsers = null;
	ConfigCheckBox chkEnableGUI = null;
	ConfigCheckBox chkEnableTrayIcon = null;
	ConfigCheckBox chkEnableTrayPopups = null;
	ConfigCheckBox chkEnableTrayPopupsAlways = null;
	ConfigCheckBox chkEnableLegacyIcons = null;
	ConfigCheckBox chkEnableCLI = null;
	ConfigCheckBox chkEnableTrivia = null;
	ConfigTextArea txtTriviaRoundLength = null;
	ConfigCheckBox chkEnableCommands = null;
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

	public GlobalConfigurationFrame() {
		super();
		setTitle("Configuration");

		initializeGui();

		setModal(true);
	}

	private static final int lblWidth = 100;
	private static final Dimension maxSize = new Dimension(lblWidth, 0);

	private ConfigTextArea makeText(String label, String value, Box parent) {
		JLabel jl = new JLabel(label);
		jl.setPreferredSize(maxSize);

		ConfigTextArea txt = new ConfigTextArea(value);

		Box boxLine = new Box(BoxLayout.X_AXIS);
		boxLine.add(jl);
		boxLine.add(txt);
		parent.add(boxLine);

		return txt;
	}

	private ConfigComboBox makeCombo(String label, Object[] values, boolean editable, Box parent) {
		JLabel jl = new JLabel(label);
		jl.setPreferredSize(maxSize);

		ConfigComboBox cmb = new ConfigComboBox(values);
		cmb.setEditable(editable);

		Box boxLine = new Box(BoxLayout.X_AXIS);
		boxLine.add(jl);
		boxLine.add(cmb);
		parent.add(boxLine);

		return cmb;
	}

	private void initializeGui() {
		Box boxTabsAndButtons = new Box(BoxLayout.Y_AXIS);
		add(boxTabsAndButtons);

		tabs = new JTabbedPane();
		boxTabsAndButtons.add(tabs);
		
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
								close();
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

		Box boxAll = new Box(BoxLayout.Y_AXIS);
		{
			txtBNLSServer = makeText("BNLS Server", GlobalSettings.bnlsServer, boxAll);
			txtTrigger = makeText("Trigger", GlobalSettings.trigger, boxAll);
			txtEmail = makeText("Email", GlobalSettings.email, boxAll);

			Box boxLine = new Box(BoxLayout.X_AXIS);
			{
				JLabel jl = new JLabel("Anti-Idle");
				jl.setPreferredSize(maxSize);
				boxLine.add(jl);
				boxLine.add(chkAntiIdle = new ConfigCheckBox("Enable", GlobalSettings.enableAntiIdle));
				boxLine.add(txtAntiIdle = new ConfigTextArea(GlobalSettings.antiIdle));
			}
			boxAll.add(boxLine);

			txtAntiIdleTimer = makeText("Anti-Idle Timer", Integer.toString(GlobalSettings.antiIdleTimer), boxAll);

			Object[] values;
			if(CurrentVersion.version().getReleaseType().isDevelopment())
				values = new ReleaseType[] {
					ReleaseType.Development };
			else
				values = new ReleaseType[] {
					ReleaseType.Stable,
					ReleaseType.ReleaseCandidate,
					ReleaseType.Beta,
					ReleaseType.Alpha };
			cmbReleaseType = makeCombo("Version Check", values, false, boxAll);
			cmbReleaseType.setSelectedItem(GlobalSettings.releaseType);

			boxAll.add(chkAutoConnect = new ConfigCheckBox("Auto Connect", GlobalSettings.autoConnect));
			boxAll.add(chkEnableFloodProtect = new ConfigCheckBox("Enable Flood Protect", GlobalSettings.enableFloodProtect));
			boxAll.add(chkPacketLog = new ConfigCheckBox("Packet Log", GlobalSettings.packetLog));
			boxAll.add(chkWhisperBack = new ConfigCheckBox("Whisper Commands", GlobalSettings.whisperBack));
			boxAll.add(Box.createVerticalGlue());
		}
		tabs.addTab("Settings", boxAll);

		boxAll = new Box(BoxLayout.Y_AXIS);
		{
			boxAll.add(chkEnableTrivia = new ConfigCheckBox("Enable Trivia (requires restart)", GlobalSettings.enableTrivia));
			txtTriviaRoundLength = makeText("Trivia Round Length", Long.toString(GlobalSettings.triviaRoundLength), boxAll);
			boxAll.add(chkEnableCLI = new ConfigCheckBox("Enable CLI (requires restart)", GlobalSettings.enableCLI));
			boxAll.add(chkEnableCommands = new ConfigCheckBox("Enable Commands (requires restart)", GlobalSettings.enableCommands));
		}
		tabs.addTab("Plugins", boxAll);

		boxAll = new Box(BoxLayout.Y_AXIS);
		{
			Object[] values = { TimeFormatter.tsFormat, "%1$tH:%1$tM:%1$tS.%1$tL", "%1$tH:%1$tM:%1$tS", "%1$tH:%1$tM" };
			cmbTSFormat = makeCombo("TimeStamp Format", values, true, boxAll);
			cmbTSFormat.setSelectedItem(TimeFormatter.tsFormat);

			values = new String[] { "Starcraft", "Diablo 2" };
			cmbColorScheme = makeCombo("Color Scheme", values, false, boxAll);
			cmbColorScheme.setSelectedIndex(GlobalSettings.colorScheme - 1);

			ArrayList<String> lafs = new ArrayList<String>();
			for(LookAndFeelInfo lafi : UIManager.getInstalledLookAndFeels())
				lafs.add(lafi.getName());
			cmbLookAndFeel = makeCombo("Look and Feel", lafs.toArray(), false, boxAll);
			cmbLookAndFeel.setSelectedItem(GlobalSettings.getLookAndFeel());
			cmbLookAndFeel.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					for(LookAndFeelInfo lafi : UIManager.getInstalledLookAndFeels())
						if(lafi.getName().equals(cmbLookAndFeel.getSelectedItem()))
							GlobalSettings.setLookAndFeel(lafi);
				}
			});
			
			cmbPlasticTheme = makeCombo("Plastic Theme", GlobalSettings.getLookAndFeelThemes(), false, boxAll);
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

			chkEnableGUI = new ConfigCheckBox("Enable GUI (requires restart)", GlobalSettings.enableGUI);
			chkEnableGUI.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent arg0) {
					if(!chkEnableGUI.isSelected()) {
						chkEnableTrayPopupsAlways.setSelected(false);
						chkEnableTrayPopups.setSelected(false);
						chkEnableTrayIcon.setSelected(false);
					}
				}});
			boxAll.add(chkEnableGUI);

			chkEnableTrayIcon = new ConfigCheckBox("Enable Tray Icon", GlobalSettings.enableTrayIcon);
			chkEnableTrayIcon.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent arg0) {
					if(chkEnableTrayIcon.isSelected())
						chkEnableGUI.setSelected(true);
					else {
						chkEnableTrayPopupsAlways.setSelected(false);
						chkEnableTrayPopups.setSelected(false);
					}
				}});
			boxAll.add(chkEnableTrayIcon);

			chkEnableTrayPopups = new ConfigCheckBox("Enable Tray Popups", GlobalSettings.enableTrayPopups);
			chkEnableTrayPopups.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent arg0) {
					if(chkEnableTrayPopups.isSelected()) {
						chkEnableGUI.setSelected(true);
						chkEnableTrayIcon.setSelected(true);
					} else {
						chkEnableTrayPopupsAlways.setSelected(false);
					}
				}});
			boxAll.add(chkEnableTrayPopups);

			chkEnableTrayPopupsAlways = new ConfigCheckBox("Enable Tray Popups When Focused", GlobalSettings.enableTrayPopupsAlways);
			chkEnableTrayPopupsAlways.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent arg0) {
					if(chkEnableTrayPopups.isSelected()) {
						chkEnableGUI.setSelected(true);
						chkEnableTrayIcon.setSelected(true);
						chkEnableTrayPopups.setSelected(true);
					}
				}});
			boxAll.add(chkEnableTrayPopupsAlways);
			
			boxAll.add(chkEnableLegacyIcons = new ConfigCheckBox("Enable Legacy Icons", GlobalSettings.enableLegacyIcons));
		}
		tabs.addTab("Display", boxAll);

		boxAll = new Box(BoxLayout.Y_AXIS);
		{
			txtCDKeys = new ConfigTextArea(null);
			loadCDKeys();
			boxAll.add(new JScrollPane(txtCDKeys));
		}
		tabs.addTab("CD Keys", boxAll);

		boxAll = new Box(BoxLayout.Y_AXIS);
		{
			dbSettings = new DatabaseSettings();
			dbSettings.load();

			Box boxLine = new Box(BoxLayout.X_AXIS);
			{
				JLabel jl = new JLabel("Driver");
				jl.setPreferredSize(maxSize);
				boxLine.add(jl);

				DefaultComboBoxModel model = new DefaultComboBoxModel();
				Enumeration<Driver> drivers = DriverManager.getDrivers();
				while(drivers.hasMoreElements()) {
					Driver d = drivers.nextElement();
					if(d instanceof DriverShim)
						model.addElement(((DriverShim)d).getDriverClass().getName());
				}

				cmbDrivers = new ConfigComboBox(model);
				cmbDrivers.setSelectedItem(dbSettings.driver);
				boxLine.add(cmbDrivers);
			}
			boxAll.add(boxLine);

			txtDriverURL = makeText("URL", dbSettings.url, boxAll);
			txtDriverUsername = makeText("Username", dbSettings.username, boxAll);
			txtDriverPassword = makeText("Password", dbSettings.password, boxAll);
			txtDriverSchema = makeText("Schema", dbSettings.schema, boxAll);
		}
		tabs.addTab("Database", boxAll);
		
		pack();

		Dimension size = this.getSize();
		if((size.height > 350) || (size.width > 400))
			this.setSize(Math.min(400, size.width), Math.min(350, size.height));
	}

	private void save() {
		// Save database info
		dbSettings.driver = (String)cmbDrivers.getSelectedItem();
		dbSettings.url = txtDriverURL.getText();
		dbSettings.username = txtDriverUsername.getText();
		dbSettings.password = txtDriverPassword.getText();
		dbSettings.schema = txtDriverSchema.getText();
		dbSettings.save();
		
		// Save global settings
		GlobalSettings.bnlsServer = txtBNLSServer.getText();
		GlobalSettings.trigger = txtTrigger.getText();
		GlobalSettings.email = txtEmail.getText();
		GlobalSettings.antiIdle = txtAntiIdle.getText();
		GlobalSettings.antiIdleTimer = Integer.parseInt(txtAntiIdleTimer.getText());
		GlobalSettings.enableAntiIdle = chkAntiIdle.isSelected();
		GlobalSettings.colorScheme = (byte)(cmbColorScheme.getSelectedIndex() + 1);
		TimeFormatter.tsFormat = (String)cmbTSFormat.getSelectedItem();
		GlobalSettings.releaseType = (ReleaseType)cmbReleaseType.getSelectedItem();
		GlobalSettings.autoConnect = chkAutoConnect.isSelected();
		GlobalSettings.displayBattleNetMOTD = chkDisplayBattleNetMOTD.isSelected();
		GlobalSettings.displayBattleNetChannels = chkDisplayBattleNetChannels.isSelected();
		GlobalSettings.displayJoinParts = chkDisplayJoinParts.isSelected();
		GlobalSettings.displayChannelUsers = chkDisplayChannelUsers.isSelected();
		GlobalSettings.enableGUI = chkEnableGUI.isSelected();
		GlobalSettings.enableTrayIcon = chkEnableTrayIcon.isSelected();
		GlobalSettings.enableTrayPopups = chkEnableTrayPopups.isSelected();
		GlobalSettings.enableTrayPopupsAlways = chkEnableTrayPopupsAlways.isSelected();
		GlobalSettings.enableLegacyIcons = chkEnableLegacyIcons.isSelected();
		GlobalSettings.enableCLI = chkEnableCLI.isSelected();
		GlobalSettings.enableTrivia = chkEnableTrivia.isSelected();
		GlobalSettings.triviaRoundLength = Integer.parseInt(txtTriviaRoundLength.getText());
		GlobalSettings.enableCommands = chkEnableCommands.isSelected();
		GlobalSettings.enableFloodProtect = chkEnableFloodProtect.isSelected();
		GlobalSettings.packetLog = chkPacketLog.isSelected();
		GlobalSettings.whisperBack = chkWhisperBack.isSelected();
		GlobalSettings.save();
		
		// Save CD keys
		try {
			FileWriter fw = new FileWriter(new File("cdkeys.txt"));
			fw.write(txtCDKeys.getText());
			fw.close();

			KeyManager.resetInitialized();
			remove(tabs);
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
		txtTrigger.setText(GlobalSettings.trigger);
		txtEmail.setText(GlobalSettings.email);
		txtAntiIdle.setText(GlobalSettings.antiIdle);
		txtAntiIdleTimer.setText(Integer.toString(GlobalSettings.antiIdleTimer));
		chkAntiIdle.setSelected(GlobalSettings.enableAntiIdle);
		cmbColorScheme.setSelectedIndex(GlobalSettings.colorScheme - 1);
		cmbTSFormat.setSelectedItem(TimeFormatter.tsFormat);
		cmbReleaseType.setSelectedItem(GlobalSettings.releaseType);
		chkAutoConnect.setSelected(GlobalSettings.autoConnect);
		chkEnableGUI.setSelected(GlobalSettings.enableGUI);
		chkEnableLegacyIcons.setSelected(GlobalSettings.enableLegacyIcons);
		chkEnableCLI.setSelected(GlobalSettings.enableCLI);
		chkEnableTrayIcon.setSelected(GlobalSettings.enableTrayIcon);
		chkEnableTrayPopups.setSelected(GlobalSettings.enableTrayPopups);
		chkEnableTrayPopupsAlways.setSelected(GlobalSettings.enableTrayPopupsAlways);
		chkEnableTrivia.setSelected(GlobalSettings.enableTrivia);
		txtTriviaRoundLength.setText(Long.toString(GlobalSettings.triviaRoundLength));
		chkEnableCommands.setSelected(GlobalSettings.enableCommands);
		chkEnableFloodProtect.setSelected(GlobalSettings.enableFloodProtect);
		chkPacketLog.setSelected(GlobalSettings.packetLog);
		chkWhisperBack.setSelected(GlobalSettings.whisperBack);
	}

	private void cancel() {
		load();
		close();
	}

	private void close() {
		String v = GlobalSettings.isValid();
		if(v == null) {
			dispose();
		} else {
			JOptionPane.showMessageDialog(
					null,
					v,
					"The configuration is invalid",
					JOptionPane.ERROR_MESSAGE);
		}
	}
}
