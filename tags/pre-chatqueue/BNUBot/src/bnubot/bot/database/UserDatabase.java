package bnubot.bot.database;

import java.io.Serializable;
import java.util.Hashtable;

public class UserDatabase implements Serializable {
	private static final long serialVersionUID = -7190639297114945144L;
	private Hashtable<String, User> users = null;
	private Database d;
	
	public UserDatabase(Database d) {
		this.d = d;
		users = new Hashtable<String, User>();
	}
	
	public void addUser(String user, User info) {
		users.put(user.toLowerCase(), info);
		d.save();
	}
	
	public User getUser(String user) {
		return users.get(user.toLowerCase());
	}
}
