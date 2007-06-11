package bnubot.bot.database.pojo;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class User implements Serializable {
	private Long id;
	private String login;
	private Account account;
	private String created;
	private String lastSeen;

	/** full constructor */
	public User(String login, Account account) {
		this.id = null;
		this.login = login.toLowerCase();
		this.account = account;
		this.created = new Date().toString();
		this.lastSeen = new Date().toString();
	}

	/** default constructor */
	public User() {
		id = null;
		login = null;
		account = null;
		created = new Date().toString();
		lastSeen = new Date().toString();
	}

	public String toString() {
		return "User[id=" + id + ",login=\"" + login + "\",created=\"" + created + "\",lastSeen=\"" + lastSeen + "\"]"; 
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
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

	public String getLastSeen() {
		return lastSeen;
	}

	public void setLastSeen(String lastSeen) {
		this.lastSeen = lastSeen;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}
}
