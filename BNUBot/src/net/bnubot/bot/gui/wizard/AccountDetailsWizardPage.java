/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.gui.wizard;

import javax.swing.JLabel;
import javax.swing.JPasswordField;

import net.bnubot.bot.gui.components.ConfigPanel;
import net.bnubot.bot.gui.components.GhostDefaultTextField;
import net.bnubot.bot.gui.components.ProductAndCDKeys;
import net.bnubot.settings.ConnectionSettings;

/**
 * @author scotta
 */
public class AccountDetailsWizardPage extends AbstractWizardPage {
	private final String header;
	private final ConnectionSettings cs;

	private GhostDefaultTextField txtUsername;
	private JPasswordField txtPassword;
	private ProductAndCDKeys prodKeys;

	public AccountDetailsWizardPage(String header, ConnectionSettings cs) {
		this.header = header;
		this.cs = cs;
	}

	@Override
	public void createComponent(ConfigPanel cp) {
		cp.add(new JLabel("<html>" +
				"<h1>" + header + "</h1>" +
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
	public String isPageComplete() {
		if(txtUsername.isGhosted())
			return "Username not set";

		cs.username = txtUsername.getText();
		cs.password = new String(txtPassword.getPassword());
		cs.product = prodKeys.getProduct();
		cs.setCDKey(prodKeys.getCDKey());
		cs.setCDKey2(prodKeys.getCDKey2());

		// Validate the configuration
		String error = cs.isValid();
		if(error != null)
			return error;

		// Config was valid
		cs.save();
		return null;
	}
}
