/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util;

import net.bnubot.core.Connection;
import net.bnubot.db.Account;
import net.bnubot.db.Rank;
import net.bnubot.db.conf.DatabaseContext;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.vercheck.CurrentVersion;
import net.bnubot.vercheck.ReleaseType;

import org.apache.cayenne.ObjectContext;

/**
 * A class responsible for formatting Battle.net usernames.
 * Now it includes support for the database, which will make toString() quite pretty.
 * A BNetUser has a Connection, and can be whispered by .sendChat()
 * @author scotta
 */
public class BNetUser {
	private Connection con;
	private String shortLogonName;	// #=yes, realm=only if different from "myRealm"
	private String fullLogonName;	// #=yes, realm=yes
	private final String fullAccountName;	// #=no, realm=yes
	private String realm = null;
	private int flags = 0;
	private int ping = -1;
	private StatString statString = null;
	
	private String lastToString = null;
	private long lastToStringTime = 0;

	/**
	 * Constructor for a BNetUser
	 * @param user		User[#N]@Realm
	 */
	public BNetUser(Connection con, String user) {
		this.con = con;
		String uAccount;
		int uNumber = 0;
		
		int i = user.indexOf('#');
		if(i != -1) {
			String num = user.substring(i + 1);
			int j = num.indexOf('@');
			if(j != -1) {
				num = num.substring(0, j);
				this.realm = user.substring(i + j + 2);
				user = user.substring(0, i) + '@' + this.realm;
			} else {
				throw new IllegalStateException("User [" + user + "] is not a valid bnet user; no realm");
			}
			
			uNumber = Integer.parseInt(num);
		}
		
		String up[] = user.split("@", 2);
		uAccount = up[0];
		if(up.length == 2)
			this.realm = up[1];
		else
			throw new IllegalStateException("User [" + user + "] is not a valid bnet user; no realm");
		
		
		// ...
		shortLogonName = uAccount;
		if(uNumber != 0)
			shortLogonName += "#" + uNumber;
		shortLogonName += "@" + this.realm;
		
		// ...
		fullLogonName = uAccount;
		if(uNumber != 0)
			fullLogonName += "#" + uNumber;
		fullLogonName += "@" + this.realm;
		
		// ...
		fullAccountName = uAccount + "@" + this.realm;
	}
	
	/**
	 * Constructor for a BNetUser
	 * @param user		User[#N][@Realm]
	 * @param myRealm	[User[#N]@]Realm
	 */
	public BNetUser(Connection con, String user, String myRealm) {
		this.con = con;
		String uAccount;
		int uNumber = 0;
		
		int i = myRealm.indexOf('@');
		if(i != -1)
			myRealm = myRealm.substring(i + 1);
		
		i = user.indexOf('#');
		if(i != -1) {
			String num = user.substring(i + 1);
			int j = num.indexOf('@');
			if(j != -1) {
				num = num.substring(0, j);
				this.realm = user.substring(i + j + 2);
				user = user.substring(0, i) + '@' + this.realm;
			} else {
				user = user.substring(0, i);
			}
			
			uNumber = Integer.parseInt(num);
		}
		
		String up[] = user.split("@", 2);
		uAccount = up[0];
		if(up.length == 2)
			this.realm = up[1];
		else
			this.realm = myRealm;
		
		Boolean onMyRealm = this.realm.equals(myRealm);
		
		// ...
		shortLogonName = uAccount;
		if(uNumber != 0)
			shortLogonName += "#" + uNumber;
		if(!onMyRealm)
			shortLogonName += "@" + this.realm;
		
		// ...
		fullLogonName = uAccount;
		if(uNumber != 0)
			fullLogonName += "#" + uNumber;
		fullLogonName += "@" + this.realm;
		
		// ...
		fullAccountName = uAccount + "@" + this.realm;
	}

	/**
	 * Gets the shortest possible logon name
	 * @return User[#N][@Realm]
	 */
	public String getShortLogonName() {
		return shortLogonName;
	}

	/**
	 * Gets the shortest possible logon name
	 * @return User[#N][@Realm]
	 */
	public String getShortLogonName(BNetUser perspective) {
		if(this.realm.equals(perspective.realm))
			return shortLogonName;
		return fullLogonName;
	}
	
