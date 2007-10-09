/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.JOptionPane;

import net.bnubot.bot.CommandEventHandler;
import net.bnubot.bot.console.ConsoleEventHandler;
import net.bnubot.bot.database.Database;
import net.bnubot.bot.gui.ConfigurationFrame;
import net.bnubot.bot.gui.GuiEventHandler;
import net.bnubot.bot.trivia.TriviaEventHandler;
import net.bnubot.core.ChatQueue;
import net.bnubot.core.EventHandler;
import net.bnubot.core.bncs.BNCSConnection;
import net.bnubot.settings.ConnectionSettings;
import net.bnubot.settings.DatabaseSettings;
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
		int numBots = 1;
		try {
			numBots = Integer.parseInt(
				Settings.read("bnubot", "numBots", "1"));
		} catch(Exception e) {}
		Settings.write("bnubot", "numBots", Integer.toString(numBots));
		
		ConnectionSettings cs = new ConnectionSettings();
		cs.load(1);
		
		boolean forceConfig = false;
		String plugins[] = null;
		
		for(int i = 0; i < args.length; i++) {
			if(args[i].charAt(0) == '-') {
				switch(args[i].charAt(1)) {
				case 'c':
					if(args[i].equals("-cli")) {
						ConnectionSettings.enableCLI = true;
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
						ConnectionSettings.enableGUI = true;
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
						ConnectionSettings.enableCLI = false;
						continue;
					}
					if(args[i].equals("-nogui")) {
						ConnectionSettings.enableGUI = false;
						continue;
					}
					break;
				case 'p':
					if(args[i].equals("-plugins")) {
						plugins = args[++i].split(":");
						continue;
					}
					break;
				}
			}

			Out.error(Main.class, "Invalid argument: " + args[i]);
			System.exit(1);
		}
		
		if((cs.isValid() != null) || forceConfig) {
			ConfigurationFrame cf = null;
			try {
				cf = new ConfigurationFrame(cs);
				cf.setVisible(true);
			} catch(Exception e) {
				Out.exception(e);
				String s = cs.isValid();
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
				Thread.sleep(10);
			}
			
			String reason = cs.isValid();
			if(reason != null) {
				JOptionPane.showMessageDialog(null, reason, "Invalid Configuration", JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
		}
		
		ChatQueue chatQueue = new ChatQueue();
		chatQueue.start();
		
		BNCSConnection primary = new BNCSConnection(cs, chatQueue);
			
		//Other plugins
		ArrayList<EventHandler> pluginEHs = new ArrayList<EventHandler>();
		if(plugins != null) {
			for(String element : plugins) {
				EventHandler eh = (EventHandler)Class.forName(element).newInstance();
				pluginEHs.add(eh);
				primary.addEventHandler(eh);
			}
		}
		
		//CLI
		EventHandler cli = null;
		if(ConnectionSettings.enableCLI) {
			cli = new ConsoleEventHandler();
			primary.addEventHandler(cli);
		}
		
		//GUI
		GuiEventHandler gui = null;
		if(ConnectionSettings.enableGUI) {
			gui = new GuiEventHandler();
			primary.addEventHandler(gui);
		}
		
		if(CurrentVersion.fromJar() && CurrentVersion.version().getReleaseType().isDevelopment())
			Out.error(CurrentVersion.class, "WARNING: This is a development build, not for distribution!");
		
		try {
			VersionCheck.checkVersion();
		} catch(Exception e) {
			Out.exception(e);
		}
		
		//Commands
		EventHandler cmd = null;
		if(ConnectionSettings.enableCommands) {
			DatabaseSettings ds = new DatabaseSettings();
			ds.load();
			
			if((ds.driver == null)
			|| (ds.url == null)) {
				String msg = "Database is not configured; disabling commands.";
				if(gui != null)
					primary.recieveInfo(msg);
				else
					Out.info(Main.class, msg);
			} else {
				try {
					new Database(ds);
					cmd = new CommandEventHandler();
					primary.addEventHandler(cmd);
					
					ds.save();
				} catch(Exception e) {
					Out.exception(e);
					String msg = "Failed to initialize the database; commands disabled.\n" + e.getMessage();
					if(gui != null)
						primary.recieveError(msg);
					else
						Out.error(Main.class, msg);
				}
			}
		}
		
		//Trivia
		if(ConnectionSettings.enableTrivia) { 
			EventHandler trivia = new TriviaEventHandler();
			primary.addEventHandler(trivia);
		}
		
		primary.start();
		
		Hashtable<String, BNCSConnection> primaries = new Hashtable<String, BNCSConnection>();
		Hashtable<String, GuiEventHandler> guis = new Hashtable<String, GuiEventHandler>();

		String profile = cs.bncsServer;
		primaries.put(profile, primary);
		guis.put(profile, gui);
		
		BNCSConnection c = primary;
		for(int i = 2; i <= numBots; i++) {
			//Wait for the previous bot to connect
			while(!c.canSendChat())
				Thread.sleep(20);
			
			//Wait an additional 500ms
			Thread.sleep(500);
			
			//Start up the next connection
			cs = new ConnectionSettings();
			cs.load(i);
			String valid = cs.isValid();
			if(valid != null)
				throw new Exception("Invalid configuration for bot " + i + ": " + valid);
			
			profile = cs.bncsServer;
			primary = primaries.get(profile);
			
			if(primary != null) {
				chatQueue = primary.getChatQueue();
				c = new BNCSConnection(cs, chatQueue);
				
				if(ConnectionSettings.enableGUI)
					c.addSecondaryEventHandler(guis.get(profile));
				
				c.start();
				primary.addSlave(c);
			} else {
				chatQueue = new ChatQueue();
				chatQueue.start();
				
				c = new BNCSConnection(cs, chatQueue);
				primaries.put(profile, c);
				
				if(ConnectionSettings.enableGUI) {
					gui = new GuiEventHandler();
					guis.put(profile, gui);
					c.addEventHandler(gui);
				}
				if(cmd != null)
					c.addEventHandler(new CommandEventHandler());
				c.start();
			}
		}
		
		// Write out any modified settings
		Settings.store();
	}
}
