/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;

import net.bnubot.bot.gui.components.ConfigComboBox;
import net.bnubot.bot.gui.components.ConfigFactory;
import net.bnubot.bot.gui.components.ConfigSpinner;
import net.bnubot.bot.gui.icons.BNetIcon;
import net.bnubot.bot.gui.icons.IconsDotBniReader;
import net.bnubot.settings.ConnectionSettings;
import net.bnubot.util.HexDump;
import net.bnubot.vercheck.CurrentVersion;
import net.bnubot.vercheck.ReleaseType;
import net.bnubot.vercheck.VersionNumber;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class VersionEditor extends JDialog {
	private static final long serialVersionUID = -2458030928870393642L;

	public static void main(String args[]) {
		new VersionEditor();
		System.exit(0);
	}
	
	VersionNumber vnCurrent = CurrentVersion.version();
	ConfigComboBox cmbReleaseType;
	ConfigSpinner spnMajor;
	ConfigSpinner spnMinor;
	ConfigSpinner spnRevision;
	ConfigSpinner spnRelease;
	JButton btnSave;
	
	public VersionEditor() throws HeadlessException {
		super();
		
		Display d = new Display();
		Shell s = new Shell(d);
		Color background = d.getSystemColor(SWT.COLOR_BLACK);
		Color foreground = d.getSystemColor(SWT.COLOR_WHITE);
		s.setImage(new Image(null, "tray.gif"));
		s.setText("Icons");
		s.setLayout(new RowLayout());
		s.setBackground(background);
		
		IconsDotBniReader.initialize(new ConnectionSettings(1));
		BNetIcon[][] iconss = {
				IconsDotBniReader.getIcons(),
				IconsDotBniReader.getIconsSTAR(),
				IconsDotBniReader.getIconsWAR3(),
				IconsDotBniReader.getIconsW3XP(),
				IconsDotBniReader.getLegacyIcons(),
				IconsDotBniReader.getIconsLag()};
		for(BNetIcon[] icons : iconss) {
			if(icons == null)
				continue;
			
			Group grpIcons = new Group(s, SWT.NULL);
			GridLayout gridLayout = new GridLayout(2, false);
			gridLayout.marginHeight = 1;
			gridLayout.marginWidth = 1;
			gridLayout.horizontalSpacing = 1;
			gridLayout.verticalSpacing = 1;
			grpIcons.setLayout(gridLayout);
			grpIcons.setBackground(background);
			for(int i = 0; i < icons.length; i++) {
				BNetIcon bni = icons[i];
				
				Label label = new Label(grpIcons, SWT.NULL);
				label.setImage(bni.getImage());
				label.pack();

				String text = "";
				if(bni.getProducts() != null)
					text += " " + HexDump.DWordToPretty(bni.getProducts()[0]);
				if(bni.getFlags() != 0)
					text += " 0x" + Integer.toHexString(bni.getFlags());
				
				label = new Label(grpIcons, SWT.NULL);
				label.setBackground(background);
				label.setForeground(foreground);
				label.setText(Integer.toString(i) + text);
				label.pack();
			}
			grpIcons.pack();
		}

		s.pack();
		s.open();
		while(!s.isDisposed()) {
			if(!d.readAndDispatch())
				d.sleep();
		}
		d.dispose();
		
		System.exit(0);
		
		
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
		Box boxAll = new Box(BoxLayout.Y_AXIS);
		add(boxAll);
		
		cmbReleaseType = ConfigFactory.makeCombo("Release Type", ReleaseType.values(), false, boxAll);
		cmbReleaseType.setSelectedItem(vnCurrent.getReleaseType());
		spnMajor = ConfigFactory.makeSpinner("Major", vnCurrent.getMajor(), boxAll);
		spnMinor = ConfigFactory.makeSpinner("Minor", vnCurrent.getMinor(), boxAll);
		spnRevision = ConfigFactory.makeSpinner("Revision", vnCurrent.getRevision(), boxAll);
		spnRelease = ConfigFactory.makeSpinner("Release", vnCurrent.getRelease(), boxAll);
		
		btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
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
