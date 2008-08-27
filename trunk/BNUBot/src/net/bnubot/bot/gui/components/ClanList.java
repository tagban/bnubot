/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui.components;

import java.awt.BorderLayout;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.bnubot.bot.gui.colors.ColorScheme;
import net.bnubot.bot.gui.icons.BNetIcon;
import net.bnubot.bot.gui.icons.IconsDotBniReader;
import net.bnubot.core.clan.ClanMember;
import net.bnubot.util.Out;

/**
 * @author scotta
 */
public class ClanList extends JPanel {
	private static final long serialVersionUID = 1138323605493806160L;

	private class ClanMemberInfo {
		int entryNumber;
		ClanMember entry;
		JLabel label;
	}

	private Hashtable<String, ClanMemberInfo> members = null;
	private Box b = null;
	private final ColorScheme cs = ColorScheme.getColors();

	private ClanMemberInfo get(String username) {
		ClanMemberInfo cmi = members.get(username);
		if(cmi != null)
			return cmi;

		Enumeration<ClanMemberInfo> en = members.elements();
		while(en.hasMoreElements()) {
			cmi = en.nextElement();
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
		Enumeration<ClanMemberInfo> en = members.elements();
		while(en.hasMoreElements()) {
			ClanMemberInfo cmi = en.nextElement();
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
		this.members = new Hashtable<String, ClanMemberInfo>();
		setBackground(cs.getBackgroundColor());
		b = new Box(BoxLayout.Y_AXIS);
		add(b, BorderLayout.NORTH);
	}

	public void clear() {
		Enumeration<ClanMemberInfo> e = members.elements();
		while(e.hasMoreElements()) {
			ClanMemberInfo cmi = e.nextElement();
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
		//BNetIcon icons[] = IconsDotBniReader.getIcons();

		for(int i = 0; i < members.length; i++) {
			ClanMember member = members[i];
			ClanMemberInfo cmi = new ClanMemberInfo();
			cmi.entryNumber = i;
			cmi.entry = member;

			cmi.label = new JLabel(member.toString());
			cmi.label.setForeground(cs.getUserNameListColor(0, false));
			setIcon(cmi);
			b.add(cmi.label, getInsertPosition(member.getRank()));

			/*if(member.getProduct() != 0) {
				for(int j = 0; j < icons.length; j++) {
					if(icons[j].useFor(0, member.getProduct())) {
						cmi.label.setIcon(icons[j].getIcon());
					}
				}
			}*/

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
		if(cmi == null)
			return;
		cmi.label.setText(member.toString());
		cmi.entry = member;
		setIcon(cmi);
		validate();
	}

	public void rankChange(byte oldRank, byte newRank, String user) {
		ClanMemberInfo cmi = get(user);
		if(cmi == null)
			return;
		cmi.entry.setRank(newRank);
		cmi.label.setText(cmi.entry.toString());
		setIcon(cmi);
		b.remove(cmi.label);
		b.add(cmi.label, getInsertPosition(newRank));
	}

}
