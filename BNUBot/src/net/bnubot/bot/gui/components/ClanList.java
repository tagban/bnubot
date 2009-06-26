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
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.bnubot.bot.gui.colors.ColorScheme;
import net.bnubot.bot.gui.icons.BNetIcon;
import net.bnubot.bot.gui.icons.IconsDotBniReader;
import net.bnubot.core.clan.ClanMember;
import net.bnubot.logging.Out;

/**
 * @author scotta
 */
public class ClanList extends JPanel {
	private static final long serialVersionUID = 1138323605493806160L;

	private class ClanMemberInfo {
		ClanMember entry;
		JLabel label;
	}

	private Map<String, ClanMemberInfo> members = null;
	private Box b = null;
	private final ColorScheme cs = ColorScheme.getColors();

	private ClanMemberInfo get(String username) {
		ClanMemberInfo cmi = members.get(username);
		if(cmi != null)
			return cmi;

		for(Entry<String, ClanMemberInfo> en : members.entrySet()) {
			cmi = en.getValue();
			if(cmi.entry.equals(username))
				return cmi;
		}

		return null;
	}

	private ClanMemberInfo get(ClanMember member) {
		return get(member.getUsername());
	}

	/**
	 * Get ClanMemberInfo from JLabel
	 * @param lbl The JLabel to look for
	 * @return The ClanMemberInfo, or null if not found
	 */
	private ClanMemberInfo get(JLabel lbl) {
		for(Entry<String, ClanMemberInfo> en : members.entrySet()) {
			ClanMemberInfo cmi = en.getValue();
			if(cmi.label == lbl)
				return cmi;
		}
		return null;
	}

	private int getInsertPosition(int priority) {
		for(int i = 0; i < b.getComponentCount(); i++) {
			JLabel lbl = (JLabel)b.getComponent(i);
			ClanMemberInfo cmi = get(lbl);
			int pCurrent = cmi.entry.getRank();

			if(priority > pCurrent)
				return i;
		}
		return b.getComponentCount();
	}

	public ClanList() {
		super(new BorderLayout());
		this.members = new HashMap<String, ClanMemberInfo>();
		setBackground(cs.getBackgroundColor());
		b = new Box(BoxLayout.Y_AXIS);
		add(b, BorderLayout.NORTH);
	}

	public void clear() {
		for(Entry<String, ClanMemberInfo> en : members.entrySet()) {
			ClanMemberInfo cmi = en.getValue();
			b.remove(cmi.label);
			cmi.label = null;
		}
		members.clear();
		validate();
	}

	private void setIcon(ClanMemberInfo cmi) {
		try {
			BNetIcon icons[] = IconsDotBniReader.getIconsClan();
			Icon icon = icons[cmi.entry.getRank()].getIcon();
			if(icon != null)
				cmi.label.setIcon(icon);
		} catch(Exception e) {
			Out.exception(e);
		}
	}

	public void showMembers(ClanMember[] members) {
		clear();

		for(ClanMember member : members) {
			ClanMemberInfo cmi = new ClanMemberInfo();
			cmi.entry = member;

			cmi.label = new JLabel(member.toString());
			cmi.label.setForeground(cs.getUserNameListColor(0, false));
			setIcon(cmi);
			b.add(cmi.label, getInsertPosition(member.getRank()));

			this.members.put(member.getUsername(), cmi);
		}
	}

	public void remove(String username) {
		ClanMemberInfo cmi = members.remove(username);
		if(cmi == null)
			cmi = get(username);

		if(cmi == null) {
			Out.error(getClass(), "Attempted to remove a clan member that was not in the ClanList: " + username);
			return;
		}

		b.remove(cmi.label);
		cmi.label = null;
		validate();
	}

	public void statusChange(ClanMember member) {
		ClanMemberInfo cmi = get(member);
		if(cmi == null) {
			Out.error(getClass(), "Attempted to change status of a clan member that was not in the ClanList: " + member.getUsername());
			return;
		}
		cmi.label.setText(member.toString());
		cmi.entry = member;
		setIcon(cmi);
		validate();
	}

	public void rankChange(byte oldRank, byte newRank, String user) {
		ClanMemberInfo cmi = get(user);
		if(cmi == null) {
			Out.error(getClass(), "Attempted to change rank of a clan member that was not in the ClanList: " + user);
			return;
		}
		cmi.entry.setRank(newRank);
		cmi.label.setText(cmi.entry.toString());
		setIcon(cmi);
		b.remove(cmi.label);
		b.add(cmi.label, getInsertPosition(newRank));
	}

}
