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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;

import net.bnubot.bot.gui.WindowPosition;
import net.bnubot.bot.gui.components.ConfigPanel;
import net.bnubot.bot.gui.components.ConfigTextField;
import net.bnubot.bot.gui.components.ProductAndCDKeys;
import net.bnubot.settings.ConnectionSettings;
import net.bnubot.settings.ConnectionSettings.ConnectionType;

/**
 * @author scotta
 */
public class ConfigurationFrame extends JDialog {
	private static final long serialVersionUID = 1308177934480442149L;

	private final ConnectionSettings cs;
	private boolean pressedCancel = false;

	// Connection
	private ConfigTextField txtProfile = null;
	private ConfigTextField txtUsername = null;
	private JPasswordField txtPassword = null;
	private ProductAndCDKeys prodKeys = null;
	private JCheckBox chkPlug = null;
	private JCheckBox chkBotNet = null;

	// Profile
	private JComboBox<ConnectionType> cmbConnectionType = null;
	private JComboBox<String> cmbServer = null;
	private ConfigTextField txtChannel = null;
	private ConfigTextField txtTrigger = null;
	private JCheckBox chkAntiIdle = null;
	private ConfigTextField txtAntiIdle = null;
	private ConfigTextField txtAntiIdleTimer = null;
	private JCheckBox chkGreetings = null;

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
		ConfigPanel boxSettings = new ConfigPanel();
		{
			txtProfile = boxSettings.makeText("Profile", cs.profile);
			txtUsername = boxSettings.makeText("Username", cs.username);
			txtPassword = boxSettings.makePass("Password", cs.password);

			prodKeys = new ProductAndCDKeys(cs, boxSettings);
			prodKeys.addProductListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					setVisibleFields();
				}});
			chkPlug = boxSettings.makeCheck("Enable Plug (No UDP support)", cs.enablePlug);
			chkBotNet = boxSettings.makeCheck("Enable BotNet", cs.enableBotNet);

			cmbConnectionType = boxSettings.makeCombo("Connection Type", ConnectionType.values(), false);
			cmbConnectionType.setSelectedItem(cs.connectionType);
			cmbConnectionType.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					setVisibleFields();
				}});

			cmbServer = boxSettings.makeCombo("Server", (new String[] {}), false);
			cmbServer.setSelectedItem(cs.server);

			setVisibleFields();

			txtChannel = boxSettings.makeText("Channel", cs.channel);
			txtTrigger = boxSettings.makeText("Trigger", cs.trigger);

			{
				ConfigPanel boxLine = boxSettings.makePanel("Anti-Idle");
				chkAntiIdle = boxLine.makeCheck("Enabled", cs.enableAntiIdle);
				txtAntiIdle = boxLine.makeText("Message", cs.antiIdle);
			}

			txtAntiIdleTimer = boxSettings.makeText("Anti-Idle Timer", Integer.toString(cs.antiIdleTimer));
			chkGreetings = boxSettings.makeCheck("Enable Greetings", cs.enableGreetings);

			boxSettings.add(Box.createVerticalGlue());
		}
		boxAll.add(boxSettings);

		boxAll.add(Box.createVerticalStrut(10));

		Box boxButtons = new Box(BoxLayout.X_AXIS);
		{
			btnKeys = new JButton("Key Editor");
			btnKeys.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent act) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
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
		boolean isBNCS = ((ConnectionType)cmbConnectionType.getSelectedItem()).equals(ConnectionType.BNCS);
		if(!hasCdKeys && isBNCS) {
			// Offer cd key window
			JOptionPane.showMessageDialog(this,
					"You have no CD keys.",
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

	private void save() {
		cs.profile = txtProfile.getText();
		cs.username = txtUsername.getText();
		cs.password = new String(txtPassword.getPassword());
		cs.enablePlug = chkPlug.isSelected();
		cs.enableBotNet = chkBotNet.isSelected();
		cs.product = prodKeys.getProduct();
		cs.setCDKey(prodKeys.getCDKey());
		cs.setCDKey2(prodKeys.getCDKey2());
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
		chkPlug.setSelected(cs.enablePlug);
		chkBotNet.setSelected(cs.enableBotNet);
		prodKeys.setProduct(cs.product);
		prodKeys.setCDKey(cs.cdkey);
		prodKeys.setCDKey2(cs.cdkey2);

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
		DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>)cmbServer.getModel();
		model.removeAllElements();

		if((ConnectionType)cmbConnectionType.getSelectedItem() == ConnectionType.DigitalText) {
			chkPlug.setVisible(false);
			chkBotNet.setVisible(false);
			prodKeys.setVisible(false);
			for(String server : ConnectionSettings.dtServers)
				model.addElement(server);
			cmbServer.setSelectedItem(cs.server);
			return;
		}

		for(String server : ConnectionSettings.bncsServers)
			model.addElement(server);
		cmbServer.setSelectedItem(cs.server);

		prodKeys.setVisible(true);
		switch(prodKeys.getProduct()) {
		case DSHR:
		case DRTL:
		case SSHR:
		case JSTR:
		case STAR:
		case SEXP:
		case W2BN:
			chkPlug.setVisible(true);
			break;
		default:
			chkPlug.setVisible(false);
			break;
		}
		chkBotNet.setVisible(true);

		pack();
	}
}
