/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import net.bnubot.bot.gui.components.ConfigPanel;
import net.bnubot.bot.gui.components.ConfigTextField;
import net.bnubot.core.Connection;
import net.bnubot.logging.Out;
import net.bnubot.util.UserProfile;

/**
 * @author scotta
 */
public class ProfileEditor extends JDialog {
	private static final long serialVersionUID = 7116099159624394301L;

	private final UserProfile p;
	private final Connection source;
	private ConfigTextField txtUsername;
	private final HashMap<String, ConfigTextField> txtBoxes = new HashMap<String, ConfigTextField>();
	private JButton btnSave;
	private JButton btnClose;

	public ProfileEditor(UserProfile p, Connection source) {
		this.p = p;
		this.source = source;

		setTitle("Profile of " + p.getUser());
		initializeGui();
		pack();
		setResizable(false);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				setModal(true);
				setVisible(true);
			}});
	}

	private void initializeGui() {
		boolean enableWrite = source.getMyUser().equals(p.getUser());

		setLayout(new FlowLayout(FlowLayout.LEFT));
		ConfigPanel boxAll = new ConfigPanel();
		{
			txtUsername = boxAll.makeText("Username", p.getUser());
			txtUsername.setEnabled(false);

			for(String key : p.keySet()) {
				ConfigTextField cta = boxAll.makeText(key, p.get(key));
				boolean enableThisKey = enableWrite && key.startsWith(UserProfile.PROFILE_);
				cta.setEnabled(enableThisKey);
				if(enableThisKey)
					txtBoxes.put(key, cta);
			}

			Box buttons = new Box(BoxLayout.X_AXIS);
			{
				if(enableWrite) {
					buttons.add(btnSave = new JButton("Save"));
					btnSave.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							for(String key : txtBoxes.keySet())
								p.put(key, txtBoxes.get(key).getText());
							try {
								source.sendWriteUserData(p);
								dispose();
							} catch (Exception ex) {
								Out.popupException(ex, ProfileEditor.this);
							}
						}});
				}
				buttons.add(btnClose = new JButton("Close"));
				btnClose.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						dispose();
					}});
			}
			boxAll.add(buttons);
		}
		add(boxAll);
	}

	public UserProfile getProfile() {
		return p;
	}
}
