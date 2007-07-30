/**
 * This file is distributed under the GPL 
 * $Id$
 */

package bnubot;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import bnubot.bot.CommandEventHandler;
import bnubot.bot.EventHandler;
import bnubot.bot.console.ConsoleEventHandler;
import bnubot.bot.database.*;
import bnubot.bot.gui.ConfigurationFrame;
import bnubot.bot.gui.GuiEventHandler;
import bnubot.bot.trivia.TriviaEventHandler;
import bnubot.core.*;
import bnubot.core.bncs.BNCSConnection;
import bnubot.core.queue.ChatQueue;
import bnubot.util.Settings;
import bnubot.vercheck.VersionCheck;

public class Main {
	
	private static void pidFile() {
		File f = new File("bnubot.pid");
		if(f.exists())
			f.deleteOnExit();
	}

	public static void main(String[] args) throws Exception {
		pidFile();
		
		int numBots = 1;
		try {
			numBots = Integer.parseInt(
				Settings.read("bnubot", "numBots", "1"));
		} catch(Exception e) {}
		Settings.write("bnubot", "numBots", Integer.toString(numBots));
		Settings.store();
		
		ConnectionSettings cs = new ConnectionSettings();
		cs.load(1);
		
		boolean forceConfig = false;
		String plugins[] = null;
		
		for(int i = 0; i < args.length; i++) {
			if(args[i].charAt(0) == '-') {
				switch(args[i].charAt(1)) {
				case 'c':
					if(args[i].equals("-cli")) {
						cs.enableCLI = true;
						continue;
					}
					if(args[i].equals("-cfg")) {
						forceConfig = true;
						continue;
					}
				case 'g':
					if(args[i].equals("-gui")) {
						cs.enableGUI = true;
						continue;
					}
				case 'n':
					if(args[i].equals("-nocli")) {
						cs.enableCLI = false;
						continue;
					}
					if(args[i].equals("-nogui")) {
						cs.enableGUI = false;
						continue;
					}
				case 'p':
					if(args[i].equals("-plugins")) {
						plugins = args[++i].split(":");
						continue;
					}
				}
			}
			
			System.err.println("Invalid argument: " + args[i]);
			System.exit(1);
		}
		
		if((cs.isValid() != null) || forceConfig) {
			ConfigurationFrame cf = null;
			try {
				cf = new ConfigurationFrame(cs);
				cf.setVisible(true);
			} catch(Exception e) {
				e.printStackTrace();
				String s = cs.isValid();
				if(s == null) {
					System.out.println("There was an error initializing the configuraiton window, but the configuration was valid.");
				} else {
					System.out.println("There was an error initializing the configuration window, and the configuration was invalid: " + s);
				}
				System.exit(1);
			}
			
			while(cf.isVisible())
				Thread.yield();
		}
		
		ChatQueue cq = new ChatQueue();
		cq.start();
		
		BNCSConnection primary = new BNCSConnection(cs, cq);
			
		//Other plugins
		if(plugins != null) {
			for(int i = 0; i < plugins.length; i++) {
				EventHandler eh = (EventHandler)Class.forName(plugins[i]).newInstance();
				primary.addEventHandler(eh);
			}
		}
		
		//CLI
		if(cs.enableCLI)
			primary.addEventHandler(new ConsoleEventHandler());
		
		//GUI
		EventHandler gui = null;
		if(cs.enableGUI) {
			gui = new GuiEventHandler();
			primary.addEventHandler(gui);
		}
		
		//Bot
		Database d = null;
		EventHandler cmd = null;
		if(cs.enableCommands) {
			String db_driver = Settings.read("database", "driver", null);
			String db_url = Settings.read("database", "url", null);
			String db_username = Settings.read("database", "username", null);
			String db_password = Settings.read("database", "password", null);
			String db_schema = Settings.read("database", "schema", null);
			
			if((db_driver == null)
			|| (db_url == null)) {
				if(gui != null)
					primary.recieveInfo("Database is not configured; disabling commands.");
				else
					System.out.println("Database is not configured; disabling commands.");
			} else {
				try {
					d = new Database(db_driver, db_url, db_username, db_password, db_schema);
					cmd = new CommandEventHandler(d);
					primary.addEventHandler(cmd);
					BNetUser.setDatabase(d);
				} catch(Exception e) {
					String msg = "Failed to initialize database:\n" + e.getMessage() + "\n\nCommands will be disabled.";
					StringWriter sw = new StringWriter();
					e.printStackTrace(new PrintWriter(sw));
					msg += "\n\n" + sw.toString();
					if(gui != null)
						primary.recieveError(msg);
					else
						System.err.println(msg);
					d = null;
				}
			}
		}
		
		//Trivia
		if(cs.enableTrivia) { 
			EventHandler trivia = new TriviaEventHandler(d);
			primary.addEventHandler(trivia);
		}
		
		try {
			if(VersionCheck.checkVersion()) {
				primary.recieveError("There is an update available!");
				primary.recieveError("Latest version: " + VersionCheck.getLatestVersion().toString());
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		primary.start();
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
	
			c = new BNCSConnection(cs, cq);
			if(gui != null)
				c.addSecondaryEventHandler(gui);
			if(cmd != null)
				c.addSecondaryEventHandler(cmd);
			c.start();
			
			primary.addSlave(c);
		}
	}
}
