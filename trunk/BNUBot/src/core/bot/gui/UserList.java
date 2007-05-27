package core.bot.gui;

import java.util.Hashtable;

import javax.swing.*;

import core.bot.gui.icons.BNetIcon;

@SuppressWarnings("serial")
public class UserList extends JPanel {
	private class UserInfo {
		int flags;
		int ping;
		String statstr;
		JLabel label;
	}
	
	BNetIcon[] icons = null;
	Hashtable<String, UserInfo> users = null;
	
	public UserList(BNetIcon[] icons) {
		this.icons = icons;
		this.users = new Hashtable<String, UserInfo>();
	}
	
	public void clear() {
		users.clear();
	}
	
	public void userShow(String user, int flags, int ping, String statstr) {
		
	}
	
	public void userJoin(String user, int flags, int ping, String statstr) {
		
	}
	
	public void userLeave(String user) {
		
	}
}
