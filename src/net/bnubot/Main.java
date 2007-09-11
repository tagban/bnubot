/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import net.bnubot.bot.CommandEventHandler;
import net.bnubot.bot.console.ConsoleEventHandler;
import net.bnubot.bot.database.Database;
import net.bnubot.bot.database.DatabaseSettings;
import net.bnubot.bot.gui.ConfigurationFrame;
import net.bnubot.bot.gui.GuiEventHandler;
import net.bnubot.bot.trivia.TriviaEventHandler;
import net.bnubot.core.ChatQueue;
import net.bnubot.core.ConnectionSettings;
import net.bnubot.core.EventHandler;
import net.bnubot.core.bncs.BNCSConnection;
import net.bnubot.util.BNetUser;
import net.bnubot.util.Out;
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
				case 'd':
					if(args[i].equals("-debug")) {
						Out.setDebug(true);
						continue;
					}
					break;
				case 'g':
					if(args[i].equals("-gui")) {
						cs.enableGUI = true;
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
		
		ChatQueue cq = new ChatQueue();
		cq.start();
		
		BNCSConnection primary = new BNCSConnection(cs, cq);
			
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
		if(cs.enableCLI) {
			cli = new ConsoleEventHandler();
			primary.addEventHandler(cli);
		}
		
		//GUI
		GuiEventHandler gui = null;
		if(cs.enableGUI) {
			gui = new GuiEventHandler();
			primary.addEventHandler(gui);
			Out.setOutputConnection(gui);
		}
		
		//Bot
		EventHandler cmd = null;
		if(cs.enableCommands) {
			DatabaseSettings ds = new DatabaseSettings();
			ds.load();
			
			if((ds.driver == null)
			|| (ds.url == null)) {
				if(gui != null)
					primary.recieveInfo("Database is not configured; disabling commands.");
				else
					Out.info(Main.class, "Database is not configured; disabling commands.");
			} else {
				try {
					new Database(ds);
					BNetUser.setDatabase();
					cmd = new CommandEventHandler();
					primary.addEventHandler(cmd);
					
					ds.save();
				} catch(Exception e) {
					Out.exception(e);
					String msg = "Failed to initialize the database; commands will be disabled.\n" + e.getMessage();
					if(gui != null)
						primary.recieveError(msg);
					else
						Out.error(Main.class, msg);
				}
			}
		}
		
		//Trivia
		if(cs.enableTrivia) { 
			EventHandler trivia = new TriviaEventHandler();
			primary.addEventHandler(trivia);
		}
		
		try {
			VersionCheck.checkVersion(cs.releaseType);
		} catch(Exception e) {
			Out.exception(e);
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
