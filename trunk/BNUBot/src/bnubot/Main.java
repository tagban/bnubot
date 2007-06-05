package bnubot;

import java.io.File;

import bnubot.bot.CommandEventHandler;
import bnubot.bot.EventHandler;
import bnubot.bot.console.ConsoleEventHandler;
import bnubot.bot.database.*;
import bnubot.bot.gui.ConfigurationFrame;
import bnubot.bot.gui.GuiEventHandler;
import bnubot.core.*;
import bnubot.core.bncs.BNCSConnection;
import bnubot.core.queue.ChatQueue;
import bnubot.util.Ini;

public class Main {

	public static void main(String[] args) throws Exception {
		int numBots = 1;
		try {
			numBots = Integer.parseInt(
				Ini.ReadIni("settings.ini", "bnubot", "numBots", "1"));
		} catch(Exception e) {
			Ini.WriteIni("settings.ini", "bnubot", "numBots", "1");
		}
		
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
		Database d = Database.load(new File("database.bin"));
		d.save();
		EventHandler cmd = new CommandEventHandler(d);
		primary.addEventHandler(cmd);
		
		//Other plugins
		if(plugins != null) {
			for(int i = 0; i < plugins.length; i++) {
				Class plugin = Class.forName(plugins[i]);
				EventHandler eh = (EventHandler)plugin.newInstance();
				primary.addEventHandler(eh);
			}
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
