/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.gui.wizard;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;

import net.bnubot.bot.gui.WindowPosition;
import net.bnubot.bot.gui.components.ConfigCheckBox;
import net.bnubot.bot.gui.components.ConfigFactory;
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
import net.bnubot.util.Out;

/**
 * @author scotta
 */
public class ConnectionWizard extends JDialog {
	private static final long serialVersionUID = -5866297845418113235L;

	public static void main(String[] args) {
		GlobalSettings.load();
		displayAndBlock(1);
		System.exit(0);
	}

	public static void displayAndBlock(int num) {
		ConnectionWizard dw = new ConnectionWizard(num);
		dw.setVisible(true);
		while(dw.isVisible()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
			Thread.yield();
		}
	}

	private int currentStep = 0;
	public ConnectionWizard(int num) {
		final ConnectionSettings cs = new ConnectionSettings(num);

		final CardLayout cardLayout = new CardLayout();
		final JPanel cards = new JPanel(cardLayout);

		ConfigPanel jp = new ConfigPanel();
		jp.add(new JLabel("<html>" +
					"<h1>Introduction</h1>" +
					"<hr/><br/>" +
					"This is the connection configuration wizard. This wizard will assist you in setting up your<br/>" +
					"first connection to Battle.net. To begin, click Next." +
					"</html>"));
		cards.add("0", jp);

		jp = new ConfigPanel();
		{
			jp.add(new JLabel("<html>" +
					"<h1>Step 1</h1>" +
					"<hr/><br/>" +
					"The first step is to enter some CD keys. It's recommended to add all<br/>" +
					"your keys here, but you only need to enter as many as you want to use.<br/>" +
					"To enter keys, click the button below. If you've already performed this<br/>" +
					"step, you may skip it.<br/>" +
					"<br/></html>"));

			JButton keys = new JButton("Enter CD Keys");
			jp.add(keys);

			keys.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						new GlobalConfigurationFrame(true);
					} catch (OperationCancelledException e1) {
						// User pressed cancel
					}
				}});
		}
		cards.add("1", jp);

		final GhostDefaultTextField txtUsername;
		final JPasswordField txtPassword;
		final ProductAndCDKeys prodKeys;

		jp = new ConfigPanel();
		{
			jp.add(new JLabel("<html>" +
					"<h1>Step 2</h1>" +
					"<hr/><br/>" +
					"Enter the login details about your Battle.net account.<br/>" +
					"If the Battle.net account does not already exist, it will be created<br/>" +
					"for you automatically when you log in to Battle.net.<br/>" +
					"<br/></html>"));

			txtUsername = ConfigFactory.makeGhost("Account", "BNU-Camel", jp);
			txtPassword = ConfigFactory.makePass("Password", null, jp);
			prodKeys = new ProductAndCDKeys(cs, jp);

			if(cs.username != null)
				txtUsername.setText(cs.username);
			if(cs.password != null)
				txtPassword.setText(cs.password);
		}
		cards.add("2", jp);

		final ConfigCheckBox chkProxyEnabled;
		final JComboBox cmbProxyType;
		final ConfigTextField txtProxyHost;
		final ConfigSpinner spnProxyPort;

		jp = new ConfigPanel();
		{
			jp.add(new JLabel("<html>" +
					"<h1>Step 3</h1>" +
					"<hr/><br/>" +
					"Do you require a proxy to connect to battle.net? If so, enter the details<br/>" +
					"below. If not, you may skip this step.<br/>" +
					"<br/></html>"));

			chkProxyEnabled = new ConfigCheckBox("Enabled", GlobalSettings.socksEnabled);
			jp.add(chkProxyEnabled);
			cmbProxyType = ConfigFactory.makeCombo("Type", GlobalSettings.SOCKSType.values(), false, jp);
			txtProxyHost = ConfigFactory.makeText("Host", GlobalSettings.socksHost, jp);
			spnProxyPort = ConfigFactory.makeSpinner("Port", GlobalSettings.socksPort, jp);
		}
		cards.add("3", jp);

		jp = new ConfigPanel();
		{
			jp.add(new JLabel("<html>" +
					"<h1>Conclusion</h1>" +
					"<hr/><br/>" +
					"You are now ready to connect to Battle.net! Click Finish to close<br/>" +
					"this window. The bot will automatically connect.<br/>" +
					"<br/></html>"));
		}
		cards.add("4", jp);

		final JButton btnBack = new JButton("< Back");
		btnBack.setEnabled(false);
		final JButton btnNext = new JButton("Next >");
		final JButton btnFinish = new JButton("Finish");
		btnFinish.setEnabled(false);

		btnBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				currentStep--;
				cardLayout.show(cards, Integer.toString(currentStep));
				switch(currentStep) {
				case 0:
					btnBack.setEnabled(false);
					break;
				case 3:
					btnNext.setEnabled(true);
					btnFinish.setEnabled(false);
					break;
				}
			}});

		btnNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switch(currentStep) {
				case 0:
					btnBack.setEnabled(true);
					break;
				case 1:
					// TODO: validate that there are some keys
					prodKeys.updateProducts();
					break;
				case 2:
					cs.username = txtUsername.getText();
					cs.password = new String(txtPassword.getPassword());
					cs.product = prodKeys.getProduct();
					cs.setCDKey(prodKeys.getCDKey());
					cs.setCDKey2(prodKeys.getCDKey2());

					// Validate the configuration
					String error = cs.isValid();
					if(error != null) {
						JOptionPane.showMessageDialog(ConnectionWizard.this,
								error,
								"Invalid configuration",
								JOptionPane.ERROR_MESSAGE);
						return;
					}

					// Config was valid
					cs.save();
					break;
				case 3:
					GlobalSettings.socksEnabled = chkProxyEnabled.isSelected();
					GlobalSettings.socksType = (SOCKSType)cmbProxyType.getSelectedItem();
					GlobalSettings.socksHost = txtProxyHost.getText();
					GlobalSettings.socksPort = spnProxyPort.getValue().intValue();
					GlobalSettings.save();

					btnNext.setEnabled(false);
					btnFinish.setEnabled(true);
					break;
				}
				cardLayout.show(cards, Integer.toString(++currentStep));
			}});

		btnFinish.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					String error = cs.isValid();
					if(error != null)
						throw new Exception(error);
					dispose();
				} catch(Exception e) {
					Out.popupException(e, ConnectionWizard.this);
				}
			}});

		Box boxButtons = new Box(BoxLayout.X_AXIS);
		boxButtons.add(btnBack);
		boxButtons.add(Box.createHorizontalGlue());
		boxButtons.add(btnNext);
		boxButtons.add(btnFinish);

		Box boxAll = new Box(BoxLayout.Y_AXIS);
		boxAll.add(cards);
		boxAll.add(boxButtons);
		add(boxAll);

		setTitle("Connection Wizard");
		setModal(true);
		setResizable(true);

		pack();
		WindowPosition.load(this);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setVisible(true);
			}});
	}
}
