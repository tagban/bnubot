/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.vercheck;

import javax.swing.JOptionPane;


/**
 * @author sanderson
 *
 */
public class InstallMain {
	public static void main(String[] args) throws Exception {
		try {
			int opt = JOptionPane.showConfirmDialog(null, "This program will install the latest version of BNU-Bot 2.0.", "Installer", JOptionPane.OK_CANCEL_OPTION);
			if(opt == JOptionPane.CANCEL_OPTION)
				return;
		} catch(Exception e) {}
		
		if(!VersionCheck.checkVersion(true, ReleaseType.Stable)) {
			try {
				JOptionPane.showMessageDialog(null, "Update failed!", "Error", JOptionPane.ERROR_MESSAGE);
			} catch(Exception e) {}
			
			System.exit(1);
		}
		
		try {
			int opt = JOptionPane.showConfirmDialog(null, "Install complete. Launch BNU-Bot?", "Installer", JOptionPane.YES_NO_OPTION);
			if(opt == JOptionPane.NO_OPTION)
				return;
		} catch(Exception e) {}
		
		// Launch the program
		String command[] = {"java", "-jar", "BNUBot.jar"};
		Runtime rt = Runtime.getRuntime();
		System.exit(rt.exec(command).waitFor());
	}
}
