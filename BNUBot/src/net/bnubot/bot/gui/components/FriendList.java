/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui.components;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.bnubot.bot.gui.colors.ColorScheme;
import net.bnubot.bot.gui.icons.BNetIcon;
import net.bnubot.bot.gui.icons.IconsDotBniReader;
import net.bnubot.core.friend.FriendEntry;
import net.bnubot.logging.Out;

/**
 * @author scotta
 */
public class FriendList extends JPanel {
	private static final long serialVersionUID = 609660115699769279L;

	private class FriendInfo {
		FriendEntry entry;
		JLabel label;
	}

	private Map<String, FriendInfo> friends = null;
	private Box b = null;
	private final ColorScheme cs = ColorScheme.getColors();

	/**
	 * Get FriendInfo from JLabel
	 * @param lbl The JLabel to look for
	 * @return The FriendInfo, or null if not found
	 */
	private FriendInfo get(JLabel lbl) {
		for(Entry<String, FriendInfo> en : friends.entrySet()) {
			FriendInfo fi = en.getValue();
			if(fi.label == lbl)
				return fi;
		}
		return null;
	}

	public FriendList() {
		super(new BorderLayout());
		this.friends = new HashMap<String, FriendInfo>();
		setBackground(cs.getBackgroundColor());
		b = new Box(BoxLayout.Y_AXIS);
		add(b, BorderLayout.NORTH);
	}

	public void clear() {
		for(Entry<String, FriendInfo> en : friends.entrySet()) {
			FriendInfo fi = en.getValue();
			b.remove(fi.label);
			fi.label = null;
		}
		friends.clear();
		validate();
	}

	private void setIcon(FriendInfo fi) {
		BNetIcon icons[] = IconsDotBniReader.getIcons();
		if(icons == null)
			return;

		if(fi.entry.getProduct() != 0) {
			for(BNetIcon element : icons) {
				if(element.useFor(0, fi.entry.getProduct())) {
					fi.label.setIcon(element.getIcon());
				}
			}
		}
	}

	public void showFriends(FriendEntry[] entries) {
		clear();

		for(FriendEntry fe : entries)
			add(fe);
	}

	public void update(FriendEntry friend) {
		try {
			JLabel jl = (JLabel)b.getComponent(friend.getEntry().intValue());
			FriendInfo fi = get(jl);
			friend.setAccount(fi.entry.getAccount());
			fi.entry = friend;
			fi.label.setText(friend.toString());
			setIcon(fi);
			validate();
		} catch(Exception e) {
			Out.exception(e);
		}
	}

	public void add(FriendEntry friend) {
		FriendInfo fi = new FriendInfo();
		fi.entry = friend;

		fi.label = new JLabel(friend.toString());
		fi.label.setForeground(cs.getUserNameListColor(0, false));

		setIcon(fi);

		b.add(fi.label);
		validate();

		friends.put(friend.getAccount(), fi);
	}

	public void position(byte oldPosition, byte newPosition) {
		JLabel jl = (JLabel)b.getComponent(oldPosition);
		b.remove(jl);
		b.add(jl, newPosition);
		validate();
	}

	public void remove(byte entry) {
		JLabel jl = (JLabel)b.getComponent(entry);
		b.remove(jl);
		FriendInfo fi = get(jl);
		friends.remove(fi.entry.getAccount());
	}
}
