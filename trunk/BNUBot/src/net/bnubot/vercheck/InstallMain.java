/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.vercheck;

import java.io.File;
import java.net.URL;

import javax.swing.JOptionPane;

import net.bnubot.util.Out;
import net.bnubot.util.URLDownloader;

/**
 * @author scotta
 */
public class InstallMain {
	/**
	 * Download helper for the installer
	 */
	private static void download(String url, String file) throws Exception {
		URLDownloader.downloadURL(new URL(url + file), new File(file), null, false);
	}
	
	public static void main(String[] args) throws Exception {
		try {
			int opt = JOptionPane.showConfirmDialog(null, "This program will install the latest version of BNU-Bot 2.0.", "Installer", JOptionPane.OK_CANCEL_OPTION);
			if(opt == JOptionPane.CANCEL_OPTION)
				System.exit(0);
		} catch(Exception e) {}
		
		String command = "java -jar BNUBot.jar";
		String jarFileName = "BNUBot.jar";
		String downloadFolder = null;
		
		if(System.getProperty("os.name").startsWith("Mac OS")) {
			// We're on OSX, so let's try to create an application
			String appdir = "BNUBot.app";
			
			Out.info(InstallMain.class, "Creating " + appdir);
			new File(appdir).mkdir();
			new File(appdir + "/Contents").mkdir();
			new File(appdir + "/Contents/MacOS").mkdir();
			new File(appdir + "/Contents/Resources").mkdir();
			new File(appdir + "/Contents/Resources/Java").mkdir();
			
			String url = "http://bnubot.googlecode.com/svn/trunk/BNUBot/Dist/OSX/";
			download(url, appdir + "/Contents/Info.plist");
			download(url, appdir + "/Contents/PkgInfo");
			download(url, appdir + "/Contents/MacOS/JavaApplicationStub");
			download(url, appdir + "/Contents/Resources/Icon.icns");

			jarFileName = appdir + "/Contents/Resources/Java/" + jarFileName;
			downloadFolder = appdir;
			command = appdir + "/Contents/MacOS/JavaApplicationStub";
		}
		
		if(!VersionCheck.checkVersion(true, ReleaseType.Stable, jarFileName, downloadFolder)) {
			try {
				JOptionPane.showMessageDialog(null, "Update failed!", "Error", JOptionPane.ERROR_MESSAGE);
			} catch(Exception e) {}
			
			System.exit(1);
		}
		
		Runtime rt = Runtime.getRuntime();
		
		// If launching in OSX, chmod the stub
		if(command.endsWith("/JavaApplicationStub")) {
			int ret = rt.exec("chmod 755 " + command).waitFor();
			if(ret != 0)
				throw new IllegalStateException(Integer.toString(ret));
		}
		
		// Ask if we should launch the bot
		try {
			int opt = JOptionPane.showConfirmDialog(null, "Install complete. Launch BNU-Bot?", "Installer", JOptionPane.YES_NO_OPTION);
			if(opt == JOptionPane.NO_OPTION)
				System.exit(0);
		} catch(Exception e) {}
		
		// Launch the program
		Out.info(InstallMain.class, "Launching: " + command);
		System.exit(rt.exec(command).waitFor());
	}
}
