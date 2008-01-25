/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core;

import net.bnubot.core.bncs.BNCSConnection;
import net.bnubot.core.digitaltext.DTConnection;
import net.bnubot.settings.ConnectionSettings;

public class ConnectionFactory {
	public static Connection createConnection(ConnectionSettings cs, ChatQueue chatQueue, Profile profile) {
		switch(cs.connectionType) {
		case BNCS:
			return new BNCSConnection(cs, chatQueue, profile);
		case DigitalText:
			cs.server = "koolaid.sidoh.org";
			cs.port = 1460;
			return new DTConnection(cs, chatQueue, profile);
		}
		return null;
	}
}
