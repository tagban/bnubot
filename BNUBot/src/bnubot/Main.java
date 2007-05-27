package bnubot;

import bnubot.core.*;
import bnubot.core.bncs.BNCSConnection;
import bnubot.core.bot.console.ConsoleEventHandler;
import bnubot.core.bot.gui.GuiEventHandler;

public class Main {

	public static void main(String[] args) throws Exception {
		ConnectionSettings cs = new ConnectionSettings();
		cs.load();

		BNCSConnection c = new BNCSConnection(cs);
		c.addEventHandler("console", new ConsoleEventHandler());
		c.addEventHandler("gui", new GuiEventHandler());
		c.start();
	}

}
