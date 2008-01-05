/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import net.bnubot.vercheck.CurrentVersion;

public class WhatsNewWindow extends JDialog {
	private static final long serialVersionUID = -2905017328939505262L;

	public WhatsNewWindow() {
		final JTabbedPane jtp = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		final String changeLog = getText();
		final String newLine = (changeLog.indexOf('\r') == -1) ? "\n" : "\r\n";
		
		// Split up the change log by version
		for(String entry : changeLog.split(newLine + newLine)) {
			String[] data = entry.split(newLine, 2);
			jtp.addTab(data[0], new JScrollPane(new JTextArea(data[1])));
		}
		
		JButton btnOK = new JButton("OK");
		btnOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}});
		
		Box boxAll = new Box(BoxLayout.Y_AXIS);
		boxAll.add(jtp);
		boxAll.add(btnOK);
		add(boxAll);
		
		setTitle("What's new in BNU-Bot " + CurrentVersion.version().toString());
		setModal(true);
		setResizable(true);
		setPreferredSize(new Dimension(400, 400));
		
		pack();
		WindowPosition.load(this);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setVisible(true);
			}});
	}

	private String getText() {
		try {
			InputStream changelog;
			if(CurrentVersion.fromJar())
				changelog = getClass().getResource("/changelog.txt").openStream();
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
