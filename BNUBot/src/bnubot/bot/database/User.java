package bnubot.bot.database;

public class User {
	private int access;
	
	public User(int access) {
		this.access = access;
	}

	public int getAccess() {
		return access;
	}

	public void setAccess(int access) {
		this.access = access;
	}
}
