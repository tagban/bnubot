/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui.wizard;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;

import net.bnubot.bot.gui.components.ConfigPanel;
import net.bnubot.bot.gui.components.GhostDefaultTextField;
import net.bnubot.db.Account;
import net.bnubot.db.BNLogin;
import net.bnubot.db.Rank;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetUser;

/**
 * @author scotta
 */
public class DatabaseWizard extends AbstractWizard {
	public static void main(String[] args) {
		GlobalSettings.load();
		new DatabaseWizard().displayAndBlock();
		System.exit(0);
	}

	private final GhostDefaultTextField[] accountName = new GhostDefaultTextField[1];
	private final GhostDefaultTextField[] bnLogins = new GhostDefaultTextField[5];

	public DatabaseWizard() {
		super("Database Wizard");

		addWizardPage(new AbstractWizardPage() {
			@Override
			public void createComponent(ConfigPanel cp) {
				cp.add(new JLabel("<html>" +
						"<h1>Introduction</h1>" +
						"<hr/><br/>" +
						"This is the database configuration wizard. This wizard will assist you in setting up your<br/>" +
						"database by helping you to configure your first account. To begin, click Next.<br/>" +
						"<br/>" +
						"If you do not wish to use the database, you may close this window now." +
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
						"Create a super-user account to store your battle.net handles.<br/>" +
						"You should not use your battle.net logon name for this, but a canonical name.<br/>" +
						"For example, my Battle.net account is BNU-Camel@Azeroth, so my account name is Camel.<br/>" +
						"I might also choose to use Scott for a more formal name. Users will use this canonical<br/>" +
						"name for things like sending you mail, so try to keep it simple.<br/>" +
						"<br/>" +
						"</html>"));
				accountName[0] = cp.makeGhost("Account", "Camel");
			}

			@Override
			public void display() {
				accountName[0].reset();
			}

			@Override
			public String isPageComplete() {
				if(accountName[0].isGhosted())
					return "Account name not set";
				return null;
			}});


		addWizardPage(new AbstractWizardPage() {
			@Override
			public void createComponent(ConfigPanel cp) {
				cp.add(new JLabel("<html>" +
						"<h1>Step 2</h1>" +
						"<hr/><br/>" +
						"Associate your battle.net handles with your account.<br/>" +
						"You must include the user namespace (the part following the @ symbol):<br/>" +
						"<ul><li>useast.battle.net: USEast / Azeroth</li>" +
						"<li>uswest.battle.net: USWest / Lordaeron</li>" +
						"<li>europe.battle.net: Europe / Northrend</li>" +
						"<li>asia.battle.net: Asia / Kalimdor</li></ul>" +
						"You may leave boxes blank if you have fewer than five handles.<br/>" +
						"<br/>" +
						"</html>"));
				bnLogins[0] = cp.makeGhost("BNet Login 1", "BNU-Camel@USEast");
				bnLogins[1] = cp.makeGhost("BNet Login 2", "BNU-Camel@Azeroth");
				bnLogins[2] = cp.makeGhost("BNet Login 3", "BNU-Camel@USWest");
				bnLogins[3] = cp.makeGhost("BNet Login 4", "BNU-Camel@Lordaeron");
				bnLogins[4] = cp.makeGhost("BNet Login 5", "");
			}

			@Override
			public void display() {
				for(GhostDefaultTextField l : bnLogins)
					l.reset();
			}

			@Override
			public String isPageComplete() {
				for(GhostDefaultTextField l : bnLogins)
					if(!l.isGhosted())
						return null;
				return "No BNLogins specified";
			}});


		addWizardPage(new AbstractWizardPage() {
			@Override
			public void createComponent(ConfigPanel cp) {
				cp.add(new JLabel("<html>" +
						"<h1>Conclusion</h1>" +
						"<hr/><br/>" +
						"When you click Finish, the new data will be added to the database.<br/>" +
						"<br/>" +
						"To create more accounts, you can use commands directly through the bot window.<br/>" +
						"<br/>" +
						"Example:<br/>" +
						"/createaccount Camel<br/>" +
						"/setaccount BNU-Camel@Azeroth Camel<br/>" +
						"<br/>" +
						"You can also use the shortened versions (aliases) of these commands:<br/>" +
						"/ca Camel<br/>" +
						"/sa BNU-Camel@Azeroth Camel<br/>" +
						"<br/>" +
						"Be sure to check out the database editors, on the Database menu, as well.<br/>" +
						"You can customize greetings, autopromtions, and how long bnet logins remain<br/>" +
						"in the database before being deleted (expireDays) with the rank editor.<br/>" +
						"</html>"));
			}

			@Override
			public String isPageComplete() {
				return null;
			}});
	}

	@Override
	public void finish() throws Exception {
		List<String> logins = new ArrayList<String>(bnLogins.length);
		for(GhostDefaultTextField l : bnLogins) {
			if(l.isGhosted())
				continue;
			String login = l.getText();
			if(login.length() <= 0)
				continue;
			if(login.indexOf('@') == -1)
				throw new Exception("Invalid BNetLogin: " + login);

			logins.add(login);
		}

		String account = accountName[0].getText();
		Account a = Account.get(account);
		if(a == null)
			a = Account.create(account, Rank.getMax(), null);
		a.updateRow();
		for(String l : logins) {
			BNLogin bnl = BNLogin.getCreate(new BNetUser(l));
			bnl.setAccount(a);
			bnl.updateRow();
		}
	}
}
