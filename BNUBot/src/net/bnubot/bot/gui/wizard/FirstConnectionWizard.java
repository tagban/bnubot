/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.gui.wizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;

import net.bnubot.bot.gui.components.ConfigPanel;
import net.bnubot.bot.gui.settings.GlobalConfigurationFrame;
import net.bnubot.bot.gui.settings.OperationCancelledException;
import net.bnubot.settings.ConnectionSettings;
import net.bnubot.settings.GlobalSettings;

/**
 * @author scotta
 */
public class FirstConnectionWizard extends AbstractWizard {
	public static void main(String[] args) {
		GlobalSettings.load();
		new FirstConnectionWizard(1).displayAndBlock();
		System.exit(0);
	}

	public FirstConnectionWizard(int num) {
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
			public String isPageComplete() {
				return null;
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
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							new GlobalConfigurationFrame(true);
						} catch (OperationCancelledException e1) {
							// User pressed cancel
						}
					}});
			}

			@Override
			public String isPageComplete() {
				return null;
			}});

		addWizardPage(new AccountDetailsWizardPage("Step 2", cs));

		addWizardPage(new ProxyDetailsWizardPage("Step 3"));

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
			public String isPageComplete() {
				return null;
			}});
	}

	@Override
	public void finish() throws Exception {
		// Nothing to do
	}
}
