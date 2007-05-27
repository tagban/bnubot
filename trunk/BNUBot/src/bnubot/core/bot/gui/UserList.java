package bnubot.core.bot.gui;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.*;

import bnubot.core.bot.gui.icons.BNetIcon;


@SuppressWarnings("serial")
public class UserList extends Box {
	private class UserInfo {
		int flags;
		int ping;
		String statstr;
		JLabel label;
	}
	
	BNetIcon[] icons = null;
	Hashtable<String, UserInfo> users = null;
	
	public UserList(BNetIcon[] icons) {
		//super(new FlowLayout(FlowLayout.LEFT));
		super(BoxLayout.Y_AXIS);
		this.icons = icons;
		this.users = new Hashtable<String, UserInfo>();
	}
	
	public void clear() {
		Enumeration<UserInfo> e = users.elements();
		while(e.hasMoreElements()) {
			UserInfo ui = e.nextElement();
			remove(ui.label);
			ui.label = null;
		}
		users.clear();
		this.validate();
	}
	
	public void showUser(String user, int flags, int ping, String statstr) {
		UserInfo ui = users.get(user);
		if(ui == null) {
			ui = new UserInfo();
			ui.flags = flags;
			ui.ping = ping;
			ui.statstr = statstr;
		}
		if(ui.label == null) {
			ui.label = new JLabel();
			this.add(ui.label);
		}
		
		Icon icon = null;
		String product = null;
		if(statstr.length() >= 4)
			product = statstr.substring(0, 4);
		for(int i = 0; i < icons.length; i++) {
			if(icons[i].useFor(flags, product)) {
				icon = icons[i].getIcon();
				break;
			}
		}
		
		ui.label.setText(user);
		if(icon != null)
			ui.label.setIcon(icon);
		
		users.put(user, ui);
		this.validate();
	}
	
	public void removeUser(String user) {
		UserInfo ui = users.get(user);
		this.remove(ui.label);
		ui.label = null;
		users.remove(user);
		this.validate();
	}
}
