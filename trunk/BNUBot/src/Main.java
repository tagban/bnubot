
import core.*;
import core.bncs.BNCSConnection;
import core.bnftp.BNFTPConnection;
import core.bot.ConsoleEventHandler;
import core.bot.gui.GuiEventHandler;
import core.bot.gui.icons.IconsDotBniReader;

public class Main {

	public static void main(String[] args) throws Exception {
		ConnectionSettings cs = new ConnectionSettings();
		cs.load();

		IconsDotBniReader.readIconsDotBni(BNFTPConnection.downloadFile(cs, "Icons.bni"));
		//BNFTPConnection.downloadFile(cs, "Icons.bni");
		
	/*	BNCSConnection c = new BNCSConnection(cs);
		c.addEventHandler("console", new ConsoleEventHandler());
		c.addEventHandler("gui", new GuiEventHandler());
		c.start();
	*/
	}

}
