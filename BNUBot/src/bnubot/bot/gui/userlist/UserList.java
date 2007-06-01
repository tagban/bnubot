package bnubot.bot.gui.userlist;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.*;

@SuppressWarnings("serial")
public class UserList extends JPanel {
	private class UserInfo {
		int flags;
		int priority;
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

	private static final int PRIORITY_BLIZZARD_REP = 5;
	private static final int PRIORITY_BNET_REP = 4;
	private static final int PRIORITY_OPERATOR= 3;
	private static final int PRIORITY_SPEAKER = 2;
	private static final int PRIORITY_BIZZARD_GUEST = 1;
	private static final int PRIORITY_NORMAL = 0;
	private int getPrioByFlags(int flags) {
		if((flags & 0x01) != 0)	return PRIORITY_BLIZZARD_REP;
		if((flags & 0x08) != 0)	return PRIORITY_BNET_REP;
		if((flags & 0x02) != 0)	return PRIORITY_OPERATOR;
		if((flags & 0x04) != 0)	return PRIORITY_SPEAKER;
		if((flags & 0x40) != 0)	return PRIORITY_BIZZARD_GUEST;
		return PRIORITY_NORMAL;
	}
	
	private int getInsertPosition(int priority) {
		for(int i = 0; i < b.getComponentCount(); i++) {
			JLabel lbl = (JLabel)b.getComponent(i);
			UserInfo ui = users.get(lbl.getText());
			int pCurrent = getPrioByFlags(ui.flags);
			
			if(priority > pCurrent)
				return i;
		}
		return b.getComponentCount();
	}
	
	public void showUser(String user, int flags, int ping, String statstr) {
		UserInfo ui = users.get(user);
		if(ui == null) {
			ui = new UserInfo();
			ui.flags = flags;
			ui.ping = ping;
			ui.statstr = statstr;
			ui.priority = getPrioByFlags(flags);
		}
		if(ui.label == null) {
			ui.label = new JLabel(user);
			ui.label.setForeground(Color.LIGHT_GRAY);
			b.add(ui.label, getInsertPosition(ui.priority));
		}
		
		//Check if the user's flags updated
		if(ui.flags != flags) {
			//They did; order the list appropriately

			int newPriority = getPrioByFlags(flags);
			if(ui.priority != newPriority) {
				ui.priority = newPriority;
				b.remove(ui.label);
				b.add(ui.label, getInsertPosition(newPriority));
			}
			
			ui.flags = flags;
		}
		ui.ping = ping;
		ui.statstr = statstr;
				
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
