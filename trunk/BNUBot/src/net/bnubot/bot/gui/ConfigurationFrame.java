/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.bot.gui;

import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
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
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.bnubot.bot.database.DatabaseSettings;
import net.bnubot.bot.database.DriverShim;
import net.bnubot.bot.gui.KeyManager.CDKey;
import net.bnubot.core.ConnectionSettings;
import net.bnubot.util.Out;
import net.bnubot.util.TimeFormatter;
import net.bnubot.vercheck.ReleaseType;

public class ConfigurationFrame extends JDialog {
	private static final long serialVersionUID = 1308177934480442149L;

	ConnectionSettings cs;
	
	JTabbedPane tabs = null;
	
	//Connection
	ConfigTextArea txtUsername = null;
	JPasswordField txtPassword = null;
	ConfigTextArea txtEmail = null;
	ConfigComboBox cmbProduct = null;
	ConfigTextArea txtTrigger = null;
	ConfigCheckBox chkAntiIdle = null;
	ConfigTextArea txtAntiIdle = null;
	ConfigTextArea txtAntiIdleTimer = null;
	ConfigComboBox cmbCDKey = null;
	ConfigComboBox cmbCDKeyLOD = null;
	ConfigComboBox cmbCDKeyTFT = null;
	ConfigTextArea txtBNCSServer = null;
	ConfigTextArea txtBNLSServer = null;
	ConfigTextArea txtChannel = null;
	ConfigComboBox cmbTSFormat = null;
	ConfigComboBox cmbReleaseType = null;
	ConfigComboBox cmbColorScheme = null;
	ConfigComboBox cmbLookAndFeel = null;
	ConfigCheckBox chkAutoConnect = null;
	ConfigCheckBox chkEnableGUI = null;
	ConfigCheckBox chkEnableCLI = null;
	ConfigCheckBox chkEnableTrivia = null;
	ConfigTextArea txtTriviaRoundLength = null;
	ConfigCheckBox chkEnableCommands = null;
	ConfigCheckBox chkEnableFloodProtect = null;
	ConfigCheckBox chkPacketLog = null;
	ConfigCheckBox chkWhisperBack = null;
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
	
	private class ConfigTextArea extends JTextArea {
		private static final long serialVersionUID = -2894805163754230265L;
		
		public ConfigTextArea(String text) {
			super(text);
			setBorder(BorderFactory.createLoweredBevelBorder());
			
			// Enable tab key for focus traversal
			// http://forum.java.sun.com/thread.jspa?threadID=283320&messageID=2194505
			setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
			setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		}
	}
	
	private class ConfigComboBox extends JComboBox {
		private static final long serialVersionUID = 4793810467982453882L;
		
		public ConfigComboBox(Object[] items) {
			super(items);
			setBorder(BorderFactory.createLoweredBevelBorder());
		}
		
		public ConfigComboBox(ComboBoxModel model) {
			super(model);
			setBorder(BorderFactory.createLoweredBevelBorder());
		}
	}
	
	private class ConfigCheckBox extends JCheckBox {
		private static final long serialVersionUID = 1831878850976738056L;

		public ConfigCheckBox(String text, boolean checked) {
			super(text, checked);
			setBorder(BorderFactory.createLoweredBevelBorder());
		}
	}
	
	public ConfigurationFrame(ConnectionSettings cs) {
		super();
		this.cs = cs;
		setTitle("Configuration");
		
		initializeGui();
		
		setModal(true);
	}
	
