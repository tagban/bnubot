/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;

import net.bnubot.bot.gui.components.ConfigPanel;
import net.bnubot.bot.gui.components.ConfigSpinner;
import net.bnubot.vercheck.CurrentVersion;
import net.bnubot.vercheck.ReleaseType;
import net.bnubot.vercheck.VersionNumber;

/**
 * @author scotta
 */
public class VersionEditor extends JDialog {
	private static final long serialVersionUID = -2458030928870393642L;

	public static void main(String args[]) {
		new VersionEditor();
		System.exit(0);
	}

	VersionNumber vnCurrent = CurrentVersion.version();
	JComboBox<ReleaseType> cmbReleaseType;
	ConfigSpinner spnMajor;
	ConfigSpinner spnMinor;
	ConfigSpinner spnRevision;
	ConfigSpinner spnRelease;
	JButton btnSave;

	public VersionEditor() throws HeadlessException {
		super();

		if(CurrentVersion.fromJar())
			throw new HeadlessException("You may not run the version editor from a JAR");

		setTitle("BNU-Bot Version Editor");

		initializeGui();

		pack();
		setResizable(false);
		setModal(true);
		setVisible(true);
	}

	private void initializeGui() {
		ConfigPanel boxAll = new ConfigPanel();
		add(boxAll);

		cmbReleaseType = boxAll.makeCombo("Release Type", ReleaseType.values(), false);
		cmbReleaseType.setSelectedItem(vnCurrent.getReleaseType());
		spnMajor = boxAll.makeSpinner("Major", vnCurrent.getMajor());
		spnMinor = boxAll.makeSpinner("Minor", vnCurrent.getMinor());
		spnRevision = boxAll.makeSpinner("Revision", vnCurrent.getRevision());
		spnRelease = boxAll.makeSpinner("Release", vnCurrent.getRelease());

		btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				save();
			}});
		boxAll.add(btnSave);
	}

	private void save() {
		vnCurrent.setReleaseType((ReleaseType)cmbReleaseType.getSelectedItem());
		vnCurrent.setMajor(spnMajor.getValue());
		vnCurrent.setMinor(spnMinor.getValue());
		vnCurrent.setRevision(spnRevision.getValue());
		vnCurrent.setRelease(spnRelease.getValue());
		vnCurrent.setBuildDate(new Date());
		CurrentVersion.setVersion(vnCurrent);
		dispose();
	}
}
