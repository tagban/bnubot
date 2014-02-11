/**
 * This file is distributed under the GPL
 * $Id$
 */
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.bnubot.bot.gui.WindowPosition;
import net.bnubot.bot.gui.components.ConfigPanel;
import net.bnubot.bot.gui.components.ConfigValueChangeListener;
import net.bnubot.logging.Out;

/**
 * @author scotta
 */
public abstract class AbstractWizard implements ConfigValueChangeListener {

	private List<AbstractWizardPage> pages = new ArrayList<AbstractWizardPage>();

	private final JDialog jd;
	private final String title;
	private final JPanel cards;
	private final JButton btnBack;
	private final JButton btnNext;
	private final JButton btnFinish;
	private int currentStep = 0;

	public AbstractWizard(String title) {
		jd = new JDialog();
		jd.setTitle(title);
		this.title = title;

		final CardLayout cardLayout = new CardLayout();
		cards = new JPanel(cardLayout);

		btnBack = new JButton("< Back");
		btnNext = new JButton("Next >");
		btnFinish = new JButton("Finish");

		setEnableButtons();

		btnBack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cardLayout.show(cards, Integer.toString(--currentStep));
				setEnableButtons();
				pages.get(currentStep).display();
			}});

		btnNext.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AbstractWizardPage page = pages.get(currentStep);
				String error = page.isPageComplete();
				if(error == null) {
					cardLayout.show(cards, Integer.toString(++currentStep));
					setEnableButtons();
					pages.get(currentStep).display();
				} else {
					JOptionPane.showMessageDialog(jd,
							error,
							"Page is incomplete",
							JOptionPane.ERROR_MESSAGE);
				}
			}});

		btnFinish.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AbstractWizardPage page = pages.get(currentStep);
				try {
					String error = page.isPageComplete();
					if(error == null) {
						finish();
						jd.dispose();
					} else {
						JOptionPane.showMessageDialog(jd,
								error,
								"Page is incomplete",
								JOptionPane.ERROR_MESSAGE);
					}
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
			@Override
			public void run() {
				jd.setVisible(true);
			}});
	}

	public void addWizardPage(AbstractWizardPage page) {
		ConfigPanel cp = new ConfigPanel();
		cp.addConfigValueChangeListener(this);

		page.createComponent(cp);
		cards.add(Integer.toString(pages.size()), cp);
		jd.pack();

		pages.add(page);
		setEnableButtons();
	}

	public abstract void finish() throws Exception;

	@Override
	public void configValueChanged() {
		setEnableButtons();
	}

	private void setEnableButtons() {
		boolean first = (currentStep == 0);
		boolean last = (currentStep == pages.size() - 1);

		String pageComplete = null;
		if(pages.size() > 0) {
			AbstractWizardPage page = pages.get(currentStep);
			pageComplete = page.isPageComplete();
		}

		btnBack.setEnabled(!first);
		btnNext.setEnabled(!last && (pageComplete == null));

		setPageIncompleteError(pageComplete);

		if(last) {
			btnFinish.setEnabled(true);
		} else {
			// If we're not on the last page, determine if all pages are complete
			boolean done = true;
			for(AbstractWizardPage awp : pages) {
				try {
					if(awp.isPageComplete() != null)
						throw new Exception();
				} catch(Exception e) {
					done = false;
					break;
				}
			}
			// Enable the finish button if all pages are complete
			btnFinish.setEnabled(done);
		}
	}

	private void setPageIncompleteError(String pageComplete) {
		String title = this.title;
		if(pageComplete != null)
			title += " [" + pageComplete + "]";
		jd.setTitle(title);
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
