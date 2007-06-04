package bnubot.core;

public class BNetUser {
	String shortLogonName;	// #=yes, realm=only if necessary
	String fullLogonName;	// #=yes, realm=yes
	String fullAccountName;	// #=no, realm=yes

	public BNetUser(String user, String myRealm) {
		String uAccount;
		String uRealm;
		int uNumber = 0;
		
		int i = myRealm.indexOf('@');
		if(i != -1)
			myRealm = myRealm.substring(i + 1);
		
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
	
	public String getShortLogonName() {
		return shortLogonName;
	}
	
	public String getFullLogonName() {
		return fullLogonName;
	}
	
	public String getFullAccountName() {
		return fullAccountName;
	}
	
	public String toString() {
		return shortLogonName;
	}
	
	public boolean Equals(Object o) {
		if(o instanceof BNetUser) {
			BNetUser u = (BNetUser)o;
			if(u.getFullLogonName().equals(fullLogonName))
				return true;
		}
		
		return false;
	}
}
