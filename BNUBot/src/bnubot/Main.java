package bnubot;

import bnubot.bot.CommandEventHandler;
import bnubot.bot.EventHandler;
import bnubot.bot.console.ConsoleEventHandler;
import bnubot.bot.database.*;
import bnubot.bot.gui.ConfigurationFrame;
import bnubot.bot.gui.GuiEventHandler;
import bnubot.core.*;
import bnubot.core.bncs.BNCSConnection;

public class Main {

	public static void main(String[] args) throws Exception {
		ConnectionSettings cs = new ConnectionSettings();
		cs.load();
		
		boolean forceConfig = false;
		String plugins[] = null;
		
		for(int i = 0; i < args.length; i++) {
			if(args[i].charAt(0) == '-') {
				switch(args[i].charAt(1)) {
				case 'c':
					if(args[i].compareToIgnoreCase("-cli") == 0) {
						cs.enableCLI = true;
						continue;
					}
					if(args[i].compareToIgnoreCase("-cfg") == 0) {
						forceConfig = true;
						continue;
					}
				case 'g':
					if(args[i].compareToIgnoreCase("-gui") == 0) {
						cs.enableGUI = true;
						continue;
					}
				case 'n':
					if(args[i].compareToIgnoreCase("-nocli") == 0) {
						cs.enableCLI = false;
						continue;
					}
					if(args[i].compareToIgnoreCase("-nogui") == 0) {
						cs.enableGUI = false;
						continue;
					}
				case 'p':
					if(args[i].compareToIgnoreCase("-plugins") == 0) {
						plugins = args[++i].split(":");
						continue;
					}
				}
			}
			
			System.err.println("Invalid argument: " + args[i]);
			System.exit(1);
		}
		
		if((cs.isValid() == null) || forceConfig) {
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
		
		BNCSConnection c = new BNCSConnection(cs);
		
		if(plugins != null) {
			for(int i = 0; i < plugins.length; i++) {
				Class plugin = Class.forName(plugins[i]);
				EventHandler eh = (EventHandler)plugin.newInstance();
				c.addEventHandler(eh);
			}
		}
		
		c.addEventHandler(new CommandEventHandler(new Database()));
		if(cs.enableCLI)
			c.addEventHandler(new ConsoleEventHandler());
		if(cs.enableGUI)
			c.addEventHandler(new GuiEventHandler());
		c.start();
	
		//IconsDotBniReader.readIconsDotBni(BNFTPConnection.downloadFile(cs, "icons_STAR.bni"));
	}

}
