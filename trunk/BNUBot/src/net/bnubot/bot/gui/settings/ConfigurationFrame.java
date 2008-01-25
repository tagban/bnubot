/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;

import net.bnubot.bot.gui.KeyManager;
import net.bnubot.bot.gui.WindowPosition;
import net.bnubot.bot.gui.KeyManager.CDKey;
import net.bnubot.bot.gui.components.ConfigCheckBox;
import net.bnubot.bot.gui.components.ConfigComboBox;
import net.bnubot.bot.gui.components.ConfigFactory;
import net.bnubot.bot.gui.components.ConfigTextArea;
import net.bnubot.settings.ConnectionSettings;
import net.bnubot.settings.ConnectionSettings.ConnectionType;

import org.jbls.util.Constants;

public class ConfigurationFrame extends JDialog {
	private static final long serialVersionUID = 1308177934480442149L;

	private final ConnectionSettings cs;
	private boolean pressedCancel = false;
	
	final String[] bncsServers = new String[] {
			"useast.battle.net",
			"uswest.battle.net",
			"europe.battle.net",
			"asia.battle.net",
			};
	
	final String[] dtServers = new String[] {
			"koolaid.sidoh.org",
			};

	// Connection
	private ConfigTextArea txtProfile = null;
	private ConfigTextArea txtUsername = null;
	private JPasswordField txtPassword = null;
	private ConfigComboBox cmbProduct = null;
	private ConfigComboBox cmbCDKey = null;
	private ConfigComboBox cmbCDKey2 = null;
	
	// Profile
	private ConfigComboBox cmbConnectionType = null;
	private ConfigComboBox cmbServer = null;
	private ConfigTextArea txtChannel = null;
	private ConfigTextArea txtTrigger = null;
	private ConfigCheckBox chkAntiIdle = null;
	private ConfigTextArea txtAntiIdle = null;
	private ConfigTextArea txtAntiIdleTimer = null;
	private ConfigCheckBox chkGreetings = null;
	
	// Buttons
	private JButton btnKeys = null;
	private JButton btnUndo = null;
	private JButton btnOK = null;
	private JButton btnCancel = null;

	public ConfigurationFrame(ConnectionSettings cs) throws OperationCancelledException {
		super();
		this.cs = cs;
		setTitle("Configuration");

		initializeGui();

		setModal(true);
		setResizable(false);
		WindowPosition.load(this);
		setVisible(true);
		
		if(pressedCancel)
			throw new OperationCancelledException();
	}

