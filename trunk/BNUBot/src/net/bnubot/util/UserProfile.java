/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author scotta
 */
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
	private List<String> keys = new ArrayList<String>();
	private Properties data = new Properties();

	public UserProfile(String user) {
		this.user = user;
	}

	public String getUser() {
		return user;
	}

	public void put(String key, String value) {
		if(!keys.contains(key))
			keys.add(key);
		data.put(key, value);
	}

	public String get(String key) {
		return data.getProperty(key);
	}

	public List<String> keySet() {
		return keys;
	}

	public List<String> keySetProfile() {
		List<String> ret = new ArrayList<String>(keys.size());
		for(String key : keys)
			if(key.startsWith(PROFILE_))
				ret.add(key);
		return ret;
	}
}