	private void initializeGui() {
		int lblWidth = 100;
		Dimension maxSize = new Dimension(lblWidth, 0);
		
		tabs = new JTabbedPane();
		
		Box boxAll = new Box(BoxLayout.Y_AXIS);
		boolean addConnectionStuff = true;
		ConnectionStuff: {
			Box boxSettings = new Box(BoxLayout.Y_AXIS);
			{				
				Box boxLine = new Box(BoxLayout.X_AXIS);
				{
					JLabel jl = new JLabel("Username");
					jl.setPreferredSize(maxSize);
					boxLine.add(jl);
					
					txtUsername = new ConfigTextArea(cs.username);
					boxLine.add(txtUsername);
				}
				boxSettings.add(boxLine);

				boxLine = new Box(BoxLayout.X_AXIS);
				{
					JLabel jl = new JLabel("Password");
					jl.setPreferredSize(maxSize);
					boxLine.add(jl);
					
					txtPassword = new JPasswordField(cs.password);
					boxLine.add(txtPassword);
				}
				boxSettings.add(boxLine);

				boxLine = new Box(BoxLayout.X_AXIS);
				{
					JLabel jl = new JLabel("Email");
					jl.setPreferredSize(maxSize);
					boxLine.add(jl);
					
					txtEmail = new ConfigTextArea(cs.email);
					boxLine.add(txtEmail);
				}
				boxSettings.add(boxLine);

				boxLine = new Box(BoxLayout.X_AXIS);
				{
					JLabel jl = new JLabel("Trigger");
					jl.setPreferredSize(maxSize);
					boxLine.add(jl);
					
					txtTrigger = new ConfigTextArea(ConnectionSettings.trigger);
					boxLine.add(txtTrigger);
				}
				boxSettings.add(boxLine);

				boxLine = new Box(BoxLayout.X_AXIS);
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

				boxLine = new Box(BoxLayout.X_AXIS);
				{
					JLabel jl = new JLabel("Anti-Idle Timer");
					jl.setPreferredSize(maxSize);
					boxLine.add(jl);
					
					txtAntiIdleTimer = new ConfigTextArea(Integer.toString(ConnectionSettings.antiIdleTimer));
					boxLine.add(txtAntiIdleTimer);
				}
				boxSettings.add(boxLine);

				boxLine = new Box(BoxLayout.X_AXIS);
				{	
					CDKey[] CDKeys = KeyManager.getKeys(KeyManager.PRODUCT_ALLNORMAL);
					if(CDKeys.length == 0) {
						JOptionPane.showMessageDialog(this,
								"You have no CD keys in cdkeys.txt.",
								"Error",
								JOptionPane.ERROR_MESSAGE);
						addConnectionStuff = false;
						break ConnectionStuff;
					}
					
					JLabel jl = new JLabel("CD Key");
					jl.setPreferredSize(maxSize);
					boxLine.add(jl);
					
					cmbCDKey = new ConfigComboBox(CDKeys);
					boxLine.add(cmbCDKey);
					
					for(int i = 0; i < CDKeys.length; i++) {
						if(CDKeys[i].getKey().equals(cs.cdkey))
							cmbCDKey.setSelectedIndex(i);
					}
				}
				boxSettings.add(boxLine);

				boxLine = new Box(BoxLayout.X_AXIS);
				{
					CDKey[] CDKeys = KeyManager.getKeys(KeyManager.PRODUCT_D2XP);
					
					JLabel jl = new JLabel("LOD Key");
					jl.setPreferredSize(maxSize);
					boxLine.add(jl);
					
					cmbCDKeyLOD = new ConfigComboBox(CDKeys);
					boxLine.add(cmbCDKeyLOD);
					
					for(int i = 0; i < CDKeys.length; i++) {
						if(CDKeys[i].getKey().equals(cs.cdkeyLOD))
							cmbCDKeyLOD.setSelectedIndex(i);
					}
				}
				boxSettings.add(boxLine);

				boxLine = new Box(BoxLayout.X_AXIS);
				{
					CDKey[] CDKeys = KeyManager.getKeys(KeyManager.PRODUCT_W3XP);
					
					JLabel jl = new JLabel("TFT Key");
					jl.setPreferredSize(maxSize);
					boxLine.add(jl);
					
					cmbCDKeyTFT = new ConfigComboBox(CDKeys);
					boxLine.add(cmbCDKeyTFT);
					
					for(int i = 0; i < CDKeys.length; i++) {
						if(CDKeys[i].getKey().equals(cs.cdkeyTFT))
							cmbCDKeyTFT.setSelectedIndex(i);
					}
				}
				boxSettings.add(boxLine);

				boxLine = new Box(BoxLayout.X_AXIS);
				{
					JLabel jl = new JLabel("Product");
					jl.setPreferredSize(maxSize);
					boxLine.add(jl);
					
					cmbProduct = new ConfigComboBox(org.jbls.util.Constants.prods);
					cmbProduct.addItemListener(new ItemListener() {
						public void itemStateChanged(ItemEvent e) {
							int prod = KeyManager.PRODUCT_ALLNORMAL;
							switch(cmbProduct.getSelectedIndex() + 1) {
							case ConnectionSettings.PRODUCT_STARCRAFT:
							case ConnectionSettings.PRODUCT_BROODWAR:
							case ConnectionSettings.PRODUCT_JAPANSTARCRAFT:
								prod = KeyManager.PRODUCT_STAR;
								break;
							case ConnectionSettings.PRODUCT_DIABLO2:
							case ConnectionSettings.PRODUCT_LORDOFDESTRUCTION:
								prod = KeyManager.PRODUCT_D2DV;
								break;
							case ConnectionSettings.PRODUCT_WARCRAFT3:
							case ConnectionSettings.PRODUCT_THEFROZENTHRONE:
								prod = KeyManager.PRODUCT_WAR3;
								break;
							case ConnectionSettings.PRODUCT_WAR2BNE:
								prod = KeyManager.PRODUCT_W2BN;
								break;
							}
							
							DefaultComboBoxModel model = (DefaultComboBoxModel)cmbCDKey.getModel();
							model.removeAllElements();
							if(prod != KeyManager.PRODUCT_ALLNORMAL) {
								CDKey[] CDKeys = KeyManager.getKeys(prod);
								for(int i = 0; i < CDKeys.length; i++) {
									model.addElement(CDKeys[i]);
									
									if(CDKeys[i].getKey().equals(cs.cdkey))
										cmbCDKey.setSelectedIndex(i);
								}
							}
							
						}
					});
					cmbProduct.setSelectedIndex(cs.product - 1);
					boxLine.add(cmbProduct);
				}
				boxSettings.add(boxLine);

				boxLine = new Box(BoxLayout.X_AXIS);
				{
					JLabel jl = new JLabel("Battle.net Server");
					jl.setPreferredSize(maxSize);
					boxLine.add(jl);
					
					txtBNCSServer = new ConfigTextArea(cs.bncsServer);
					boxLine.add(txtBNCSServer);
				}
				boxSettings.add(boxLine);

				boxLine = new Box(BoxLayout.X_AXIS);
				{
					JLabel jl = new JLabel("BNLS Server");
					jl.setPreferredSize(maxSize);
					boxLine.add(jl);
					
					txtBNLSServer = new ConfigTextArea(cs.bnlsServer);
					boxLine.add(txtBNLSServer);
				}
				boxSettings.add(boxLine);

				boxLine = new Box(BoxLayout.X_AXIS);
				{
					JLabel jl = new JLabel("Channel");
					jl.setPreferredSize(maxSize);
					boxLine.add(jl);
					
					txtChannel = new ConfigTextArea(cs.channel);
					boxLine.add(txtChannel);
				}
				boxSettings.add(boxLine);

				boxLine = new Box(BoxLayout.X_AXIS);
				{
					JLabel jl = new JLabel("TimeStamp Format");
					jl.setPreferredSize(maxSize);
					boxLine.add(jl);
					
					cmbTSFormat = new ConfigComboBox(new String[] { TimeFormatter.tsFormat, "%1$tH:%1$tM:%1$tS.%1$tL", "%1$tH:%1$tM:%1$tS", "%1$tH:%1$tM" });
					cmbTSFormat.setSelectedItem(TimeFormatter.tsFormat);
					cmbTSFormat.setEditable(true);
					boxLine.add(cmbTSFormat);
				}
				boxSettings.add(boxLine);

				boxLine = new Box(BoxLayout.X_AXIS);
				{
					JLabel jl = new JLabel("Version Check");
					jl.setPreferredSize(maxSize);
					boxLine.add(jl);
					
					cmbReleaseType = new ConfigComboBox(ReleaseType.values());
					cmbReleaseType.setSelectedItem(ConnectionSettings.releaseType);
					boxLine.add(cmbReleaseType);
				}
				boxSettings.add(boxLine);

				boxLine = new Box(BoxLayout.X_AXIS);
				{
					JLabel jl = new JLabel("Color Scheme");
					jl.setPreferredSize(maxSize);
					boxLine.add(jl);
					
					cmbColorScheme = new ConfigComboBox(new String[] { "Starcraft", "Diablo 2" });
					cmbColorScheme.setSelectedIndex(ConnectionSettings.colorScheme - 1);
					boxLine.add(cmbColorScheme);
				}
				boxSettings.add(boxLine);

				boxLine = new Box(BoxLayout.X_AXIS);
				{
					JLabel jl = new JLabel("Look and Feel");
					jl.setPreferredSize(maxSize);
					boxLine.add(jl);
					
					DefaultComboBoxModel model = new DefaultComboBoxModel();
					LookAndFeelInfo selected = null;
					for(LookAndFeelInfo lafi : UIManager.getInstalledLookAndFeels()) {
						model.addElement(lafi.getClassName());
						if(cs.getLookAndFeel().equals(lafi.getClassName()))
							selected = lafi;
					}
					cmbLookAndFeel = new ConfigComboBox(model);
					if(selected != null)
						cmbLookAndFeel.setSelectedItem(selected.getClassName());
					cmbLookAndFeel.addItemListener(new ItemListener() {
						public void itemStateChanged(ItemEvent e) {
							cs.setLookAndFeel((String)cmbLookAndFeel.getSelectedItem());
						}
					});
					boxLine.add(cmbLookAndFeel);
				}
				boxSettings.add(boxLine);
				
				boxLine = new Box(BoxLayout.X_AXIS);
				{
					boxLine.add(Box.createRigidArea(maxSize));
					
					Box boxCheckboxes = new Box(BoxLayout.Y_AXIS);
					{
						chkAutoConnect = new ConfigCheckBox("Auto Connect", ConnectionSettings.autoconnect);
						boxCheckboxes.add(chkAutoConnect);
	
						chkEnableGUI = new ConfigCheckBox("Enable GUI (requires restart)", ConnectionSettings.enableGUI);
						boxCheckboxes.add(chkEnableGUI);
	
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
		if(addConnectionStuff)
			tabs.addTab("Connection", boxAll);
		
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
			
			boxLine = new Box(BoxLayout.X_AXIS);
			{
				JLabel jl = new JLabel("URL");
				jl.setPreferredSize(maxSize);
				boxLine.add(jl);
				
				txtDriverURL = new ConfigTextArea(dbSettings.url);
				boxLine.add(txtDriverURL);
			}
			boxAll.add(boxLine);
			
			boxLine = new Box(BoxLayout.X_AXIS);
			{
				JLabel jl = new JLabel("Username");
				jl.setPreferredSize(maxSize);
				boxLine.add(jl);

				txtDriverUsername = new ConfigTextArea(dbSettings.username);
				boxLine.add(txtDriverUsername);
			}
			boxAll.add(boxLine);
			
			boxLine = new Box(BoxLayout.X_AXIS);
			{
				JLabel jl = new JLabel("Password");
				jl.setPreferredSize(maxSize);
				boxLine.add(jl);

				txtDriverPassword = new ConfigTextArea(dbSettings.password);
				boxLine.add(txtDriverPassword);
			}
			boxAll.add(boxLine);
			
			boxLine = new Box(BoxLayout.X_AXIS);
			{
				JLabel jl = new JLabel("Schema");
				jl.setPreferredSize(maxSize);
				boxLine.add(jl);

				txtDriverSchema = new ConfigTextArea(dbSettings.schema);
				boxLine.add(txtDriverSchema);
			}
			boxAll.add(boxLine);
			
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
		if((size.height > 700) || (size.width > 400))
			this.setSize(Math.min(400, size.width), Math.min(700, size.height));
	}
	
	private String formatCDKey(String in) {
		String out = new String(in);
		out = out.replaceAll("-", "");
		out = out.replaceAll(" ", "");
		out = out.replaceAll("\t", "");
		return out.toUpperCase();
	}
	
	private void save() {
		cs.username = txtUsername.getText();
		cs.password = new String(txtPassword.getPassword());
		cs.email = txtEmail.getText();
		cs.product = (byte)(cmbProduct.getSelectedIndex() + 1);
		ConnectionSettings.trigger = txtTrigger.getText();
		ConnectionSettings.antiIdle = txtAntiIdle.getText();
		ConnectionSettings.antiIdleTimer = Integer.parseInt(txtAntiIdleTimer.getText());
		ConnectionSettings.enableAntiIdle = chkAntiIdle.isSelected();
		
		CDKey k = (CDKey)cmbCDKey.getSelectedItem();
		CDKey kLOD = (CDKey)cmbCDKeyLOD.getSelectedItem();
		CDKey kTFT = (CDKey)cmbCDKeyTFT.getSelectedItem();
		
		if(k != null)
			cs.cdkey = formatCDKey(k.getKey());
		if(kLOD != null)
			cs.cdkeyLOD = formatCDKey(kLOD.getKey());
		if(kTFT != null)
			cs.cdkeyTFT = formatCDKey(kTFT.getKey());
		cs.bncsServer = txtBNCSServer.getText();
		cs.bnlsServer = txtBNLSServer.getText();
		cs.channel = txtChannel.getText();
		ConnectionSettings.colorScheme = (byte)(cmbColorScheme.getSelectedIndex() + 1);
		TimeFormatter.tsFormat = (String)cmbTSFormat.getSelectedItem();
		ConnectionSettings.releaseType = (ReleaseType)cmbReleaseType.getSelectedItem();
		ConnectionSettings.autoconnect = chkAutoConnect.isSelected();
		ConnectionSettings.enableGUI = chkEnableGUI.isSelected();
		ConnectionSettings.enableCLI = chkEnableCLI.isSelected();
		ConnectionSettings.enableTrivia = chkEnableTrivia.isSelected();
		ConnectionSettings.triviaRoundLength = Integer.parseInt(txtTriviaRoundLength.getText());
		ConnectionSettings.enableCommands = chkEnableCommands.isSelected();
		ConnectionSettings.enableFloodProtect = chkEnableFloodProtect.isSelected();
		ConnectionSettings.packetLog = chkPacketLog.isSelected();
		ConnectionSettings.whisperBack = chkWhisperBack.isSelected();
		
		cs.save();
	}
	
	private void load() {
		cs.load(cs.botNum);
		txtUsername.setText(cs.username);
		txtPassword.setText(cs.password);
		txtEmail.setText(cs.email);
		cmbProduct.setSelectedIndex(cs.product - 1);
		txtTrigger.setText(ConnectionSettings.trigger);
		txtAntiIdle.setText(ConnectionSettings.antiIdle);
		txtAntiIdleTimer.setText(Integer.toString(ConnectionSettings.antiIdleTimer));
		chkAntiIdle.setSelected(ConnectionSettings.enableAntiIdle);
		cmbCDKey.setSelectedItem(cs.cdkey);
		cmbCDKeyLOD.setSelectedItem(cs.cdkeyLOD);
		cmbCDKeyTFT.setSelectedItem(cs.cdkeyTFT);
		txtBNCSServer.setText(cs.bncsServer);
		txtBNLSServer.setText(cs.bnlsServer);
		txtChannel.setText(cs.channel);
		cmbColorScheme.setSelectedIndex(ConnectionSettings.colorScheme - 1);
		cmbTSFormat.setSelectedItem(TimeFormatter.tsFormat);
		cmbReleaseType.setSelectedItem(ConnectionSettings.releaseType);
		chkAutoConnect.setSelected(ConnectionSettings.autoconnect);
		chkEnableGUI.setSelected(ConnectionSettings.enableGUI);
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
		String v = cs.isValid();
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
