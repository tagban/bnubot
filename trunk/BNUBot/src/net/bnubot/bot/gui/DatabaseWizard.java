/**
 *
 */
package net.bnubot.bot.gui;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.bnubot.bot.gui.components.ConfigFactory;
import net.bnubot.settings.GlobalSettings;

/**
 * @author sanderson
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

	private class Centralizer extends Box {
		private static final long serialVersionUID = -7876228470253276798L;

		public Centralizer(Component contents) {
			super(BoxLayout.X_AXIS);

			Box b = new Box(BoxLayout.Y_AXIS);
			b.add(Box.createVerticalGlue());
			b.add(contents);
			b.add(Box.createVerticalGlue());

			add(Box.createHorizontalGlue());
			add(b);
			add(Box.createHorizontalGlue());
		}
	}

	private int currentStep = 0;
	public DatabaseWizard() {
		final CardLayout cardLayout = new CardLayout();
		final JPanel cards = new JPanel(cardLayout);

		JPanel jp = new JPanel();
		jp.add(new JLabel("<html>" +
				"<h1>Introduction</h1>" +
				"<hr/><br/>" +
				"This is the database configuration wizard." +
				"</html>"));
		cards.add("0", new Centralizer(jp));

		jp = new JPanel();
		{
			jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));
			jp.add(new JLabel("<html>" +
					"<h1>Step 1</h1>" +
					"<hr/><br/>" +
					"Create a super-user account to store your battle.net handles.<br/>" +
					"You should not use your battle.net logon name for this.<br/>" +
					"</html>"));
			/*final ConfigTextField accountName =*/ ConfigFactory.makeText("Account", "Example", jp);
		}
		cards.add("1", new Centralizer(jp));

		jp = new JPanel();
		jp.add(new JLabel("step 2 46895"));
		cards.add("2", new Centralizer(jp));

		jp = new JPanel();
		jp.add(new JLabel("step 3 46895"));
		cards.add("3", new Centralizer(jp));

		final JButton btnBack = new JButton("< Back");
		btnBack.setEnabled(false);
		final JButton btnNext = new JButton("Next >");
		final JButton btnFinish = new JButton("Finish");

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
			public void actionPerformed(ActionEvent e) {
				dispose();
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
