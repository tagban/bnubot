/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class UserProfile {
	public static final String PROFILE_ = "profile\\";
	public static final String PROFILE_SEX = PROFILE_ + "sex";
	//public static final String PROFILE_AGE = PROFILE_ + "age";
	public static final String PROFILE_LOCATION = PROFILE_ + "location";
	public static final String PROFILE_DESCRIPTION = PROFILE_ + "description";
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
		data.put(key, value);
	}
	
	public String get(Object key) {
		return (String)data.get(key);
	}
	
	public Set<Object> keySet() {
		return data.keySet();
	}
	
	public List<String> keySetProfile() {
		List<String> keys = new ArrayList<String>(data.size());
		for(Object o : data.keySet()) {
			String key = o.toString();
			if(key.startsWith(PROFILE_))
				keys.add(key);
		}
		return keys;
	}
}
