package bnubot.core;

public class BNetUser {
	String shortLogonName;	// #=yes, realm=only if different from "myRealm"
	String fullLogonName;	// #=yes, realm=yes
	String fullAccountName;	// #=no, realm=yes

	/**
	 * Constructor for a BNetUser
	 * @param user		User[@Realm][#N]
	 * @param myRealm	[User@]Realm
	 */
	public BNetUser(String user, String myRealm) {
		String uAccount;
		String uRealm;
		int uNumber = 0;
		
		int i = myRealm.indexOf('@');
		if(i != -1)
			myRealm = myRealm.substring(i + 1);
		i = myRealm.indexOf('#');
		if(i != -1)
			myRealm = myRealm.substring(0, i);
		
		i = user.indexOf('#');
		if(i != -1) {
			uNumber = Integer.parseInt(user.substring(i + 1));
			user = user.substring(0, i);
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
		if(!onMyRealm)
			shortLogonName += "@" + uRealm;
		if(uNumber != 0)
			shortLogonName += "#" + uNumber;
		
		// ...
		fullLogonName = uAccount + "@" + uRealm;
		if(uNumber != 0)
			fullLogonName += "#" + uNumber;
		
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
			if(u.getFullLogonName().equals(fullLogonName))
				return true;
		}
		
		return false;
	}
}
