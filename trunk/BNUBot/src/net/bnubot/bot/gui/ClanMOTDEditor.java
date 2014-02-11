/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;

import net.bnubot.core.Connection;
import net.bnubot.logging.Out;

/**
 * @author scotta
 */
public class ClanMOTDEditor extends JFrame {
	private static final long serialVersionUID = 1208173058716246046L;
	Connection c;
	Box b;
	JTextArea txtMOTD;
	JButton btnSave;

	public ClanMOTDEditor(Connection c) {
		super("Clan MOTD Editor");
		this.c = c;

		initializeGui();

		setBounds(0, 0, 300, 150);
	}

	public void initializeGui() {
		b = new Box(BoxLayout.Y_AXIS);
		{
			txtMOTD = new JTextArea();
			b.add(txtMOTD);

			btnSave = new JButton("Save");
			btnSave.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					try {
						c.sendClanSetMOTD(txtMOTD.getText());
					} catch (Exception e) {
						Out.fatalException(e);
					}
					dispose();
				} });
			b.add(btnSave);
		}
		add(b);
	}

	public void setMOTD(String text) {
		txtMOTD.setText(text);
	}
}
