package bnubot.core;

import java.sql.*;
import java.util.Hashtable;

import bnubot.bot.database.Database;

public class BNetUser {
	private static Hashtable<String, BNetUser> bnCache = new Hashtable<String, BNetUser>(); 
	
	private String shortLogonName;	// #=yes, realm=only if different from "myRealm"
	private String fullLogonName;	// #=yes, realm=yes
	private String fullAccountName;	// #=no, realm=yes
	private String prettyName = null;
	
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
		String key = user;
		
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
		String key = user + ';' + myRealm;
		
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
	 * @return User[@Realm][#N]
	 */
	public String getShortLogonName() {
		return shortLogonName;
	}
	
	/**
	 * Gets the full logon name
	 * @return User@Realm[#N]
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
	 * Get the pretty name of a BNetUser
	 * @param d The database to work out of
	 * @return "[<prefix> ][<account> ]([<alias>,<alias>..])"
	 */
	public String getPrettyName(Database d) {
		if(prettyName == null) {
			try {
				ResultSet rsAccount = d.getAccount(this);
				if((rsAccount != null) && rsAccount.next()) {
					String account = rsAccount.getString("name");
					
					if(account != null)
						prettyName = account + " (" + fullLogonName + ")";
					else
						prettyName = fullLogonName;

					long access = rsAccount.getLong("access");
					ResultSet rsRank = d.getRank(access);
					if(rsRank.next()) {
						String prefix = rsRank.getString("prefix");
						if(prefix != null)
							prettyName = prefix + " " + prettyName;
					}
					rsRank.close();
				} else {
					prettyName = "[NOACCOUNT:" + fullAccountName + "]";
				}
			} catch(SQLException e) {
				e.printStackTrace();
				prettyName = "[SQLException:" + fullAccountName + "]";
			}
		}
		return prettyName;
	}
	
	/**
	 * Equivalent to getShortLogonName
	 * @return User[@Realm][#N]
	 */
	public String toString() {
		return shortLogonName;
	}
	
	public boolean equals(Object o) {
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
}
