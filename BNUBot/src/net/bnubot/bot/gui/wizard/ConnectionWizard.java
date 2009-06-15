/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.gui.wizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import net.bnubot.bot.gui.components.ConfigPanel;
import net.bnubot.bot.gui.components.ConfigSpinner;
import net.bnubot.bot.gui.components.ConfigTextField;
import net.bnubot.bot.gui.components.GhostDefaultTextField;
import net.bnubot.bot.gui.components.ProductAndCDKeys;
import net.bnubot.bot.gui.settings.GlobalConfigurationFrame;
import net.bnubot.bot.gui.settings.OperationCancelledException;
import net.bnubot.settings.ConnectionSettings;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.settings.GlobalSettings.SOCKSType;

/**
 * @author scotta
 */
public class ConnectionWizard extends AbstractWizard {
	private static final long serialVersionUID = -5866297845418113235L;

	public static void main(String[] args) {
		GlobalSettings.load();
		new ConnectionWizard(1).displayAndBlock();
		System.exit(0);
	}

	public ConnectionWizard(int num) {
		super("Connection Wizard");

		final ConnectionSettings cs = new ConnectionSettings(num);

		addWizardPage(new AbstractWizardPage() {
			@Override
			public void createComponent(ConfigPanel cp) {
				cp.add(new JLabel("<html>" +
						"<h1>Introduction</h1>" +
						"<hr/><br/>" +
						"This is the connection configuration wizard. This wizard will assist you in setting up your<br/>" +
						"first connection to Battle.net. To begin, click Next." +
						"</html>"));
			}

			@Override
			public boolean isPageComplete() {
				return true;
			}});

		addWizardPage(new AbstractWizardPage() {
			@Override
			public void createComponent(ConfigPanel cp) {
				cp.add(new JLabel("<html>" +
						"<h1>Step 1</h1>" +
						"<hr/><br/>" +
						"The first step is to enter some CD keys. It's recommended to add all<br/>" +
						"your keys here, but you only need to enter as many as you want to use.<br/>" +
						"To enter keys, click the button below. If you've already performed this<br/>" +
						"step, you may skip it.<br/>" +
						"<br/></html>"));

				JButton keys = new JButton("Enter CD Keys");
				cp.add(keys);

				keys.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							new GlobalConfigurationFrame(true);
						} catch (OperationCancelledException e1) {
							// User pressed cancel
						}
					}});
			}

			@Override
			public boolean isPageComplete() {
				return true;
			}});

		addWizardPage(new AbstractWizardPage() {
			GhostDefaultTextField txtUsername;
			JPasswordField txtPassword;
			ProductAndCDKeys prodKeys;

			@Override
			public void createComponent(ConfigPanel cp) {
				cp.add(new JLabel("<html>" +
						"<h1>Step 2</h1>" +
						"<hr/><br/>" +
						"Enter the login details about your Battle.net account.<br/>" +
						"If the Battle.net account does not already exist, it will be created<br/>" +
						"for you automatically when you log in to Battle.net.<br/>" +
						"<br/></html>"));

				txtUsername = cp.makeGhost("Account", "BNU-Camel");
				txtPassword = cp.makePass("Password", null);
				prodKeys = new ProductAndCDKeys(cs, cp);

				if(cs.username != null)
					txtUsername.setText(cs.username);
				if(cs.password != null)
					txtPassword.setText(cs.password);
			}

			@Override
			public void display() {
				prodKeys.updateProducts();
			}

			@Override
			public boolean isPageComplete() {
				cs.username = txtUsername.getText();
				cs.password = new String(txtPassword.getPassword());
				cs.product = prodKeys.getProduct();
				cs.setCDKey(prodKeys.getCDKey());
				cs.setCDKey2(prodKeys.getCDKey2());

				// Validate the configuration
				String error = cs.isValid();
				if(error != null) {
					JOptionPane.showMessageDialog(txtUsername.getParent(),
							error,
							"Invalid configuration",
							JOptionPane.ERROR_MESSAGE);
					return false;
				}

				// Config was valid
				cs.save();
				return true;
			}});

		addWizardPage(new AbstractWizardPage() {
			JCheckBox chkProxyEnabled;
			JComboBox cmbProxyType;
			ConfigTextField txtProxyHost;
			ConfigSpinner spnProxyPort;

			@Override
			public void createComponent(ConfigPanel cp) {
				cp.add(new JLabel("<html>" +
						"<h1>Step 3</h1>" +
						"<hr/><br/>" +
						"Do you require a proxy to connect to battle.net? If so, enter the details<br/>" +
						"below. If not, you may skip this step.<br/>" +
						"<br/></html>"));

				chkProxyEnabled = cp.makeCheck("Enabled", GlobalSettings.socksEnabled);
				cmbProxyType = cp.makeCombo("Type", GlobalSettings.SOCKSType.values(), false);
				txtProxyHost = cp.makeText("Host", GlobalSettings.socksHost);
				spnProxyPort = cp.makeSpinner("Port", GlobalSettings.socksPort);
			}

			@Override
			public boolean isPageComplete() {
				GlobalSettings.socksEnabled = chkProxyEnabled.isSelected();
				GlobalSettings.socksType = (SOCKSType)cmbProxyType.getSelectedItem();
				GlobalSettings.socksHost = txtProxyHost.getText();
				GlobalSettings.socksPort = spnProxyPort.getValue().intValue();
				GlobalSettings.save();
				return true;
			}});

		addWizardPage(new AbstractWizardPage() {
			@Override
			public void createComponent(ConfigPanel cp) {
				cp.add(new JLabel("<html>" +
						"<h1>Conclusion</h1>" +
						"<hr/><br/>" +
						"You are now ready to connect to Battle.net! Click Finish to close<br/>" +
						"this window. The bot will automatically connect.<br/>" +
						"<br/></html>"));
			}

			@Override
			public boolean isPageComplete() {
				return true;
			}});
	}
}
