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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;

import net.bnubot.bot.gui.KeyManager.CDKey;
import net.bnubot.bot.gui.components.ConfigComboBox;
import net.bnubot.bot.gui.components.ConfigTextArea;
import net.bnubot.settings.ConnectionSettings;

import org.jbls.util.Constants;

public class ConfigurationFrame extends JDialog {
	private static final long serialVersionUID = 1308177934480442149L;

	ConnectionSettings cs;

	//Connection
	ConfigTextArea txtUsername = null;
	JPasswordField txtPassword = null;
	ConfigTextArea txtEmail = null;
	ConfigComboBox cmbProduct = null;
	ConfigComboBox cmbCDKey = null;
	ConfigComboBox cmbCDKeyLOD = null;
	ConfigComboBox cmbCDKeyTFT = null;
	ConfigTextArea txtBNCSServer = null;
	ConfigTextArea txtChannel = null;
	JButton btnLoad = null;
	JButton btnOK = null;
	JButton btnCancel = null;

	public ConfigurationFrame(ConnectionSettings cs) {
		super();
		this.cs = cs;
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

	private JPasswordField makePass(String label, String value, Box parent) {
		JLabel jl = new JLabel(label);
		jl.setPreferredSize(maxSize);

		JPasswordField pass = new JPasswordField(value);

		Box boxLine = new Box(BoxLayout.X_AXIS);
		boxLine.add(jl);
		boxLine.add(pass);
		parent.add(boxLine);

		return pass;
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
		Box boxAll = new Box(BoxLayout.Y_AXIS);
		boolean addConnectionStuff = true;
		ConnectionStuff: {
			Box boxSettings = new Box(BoxLayout.Y_AXIS);
			{
				txtUsername = makeText("Username", cs.username, boxSettings);
				txtPassword = makePass("Password", cs.password, boxSettings);
				txtEmail = makeText("Email", cs.email, boxSettings);

				cmbProduct = makeCombo("Product", Constants.prodsDisplay, false, boxSettings);
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
				//Initialize CD Keys combo box before setting product

				CDKey[] CDKeys = KeyManager.getKeys(KeyManager.PRODUCT_ALLNORMAL);
				if(CDKeys.length == 0) {
					JOptionPane.showMessageDialog(this,
							"You have no CD keys in cdkeys.txt.",
							"Error",
							JOptionPane.ERROR_MESSAGE);
					addConnectionStuff = false;
					break ConnectionStuff;
				}
				cmbCDKey = makeCombo("CD key", CDKeys, false, boxSettings);
				
				cmbProduct.setSelectedIndex(cs.product - 1);
				cmbCDKey.setSelectedItem(cs.cdkey);

				CDKeys = KeyManager.getKeys(KeyManager.PRODUCT_D2XP);
				cmbCDKeyLOD = makeCombo("LOD key", CDKeys, false, boxSettings);
				cmbCDKeyLOD.setSelectedItem(cs.cdkeyLOD);

				CDKeys = KeyManager.getKeys(KeyManager.PRODUCT_W3XP);
				cmbCDKeyTFT = makeCombo("TFT key", CDKeys, false, boxSettings);
				cmbCDKeyTFT.setSelectedItem(cs.cdkeyTFT);

				txtBNCSServer = makeText("Battle.net Server", cs.bncsServer, boxSettings);
				txtChannel = makeText("Channel", cs.channel, boxSettings);

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
			add(boxAll);
		else {
			new GlobalConfigurationFrame().setVisible(true);
			return;
		}
		pack();

		Dimension size = this.getSize();
		if((size.height > 300) || (size.width > 400))
			this.setSize(Math.min(400, size.width), Math.min(300, size.height));
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
		cs.channel = txtChannel.getText();

		cs.save();
	}

	private void load() {
		cs.load();
		
		txtUsername.setText(cs.username);
		txtPassword.setText(cs.password);
		txtEmail.setText(cs.email);
		cmbProduct.setSelectedIndex(cs.product - 1);
		cmbCDKey.setSelectedItem(cs.cdkey);
		cmbCDKeyLOD.setSelectedItem(cs.cdkeyLOD);
		cmbCDKeyTFT.setSelectedItem(cs.cdkeyTFT);
		txtBNCSServer.setText(cs.bncsServer);
		txtChannel.setText(cs.channel);
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
