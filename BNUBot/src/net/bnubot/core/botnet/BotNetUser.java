/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.core.botnet;


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
	String server = null;
	String account = null;
	String database = null;

	public String toString() {
		StringBuilder sb = new StringBuilder("User [#");
		sb.append(Integer.toHexString(number)).append("] ");
		sb.append(name).append(" in channel [ ");
		sb.append(channel).append(" ] of server [ ");
		sb.append(server).append(" ]");
		if((account != null) && !account.equals(""))
			sb.append(" with account [ ").append(account).append(" ]");
		if((database != null) && !database.equals(""))
			sb.append(" on database [ ").append(database).append(" ]");
		
		return sb.toString();
	}
}
