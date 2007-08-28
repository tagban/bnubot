/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.bot.gui;

import java.awt.FlowLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;

import net.bnubot.vercheck.CurrentVersion;

public class AboutWindow extends JDialog {
	private static final long serialVersionUID = -5776139820198349083L;

	public AboutWindow() {
		initializeGUI();
		setTitle("About BNU-Bot");
		
		pack();
		setAlwaysOnTop(true);
		setVisible(true);
	}

	public void initializeGUI() {
		setLayout(new FlowLayout(FlowLayout.CENTER));

		Box b = new Box(BoxLayout.Y_AXIS);
		{
			b.add(new JLabel("BNU-Bot v" + CurrentVersion.version()));
			b.add(new JLabel("Built " + CurrentVersion.version().getBuildDate()));
			b.add(new JLabel("Created by BNU-Camel"));
			b.add(Box.createVerticalStrut(15));
			b.add(new JLabel("Special thanks to:"));
			b.add(new JLabel("Google: project hosting"));
			b.add(new JLabel("BNU-Fantasma: Alpha testing"));
			b.add(new JLabel("BNU-Sorceress: Alpha testing"));
			b.add(Box.createVerticalStrut(15));
			b.add(new JLabel("Contributors:"));
			b.add(new JLabel("HDX, the JBLS project"));
			b.add(new JLabel("Protege-2000: browser launcher"));
			b.add(Box.createVerticalStrut(15));
			b.add(new JLabel("This project is distributed under the"));
			b.add(new JLabel("GNU Public License, Version 2."));
			b.add(Box.createVerticalStrut(15));
			b.add(new JLabel("Want to contribute?"));
			b.add(new JLabel("Visit the project website:"));
			b.add(new JLabel("http://code.google.com/p/bnubot/"));
		}
		add(b);
	}
}