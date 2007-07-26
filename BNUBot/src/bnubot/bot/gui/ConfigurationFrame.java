/**
 * This file is distributed under the GPL 
 * $Id$
 */

package bnubot.bot.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.*;

import bnubot.bot.gui.KeyManager.CDKey;
import bnubot.core.ConnectionSettings;

@SuppressWarnings("serial")
public class ConfigurationFrame extends JDialog {
	ConnectionSettings cs;
	
	JTextArea txtUsername = null;
	JPasswordField txtPassword = null;
	JTextArea txtEmail = null;
	JComboBox cmbProduct = null;
	JTextArea txtTrigger = null;
	JCheckBox chkAntiIdle = null;
	JTextArea txtAntiIdle = null;
	JComboBox cmbCDKey = null;
	JComboBox cmbCDKeyLOD = null;
	JComboBox cmbCDKeyTFT = null;
	JTextArea txtBNCSServer = null;
	JTextArea txtBNLSServer = null;
	JTextArea txtChannel = null;
	JComboBox cmbColorScheme = null;
	JCheckBox chkAutoConnect = null;
	JCheckBox chkEnableGUI = null;
	JCheckBox chkEnableCLI = null;
	JCheckBox chkEnableTrivia = null;
	JCheckBox chkPacketLog = null;
	JCheckBox chkWhisperBack = null;
	JButton btnLoad = null;
	JButton btnSave = null;
	JButton btnCancel = null;
	
	private class ConfigTextArea extends JTextArea {
		public ConfigTextArea(String text) {
			super(text);
			setBorder(BorderFactory.createEtchedBorder());
		}
	}
	
	public ConfigurationFrame(ConnectionSettings cs) {
		super();
		
		this.cs = cs;

		initializeGui();
		setupActions();
		setTitle(cs.username);
		pack();
	}
	
