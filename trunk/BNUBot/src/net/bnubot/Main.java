/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import net.bnubot.bot.gui.GuiDesktop;
import net.bnubot.bot.gui.settings.GlobalConfigurationFrame;
import net.bnubot.core.Profile;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.settings.Settings;
import net.bnubot.util.OperatingSystem;
import net.bnubot.util.Out;
import net.bnubot.vercheck.CurrentVersion;
import net.bnubot.vercheck.VersionCheck;

public class Main {
	static {
		// Force the static initializers of GlobalSettings to run
		if(GlobalSettings.enableGUI)
			// Force the Swing GUI to start
			GuiDesktop.getInstance();
		
		// Delete the bnubot.pid file on application exit
		File f = new File("bnubot.pid");
		if(f.exists())
			f.deleteOnExit();

		// On OSX, set the application name
		switch(OperatingSystem.userOS) {
		case OSX:
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "BNU-Bot");
			break;
		}
	}

	public static void main(String[] args) {
		boolean forceConfig = !new File("settings.ini").exists();
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
						try {
							Out.setOutputStream(new PrintStream(new File(args[++i])));
						} catch (FileNotFoundException e) {
							Out.exception(e);
						}
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
		
		if(forceConfig) {
			try {
				new GlobalConfigurationFrame();
			} catch(Exception e) {
				Out.exception(e);
				String error = "There was an error initializing the configuraiton window";
				Out.error(Main.class, error);
				System.exit(1);
			}
		}
		
		for(int i = 1; i <= GlobalSettings.numBots; i++)
			Profile.newConnection(i);
		
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
