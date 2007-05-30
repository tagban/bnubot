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
	JTextArea txtCDKey = null;
	JTextArea txtCDKeyLOD = null;
	JTextArea txtCDKeyTFT = null;
	JTextArea txtServer = null;
	JCheckBox chkAutoConnect = null;
	JCheckBox chkEnableGUI = null;
	JCheckBox chkEnableCLI = null;
	JCheckBox chkPacketLog = null;
	JButton btnSave = null;
	JButton btnCancel = null;
	
	public ConfigurationFrame(ConnectionSettings cs) {
		super();
		
		this.cs = cs;

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//setLayout(new FlowLayout(FlowLayout.LEFT));
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
				btnSave = new JButton("Save");
				btnCancel = new JButton("Cancel");
				boxButtons.add(Box.createHorizontalGlue());
				boxButtons.add(btnSave);
				boxButtons.add(btnCancel);
			}
			boxAll.add(boxButtons);
		}
		add(boxAll);
	}

	private void setupActions() {
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent act) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						save();
					}
				});
			}
		});
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent act) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						close();
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
		cs.cdkey = formatCDKey(txtCDKey.getText());
		cs.cdkeyLOD = formatCDKey(txtCDKeyLOD.getText());
		cs.cdkeyTFT = formatCDKey(txtCDKeyTFT.getText());
		cs.server = txtServer.getText();
		cs.autoconnect = chkAutoConnect.isSelected();
		cs.enableGUI = chkEnableGUI.isSelected();
		cs.enableCLI = chkEnableCLI.isSelected();
		cs.packetLog = chkPacketLog.isSelected();
		
		if(cs.isValid()) {
			cs.save();
			close();
		} else {
			JOptionPane.showMessageDialog(this, "The configuration is invalid!");
		}
	}
	
	private void close() {
		if(cs.isValid()) {
			dispose();
		} else {
			JOptionPane.showMessageDialog(this, "The configuration is invalid!");
		}
	}
}
