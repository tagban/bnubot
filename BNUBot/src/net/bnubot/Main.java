/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot;

import java.io.File;
import java.util.ArrayList;

import net.bnubot.bot.CommandEventHandler;
import net.bnubot.bot.EventHandler;
import net.bnubot.bot.console.ConsoleEventHandler;
import net.bnubot.bot.database.*;
import net.bnubot.bot.gui.ConfigurationFrame;
import net.bnubot.bot.gui.GuiEventHandler;
import net.bnubot.bot.trivia.TriviaEventHandler;
import net.bnubot.core.*;
import net.bnubot.core.bncs.BNCSConnection;
import net.bnubot.core.queue.ChatQueue;
import net.bnubot.util.Settings;
import net.bnubot.vercheck.VersionCheck;

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
					break;
				case 'g':
					if(args[i].equals("-gui")) {
						cs.enableGUI = true;
						continue;
					}
					break;
				case 'n':
					if(args[i].equals("-nocli")) {
						cs.enableCLI = false;
						continue;
					}
					if(args[i].equals("-nogui")) {
						cs.enableGUI = false;
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
				System.err.print("There was an error initializing the configuraiton window, ");
				if(s == null) {
					System.err.println("but the configuration was valid.");
				} else {
					System.err.println("and the configuration was invalid: " + s);
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
		ArrayList<EventHandler> pluginEHs = new ArrayList<EventHandler>();
		if(plugins != null) {
			for(int i = 0; i < plugins.length; i++) {
				EventHandler eh = (EventHandler)Class.forName(plugins[i]).newInstance();
				pluginEHs.add(eh);
				primary.addEventHandler(eh);
			}
		}
		
		//CLI
		EventHandler cli = null;
		if(cs.enableCLI) {
			cli = new ConsoleEventHandler();
			primary.addEventHandler(cli);
		}
		
		//GUI
		EventHandler gui = null;
		if(cs.enableGUI) {
			gui = new GuiEventHandler();
			primary.addEventHandler(gui);
		}
		
		//Bot
		EventHandler cmd = null;
		if(cs.enableCommands) {
			String db_driver = Settings.read("database", "driver", "org.apache.derby.jdbc.EmbeddedDriver");
			String db_url = Settings.read("database", "url", "jdbc:derby:database;create=true");
			String db_username = Settings.read("database", "username", null);
			String db_password = Settings.read("database", "password", null);
			String db_schema = Settings.read("database", "schema", "schema.derby");
			
			if((db_driver == null)
			|| (db_url == null)) {
				if(gui != null)
					primary.recieveInfo("Database is not configured; disabling commands.");
				else
					System.out.println("Database is not configured; disabling commands.");
			} else {
				try {
					new Database(db_driver, db_url, db_username, db_password, db_schema);
					BNetUser.setDatabase();
					cmd = new CommandEventHandler();
					primary.addEventHandler(cmd);
					
					Settings.write("database", "driver", db_driver);
					Settings.write("database", "url", db_url);
					Settings.write("database", "username", db_username);
					Settings.write("database", "password", db_password);
					Settings.write("database", "schema", db_schema);
				} catch(Exception e) {
					e.printStackTrace();
					String msg = "Failed to initialize the database; commands will be disabled.\n" + e.getMessage();
					if(gui != null)
						primary.recieveError(msg);
					else
						System.err.println(msg);
				}
			}
		}
		
		//Trivia
		if(cs.enableTrivia) { 
			EventHandler trivia = new TriviaEventHandler();
			primary.addEventHandler(trivia);
		}
		
		try {
			VersionCheck.checkVersion(primary);
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
			for(EventHandler eh : pluginEHs)
				c.addSecondaryEventHandler(eh);
			if(cli != null)
				c.addSecondaryEventHandler(cli);
			if(gui != null)
				c.addSecondaryEventHandler(gui);
			if(cmd != null)
				c.addSecondaryEventHandler(cmd);
			c.start();
			
			primary.addSlave(c);
		}
		
		// Write out any modified settings
		Settings.store();
	}
}
