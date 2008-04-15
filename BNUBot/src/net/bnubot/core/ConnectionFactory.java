/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core;

import net.bnubot.core.bncs.BNCSConnection;
import net.bnubot.core.botnet.BotNetConnection;
import net.bnubot.core.digitaltext.DTConnection;
import net.bnubot.settings.ConnectionSettings;

public class ConnectionFactory {
	public static Connection createConnection(ConnectionSettings cs, ChatQueue chatQueue, Profile profile) {
		switch(cs.connectionType) {
		case BNCS:
			if((cs.port == 0x5555) || (cs.port == 1460))
				cs.port = 6112;
			return new BNCSConnection(cs, profile);
		case BotNet:
			if((cs.port == 6112) || (cs.port == 1460) || cs.server.endsWith(".battle.net")) {
				cs.server = "botnet.valhallalegends.com";
				cs.port = 0x5555;
			}
			return new BotNetConnection(cs, profile);
		case DigitalText:
			if((cs.port == 6112) || (cs.port == 0x5555)) {
				cs.server = "koolaid.sidoh.org";
				cs.port = 1460;
			}
			return new DTConnection(cs, profile);
		}
		return null;
	}
}
