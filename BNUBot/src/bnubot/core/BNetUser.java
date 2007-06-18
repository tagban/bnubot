package bnubot.core;

import java.sql.*;
import java.util.Hashtable;

import bnubot.bot.database.Database;

/**
 * A class responsible for formatting Battle.net usernames.
 * Now it includes support for the database, which will make toString() quite pretty.
 * @author Scott Anderson
 */
public class BNetUser {
	private static Hashtable<String, BNetUser> bnCache = new Hashtable<String, BNetUser>(); 
	
	private String shortLogonName;	// #=yes, realm=only if different from "myRealm"
	private String fullLogonName;	// #=yes, realm=yes
	private String fullAccountName;	// #=no, realm=yes
	private String prettyName = null;
	private Integer flags = null;
	private Integer ping = null;

	private static Database d = null;
	
	public static void setDatabase(Database d) {
		BNetUser.d = d;
	}
	
	/**
	 * Clear the BNetUser cache
	 */
	public static void clearCache() {
		bnCache.clear();
	}

	/**
	 * Cacheing constructor for a BNetUser
	 * @param user		User[#N]@Realm
	 */
	public static BNetUser getBNetUser(String user) {
		String key = user.toLowerCase();
		BNetUser bnc = bnCache.get(key);
		if(bnc == null) {
			bnc = new BNetUser(user);
			bnCache.put(key, bnc);
		}
		return bnc;
	}

	/**
	 * Cacheing constructor for a BNetUser
	 * @param user		User[#N][@Realm]
	 * @param myRealm	[User[#N]@]Realm
	 */
	public static BNetUser getBNetUser(String user, String myRealm) {
		// Drop the @ from realm
		int i = myRealm.indexOf('@');
		if(i != -1)
			myRealm = myRealm.substring(i + 1);
		
		//Generate a unique key
		String key = user;
		i = key.indexOf('@');
		if(i == -1)
			key += '@' + myRealm;
		
		key = key.toLowerCase();
		
		//Look for the key in cache
		BNetUser bnc = bnCache.get(key);
		if(bnc == null) {
			//No cache hit; create and add it
			bnc = new BNetUser(user, myRealm);
			bnCache.put(key, bnc);
		}
		return bnc;
	}

	/**
	 * Cacheing constructor for a BNetUser
	 * @param user		User[#N][@Realm]
	 * @param myRealm	<BNetUser>
	 */
	public static BNetUser getBNetUser(String user, BNetUser model) {
		return getBNetUser(user, model.getFullAccountName());
	}

	/**
	 * Constructor for a BNetUser
	 * @param user		User[#N]@Realm
	 */
	public BNetUser(String user) {
		String uAccount;
		String uRealm;
		int uNumber = 0;
		
		int i = user.indexOf('#');
		if(i != -1) {
			String num = user.substring(i + 1);
			int j = num.indexOf('@');
			if(j != -1) {
				num = num.substring(0, j);
				uRealm = user.substring(i + j + 2);
				user = user.substring(0, i) + '@' + uRealm;
			} else {
				throw new IllegalStateException("User [" + user + "] is not a valid bnet user; no realm");
			}
			
			uNumber = Integer.parseInt(num);
		}
		
		String up[] = user.split("@", 2);
		uAccount = up[0];
		if(up.length == 2)
			uRealm = up[1];
		else
			throw new IllegalStateException("User [" + user + "] is not a valid bnet user; no realm");
		
		
		// ...
		shortLogonName = uAccount;
		if(uNumber != 0)
			shortLogonName += "#" + uNumber;
		shortLogonName += "@" + uRealm;
		
		// ...
		fullLogonName = uAccount;
		if(uNumber != 0)
			fullLogonName += "#" + uNumber;
		fullLogonName += "@" + uRealm;
		
		// ...
		fullAccountName = uAccount + "@" + uRealm;
	}
	
	/**
	 * Constructor for a BNetUser
	 * @param user		User[#N][@Realm]
	 * @param myRealm	[User[#N]@]Realm
	 */
	public BNetUser(String user, String myRealm) {
		String uAccount;
		String uRealm;
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
				uRealm = user.substring(i + j + 2);
				user = user.substring(0, i) + '@' + uRealm;
			} else {
				user = user.substring(0, i);
			}
			
			uNumber = Integer.parseInt(num);
		}
		
		String up[] = user.split("@", 2);
		uAccount = up[0];
		if(up.length == 2)
			uRealm = up[1];
		else
			uRealm = myRealm;
		
		Boolean onMyRealm = uRealm.equals(myRealm);
		
		// ...
		shortLogonName = uAccount;
		if(uNumber != 0)
			shortLogonName += "#" + uNumber;
		if(!onMyRealm)
			shortLogonName += "@" + uRealm;
		
		// ...
		fullLogonName = uAccount;
		if(uNumber != 0)
			fullLogonName += "#" + uNumber;
		fullLogonName += "@" + uRealm;
		
		// ...
		fullAccountName = uAccount + "@" + uRealm;
	}
	
	/**
	 * Gets the shortest possible logon name
	 * @return User[#N][@Realm]
	 */
	public String getShortLogonName() {
		return shortLogonName;
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
		prettyName = null;
	}
	
	/**
	 * Equivalent to getShortLogonName if there is no database or if the user isn't in it;
	 * @return User[#N][@Realm] or [Prefix ][Account (]FullLogonName[)]
	 */
	public String toString() {
		if(d == null)
			return shortLogonName;

		if(prettyName == null) {
			prettyName = shortLogonName;
			try {
				ResultSet rsAccount = d.getAccount(this);
				if((rsAccount != null) && rsAccount.next()) {
					String account = rsAccount.getString("name");
					
					if(account != null)
						prettyName = account + " (" + prettyName + ")";

					long access = rsAccount.getLong("access");
					ResultSet rsRank = d.getRank(access);
					if(rsRank.next()) {
						String prefix = rsRank.getString("prefix");
						if(prefix != null)
							prettyName = prefix + " " + prettyName;
					}
					rsRank.close();
				}
			} catch(SQLException e) {
				e.printStackTrace();
			}
		}
		return prettyName;
	}
	
	public boolean equals(Object o) {
		if(o == this)
			return true;
		
		if(o instanceof BNetUser) {
			BNetUser u = (BNetUser)o;
			if(u.getFullLogonName().compareToIgnoreCase(fullLogonName) == 0)
				return true;
		} else if(o instanceof String) {
			String s = (String)o;
			if(s.compareToIgnoreCase(fullLogonName) == 0)
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
}
