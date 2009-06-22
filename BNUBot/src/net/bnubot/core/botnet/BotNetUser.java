/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.core.botnet;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import net.bnubot.logging.Out;
import net.bnubot.settings.ConnectionSettings;
import net.bnubot.util.BNetInputStream;
import net.bnubot.util.BNetUser;
import net.bnubot.util.crypto.HexDump;

/**
 * @author scotta
 */
public class BotNetUser extends BNetUser {
	private static final Map<Integer, InetAddress> servers = new HashMap<Integer, InetAddress>();
	static {
		for(String hostname : ConnectionSettings.bncsServers) {
			try {
				for(InetAddress ina : InetAddress.getAllByName(hostname))
					servers.put(
							BNetInputStream.readDWord(ina.getAddress(), 0),
							ina);
			} catch (UnknownHostException e) {}
		}
	}

	final int number;
	int dbflag = 0;
	int ztff = 0;
	String name = null;
	String channel = null;
	int server = -1;
	String account = null;
	String database = null;

	public BotNetUser(BotNetConnection con, int number, String name) {
		super(con, name, "BotNet");
		this.number = number;
		this.name = name;
	}

	public int getNumber() {
		return number;
	}

	public String getHandle() {
		return "*" + name + "%" + number;
	}

	public String getZTFF() {
		return getZTFF(ztff);
	}

	public static String getZTFF(int flags) {
		StringBuilder out = new StringBuilder();
		int f = 1;
		for(int i = 0; i < 32; i++) {
			if((flags & f) != 0)
				out.append((char)('A' + i));
			f <<= 1;
		}
		return out.toString();
	}

	@Override
	public String toString() {
		// Formatted username
		if((account == null) || (account.length() == 0))
			return getHandle();

		StringBuilder sb = new StringBuilder(account);
		sb.append(" (").append(getHandle()).append(")");
		return sb.toString();
	}

	@Override
	public String toStringEx() {
		StringBuilder sb = new StringBuilder(toString());

		// ZTFF flags
		if(ztff != 0)
			sb.append(" with flags ").append(getZTFF(ztff));

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
				sb.append(" access)");
			}
		}

		// Channel
		if((channel != null) && (channel.length() > 0))
			sb.append(" from channel ").append(channel);

		// Server
		if(server != -1) {
			sb.append(" of server ");
			InetAddress x = servers.get(server);
			if(x == null)
				sb.append(HexDump.DWordToIP(server));
			else
				sb.append(x);
			sb.append(".");
		}

		return sb.toString();
	}

	@Override
	public void sendChat(String text, boolean whisperBack, int priority) {
		try {
			BotNetConnection con = (BotNetConnection)super.con;
			con.sendWhisper(this, text);
		} catch (Exception e) {
			Out.exception(e);
		}
	}

	public String getDatabase() {
		return database;
	}

	@Override
	public String getWhisperCommand() {
		return "/botnet whisper %" + this.number + " ";
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof BotNetUser)
			return number == ((BotNetUser)o).number;
		return false;
	}
}
