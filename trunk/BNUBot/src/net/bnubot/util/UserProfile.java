/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util;

import java.util.ArrayList;
import java.util.List;

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
	
	private class KeyValue {
		public String key;
		public String value;
	}
	
	private String user;
	private List<KeyValue> data = new ArrayList<KeyValue>();
	
	public UserProfile(String user) {
		this.user = user;
	}
	
	public String getUser() {
		return user;
	}
	
	public void put(String key, String value) {
		KeyValue kv = new KeyValue();
		kv.key = key;
		kv.value = value;
		data.add(kv);
	}
	
	public String get(Object key) {;
		for(KeyValue kv : data)
			if(kv.key.equals(key))
				return kv.value;
		return null;
	}
	
	public List<String> keySet() {
		List<String> keys = new ArrayList<String>(data.size());
		for(KeyValue kv : data)
			keys.add(kv.key);
		return keys;
	}
	
	public List<String> keySetProfile() {
		List<String> keys = new ArrayList<String>(data.size());
		for(KeyValue kv : data)
			if(kv.key.startsWith(PROFILE_))
				keys.add(kv.key);
		return keys;
	}
}
