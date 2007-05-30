package bnubot;

import bnubot.bot.CommandEventHandler;
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
		
		if(!cs.isValid()) {
			ConfigurationFrame cf = new ConfigurationFrame(cs);
			cf.setVisible(true);
			
			while(cf.isVisible())
				Thread.yield();
		}
		
		BNCSConnection c = new BNCSConnection(cs);
		c.addEventHandler("command", new CommandEventHandler(new Database()));
		if(cs.enableCLI)
			c.addEventHandler("console", new ConsoleEventHandler());
		if(cs.enableGUI)
			c.addEventHandler("gui", new GuiEventHandler());
		c.start();
	
		//IconsDotBniReader.readIconsDotBni(BNFTPConnection.downloadFile(cs, "icons_STAR.bni"));
	}

}
