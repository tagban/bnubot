
import core.*;
import core.bncs.BNCSConnection;

public class Main {

	public static void main(String[] args) throws Exception {
		ConnectionSettings cs = new ConnectionSettings();
		cs.autoconnect = true;
		cs.cdkey = "2148022963770";
		cs.password = "test";
		cs.username = "test";
		if(true) {
			cs.server = "uswest.battle.net";
			cs.port = 6112;
		} else {
			cs.server = "sigpi.ath.cx";
			cs.port = 80;
		}
		
		EventHandler e = null;
		BNCSConnection c = new BNCSConnection(cs, e);
		c.run();
	}

}
