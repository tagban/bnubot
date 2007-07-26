/**
 * This file is distributed under the GPL 
 * $Id: Version.java 433 2007-07-26 15:13:53Z scotta $
 */

package bnubot.bot.gui.components;

import java.awt.FlowLayout;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import bnubot.bot.gui.ColorScheme.ColorScheme;
import bnubot.core.clan.ClanMember;

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
	
	public void showMembers(ClanMember[] members) {
		clear();
		//BNetIcon icons[] = IconsDotBniReader.getIcons();
		
		for(int i = 0; i < members.length; i++) {
			ClanMember member = members[i];
			ClanMemberInfo cmi = new ClanMemberInfo();
			cmi.entryNumber = i;
			
			cmi.label = new JLabel(member.getUsername());
			cmi.label.setForeground(cs.getUserNameListColor(0));
			b.add(cmi.label);
			
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
		//TODO: implement
		System.out.println("TODO: Implement ClanList.remove(" + username + ")");
	}
	
	public void statusChange(ClanMember member) {
		//TODO: implement
		System.out.println("TODO: Implement ClanList.statusChange(" + member + ")");
	}
	
	public void rankChange(byte oldRank, byte newRank, String user) {
		//TODO: implement
		System.out.println("TODO: Implement ClanList.rankChange(" + oldRank + "," + newRank + "," + user + ")");
	}

}
