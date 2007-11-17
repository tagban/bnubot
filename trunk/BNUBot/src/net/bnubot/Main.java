/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot;

import java.io.File;
import java.io.PrintStream;

import javax.swing.JOptionPane;

import net.bnubot.bot.gui.ConfigurationFrame;
import net.bnubot.bot.gui.GlobalConfigurationFrame;
import net.bnubot.core.Profile;
import net.bnubot.settings.ConnectionSettings;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.settings.Settings;
import net.bnubot.util.Out;
import net.bnubot.vercheck.CurrentVersion;
import net.bnubot.vercheck.VersionCheck;

public class Main {
	static {
		// Delete the bnubot.pid file on application exit
		File f = new File("bnubot.pid");
		if(f.exists())
			f.deleteOnExit();

		// On OSX, set the application name
		String osName = System.getProperty("os.name");
		if (osName.startsWith("Mac OS")) {
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "BNU-Bot");
		}
	}

	public static void main(String[] args) throws Exception {
		boolean forceConfig = false;
		GlobalSettings.touch();
		
		for(int i = 0; i < args.length; i++) {
			if(args[i].charAt(0) == '-') {
				switch(args[i].charAt(1)) {
				case 'c':
					if(args[i].equals("-cli")) {
						GlobalSettings.enableCLI = true;
						continue;
					}
					if(args[i].equals("-cfg")) {
						forceConfig = true;
						continue;
					}
					break;
				case 'd':
					if(args[i].equals("-debug")) {
						Out.setDebug(true);
						continue;
					}
					break;
				case 'g':
					if(args[i].equals("-gui")) {
						GlobalSettings.enableGUI = true;
						continue;
					}
					break;
				case 'l':
					if(args[i].equals("-logfile")) {
						Out.setOutputStream(new PrintStream(new File(args[++i])));
						continue;
					}
					break;
				case 'n':
					if(args[i].equals("-nocli")) {
						GlobalSettings.enableCLI = false;
						continue;
					}
					if(args[i].equals("-nogui")) {
						GlobalSettings.enableGUI = false;
						continue;
					}
					break;
				case 'p':
					if(args[i].equals("-plugins")) {
						Profile.setPlugins(args[++i].split(":"));
						continue;
					}
					break;
				case 'v':
					if(args[i].equals("-v") || args[i].equals("-version")) {
						System.out.println(CurrentVersion.version().toString());
						System.exit(0);
					}
					break;
				}
			}

			Out.error(Main.class, "Invalid argument: " + args[i]);
			System.exit(1);
		}
		
		if((GlobalSettings.isValid() != null) || forceConfig) {
			GlobalConfigurationFrame cf = null;
			try {
				cf = new GlobalConfigurationFrame();
				cf.setVisible(true);
			} catch(Exception e) {
				Out.exception(e);
				String s = GlobalSettings.isValid();
				String error = "There was an error initializing the configuraiton window, ";
				if(s == null)
					error += "but the configuration was valid.";
				else
					error += "and the configuration was invalid: " + s;
				Out.error(Main.class, error);
				System.exit(1);
			}
			
			while(cf.isVisible()) {
				Thread.yield();
				Thread.sleep(200);
			}
			
			String reason = GlobalSettings.isValid();
			if(reason != null) {
				JOptionPane.showMessageDialog(null, reason, "Invalid Configuration", JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
		}
		
		for(int i = 1; i <= GlobalSettings.numBots; i++) {
			//Start up the next connection
			ConnectionSettings cs = new ConnectionSettings(i);
			String valid = cs.isValid();
			if(valid != null)
				new ConfigurationFrame(cs).setVisible(true);
			
			valid = cs.isValid();
			if(valid == null)
				Profile.add(cs);
			else
				throw new Exception("Invalid configuration for bot " + i + ": " + valid);
		}
		
		if(CurrentVersion.fromJar() && CurrentVersion.version().getReleaseType().isDevelopment())
			Out.error(CurrentVersion.class, "WARNING: This is a development build, not for distribution!");
		
		try {
			VersionCheck.checkVersion();
		} catch(Exception e) {
			Out.exception(e);
		}
		
		// Write out any modified settings
		Settings.store();
	}
}
