/**
 * This file is distributed under the GPL 
 * $Id$
 */

package bnubot.bot.gui;

import java.awt.FlowLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;

import bnubot.vercheck.CurrentVersion;

@SuppressWarnings("serial")
public class AboutWindow extends JDialog {

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
			b.add(Box.createVerticalStrut(15));
			b.add(new JLabel("Created by:"));
			b.add(new JLabel("BNU-Camel"));
			b.add(Box.createVerticalStrut(15));
			b.add(new JLabel("Distributed under the GPL"));
			b.add(Box.createVerticalStrut(15));
			b.add(new JLabel("Special thanks to:"));
			b.add(new JLabel("HDX, the JBLS project"));
			b.add(new JLabel("Google: project hosting"));
			b.add(Box.createVerticalStrut(15));
			b.add(new JLabel("Alpha testers:"));
			b.add(new JLabel("BNU-Fantasma"));
			b.add(new JLabel("BNU-Sorceress"));
			b.add(Box.createVerticalStrut(15));
			b.add(new JLabel("Want to contribute?"));
			b.add(new JLabel("Visit the project website:"));
			b.add(new JLabel("http://code.google.com/p/bnubot/"));
		}
		add(b);
	}
}