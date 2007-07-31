/**
 * This file is distributed under the GPL 
 * $Id: Version.java 433 2007-07-26 15:13:53Z scotta $
 */

package net.bnubot.bot.gui.components;

import java.awt.FlowLayout;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.bnubot.bot.gui.ColorScheme.ColorScheme;
import net.bnubot.bot.gui.icons.BNetIcon;
import net.bnubot.bot.gui.icons.IconsDotBniReader;
import net.bnubot.core.clan.ClanMember;

@SuppressWarnings("serial")
public class ClanList extends JPanel {
	private class ClanMemberInfo {
		int entryNumber;
		ClanMember entry;
		JLabel label;
	}
	
	private Hashtable<String, ClanMemberInfo> members = null;
	private Box b = null;
	private ColorScheme cs = null;
	
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
	
	public ClanList(ColorScheme cs) {
		super(new FlowLayout(FlowLayout.LEFT));
		this.members = new Hashtable<String, ClanMemberInfo>();
		this.cs = cs;
		setBackground(cs.getBackgroundColor());
		b = new Box(BoxLayout.Y_AXIS);
		add(b);
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
		BNetIcon iconsWAR3[] = IconsDotBniReader.getIconsWAR3();
		BNetIcon iconsW3XP[] = IconsDotBniReader.getIconsW3XP();

		// 0 - Init - WAR3-W3D3 (2)
		// 1 - Peon - WAR3-W3O1 (10)
		// 2 - Grunt - WAR3-W302 (11)
		// 3 - Shaman - W3XP-W303 (20)
		// 4 - Chieftain - WAR3-W3R3 (17)

		Icon icon = null;
		switch(cmi.entry.getRank()) {
		case 0: icon = iconsWAR3[2].getIcon(); break;
		case 1: icon = iconsWAR3[10].getIcon(); break;
		case 2: icon = iconsWAR3[11].getIcon(); break;
		case 3: icon = iconsW3XP[20].getIcon(); break;
		case 4: icon = iconsWAR3[17].getIcon(); break;
		}
		
		if(icon != null)
			cmi.label.setIcon(icon);
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
			cmi.label.setForeground(cs.getUserNameListColor(0));
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
			System.err.println("Attempted to remove a clan member that was not in the ClanList: " + username);
			return;
		}
		
		b.remove(cmi.label);
		cmi.label = null;
		validate();
	}
	
	public void statusChange(ClanMember member) {
		ClanMemberInfo cmi = get(member);
		cmi.label.setText(member.toString());
		cmi.entry = member;
		setIcon(cmi);
		validate();
	}
	
	public void rankChange(byte oldRank, byte newRank, String user) {
		ClanMemberInfo cmi = get(user);
		cmi.entry.setRank(newRank);
		cmi.label.setText(cmi.entry.toString());
		setIcon(cmi);
		b.remove(cmi.label);
		b.add(cmi.label, getInsertPosition(newRank));
	}

}
