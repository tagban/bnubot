package bnubot;

import bnubot.core.*;
import bnubot.core.bncs.BNCSConnection;
import bnubot.core.bot.console.ConsoleEventHandler;
import bnubot.core.bot.gui.GuiEventHandler;

public class Main {

	public static void main(String[] args) throws Exception {
		ConnectionSettings cs = new ConnectionSettings();
		cs.load();
		cs.save();

		BNCSConnection c = new BNCSConnection(cs);
		if(cs.enableCLI)
			c.addEventHandler("console", new ConsoleEventHandler());
		if(cs.enableGUI)
			c.addEventHandler("gui", new GuiEventHandler());
		c.start();
	}

}
