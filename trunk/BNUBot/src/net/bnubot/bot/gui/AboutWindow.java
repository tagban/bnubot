/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.bot.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;

import net.bnubot.util.BrowserLauncher;
import net.bnubot.util.Out;
import net.bnubot.vercheck.CurrentVersion;
import net.bnubot.vercheck.VersionNumber;

public class AboutWindow extends JDialog {
	private static final long serialVersionUID = -5776139820198349083L;

	public AboutWindow() {
		initializeGUI();
		setTitle("About BNU-Bot");
		setModal(true);
		setResizable(false);
		
		pack();
		setVisible(true);
	}
	
	private static class LinkLabel extends JLabel {
		private static final long serialVersionUID = -5801691479517417290L;

		public LinkLabel(String url) {
			this(url, url);
		}
		
		public LinkLabel(String caption, String url) {
			super(caption);
			setForeground(Color.BLUE);
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent e) {
					try {
						BrowserLauncher.openURL("http://bnubot.net/");
					} catch (IOException ex) {
						Out.exception(ex);
					}
				}
				public void mouseEntered(MouseEvent e) {}
				public void mouseExited(MouseEvent e) {}
				public void mousePressed(MouseEvent e) {}
				public void mouseReleased(MouseEvent e) {}
				});
		}
	}

	public void initializeGUI() {
		setLayout(new FlowLayout(FlowLayout.CENTER));

		Box b = new Box(BoxLayout.Y_AXIS);
		{
			VersionNumber vn = CurrentVersion.version();
			b.add(new JLabel("BNU-Bot v" + vn.toString()));
			b.add(new JLabel("Built " + vn.getBuildDate()));
			b.add(new JLabel("Created by BNU-Camel"));
			b.add(Box.createVerticalStrut(15));
			b.add(new JLabel("Special thanks to:"));
			b.add(new JLabel("Google: Project hosting"));
			b.add(new JLabel("Fantasma, Sorceress: Alpha testing"));
			b.add(new JLabel("Hdx: The JBLS project"));
			b.add(new JLabel("iago: Canadian ambassador"));
			b.add(new JLabel("Chavo: Mirror selector"));
			b.add(Box.createVerticalStrut(15));
			b.add(new JLabel("This project is distributed under the"));
			b.add(new JLabel("GNU Public License, Version 2"));
			b.add(Box.createVerticalStrut(15));
			b.add(new JLabel("Want to contribute?"));
			b.add(new JLabel("Visit the project website:"));
			b.add(new LinkLabel("http://bnubot.net/"));
		}
		add(b);
	}
}