	private void initializeGui() {
		getContentPane().removeAll();
		
		final Box boxAll = new Box(BoxLayout.Y_AXIS);
		boolean hasCdKeys = true;
		Box boxSettings = new Box(BoxLayout.Y_AXIS);
		{
			txtProfile = ConfigFactory.makeText("Profile", cs.profile, boxSettings);
			txtUsername = ConfigFactory.makeText("Username", cs.username, boxSettings);
			txtPassword = ConfigFactory.makePass("Password", cs.password, boxSettings);

			cmbProduct = ConfigFactory.makeCombo("Product", Constants.prodsDisplay, false, boxSettings);
			cmbProduct.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					setVisibleFields();
				}});
			//Initialize CD Keys combo box before setting product

			CDKey[] CDKeys = KeyManager.getKeys(KeyManager.PRODUCT_ALLNORMAL);
			if(CDKeys.length == 0)
				hasCdKeys = false;
			cmbCDKey = ConfigFactory.makeCombo("CD key", CDKeys, false, boxSettings);
			cmbCDKey2 = ConfigFactory.makeCombo("CD key 2", CDKeys, false, boxSettings);
			cmbConnectionType = ConfigFactory.makeCombo("Connection Type", ConnectionType.values(), false, boxSettings);
			
			try {
				cmbProduct.setSelectedIndex(cs.product - 1);
				cmbCDKey.setSelectedItem(cs.cdkey);
				cmbCDKey2.setSelectedItem(cs.cdkey2);
			} catch(Exception e) {}

			CDKeys = null;
			switch(cmbProduct.getSelectedIndex() + 1) {
			case ConnectionSettings.PRODUCT_LORDOFDESTRUCTION:
				CDKeys = KeyManager.getKeys(KeyManager.PRODUCT_D2XP);
				break;
			case ConnectionSettings.PRODUCT_THEFROZENTHRONE:
				CDKeys = KeyManager.getKeys(KeyManager.PRODUCT_W3XP);
				break;
			}
			
			cmbConnectionType.setSelectedItem(cs.connectionType);
			cmbConnectionType.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					setVisibleFields();
				}});
			
			cmbServer = ConfigFactory.makeCombo("Server", new String[] {}, false, boxSettings);
			cmbServer.setSelectedItem(cs.server);
			
			setVisibleFields();
			
			txtChannel = ConfigFactory.makeText("Channel", cs.channel, boxSettings);
			txtTrigger = ConfigFactory.makeText("Trigger", cs.trigger, boxSettings);
			
			Box boxLine = new Box(BoxLayout.X_AXIS);
			{
				boxLine.add(ConfigFactory.makeLabel("Anti-Idle"));
				boxLine.add(chkAntiIdle = new ConfigCheckBox("Enable", cs.enableAntiIdle));
				boxLine.add(txtAntiIdle = new ConfigTextArea(cs.antiIdle));
				txtAntiIdle.setMaximumSize(ConfigFactory.getMaxComponentSize());
			}
			boxSettings.add(boxLine);
			
			txtAntiIdleTimer = ConfigFactory.makeText("Anti-Idle Timer", Integer.toString(cs.antiIdleTimer), boxSettings);
			boxSettings.add(chkGreetings = new ConfigCheckBox("Enable Greetings", cs.enableGreetings));
			
			boxSettings.add(Box.createVerticalGlue());
		}
		boxAll.add(boxSettings);

		boxAll.add(Box.createVerticalStrut(10));

		Box boxButtons = new Box(BoxLayout.X_AXIS);
		{
			btnKeys = new JButton("Key Editor");
			btnKeys.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent act) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							remove(boxAll);
							try {
								new GlobalConfigurationFrame(true);
							} catch (OperationCancelledException e) {}
							initializeGui();
						}});
				}
			});
			
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
			boxButtons.add(btnKeys);
			boxButtons.add(Box.createHorizontalStrut(50));
			boxButtons.add(btnUndo);
			boxButtons.add(Box.createHorizontalStrut(50));
			boxButtons.add(btnOK);
			boxButtons.add(btnCancel);
		}
		boxAll.add(boxButtons);
		
		add(boxAll);
		if(!hasCdKeys) {
			// Offer cd key window
			JOptionPane.showMessageDialog(this,
					"You have no CD keys in cdkeys.txt.",
					"Error",
					JOptionPane.ERROR_MESSAGE);
			try {
				new GlobalConfigurationFrame(true);
				initializeGui();
				return;
			} catch(OperationCancelledException e) {
				// If they click cancel, just let them proceed
			}
		}
		
		pack();
	}

	private String formatCDKey(String in) {
		String out = new String(in);
		out = out.replaceAll("-", "");
		out = out.replaceAll(" ", "");
		out = out.replaceAll("\t", "");
		return out.toUpperCase();
	}

	private void save() {
		cs.profile = txtProfile.getText();
		cs.username = txtUsername.getText();
		cs.password = new String(txtPassword.getPassword());
		cs.product = (byte)(cmbProduct.getSelectedIndex() + 1);
		CDKey k = (CDKey)cmbCDKey.getSelectedItem();
		CDKey k2 = (CDKey)cmbCDKey2.getSelectedItem();

		if(k != null)
			cs.cdkey = formatCDKey(k.getKey());
		if(k2 != null)
			cs.cdkey2 = formatCDKey(k2.getKey());
		cs.connectionType = (ConnectionType)cmbConnectionType.getSelectedItem();
		cs.server = (String)cmbServer.getSelectedItem();
		cs.channel = txtChannel.getText();
		
		// Profile
		cs.trigger = txtTrigger.getText();
		cs.antiIdle = txtAntiIdle.getText();
		cs.antiIdleTimer = Integer.parseInt(txtAntiIdleTimer.getText());
		cs.enableAntiIdle = chkAntiIdle.isSelected();
		cs.enableGreetings = chkGreetings.isSelected();

		cs.save();
	}

	private void load() {
		cs.load();
		
		txtProfile.setText(cs.profile);
		txtUsername.setText(cs.username);
		txtPassword.setText(cs.password);
		cmbProduct.setSelectedIndex(cs.product - 1);
		cmbCDKey.setSelectedItem(cs.cdkey);
		cmbCDKey2.setSelectedItem(cs.cdkey2);
		
		// Profile
		cmbConnectionType.setSelectedItem(cs.connectionType);
		cmbServer.setSelectedItem(cs.server);
		txtChannel.setText(cs.channel);
		txtTrigger.setText(cs.trigger);
		txtAntiIdle.setText(cs.antiIdle);
		txtAntiIdleTimer.setText(Integer.toString(cs.antiIdleTimer));
		chkAntiIdle.setSelected(cs.enableAntiIdle);
		chkGreetings.setSelected(cs.enableGreetings);
	}

	private void cancel() {
		pressedCancel = true;
		load();
		dispose();
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

	private void setVisibleFields() {
		DefaultComboBoxModel model = (DefaultComboBoxModel)cmbServer.getModel();
		model.removeAllElements();
		
		if((ConnectionType)cmbConnectionType.getSelectedItem() == ConnectionType.DigitalText) {
			cmbProduct.setVisible(false);
			cmbCDKey.setVisible(false);
			cmbCDKey2.setVisible(false);
			for(String server : dtServers)
				model.addElement(server);
			cmbServer.setSelectedItem(cs.server);
			return;
		}

		for(String server : bncsServers)
			model.addElement(server);
		cmbServer.setSelectedItem(cs.server);
		
		cmbProduct.setVisible(true);
		
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

		CDKey[] CDKeys2 = null;
		switch(cmbProduct.getSelectedIndex() + 1) {
		case ConnectionSettings.PRODUCT_DIABLO:
		case ConnectionSettings.PRODUCT_DIABLOSHAREWARE:
		case ConnectionSettings.PRODUCT_STARCRAFTSHAREWARE:
			cmbCDKey.setVisible(false);
			cmbCDKey2.setVisible(false);
			break;
		case ConnectionSettings.PRODUCT_JAPANSTARCRAFT:
		case ConnectionSettings.PRODUCT_STARCRAFT:
		case ConnectionSettings.PRODUCT_BROODWAR:
		case ConnectionSettings.PRODUCT_DIABLO2:
		case ConnectionSettings.PRODUCT_WARCRAFT3:
		case ConnectionSettings.PRODUCT_WAR2BNE:
			cmbCDKey.setVisible(true);
			cmbCDKey2.setVisible(false);
			break;
		case ConnectionSettings.PRODUCT_LORDOFDESTRUCTION:
		case ConnectionSettings.PRODUCT_THEFROZENTHRONE:
			cmbCDKey.setVisible(true);
			cmbCDKey2.setVisible(true);
			
			if((cmbProduct.getSelectedIndex() + 1) == ConnectionSettings.PRODUCT_LORDOFDESTRUCTION)
				CDKeys2 = KeyManager.getKeys(KeyManager.PRODUCT_D2XP);
			if((cmbProduct.getSelectedIndex() + 1) == ConnectionSettings.PRODUCT_THEFROZENTHRONE)
				CDKeys2 = KeyManager.getKeys(KeyManager.PRODUCT_W3XP);
			break;
		}

		model = (DefaultComboBoxModel)cmbCDKey.getModel();
		model.removeAllElements();
		if(prod != KeyManager.PRODUCT_ALLNORMAL) {
			for(CDKey key : KeyManager.getKeys(prod)) {
				model.addElement(key);

				if(key.getKey().equalsIgnoreCase(cs.cdkey))
					cmbCDKey.setSelectedItem(key);
			}
		}
		

		DefaultComboBoxModel model2 = (DefaultComboBoxModel)cmbCDKey2.getModel();
		model2.removeAllElements();
		if(CDKeys2 != null) {
			for(CDKey key : CDKeys2) {
				model2.addElement(key);

				if(key.getKey().equalsIgnoreCase(cs.cdkey2))
					cmbCDKey.setSelectedItem(key);
			}
		}
		
		pack();
	}
}
