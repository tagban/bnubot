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

import net.bnubot.bot.database.DriverShim;
import net.bnubot.bot.gui.components.ConfigCheckBox;
import net.bnubot.bot.gui.components.ConfigComboBox;
import net.bnubot.bot.gui.components.ConfigTextArea;
import net.bnubot.settings.ConnectionSettings;
import net.bnubot.settings.DatabaseSettings;
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
	ConfigComboBox cmbTSFormat = null;
	ConfigTextArea txtBNLSServer = null;
	ConfigComboBox cmbReleaseType = null;
	ConfigComboBox cmbColorScheme = null;
	ConfigComboBox cmbLookAndFeel = null;
	ConfigCheckBox chkAutoConnect = null;
	ConfigCheckBox chkEnableGUI = null;
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
	JButton btnOK = null;
	JButton btnCancel = null;

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
	private static Dimension maxSize = new Dimension(lblWidth, 0);

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
		int lblWidth = 100;
		Dimension maxSize = new Dimension(lblWidth, 0);

		tabs = new JTabbedPane();

		Box boxAll = new Box(BoxLayout.Y_AXIS);
		{
			Box boxSettings = new Box(BoxLayout.Y_AXIS);
			{
				txtBNLSServer = makeText("BNLS Server", ConnectionSettings.bnlsServer, boxSettings);
				
				txtTrigger = makeText("Trigger", ConnectionSettings.trigger, boxSettings);

				Box boxLine = new Box(BoxLayout.X_AXIS);
				{
					JLabel jl = new JLabel("Anti-Idle");
					jl.setPreferredSize(maxSize);
					boxLine.add(jl);

					chkAntiIdle = new ConfigCheckBox("Enable", ConnectionSettings.enableAntiIdle);
					boxLine.add(chkAntiIdle);

					txtAntiIdle = new ConfigTextArea(ConnectionSettings.antiIdle);
					boxLine.add(txtAntiIdle);
				}
				boxSettings.add(boxLine);

				txtAntiIdleTimer = makeText("Anti-Idle Timer", Integer.toString(ConnectionSettings.antiIdleTimer), boxSettings);

				Object[] values = { TimeFormatter.tsFormat, "%1$tH:%1$tM:%1$tS.%1$tL", "%1$tH:%1$tM:%1$tS", "%1$tH:%1$tM" };
				cmbTSFormat = makeCombo("TimeStamp Format", values, true, boxSettings);
				cmbTSFormat.setSelectedItem(TimeFormatter.tsFormat);

				if(CurrentVersion.version().getReleaseType().isDevelopment())
					values = new ReleaseType[] {
						ReleaseType.Development };
				else
					values = new ReleaseType[] {
						ReleaseType.Stable,
						ReleaseType.ReleaseCandidate,
						ReleaseType.Beta,
						ReleaseType.Alpha };
				cmbReleaseType = makeCombo("Version Check", values, false, boxSettings);
				cmbReleaseType.setSelectedItem(ConnectionSettings.releaseType);

				values = new String[] { "Starcraft", "Diablo 2" };
				cmbColorScheme = makeCombo("Color Scheme", values, false, boxSettings);
				cmbColorScheme.setSelectedIndex(ConnectionSettings.colorScheme - 1);

				ArrayList<String> lafs = new ArrayList<String>();
				for(LookAndFeelInfo lafi : UIManager.getInstalledLookAndFeels())
					lafs.add(lafi.getName());
				cmbLookAndFeel = makeCombo("Look and Feel", lafs.toArray(), false, boxSettings);
				cmbLookAndFeel.setSelectedItem(ConnectionSettings.getLookAndFeel());
				cmbLookAndFeel.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent e) {
						for(LookAndFeelInfo lafi : UIManager.getInstalledLookAndFeels())
							if(lafi.getName().equals(cmbLookAndFeel.getSelectedItem()))
								ConnectionSettings.setLookAndFeel(lafi);
					}
				});

				boxLine = new Box(BoxLayout.X_AXIS);
				{
					boxLine.add(Box.createRigidArea(maxSize));

					Box boxCheckboxes = new Box(BoxLayout.Y_AXIS);
					{
						chkAutoConnect = new ConfigCheckBox("Auto Connect", ConnectionSettings.autoconnect);
						boxCheckboxes.add(chkAutoConnect);

						chkEnableGUI = new ConfigCheckBox("Enable GUI (requires restart)", ConnectionSettings.enableGUI);
						boxCheckboxes.add(chkEnableGUI);

						chkEnableLegacyIcons = new ConfigCheckBox("Enable Legacy Icons", ConnectionSettings.enableLegacyIcons);
						boxCheckboxes.add(chkEnableLegacyIcons);

						chkEnableCLI = new ConfigCheckBox("Enable CLI (requires restart)", ConnectionSettings.enableCLI);
						boxCheckboxes.add(chkEnableCLI);

						chkEnableTrivia = new ConfigCheckBox("Enable Trivia (requires restart)", ConnectionSettings.enableTrivia);
						boxCheckboxes.add(chkEnableTrivia);

						JLabel jl = new JLabel("Trivia Round Length");
						boxCheckboxes.add(jl);

						txtTriviaRoundLength = new ConfigTextArea(Long.toString(ConnectionSettings.triviaRoundLength));
						boxCheckboxes.add(txtTriviaRoundLength);

						chkEnableCommands = new ConfigCheckBox("Enable Commands (requires restart)", ConnectionSettings.enableCommands);
						boxCheckboxes.add(chkEnableCommands);

						chkEnableFloodProtect = new ConfigCheckBox("Enable Flood Protect", ConnectionSettings.enableFloodProtect);
						boxCheckboxes.add(chkEnableFloodProtect);

						chkPacketLog = new ConfigCheckBox("Packet Log", ConnectionSettings.packetLog);
						boxCheckboxes.add(chkPacketLog);

						chkWhisperBack = new ConfigCheckBox("Whisper Commands", ConnectionSettings.whisperBack);
						boxCheckboxes.add(chkWhisperBack);
					}
					boxLine.add(boxCheckboxes);

					boxLine.add(Box.createHorizontalGlue());
				}
				boxSettings.add(boxLine);
				boxAll.add(Box.createVerticalGlue());
			}
			boxAll.add(boxSettings);
			boxAll.add(Box.createVerticalStrut(10));

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

				boxButtons.add(Box.createHorizontalGlue());
				boxButtons.add(btnLoad);
				boxButtons.add(Box.createHorizontalStrut(50));
				boxButtons.add(btnOK);
				boxButtons.add(btnCancel);
			}
			boxAll.add(boxButtons);
		}
		tabs.addTab("Settings", boxAll);

		boxAll = new Box(BoxLayout.Y_AXIS);
		{
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

			txtCDKeys = new ConfigTextArea(keys);
			boxAll.add(new JScrollPane(txtCDKeys));

			btnSaveKeys = new JButton("Save");
			btnSaveKeys.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
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
					});
				}
			});
			boxAll.add(btnSaveKeys);
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

			btnSaveDatabase = new JButton("Save");
			btnSaveDatabase.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					dbSettings.driver = (String)cmbDrivers.getSelectedItem();
					dbSettings.url = txtDriverURL.getText();
					dbSettings.username = txtDriverUsername.getText();
					dbSettings.password = txtDriverPassword.getText();
					dbSettings.schema = txtDriverSchema.getText();
					dbSettings.save();
				}
			});
			boxAll.add(btnSaveDatabase);
		}
		tabs.addTab("Database", boxAll);

		add(tabs);
		pack();

		Dimension size = this.getSize();
		if((size.height > 500) || (size.width > 400))
			this.setSize(Math.min(400, size.width), Math.min(500, size.height));
	}

	private void save() {
		ConnectionSettings.bnlsServer = txtBNLSServer.getText();
		ConnectionSettings.trigger = txtTrigger.getText();
		ConnectionSettings.antiIdle = txtAntiIdle.getText();
		ConnectionSettings.antiIdleTimer = Integer.parseInt(txtAntiIdleTimer.getText());
		ConnectionSettings.enableAntiIdle = chkAntiIdle.isSelected();
		ConnectionSettings.colorScheme = (byte)(cmbColorScheme.getSelectedIndex() + 1);
		TimeFormatter.tsFormat = (String)cmbTSFormat.getSelectedItem();
		ConnectionSettings.releaseType = (ReleaseType)cmbReleaseType.getSelectedItem();
		ConnectionSettings.autoconnect = chkAutoConnect.isSelected();
		ConnectionSettings.enableGUI = chkEnableGUI.isSelected();
		ConnectionSettings.enableLegacyIcons = chkEnableLegacyIcons.isSelected();
		ConnectionSettings.enableCLI = chkEnableCLI.isSelected();
		ConnectionSettings.enableTrivia = chkEnableTrivia.isSelected();
		ConnectionSettings.triviaRoundLength = Integer.parseInt(txtTriviaRoundLength.getText());
		ConnectionSettings.enableCommands = chkEnableCommands.isSelected();
		ConnectionSettings.enableFloodProtect = chkEnableFloodProtect.isSelected();
		ConnectionSettings.packetLog = chkPacketLog.isSelected();
		ConnectionSettings.whisperBack = chkWhisperBack.isSelected();

		ConnectionSettings.globalSave();
	}

	private void load() {
		ConnectionSettings.globalLoad();
		txtBNLSServer.setText(ConnectionSettings.bnlsServer);
		txtTrigger.setText(ConnectionSettings.trigger);
		txtAntiIdle.setText(ConnectionSettings.antiIdle);
		txtAntiIdleTimer.setText(Integer.toString(ConnectionSettings.antiIdleTimer));
		chkAntiIdle.setSelected(ConnectionSettings.enableAntiIdle);
		cmbColorScheme.setSelectedIndex(ConnectionSettings.colorScheme - 1);
		cmbTSFormat.setSelectedItem(TimeFormatter.tsFormat);
		cmbReleaseType.setSelectedItem(ConnectionSettings.releaseType);
		chkAutoConnect.setSelected(ConnectionSettings.autoconnect);
		chkEnableGUI.setSelected(ConnectionSettings.enableGUI);
		chkEnableLegacyIcons.setSelected(ConnectionSettings.enableLegacyIcons);
		chkEnableCLI.setSelected(ConnectionSettings.enableCLI);
		chkEnableTrivia.setSelected(ConnectionSettings.enableTrivia);
		txtTriviaRoundLength.setText(Long.toString(ConnectionSettings.triviaRoundLength));
		chkEnableCommands.setSelected(ConnectionSettings.enableCommands);
		chkEnableFloodProtect.setSelected(ConnectionSettings.enableFloodProtect);
		chkPacketLog.setSelected(ConnectionSettings.packetLog);
		chkWhisperBack.setSelected(ConnectionSettings.whisperBack);
	}

	private void cancel() {
		load();
		close();
	}

	private void close() {
		String v = ConnectionSettings.isValidGlobal();
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
