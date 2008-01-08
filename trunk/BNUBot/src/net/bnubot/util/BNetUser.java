/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util;

import java.sql.SQLException;

import net.bnubot.bot.database.AccountResultSet;
import net.bnubot.bot.database.Database;
import net.bnubot.bot.database.RankResultSet;
import net.bnubot.settings.GlobalSettings;

/**
 * A class responsible for formatting Battle.net usernames.
 * Now it includes support for the database, which will make toString() quite pretty.
 * @author scotta
 */
public class BNetUser {
	private String shortLogonName;	// #=yes, realm=only if different from "myRealm"
	private String fullLogonName;	// #=yes, realm=yes
	private final String fullAccountName;	// #=no, realm=yes
	private String realm = null;
	private Integer flags = null;
	private Integer ping = null;
	private StatString statString = null;
	
	private String lastToString = null;
	private long lastToStringTime = 0;

	/**
	 * Constructor for a BNetUser
	 * @param user		User[#N]@Realm
	 */
	public BNetUser(String user) {
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
	public BNetUser(String user, String myRealm) {
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
		Database d = Database.getInstance();
		if(d == null)
			return shortLogonName;
		
		String shortPrettyName = shortLogonName;
		try {
			AccountResultSet rsAccount = d.getAccount(this);
			if((rsAccount != null) && rsAccount.next()) {
				String account = rsAccount.getName();
				
				if(account != null)
					shortPrettyName = account;

				long access = rsAccount.getAccess();
				RankResultSet rsRank = d.getRank(access);
				if(rsRank.next()) {
					String prefix = rsRank.getShortPrefix();
					if(prefix == null)
						prefix = rsRank.getPrefix();
					if(prefix != null)
						shortPrettyName = prefix + " " + shortPrettyName;
				}
				d.close(rsRank);
			}
			if(rsAccount != null)
				d.close(rsAccount);
		} catch(SQLException e) {
			Out.exception(e);
		}
		
		return shortPrettyName;
	}
	
	/**
	 * Equivalent to getShortLogonName if there is no database or if the user isn't in it;
	 * @return User[#N][@Realm] or [Prefix ][Account (]FullLogonName[)]
	 */
	private String getPrettyName() {
		Database d = Database.getInstance();
		if(d == null)
			return shortLogonName;

		String prettyName = shortLogonName;
		try {
			AccountResultSet rsAccount = d.getAccount(this);
			if((rsAccount != null) && rsAccount.next()) {
				String account = rsAccount.getName();
				
				if(account != null)
					prettyName = account + " (" + prettyName + ")";

				long access = rsAccount.getAccess();
				RankResultSet rsRank = d.getRank(access);
				if(rsRank.next()) {
					String prefix = rsRank.getPrefix();
					if(prefix != null)
						prettyName = prefix + " " + prettyName;
				}
				d.close(rsRank);
			}
			if(rsAccount != null)
				d.close(rsAccount);
		} catch(SQLException e) {
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
		}
		throw new IllegalStateException("Unknown GlobalSettings.bnUserToString " + GlobalSettings.bnUserToString);
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

	public Integer getFlags() {
		return flags;
	}

	public void setFlags(Integer flags) {
		this.flags = flags;
	}

	public Integer getPing() {
		return ping;
	}

	public void setPing(Integer ping) {
		this.ping = ping;
	}

	public StatString getStatString() {
		return this.statString;
	}

	public void setStatString(StatString statString) {
		this.statString = statString;
	}

	/**
	 * Convert a BNetUser to a BNetUser from a different perspective
	 */
	public BNetUser toPerspective(BNetUser myRealm) {
		BNetUser out = new BNetUser(fullLogonName, myRealm.getFullAccountName());
		out.setFlags(getFlags());
		out.setPing(getPing());
		out.setStatString(getStatString());
		return out;
	}
}