	/**
	 * Gets the full logon name
	 * @return User[#N]@Realm
	 */
	public String getFullLogonName() {
		return fullLogonName;
	}
	
	/**
	 * Gets the full account name
	 * @return "User@Realm"
	 */
	public String getFullAccountName() {
		return fullAccountName;
	}
	
	/**
	 * Resets the pretty name back to null, so it will be re-evaluated next time toString() is called
	 */
	public void resetPrettyName() {
		lastToString = null;
		lastToStringTime = 0;
	}
	
	private String getShortPrettyName() {
		ObjectContext context = DatabaseContext.getContext();
		if(context == null)
			return shortLogonName;
		
		String shortPrettyName = shortLogonName;
		try {
			Account account = Account.get(this);
			if(account != null) {
				String name = account.getName();
				if(name != null)
					shortPrettyName = name;

				Rank rank = account.getRank();
				if(rank != null) {
					String prefix = rank.getShortPrefix();
					if(prefix == null)
						prefix = rank.getPrefix();
					if(prefix != null)
						shortPrettyName = prefix + " " + shortPrettyName;
				}
			}
		} catch(Exception e) {
			Out.exception(e);
		}
		
		return shortPrettyName;
	}
	
	/**
	 * Equivalent to getShortLogonName if there is no database or if the user isn't in it;
	 * @return User[#N][@Realm] or [Prefix ][Account (]FullLogonName[)]
	 */
	private String getPrettyName() {
		ObjectContext context = DatabaseContext.getContext();
		if(context == null)
			return shortLogonName;

		String prettyName = shortLogonName;
		try {
			Account account = Account.get(this);
			if(account != null) {
				String name = account.getName();
				if(name != null)
					prettyName = name + " (" + prettyName + ")";

				Rank rank = account.getRank();
				if(rank != null) {
					String prefix = rank.getPrefix();
					if(prefix != null)
						prettyName = prefix + " " + prettyName;
				}
			}
		} catch(Exception e) {
			Out.exception(e);
		}
		
		return prettyName;
	}
	
	/**
	 * Equivalent to getShortLogonName if there is no database or if the user isn't in it;
	 * @return User[#N][@Realm] or &lt;Account> [(FullLogonName)]
	 */
	private String getAccountAndLogin() {
		ObjectContext context = DatabaseContext.getContext();
		if(context == null)
			return shortLogonName;

		String prettyName = shortLogonName;
		try {
			Account account = Account.get(this);
			if(account != null) {
				String name = account.getName();
				if(name != null)
					prettyName = name + " (" + prettyName + ")";
			}
		} catch(Exception e) {
			Out.exception(e);
		}
		
		return prettyName;
	}
	
	/**
	 * Equivalent to getShortLogonName if there is no database or if the user isn't in it;
	 * @return User[#N][@Realm] or &lt;Account>
	 */
	private String getAccountOrLogin() {
		ObjectContext context = DatabaseContext.getContext();
		if(context == null)
			return shortLogonName;

		String prettyName = shortLogonName;
		try {
			Account account = Account.get(this);
			if(account != null) {
				String name = account.getName();
				if(name != null)
					prettyName = name;
			}
		} catch(Exception e) {
			Out.exception(e);
		}
		
		return prettyName;
	}
	
	/**
	 * Returns user-desirable display string
	 */
	public String toString() {
		// Check if we should re-generate the string; cache it for five seconds
		if((lastToString == null)
		|| (System.currentTimeMillis() - lastToStringTime > 5000)) {
			lastToStringTime = System.currentTimeMillis();
			lastToString = toString(GlobalSettings.bnUserToString);
		}
		
		return lastToString;
	}

	public String toString(int type) {
		switch(type) {
		case 0: return getFullLogonName();		// BNLogin@Gateway
		case 1: return getShortLogonName();		// BNLogin
		case 2: return getShortPrettyName();	// Prefix Account
		case 3: return getPrettyName();			// Prefix Account (BNLogin)
		case 4: return getAccountOrLogin();		// Account
		case 5: return getAccountAndLogin();	// Account (BNLogin)
		}
		throw new IllegalStateException("Unknown BNetUser.toString(int) type " + GlobalSettings.bnUserToString);
	}
	
