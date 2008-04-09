/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui;

/* Some Theory Eventually I actually want to have 2 tabs on this profile editor let it view stats like
 * some clients allow bleh, but for now we will grab the selected user in the lists username
 * compare it with current bots username, enable editable only if it matches.
 */
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;

import net.bnubot.bot.gui.components.ConfigFactory;
import net.bnubot.bot.gui.components.ConfigTextArea;
import net.bnubot.core.Connection;
import net.bnubot.util.Out;
import net.bnubot.util.UserProfile;

public class ProfileEditor extends JDialog {
	private static final long serialVersionUID = 7116099159624394301L;
	
	private UserProfile p;
	private Connection source;
	private ConfigTextArea txtUsername;
	private HashMap<String, ConfigTextArea> txtBoxes = new HashMap<String, ConfigTextArea>();
	private JButton btnSave;
	private JButton btnClose;

	public ProfileEditor(UserProfile p, Connection source) {
		this.p = p;
		this.source = source;
		
		setModal(true);
		setTitle("Profile of " + p.getUser());
		initializeGui();
		pack();
		setResizable(false);
		setVisible(true);
	}

	private void initializeGui() {
		boolean enableWrite = source.getMyUser().equals(p.getUser());
		
		setLayout(new FlowLayout(FlowLayout.LEFT));
		Box boxAll = new Box(BoxLayout.Y_AXIS);
		{
			txtUsername = ConfigFactory.makeText("Username", p.getUser(), boxAll);
			txtUsername.setEnabled(false);
			
			for(String key : p.keySet()) {
				ConfigTextArea cta = ConfigFactory.makeText(key, p.get(key), boxAll);
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
