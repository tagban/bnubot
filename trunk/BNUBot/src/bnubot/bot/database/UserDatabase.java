package bnubot.bot.database;

import java.util.Hashtable;

public class UserDatabase {
	private Hashtable<String, User> users = null;
	
	public UserDatabase() {
		users = new Hashtable<String, User>();
	}
	
	public void addUser(String user, User info) {
		users.put(user.toLowerCase(), info);
	}
	
	public User getUser(String user) {
		return users.get(user.toLowerCase());
	}
}
