/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.gui.wizard;

import net.bnubot.settings.ConnectionSettings;
import net.bnubot.settings.GlobalSettings;

/**
 * @author scotta
 */
public class NewProfileWizard extends AbstractWizard {

	public static void main(String[] args) {
		GlobalSettings.load();
		new NewProfileWizard(new ConnectionSettings(1)).displayAndBlock();
		System.exit(0);
	}

	public NewProfileWizard(ConnectionSettings cs) {
		super("Create profile");
		addWizardPage(new AccountDetailsWizardPage("Battle.net Account", cs));
	}

	@Override
	public void finish() throws Exception {
		// Nothing to do
	}

}
