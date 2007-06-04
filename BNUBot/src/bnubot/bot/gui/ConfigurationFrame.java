package bnubot.bot.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import bnubot.core.ConnectionSettings;

@SuppressWarnings("serial")
public class ConfigurationFrame extends JDialog {
	ConnectionSettings cs;
	
	JTextArea txtUsername = null;
	JPasswordField txtPassword = null;
	JTextArea txtEmail = null;
	JComboBox cmbProduct = null;
	JTextArea txtTrigger = null;
	JTextArea txtAntiIdle = null;
	JTextArea txtCDKey = null;
	JTextArea txtCDKeyLOD = null;
	JTextArea txtCDKeyTFT = null;
	JTextArea txtBNCSServer = null;
	JTextArea txtBNLSServer = null;
	JTextArea txtChannel = null;
	JCheckBox chkAutoConnect = null;
	JCheckBox chkEnableGUI = null;
	JCheckBox chkEnableCLI = null;
	JCheckBox chkPacketLog = null;
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
					JLabel jl = new JLabel("Product");
					jl.setPreferredSize(maxSize);
					boxLine.add(jl);
					cmbProduct = new JComboBox(util.Constants.prods);
					cmbProduct.setSelectedIndex(cs.product - 1);
					boxLine.add(cmbProduct);
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
					txtAntiIdle = new ConfigTextArea(cs.antiIdle);
					boxLine.add(txtAntiIdle);
				}
				boxSettings.add(boxLine);

				boxLine = new Box(BoxLayout.X_AXIS);
				{
					JLabel jl = new JLabel("CD Key");
					jl.setPreferredSize(maxSize);
					boxLine.add(jl);
					txtCDKey = new ConfigTextArea(cs.cdkey);
					boxLine.add(txtCDKey);
				}
				boxSettings.add(boxLine);

				boxLine = new Box(BoxLayout.X_AXIS);
				{
					JLabel jl = new JLabel("LOD Key");
					jl.setPreferredSize(maxSize);
					boxLine.add(jl);
					txtCDKeyLOD = new ConfigTextArea(cs.cdkeyLOD);
					boxLine.add(txtCDKeyLOD);
				}
				boxSettings.add(boxLine);

				boxLine = new Box(BoxLayout.X_AXIS);
				{
					JLabel jl = new JLabel("TFT Key");
					jl.setPreferredSize(maxSize);
					boxLine.add(jl);
					txtCDKeyTFT = new ConfigTextArea(cs.cdkeyTFT);
					boxLine.add(txtCDKeyTFT);
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
					Box boxCheckboxes = new Box(BoxLayout.Y_AXIS);
					{
						chkAutoConnect = new JCheckBox("Auto Connect", cs.autoconnect);
						boxCheckboxes.add(chkAutoConnect);
	
						chkEnableGUI = new JCheckBox("Enable GUI", cs.enableGUI);
						boxCheckboxes.add(chkEnableGUI);
	
						chkEnableCLI = new JCheckBox("Enable CLI", cs.enableCLI);
						boxCheckboxes.add(chkEnableCLI);
	
						chkPacketLog = new JCheckBox("Packet Log", cs.packetLog);
						boxCheckboxes.add(chkPacketLog);
					}
					boxLine.add(boxCheckboxes);
				}
				boxSettings.add(boxLine);
			}
			boxAll.add(boxSettings);

			boxAll.add(Box.createVerticalGlue());

			Box boxButtons = new Box(BoxLayout.X_AXIS);
			{
				btnLoad = new JButton("Load");
				btnSave = new JButton("OK");
				btnCancel = new JButton("Cancel");
				boxButtons.add(Box.createHorizontalGlue());
				boxButtons.add(btnLoad);
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
		cs.cdkey = formatCDKey(txtCDKey.getText());
		cs.cdkeyLOD = formatCDKey(txtCDKeyLOD.getText());
		cs.cdkeyTFT = formatCDKey(txtCDKeyTFT.getText());
		cs.bncsServer = txtBNCSServer.getText();
		cs.bnlsServer = txtBNLSServer.getText();
		cs.channel = txtChannel.getText();
		cs.autoconnect = chkAutoConnect.isSelected();
		cs.enableGUI = chkEnableGUI.isSelected();
		cs.enableCLI = chkEnableCLI.isSelected();
		cs.packetLog = chkPacketLog.isSelected();
		
		cs.save();
	}
	
	private void load() {
		cs.load();
		txtUsername.setText(cs.username);
		txtPassword.setText(cs.password);
		txtEmail.setText(cs.email);
		cmbProduct.setSelectedIndex(cs.product - 1);
		txtTrigger.setText(cs.trigger);
		txtAntiIdle.setText(cs.antiIdle);
		txtCDKey.setText(cs.cdkey);
		txtCDKeyLOD.setText(cs.cdkeyLOD);
		txtCDKeyTFT.setText(cs.cdkeyTFT);
		txtBNCSServer.setText(cs.bncsServer);
		txtBNLSServer.setText(cs.bnlsServer);
		txtChannel.setText(cs.channel);
		chkAutoConnect.setSelected(cs.autoconnect);
		chkEnableGUI.setSelected(cs.enableGUI);
		chkEnableCLI.setSelected(cs.enableCLI);
		chkPacketLog.setSelected(cs.packetLog);
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
