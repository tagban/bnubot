/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.core.botnet;

import net.bnubot.util.HexDump;


/**
 * @author sanderson
 *
 */
public class BotNetUser {
	int number = 0;
	int dbflag = 0;
	int ztff = 0;
	String name = null;
	String channel = null;
	int server = -1;
	String account = null;
	String database = null;

	public String toString() {
		StringBuilder sb = new StringBuilder("User [#");
		sb.append(Integer.toHexString(number)).append("] ");
		sb.append(name).append(" in channel [ ");
		sb.append(channel).append(" ]");
		if(server != -1)
			sb.append(" of server [ ").append(HexDump.DWordToIP(server)).append(" ]");
		if((account != null) && !account.equals(""))
			sb.append(" with account [ ").append(account).append(" ]");
		if((database != null) && !database.equals(""))
			sb.append(" on database [ ").append(database).append(" ]");
		
		return sb.toString();
	}
}
