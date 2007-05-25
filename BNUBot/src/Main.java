
import core.*;
import core.bncs.BNCSConnection;
import core.bnftp.BNFTPConnection;

public class Main {

	public static void main(String[] args) throws Exception {
		ConnectionSettings cs = new ConnectionSettings();
		cs.autoconnect = true;
		/*
		cs.cdkey = "2148022963770";
		cs.product = ConnectionSettings.PRODUCT_STARCRAFT;
		
		cs.cdkey = "C489Y449XM66MDHNYZKGGE6PHP";
		cs.product = ConnectionSettings.PRODUCT_WARCRAFT3;
		*/
		cs.cdkey = "2TK68RK6CKPBV2C2";
		cs.product = ConnectionSettings.PRODUCT_DIABLO2;
		
		cs.password = "test";
		cs.username = "test";
		
		cs.server = "uswest.battle.net";
		cs.port = 6112;
		
		//cs.server = "sigpi.ath.cx";
		//cs.port = 80;
		
		//BNFTPConnection f = new BNFTPConnection(cs, "Icons.bni");
		//f.run();
		
		EventHandler e = null;
		BNCSConnection c = new BNCSConnection(cs, e);
		c.run();
	}

}
