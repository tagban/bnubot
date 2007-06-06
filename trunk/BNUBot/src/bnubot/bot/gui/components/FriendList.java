package bnubot.bot.gui.components;

import java.awt.FlowLayout;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import bnubot.bot.gui.ColorScheme.ColorScheme;
import bnubot.bot.gui.icons.BNetIcon;
import bnubot.core.friend.FriendEntry;

@SuppressWarnings("serial")
public class FriendList extends JPanel {
	private class FriendInfo {
		int entryNumber;
		FriendEntry entry;
		JLabel label;
	}
	
	private BNetIcon[] icons = null;
	private Hashtable<String, FriendInfo> friends = null;
	private Box b = null;
	private ColorScheme cs = null;
	
	public FriendList(BNetIcon[] icons, ColorScheme cs) {
		super(new FlowLayout(FlowLayout.LEFT));
		this.icons = icons;
		this.friends = new Hashtable<String, FriendInfo>();
		this.cs = cs;
		setBackground(cs.getBackgroundColor());
		b = new Box(BoxLayout.Y_AXIS);
		add(b);
	}
	
	public void clear() {
		Enumeration<FriendInfo> e = friends.elements();
		while(e.hasMoreElements()) {
			FriendInfo fi = e.nextElement();
			b.remove(fi.label);
			fi.label = null;
		}
		friends.clear();
		validate();
	}
	
	public void showFriends(FriendEntry[] entries) {
		clear();
		
		for(int i = 0; i < entries.length; i++) {
			FriendEntry entry = entries[i];
			FriendInfo fi = new FriendInfo();
			fi.entryNumber = i;
			
			fi.label = new JLabel(entry.getAccount());
			fi.label.setForeground(cs.getUserNameListColor(0));
			b.add(fi.label);
			
			friends.put(entry.getAccount(), fi);
		}
	}
}
