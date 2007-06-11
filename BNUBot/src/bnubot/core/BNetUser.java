package bnubot.core;

public class BNetUser {
	String shortLogonName;	// #=yes, realm=only if different from "myRealm"
	String fullLogonName;	// #=yes, realm=yes
	String fullAccountName;	// #=no, realm=yes

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
