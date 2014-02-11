/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui;

import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;

import net.bnubot.logging.Out;
import net.bnubot.util.BrowserLauncher;
import net.bnubot.vercheck.CurrentVersion;
import net.bnubot.vercheck.VersionNumber;

/**
 * @author scotta
 */
public class AboutWindow extends JDialog {
	private static final long serialVersionUID = -5776139820198349083L;

	public AboutWindow() {
		initializeGUI();
		setTitle("About BNU-Bot");
		setModal(true);
		setResizable(false);

		pack();
		WindowPosition.load(this);
		setVisible(true);
	}

	private static class ULabel extends JLabel {
		private static final long serialVersionUID = -2808638250751879735L;

		public ULabel(String caption) {
			super("<html><u>" + caption + "</u></html>");
		}
	}

	private static class LinkLabel extends JLabel {
		private static final long serialVersionUID = -5801691479517417290L;

		public LinkLabel(String url) {
			this(url, url);
		}

		public LinkLabel(String caption, final String url) {
			super("<html><a href=" + url + ">" + caption + "</a></html>");
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			addMouseListener(new MouseListener() {
				@Override
				public void mouseClicked(MouseEvent e) {
					try {
						BrowserLauncher.openURL(url);
					} catch (IOException ex) {
						Out.exception(ex);
					}
				}
				@Override
				public void mouseEntered(MouseEvent e) {}
				@Override
				public void mouseExited(MouseEvent e) {}
				@Override
				public void mousePressed(MouseEvent e) {}
				@Override
				public void mouseReleased(MouseEvent e) {}
			});
		}
	}

	public void initializeGUI() {
		setLayout(new FlowLayout(FlowLayout.CENTER));

		Box b = new Box(BoxLayout.Y_AXIS);
		{
			VersionNumber vn = CurrentVersion.version();
			b.add(new JLabel("BNU-Bot " + vn.toString()));
			b.add(new JLabel("Built " + vn.getBuildDate()));
			b.add(new JLabel("Created by BNU-Camel"));
			b.add(Box.createVerticalStrut(15));
			b.add(new JLabel("This project is distributed under the"));
			b.add(new JLabel("GNU Public License, Version 2"));
			b.add(Box.createVerticalStrut(15));
			b.add(new ULabel("Special thanks to:"));
			b.add(new JLabel("Google: Project hosting"));
			b.add(new JLabel("iago: Canadian ambassador, Warden"));
			b.add(new JLabel("Hdx: The JBLS project, Warden"));
			b.add(new JLabel("Chavo: Mirror selector first draft"));
			b.add(new JLabel("JDIC: The MPControl library; Winamp/XMMS/Rhythmbox/GMusicBrowser"));
			b.add(new JLabel("The JACOB project: ActiveX controller; Tunes in Windows"));
			b.add(new JLabel("JGoodies: Looks for Swing"));
			b.add(Box.createVerticalStrut(15));
			b.add(new ULabel("Alpha testers:"));
			b.add(new JLabel("BNU-Fantasma, BNU-Sorceress, Berzerk"));
			b.add(Box.createVerticalStrut(15));
			b.add(new JLabel("Want to find out more? Visit the project website:"));
			b.add(new LinkLabel("http://bnubot.net/"));
		}
		add(b);
	}
}