	public String toStringEx() {
		String out = toString() + " [" + ping + "ms]";
		if(flags != 0) {
			out += " (";
			if((flags & 0x01) != 0)
				out += "Blizzard Representative, ";
			if((flags & 0x08) != 0)
				out += "Battle.net Representative, ";
			if((flags & 0x02) != 0)
				out += "Channel Operator, ";
			if((flags & 0x04) != 0)
				out += "Speaker, ";
			if((flags & 0x10) != 0)
				out += "No UDP Support, ";
			if((flags & 0x40) != 0)
				out += "Battle.net Guest, ";
			if((flags & 0x20) != 0)
				out += "Squelched, ";
			if((flags & 0x100000) != 0)
				out += "GF Official, ";
			if((flags & 0x200000) != 0)
				out += "GF Player, ";
			int flags2 = flags & ~0x30007F;
			if(flags2 != 0)
				out += "0x" + Integer.toHexString(flags2) + ", ";
			out = out.substring(0, out.length() - 2);
			out += ")";
		}
		return out;
	}
	
	public boolean equals(Object o) {
		if(o == this)
			return true;
		
		if(o instanceof BNetUser) {
			BNetUser u = (BNetUser)o;
			if(u.getFullLogonName().equalsIgnoreCase(fullLogonName))
				return true;
		} else if(o instanceof String) {
			String s = (String)o;
			if(s.equalsIgnoreCase(fullLogonName))
				return true;
			if(s.equalsIgnoreCase(shortLogonName))
				return true;
		} else {
			throw new IllegalArgumentException("Unknown type " + o.getClass().getName());
		}
		
		return false;
	}

	public int getFlags() {
		return flags;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	public int getPing() {
		return ping;
	}

	public void setPing(int ping) {
		this.ping = ping;
	}

	public StatString getStatString() {
		return statString;
	}

	public void setStatString(StatString statString) {
		this.statString = statString;
	}

	/**
	 * Convert a BNetUser to a BNetUser from a different perspective
	 */
	public BNetUser toPerspective(BNetUser myRealm) {
		if(myRealm.con != con)
			throw new IllegalStateException("Can not format BNetUser to a perspective on a different Connection");
		BNetUser out = new BNetUser(con, fullLogonName, myRealm.getFullAccountName());
		out.flags = flags;
		out.ping = ping;
		out.statString = statString;
		return out;
	}

	/**
	 * Send chat to a user in command response style - either whispered, or formatted with the user's name
	 * @param msg
	 * @param b
	 */
	public static final int MAX_CHAT_LENGTH = 242;
	public void sendChat(String text, boolean whisperBack) {
		if((text == null) || (con == null))
			return;

		text = con.cleanText(text, true);
		
		boolean isMyUser = false;
		BNetUser myUser = con.getMyUser();
		if(myUser != null)
			isMyUser = myUser.equals(this);
		
		if(whisperBack && isMyUser)
			con.recieveInfo(text);
		else {
			String prefix;
			if(whisperBack || isMyUser) {
				if(whisperBack)
					prefix = "/w " + this.getFullLogonName() + " ";
				else
					prefix = "";
				
				prefix += "[BNU";
				ReleaseType rt = CurrentVersion.version().getReleaseType();
				if(rt.isNightly())
					prefix += " Nightly";
				else if(rt.isAlpha())
					prefix += " Alpha";
				else if(rt.isBeta())
					prefix += " Beta";
				else if(rt.isReleaseCandidate())
					prefix += " RC";
				prefix += "] ";
			} else {
				prefix = this.toString(GlobalSettings.bnUserToStringCommandResponse) + ": ";
			}
		
			//Split up the text in to appropriate sized pieces
			int pieceSize = MAX_CHAT_LENGTH - prefix.length();
			for(int i = 0; i < text.length(); i += pieceSize) {
				String piece = prefix + text.substring(i);
				if(piece.length() > MAX_CHAT_LENGTH)
					piece = piece.substring(0, MAX_CHAT_LENGTH);
				con.queueChatHelper(piece, false);
			}
		}
	}
}
