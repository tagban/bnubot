package net.bnubot.bot.gui.wizard;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.bnubot.bot.gui.WindowPosition;
import net.bnubot.bot.gui.components.ConfigPanel;
import net.bnubot.util.Out;

public class AbstractWizard {

	private List<AbstractWizardPage> pages = new ArrayList<AbstractWizardPage>();

	private JDialog jd = new JDialog();
	final JPanel cards;

	private int currentStep = 0;

	public AbstractWizard(String title) {
		final CardLayout cardLayout = new CardLayout();
		cards = new JPanel(cardLayout);

		final JButton btnBack = new JButton("< Back");
		btnBack.setEnabled(false);
		final JButton btnNext = new JButton("Next >");
		final JButton btnFinish = new JButton("Finish");
		btnFinish.setEnabled(false);

		btnBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(currentStep == pages.size()-1) {
					btnNext.setEnabled(true);
					btnFinish.setEnabled(false);
				}

				cardLayout.show(cards, Integer.toString(--currentStep));
				pages.get(currentStep).display();

				if(currentStep == 0)
					btnBack.setEnabled(false);
			}});

		btnNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AbstractWizardPage page = pages.get(currentStep);
				if(page.isPageComplete()) {
					if(currentStep == 0)
						btnBack.setEnabled(true);

					cardLayout.show(cards, Integer.toString(++currentStep));
					pages.get(currentStep).display();

					if(currentStep == pages.size()-1) {
						btnNext.setEnabled(false);
						btnFinish.setEnabled(true);
					}
				}
			}});

		btnFinish.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AbstractWizardPage page = pages.get(currentStep);
				try {
					if(page.isPageComplete())
						jd.dispose();
				} catch(Exception ex) {
					Out.popupException(ex);
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
		jd.add(boxAll);

		jd.setTitle(title);
		jd.setModal(true);
		jd.setResizable(true);

		jd.pack();
		WindowPosition.load(jd);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				jd.setVisible(true);
			}});
	}

	public void addWizardPage(AbstractWizardPage page) {
		ConfigPanel cp = new ConfigPanel();
		page.createComponent(cp);
		cards.add(Integer.toString(pages.size()), cp);
		jd.pack();

		pages.add(page);
	}

	public void displayAndBlock() {
		jd.setVisible(true);
		while(jd.isVisible()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
			Thread.yield();
		}
	}
}
