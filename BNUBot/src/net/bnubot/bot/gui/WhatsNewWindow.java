/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.JTextComponent;

import net.bnubot.bot.gui.components.ColoredTextArea;
import net.bnubot.settings.Settings;
import net.bnubot.util.SHA1Sum;
import net.bnubot.vercheck.CurrentVersion;

/**
 * A JDialog to parse and display the changelog
 * @author scotta
 */
public class WhatsNewWindow extends JDialog {
	private static final long serialVersionUID = -2905017328939505262L;

	public static void displayIfNew() {
		String whatsNew = WhatsNewWindow.getText();
		String currentVersionHash = null;
		try {
			currentVersionHash = new SHA1Sum(whatsNew.getBytes()).toString();
		} catch (Exception e) {
			// SHA1 not available? Unlikely, but whatever...
			currentVersionHash = "[" + whatsNew.length() + "bytes]";
		}
		String lastWhatsNewHash = Settings.getSection(null).read("whatsNewHash", (String)null);
		if((lastWhatsNewHash == null)
				|| !lastWhatsNewHash.equalsIgnoreCase(currentVersionHash)) {
			Settings.getSection(null).write("whatsNewHash", currentVersionHash);
			Settings.store();
			WhatsNewWindow jd = new WhatsNewWindow();
			while(jd.isVisible()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {}
				Thread.yield();
			}
		}
	}

	public WhatsNewWindow() {
		final CardLayout cardLayout = new CardLayout();
		final JPanel cards = new JPanel(cardLayout);
		DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>();
		final String[] changeLog = getText().split("\n\n");

		// Split up the change log by version
		for(String entry : changeLog) {
			String[] data = entry.split("\n", 2);
			JTextComponent jta = new ColoredTextArea();
			jta.setText(data[1]);
			jta.setEditable(false);
			cards.add(data[0], new JScrollPane(jta));
			model.addElement(data[0]);
		}

		JComboBox<String> cb = new JComboBox<String>(model);
		cb.setEditable(false);
		cb.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				cardLayout.show(cards, (String)e.getItem());
			}});
		JPanel comboBoxPane = new JPanel();
		comboBoxPane.add(cb);

		JButton btnOK = new JButton("OK");
		btnOK.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}});

		Box boxAll = new Box(BoxLayout.Y_AXIS);
		boxAll.add(comboBoxPane);
		boxAll.add(cards);
		boxAll.add(btnOK);
		add(boxAll);

		setTitle("What's new in BNU-Bot " + CurrentVersion.version().toString());
		setModal(true);
		setResizable(true);
		setPreferredSize(new Dimension(600, 400));

		pack();

		// Center the dialog on the screen
		Rectangle bounds = getBounds();
		Dimension size = getToolkit().getScreenSize();
		setLocation((size.width - bounds.width) / 2,
				(size.height - bounds.height) / 2);

		// Show the dialog
		setVisible(true);
	}

	public static String getText() {
		try {
			InputStream changelog;
			if(CurrentVersion.fromJar())
				changelog = WhatsNewWindow.class.getResource("/changelog.txt").openStream();
			else
				changelog = new FileInputStream(new File("changelog.txt"));
			byte[] data = new byte[0x100];
			String out = new String();

			int len;
			do {
				len = changelog.read(data);
				if(len == -1)
					break;
				out += new String(data, 0, len);
			} while(len > 0);

			return out;
		} catch(Exception e) {
			return e.getClass().getSimpleName() + ": " + e.getMessage();
		}
	}
}
