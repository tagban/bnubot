
import core.*;
import core.bncs.BNCSConnection;
import core.bot.ConsoleEventHandler;

public class Main {

	public static void main(String[] args) throws Exception {
		ConnectionSettings cs = new ConnectionSettings();
		cs.load();
		
		//BNFTPConnection f = new BNFTPConnection(cs, "Icons.bni");
		//f.start();
		
		BNCSConnection c = new BNCSConnection(cs);
		c.addEventHandler("test", new ConsoleEventHandler());
		c.start();
	}

}
