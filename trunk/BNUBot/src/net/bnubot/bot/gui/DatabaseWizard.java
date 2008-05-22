/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.bnubot.bot.gui.components.ConfigFactory;
import net.bnubot.bot.gui.components.ConfigTextField;
import net.bnubot.db.Account;
import net.bnubot.db.BNLogin;
import net.bnubot.db.Rank;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetUser;
import net.bnubot.util.Out;

/**
 * @author scotta
 *
 */
public class DatabaseWizard extends JDialog {
	private static final long serialVersionUID = -3827493801847545042L;

	public static void main(String[] args) {
		GlobalSettings.load();
		DatabaseWizard dw = new DatabaseWizard();
		dw.setVisible(true);
		while(dw.isVisible()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
			Thread.yield();
		}
		System.exit(0);
	}

	private int currentStep = 0;
	public DatabaseWizard() {
		final CardLayout cardLayout = new CardLayout();
		final JPanel cards = new JPanel(cardLayout);

		JPanel jp = new JPanel();
		jp.add(new JLabel("<html>" +
					"<h1>Introduction</h1>" +
					"<hr/><br/>" +
					"This is the database configuration wizard. This wizard will assist you in setting up your<br/>" +
					"database by helping you to configure your first account. To begin, click Next." +
					"</html>"));
		cards.add("0", jp);

		final ConfigTextField accountName;
		final ConfigTextField[] bnLogins = new ConfigTextField[5];

		jp = new JPanel();
		{
			jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));
			jp.add(new JLabel("<html>" +
					"<h1>Step 1</h1>" +
					"<hr/><br/>" +
					"Create a super-user account to store your battle.net handles.<br/>" +
					"You should not use your battle.net logon name for this, but a canonical name.<br/>" +
					"For example, my Battle.net account is BNU-Camel@Azeroth, so my account name is Camel.<br/>" +
					"I might also choose to use Scott for a more formal name. Users will use this canonical<br/>" +
					"name for things like sending you mail, so try to keep it simple.<br/>" +
					"<br/>" +
					"</html>"));
			accountName = ConfigFactory.makeText("Account", "Camel", jp);
		}
		cards.add("1", jp);

		jp = new JPanel();
		{
			jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));
			jp.add(new JLabel("<html>" +
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
			bnLogins[0] = ConfigFactory.makeText("BNet Login 1", "BNU-Camel@USEast", jp);
			bnLogins[1] = ConfigFactory.makeText("BNet Login 2", "BNU-Camel@Azeroth", jp);
			bnLogins[2] = ConfigFactory.makeText("BNet Login 3", "BNU-Camel@USWest", jp);
			bnLogins[3] = ConfigFactory.makeText("BNet Login 4", "BNU-Camel@Lordaeron", jp);
			bnLogins[4] = ConfigFactory.makeText("BNet Login 5", "", jp);
		}
		cards.add("2", jp);

		jp = new JPanel();
		{
			jp.add(new JLabel("<html>" +
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
					"/sa BNU-CameL@Azeroth Camel<br/>" +
					"<br/>" +
					"Be sure to check out the database editors, on the Database menu, as well.<br/>" +
					"You can customize greetings, autopromtions, and how long bnet logins remain<br/>" +
					"in the database before being deleted (expireDays) with the rank editor.<br/>" +
					"</html>"));
		}
		cards.add("3", jp);

		final JButton btnBack = new JButton("< Back");
		btnBack.setEnabled(false);
		final JButton btnNext = new JButton("Next >");
		final JButton btnFinish = new JButton("Finish");
		btnFinish.setEnabled(false);

		btnBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				currentStep--;
				cardLayout.show(cards, Integer.toString(currentStep));
				if(currentStep == 0)
					btnBack.setEnabled(false);
				if(currentStep == 2) {
					btnNext.setEnabled(true);
					btnFinish.setEnabled(false);
				}
			}});

		btnNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				currentStep++;
				cardLayout.show(cards, Integer.toString(currentStep));
				if(currentStep == 1)
					btnBack.setEnabled(true);
				if(currentStep == 3) {
					btnNext.setEnabled(false);
					btnFinish.setEnabled(true);
				}
			}});

		btnFinish.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					String account = accountName.getText();
					List<String> logins = new ArrayList<String>(bnLogins.length);
					for(ConfigTextField l : bnLogins) {
						String login = l.getText();
						if(login.length() <= 0)
							continue;
						if(login.indexOf('@') == -1) {
							JOptionPane.showMessageDialog(DatabaseWizard.this,
									"Invalid BNetLogin: " + login,
									"Invalid BNetLogin",
									JOptionPane.ERROR_MESSAGE);
							return;
						}

						logins.add(login);
					}

					Account a = Account.get(account);
					if(a == null)
						a = Account.create(account, Rank.getMax(), null);
					a.updateRow();
					for(String l : logins) {
						BNLogin bnl = BNLogin.getCreate(new BNetUser(null, l, l));
						bnl.setAccount(a);
						bnl.updateRow();
					}

					dispose();
				} catch(Exception e) {
					Out.popupException(e, DatabaseWizard.this);
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

		setTitle("Database Wizard");
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
