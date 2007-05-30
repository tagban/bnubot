package bnubot.bot.gui.userlist;

import java.awt.Color;
import java.awt.FlowLayout;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.*;

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
	Box b = null;
	
	public UserList(BNetIcon[] icons) {
		super(new FlowLayout(FlowLayout.LEFT));
		this.icons = icons;
		this.users = new Hashtable<String, UserInfo>();
		setBackground(Color.BLACK);
		b = new Box(BoxLayout.Y_AXIS);
		add(b);
	}
	
	public void clear() {
		Enumeration<UserInfo> e = users.elements();
		while(e.hasMoreElements()) {
			UserInfo ui = e.nextElement();
			b.remove(ui.label);
			ui.label = null;
		}
		users.clear();
		validate();
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
			ui.label.setForeground(Color.LIGHT_GRAY);
			b.add(ui.label);
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
		validate();
	}
	
	public void removeUser(String user) {
		UserInfo ui = users.get(user);
		if(ui != null) {
			b.remove(ui.label);
			ui.label = null;
			users.remove(user);
			validate();
		} else {
			System.err.println("Attempted to remove a user that was not in the UserList: " + user);
		}
	}
}
