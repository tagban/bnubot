/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util;

import java.util.Properties;
import java.util.Set;

public class UserProfile {
	public static final String PROFILE_SEX = "profile\\sex";
	public static final String PROFILE_AGE = "profile\\age";
	public static final String PROFILE_LOCATION = "profile\\location";
	public static final String PROFILE_DESCRIPTION = "profile\\description";
	public static final String SYSTEM_ACCOUNT_CREATED = "System\\Account Created";
	public static final String SYSTEM_LAST_LOGON = "System\\Last Logon";
	public static final String SYSTEM_LAST_LOGOFF = "System\\Last Logoff";
	public static final String SYSTEM_TIME_LOGGED = "System\\Time Logged";
	public static final String SYSTEM_USERNAME = "System\\Username";
	
	private String user;
	private Properties data = new SortedProperties();
	
	public UserProfile(String user) {
		this.user = user;
	}
	
	public String getUser() {
		return user;
	}
	
	public void put(String key, String value) {
		if(key.startsWith("profile\\"))
			data.put(key, value);
	}
	
	public String get(Object key) {
		return (String)data.get(key);
	}
	
	public Set<Object> keySet() {
		return data.keySet();
	}
}
