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
	JTextArea txtCDKey = null;
	JTextArea txtCDKeyLOD = null;
	JTextArea txtCDKeyTFT = null;
	JTextArea txtServer = null;
	JTextArea txtChannel = null;
	JCheckBox chkAutoConnect = null;
	JCheckBox chkEnableGUI = null;
	JCheckBox chkEnableCLI = null;
	JCheckBox chkPacketLog = null;
	JButton btnLoad = null;
	JButton btnSave = null;
	JButton btnCancel = null;
	
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
			JPanel pnlSettings = new JPanel(new GridLayout(0, 2));
			{
				pnlSettings.add(new JLabel("Username"));
				txtUsername = new JTextArea(cs.username);
				pnlSettings.add(txtUsername);
				
				pnlSettings.add(new JLabel("Password"));
				txtPassword = new JPasswordField(cs.password);
				pnlSettings.add(txtPassword);
				
				pnlSettings.add(new JLabel("Email"));
				txtEmail = new JTextArea(cs.email);
				pnlSettings.add(txtEmail);
				
				pnlSettings.add(new JLabel("Product"));
				cmbProduct = new JComboBox(util.Constants.prods);
				cmbProduct.setSelectedIndex(cs.product - 1);
				pnlSettings.add(cmbProduct);
				
				pnlSettings.add(new JLabel("Trigger"));
				txtTrigger = new JTextArea(cs.trigger);
				txtTrigger.setRows(1);
				txtTrigger.setColumns(1);
				pnlSettings.add(txtTrigger);
				
				pnlSettings.add(new JLabel("CD Key"));
				txtCDKey = new JTextArea(cs.cdkey);
				pnlSettings.add(txtCDKey);
				
				pnlSettings.add(new JLabel("LOD Key"));
				txtCDKeyLOD = new JTextArea(cs.cdkeyLOD);
				pnlSettings.add(txtCDKeyLOD);
				
				pnlSettings.add(new JLabel("TFT Key"));
				txtCDKeyTFT = new JTextArea(cs.cdkeyTFT);
				pnlSettings.add(txtCDKeyTFT);
				
				pnlSettings.add(new JLabel("Server"));
				txtServer = new JTextArea(cs.server);
				pnlSettings.add(txtServer);
				
				pnlSettings.add(new JLabel("Channel"));
				txtChannel = new JTextArea(cs.channel);
				pnlSettings.add(txtChannel);
			}
			boxAll.add(pnlSettings);

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
			boxAll.add(boxCheckboxes);
			
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
		cs.cdkey = formatCDKey(txtCDKey.getText());
		cs.cdkeyLOD = formatCDKey(txtCDKeyLOD.getText());
		cs.cdkeyTFT = formatCDKey(txtCDKeyTFT.getText());
		cs.server = txtServer.getText();
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
		txtCDKey.setText(cs.cdkey);
		txtCDKeyLOD.setText(cs.cdkeyLOD);
		txtCDKeyTFT.setText(cs.cdkeyTFT);
		txtServer.setText(cs.server);
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
			JOptionPane.showMessageDialog(this, "The configuration is invalid: " + v);
		}
	}
}
