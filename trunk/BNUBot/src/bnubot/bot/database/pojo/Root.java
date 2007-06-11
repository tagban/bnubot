package bnubot.bot.database.pojo;

import java.util.Set;

public class Root {
	private Long id;
	private Set<Account> accounts;

	public Root() {
		this.id = new Long(1);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public Set<Account> getAccounts() {
		return accounts;
	}

	public void setAccounts(Set<Account> accounts) {
		this.accounts = accounts;
	}
	
	public String toString() {
		String out = "Root[id=" + id;
		Object[] acts = accounts.toArray();
		for(int i = 0; i < acts.length; i++) {
			out += "\n\t[" + i + "] - " + acts[i].toString();
		}
		return out;
	}
}
