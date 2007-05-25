
import core.*;
import core.bncs.BNCSConnection;
import core.bot.ConsoleEventHandler;

public class Main {

	public static void main(String[] args) throws Exception {
		ConnectionSettings cs = new ConnectionSettings();
		cs.autoconnect = true;
		
		cs.cdkey = "2148022963770";
		cs.product = ConnectionSettings.PRODUCT_STARCRAFT;
		
		cs.cdkey = "C489Y449XM66MDHNYZKGGE6PHP";
		cs.product = ConnectionSettings.PRODUCT_WARCRAFT3;
		
		//cs.cdkey = "2TK68RK6CKPBV2C2";
		//cs.product = ConnectionSettings.PRODUCT_DIABLO2;
		
		cs.username = "BNU-Bot";
		cs.password = "012040a";
		
		cs.server = "useast.battle.net";
		cs.port = 6112;
		
		//cs.server = "sigpi.ath.cx";
		//cs.port = 80;
		
		//BNFTPConnection f = new BNFTPConnection(cs, "Icons.bni");
		//f.start();
		
		BNCSConnection c = new BNCSConnection(cs);
		c.addEventHandler("test", new ConsoleEventHandler());
		c.start();
	}

}
