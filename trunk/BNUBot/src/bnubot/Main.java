package bnubot;

import bnubot.bot.console.ConsoleEventHandler;
import bnubot.bot.gui.GuiEventHandler;
import bnubot.bot.gui.userlist.IconsDotBniReader;
import bnubot.core.*;
import bnubot.core.bncs.BNCSConnection;
import bnubot.core.bnftp.BNFTPConnection;

public class Main {

	public static void main(String[] args) throws Exception {
		ConnectionSettings cs = new ConnectionSettings();
		cs.load();
		//cs.save();

		BNCSConnection c = new BNCSConnection(cs);
		if(cs.enableCLI)
			c.addEventHandler("console", new ConsoleEventHandler());
		if(cs.enableGUI)
			c.addEventHandler("gui", new GuiEventHandler());
		c.start();

		//IconsDotBniReader.readIconsDotBni(BNFTPConnection.downloadFile(cs, "icons_STAR.bni"));
	}

}
