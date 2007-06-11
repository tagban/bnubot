package bnubot.bot.database.pojo;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import bnubot.core.BNetUser;

public class Account {
	private Long id;
	private Root root;
	private Long access;
	private String name;
	private String created;
	private Set<User> users;
	
	public Account(Long access, String name) {
		this.id = null;
		this.access = access;
		this.name = name;
		this.created = new Date().toString();
		this.users = new HashSet<User>();
	}
	
	public Account() {
		this(null, null);
	}

	public Long getAccess() {
		return access;
	}

	public void setAccess(Long access) {
		this.access = access;
	}
	
	public Root getRoot() {
		return root;
	}
	
	public void setRoot(Root root) {
		this.root = root;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<User> getUsers() {
		return users;
	}

	public void setUsers(Set<User> users) {
		this.users = users;
	}
	
	public String toString() {
		String out = "Account[id=" + id + ",access=" + access + ",name=\"" + name + "\",created=\"" + created + "\"]";
		
		Object[] us = users.toArray();
		for(int i = 0; i < us.length; i++) {
			out += "\n\t\t[" + i + "] - " + us[i].toString();
		}
		
		return out;
	}
	
	public boolean belongsTo(BNetUser u) {
		Iterator<User> it = users.iterator();
		while(it.hasNext()) {
			if(u.equals(it.next().getLogin()))
				return true;
		}
		return false;
	}
}
