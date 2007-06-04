package bnubot.bot.database;

import java.io.Serializable;

public class User implements Serializable {
	private static final long serialVersionUID = -8494495858058893950L;
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