	private void initializeGui() {
		Box boxAll = new Box(BoxLayout.Y_AXIS);
		{
			Box boxSettings = new Box(BoxLayout.Y_AXIS);
			{
				int lblWidth = 100;
				Dimension maxSize = new Dimension(lblWidth, 0);
				
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
					txtTrigger = new ConfigTextArea(cs.trigger);
					boxLine.add(txtTrigger);
				}
				boxSettings.add(boxLine);

				boxLine = new Box(BoxLayout.X_AXIS);
				{
					JLabel jl = new JLabel("Anti-Idle");
					jl.setPreferredSize(maxSize);
					boxLine.add(jl);
					
					chkAntiIdle = new JCheckBox("Enable", cs.enableAntiIdle);
					boxLine.add(chkAntiIdle);
					
					txtAntiIdle = new ConfigTextArea(cs.antiIdle);
					boxLine.add(txtAntiIdle);
				}
				boxSettings.add(boxLine);

				boxLine = new Box(BoxLayout.X_AXIS);
				{
					CDKey[] CDKeys = KeyManager.getKeys(KeyManager.PRODUCT_ALLNORMAL);
					
					JLabel jl = new JLabel("CD Key");
					jl.setPreferredSize(maxSize);
					boxLine.add(jl);
					cmbCDKey = new JComboBox(CDKeys);
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
					cmbCDKeyLOD = new JComboBox(CDKeys);
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
					cmbCDKeyTFT = new JComboBox(CDKeys);
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
					cmbProduct = new JComboBox(util.Constants.prods);
					cmbProduct.addItemListener(new ItemListener() {
						public void itemStateChanged(ItemEvent e) {
							int prod = KeyManager.PRODUCT_ALLNORMAL;
							switch(cmbProduct.getSelectedIndex() + 1) {
							case ConnectionSettings.PRODUCT_STARCRAFT:
							case ConnectionSettings.PRODUCT_BROODWAR:
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
							if(prod != KeyManager.PRODUCT_ALLNORMAL) {
								DefaultComboBoxModel model = (DefaultComboBoxModel)cmbCDKey.getModel();
								model.removeAllElements();
								
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
					JLabel jl = new JLabel("Color Scheme");
					jl.setPreferredSize(maxSize);
					boxLine.add(jl);
					cmbColorScheme = new JComboBox(new String[] { "Starcraft", "Diablo 2" });
					cmbColorScheme.setSelectedIndex(cs.colorScheme - 1);
					boxLine.add(cmbColorScheme);
				}
				boxSettings.add(boxLine);
				
				boxLine = new Box(BoxLayout.X_AXIS);
				{
					boxLine.add(Box.createRigidArea(maxSize));
					
					Box boxCheckboxes = new Box(BoxLayout.Y_AXIS);
					{
						chkAutoConnect = new JCheckBox("Auto Connect", cs.autoconnect);
						boxCheckboxes.add(chkAutoConnect);
	
						chkEnableGUI = new JCheckBox("Enable GUI", cs.enableGUI);
						boxCheckboxes.add(chkEnableGUI);
	
						chkEnableCLI = new JCheckBox("Enable CLI", cs.enableCLI);
						boxCheckboxes.add(chkEnableCLI);;
	
						chkEnableTrivia = new JCheckBox("Enable Trivia", cs.enableTrivia);
						boxCheckboxes.add(chkEnableTrivia);
	
						chkPacketLog = new JCheckBox("Packet Log", cs.packetLog);
						boxCheckboxes.add(chkPacketLog);
	
						chkWhisperBack = new JCheckBox("Whisper Commands", cs.whisperBack);
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
				btnLoad = new JButton("Load");
				btnSave = new JButton("OK");
				btnCancel = new JButton("Cancel");
				boxButtons.add(Box.createHorizontalGlue());
				boxButtons.add(btnLoad);
				boxButtons.add(Box.createHorizontalStrut(50));
				boxButtons.add(btnSave);
				boxButtons.add(btnCancel);
			}
			boxAll.add(boxButtons);
		}
		add(boxAll);
	}

	private void setupActions() {
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent act) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						load();
					}
				});
			}
		});
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent act) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						save();
						close();
					}
				});
			}
		});
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent act) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						cancel();
					}
				});
			}
		});
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
		cs.trigger = txtTrigger.getText();
		cs.antiIdle = txtAntiIdle.getText();
		cs.enableAntiIdle = chkAntiIdle.isSelected();
		
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
		cs.colorScheme = (byte)(cmbColorScheme.getSelectedIndex() + 1);
		cs.autoconnect = chkAutoConnect.isSelected();
		cs.enableGUI = chkEnableGUI.isSelected();
		cs.enableCLI = chkEnableCLI.isSelected();
		cs.enableTrivia = chkEnableTrivia.isSelected();
		cs.packetLog = chkPacketLog.isSelected();
		cs.whisperBack = chkWhisperBack.isSelected();
		
		cs.save();
	}
	
	private void load() {
		cs.load(cs.botNum);
		txtUsername.setText(cs.username);
		txtPassword.setText(cs.password);
		txtEmail.setText(cs.email);
		cmbProduct.setSelectedIndex(cs.product - 1);
		txtTrigger.setText(cs.trigger);
		txtAntiIdle.setText(cs.antiIdle);
		chkAntiIdle.setSelected(cs.enableAntiIdle);
		cmbCDKey.setSelectedItem(cs.cdkey);
		cmbCDKeyLOD.setSelectedItem(cs.cdkeyLOD);
		cmbCDKeyTFT.setSelectedItem(cs.cdkeyTFT);
		txtBNCSServer.setText(cs.bncsServer);
		txtBNLSServer.setText(cs.bnlsServer);
		txtChannel.setText(cs.channel);
		cmbColorScheme.setSelectedIndex(cs.colorScheme - 1);
		chkAutoConnect.setSelected(cs.autoconnect);
		chkEnableGUI.setSelected(cs.enableGUI);
		chkEnableCLI.setSelected(cs.enableCLI);
		chkEnableTrivia.setSelected(cs.enableTrivia);
		chkPacketLog.setSelected(cs.packetLog);
		chkWhisperBack.setSelected(cs.whisperBack);
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
			JOptionPane.showMessageDialog(this, "The configuration is invalid:\n" + v);
		}
	}
}
