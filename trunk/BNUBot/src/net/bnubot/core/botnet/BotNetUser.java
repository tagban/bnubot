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
		StringBuilder sb = new StringBuilder();
		
		// Formatted username
		String fmtUser = "*" + name + "%" + number;
		if((account == null) || (account.length() == 0))
			return fmtUser;
		
		sb.append(account).append(" (").append(fmtUser).append(")");
		return sb.toString();
	}
	
	public String toStringEx() {
		StringBuilder sb = new StringBuilder(toString());
		
		// ZTFF flags
		if(ztff != 0)
			sb.append(" with flags [ 0x").append(Integer.toHexString(ztff)).append(" ]");
		
		// Database/dbflags
		if((database != null) && (database.length() > 0)) {
			sb.append(" under database ").append(database);
			if(dbflag != 0) {
				sb.append(" (with ");
				//.append(dbflag).append(" ]");
				if((dbflag & 4) != 0)
					sb.append("restricted");
				else if((dbflag & 2) != 0)
					sb.append("write");
				else if((dbflag & 1) != 0)
					sb.append("read");
				else
					sb.append("0x").append(Integer.toHexString(dbflag));
				sb.append(" access");
			}
		}
		
		// Channel
		if((channel != null) && (channel.length() > 0))
			sb.append(" from channel ").append(channel);
		
		// Server
		if(server != -1)
			sb.append(" of server ").append(HexDump.DWordToIP(server)).append(".");
		
		return sb.toString();
	}
}
