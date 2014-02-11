/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;

import net.bnubot.bot.gui.WhatsNewWindow;
import net.bnubot.bot.gui.settings.GlobalConfigurationFrame;
import net.bnubot.bot.gui.wizard.FirstConnectionWizard;
import net.bnubot.core.PluginManager;
import net.bnubot.core.Profile;
import net.bnubot.logging.Out;
import net.bnubot.logging.PrintStreamOutputLogger;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.settings.Settings;
import net.bnubot.vercheck.CurrentVersion;
import net.bnubot.vercheck.ExceptionReporter;
import net.bnubot.vercheck.ReleaseType;
import net.bnubot.vercheck.VersionCheck;

import org.apache.commons.logging.impl.NoOpLog;
import org.eclipse.swt.widgets.Display;

/**
 * @author scotta
 */
public class Main {
	static {
		// Set default exception handler
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable t) {
				Out.fatalException(t);
			}});

		// Delete the bnubot.pid file on application exit
		File f = new File("bnubot.pid");
		if(f.exists())
			f.deleteOnExit();

		// Disable Cayenne logging!
		System.setProperty("org.apache.commons.logging.Log", NoOpLog.class.getName());
	}

	public static void main(String[] args) {
		JARLoader.getClassLoader();
		GlobalSettings.load();

		boolean forceConfig = false;
		boolean logLocationSet = false;
		for(int i = 0; i < args.length; i++) {
			if(args[i].charAt(0) == '-') {
				switch(args[i].charAt(1)) {
				case 'c':
					if(args[i].equals("-cli")) {
						PluginManager.setEnableCli(true);
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
						PluginManager.setEnableGui(true);
						continue;
					}
					break;
				case 'l':
					if(args[i].equals("-logfile")
					|| args[i].equals("-log")) {
						try {
							Out.addOutputLogger(new PrintStreamOutputLogger(new PrintStream(new File(args[++i]))));
							logLocationSet = true;
						} catch (FileNotFoundException e) {
							Out.exception(e);
						}
						continue;
					}
					break;
				case 'n':
					if(args[i].equals("-nocli")) {
						PluginManager.setEnableCli(false);
						continue;
					}
					if(args[i].equals("-nogui")) {
						PluginManager.setEnableGui(false);
						continue;
					}
					break;
				case 's':
					if(args[i].equals("-stdout")) {
						Out.addOutputLogger(new PrintStreamOutputLogger(System.out));
						logLocationSet = true;
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

		// Set the default log file
		if(CurrentVersion.fromJar()) {
			try {
				File logFile = new File("log.txt");

				// Report errors for nightly builds
				if(CurrentVersion.version().getReleaseType().isNightly()
				&& logFile.exists()
				&& (logFile.length() > 0))
					ExceptionReporter.reportErrors(logFile);

				if(!logLocationSet)
					Out.addOutputLogger(new PrintStreamOutputLogger(new PrintStream(logFile)));
			} catch(Exception e) {
				Out.popupException(e);
			}
		} else {
			// Running in the debugger
			if(!logLocationSet)
				Out.addOutputLogger(new PrintStreamOutputLogger(System.out));
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

		if(PluginManager.getEnableGui()) {
			if(GlobalSettings.firstRun) {
				// Run the first-run wizard
				new FirstConnectionWizard(1).displayAndBlock();

				GlobalSettings.firstRun = false;
				GlobalSettings.save();
				Settings.store();
			}

			if(CurrentVersion.fromJar()) {
				// If we're launching a new version, pop up the what's new window
				WhatsNewWindow.displayIfNew();
			}
		}

		if(PluginManager.getEnableSwt()) {
			Display display = Display.getCurrent();
			while(display.readAndDispatch());
		}

		// Version check thread
		new Thread() {
			@Override
			public void run() {
				try {
					// Do the version check; no force
					VersionCheck.checkVersion(false);
				} catch(Exception e) {
					Out.exception(e);
				}
			}
		}.start();

		// Create connections for the default bots
		for(int i = 1; i <= GlobalSettings.numBots; i++)
			Profile.newConnection(i);

		if(CurrentVersion.fromJar()) {
			ReleaseType rt = CurrentVersion.version().getReleaseType();
			if(!rt.isStable())
				Out.error(CurrentVersion.class, "WARNING: This is a " + rt.name() + " build. It may contain bugs, or be unstable. Use at your own risk!");
		}

		// Write out any modified settings
		Settings.store();

		// SWT requires the main thread to be the event thread
		if(PluginManager.getEnableSwt()) {
			Display display = Display.getCurrent();
			while(!display.isDisposed()) {
				if(!display.readAndDispatch())
					display.sleep();
			}
			System.exit(0);
		}
	}
}
