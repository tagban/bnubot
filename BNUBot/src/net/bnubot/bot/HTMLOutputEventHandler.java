/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.bot;

import java.awt.Color;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingUtilities;

import net.bnubot.bot.gui.ColorScheme.ColorScheme;
import net.bnubot.core.ChannelListPriority;
import net.bnubot.core.Connection;
import net.bnubot.core.EventHandler;
import net.bnubot.core.clan.ClanMember;
import net.bnubot.core.friend.FriendEntry;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetUser;
import net.bnubot.util.HexDump;
import net.bnubot.util.Out;
import net.bnubot.util.StatString;

public class HTMLOutputEventHandler implements EventHandler {
	private class SortOrderComparator implements Comparator<BNetUser> {
		public int compare(BNetUser arg0, BNetUser arg1) {
			int prio0 = ChannelListPriority.getPrioByFlags(arg0.getFlags());
			int prio1 = ChannelListPriority.getPrioByFlags(arg0.getFlags());
			return new Integer(prio0).compareTo(prio1);
		}
	}
	
	private String channel = null;
	private boolean generationNeeded = false;
	private Runnable writeUserListRunnable = null;
	private ColorScheme cs = null;
	
	private List<BNetUser> users;
	
	private BNetUser get(BNetUser u) {
		for(BNetUser ui : users.toArray(new BNetUser[users.size()])) {
			if(u.equals(ui))
				return ui;
		}
		return null;
	}

	public void bnetConnected() {
		users = new LinkedList<BNetUser>();
		File f = new File("html");
		f.mkdir();
	}
	
	public void bnetDisconnected() {
		users.clear();
		writeUserList();
	}

	public void titleChanged() {}

	public void channelJoin(BNetUser user) {
		users.add(user);
		
		writeUserList();
		
		append(user + " has joined the channel" + user.getStatString().toString() + ".",
			cs.getChannelColor());
	}
	
	public void channelLeave(BNetUser user) {
		if(!users.remove(get(user)))
			Out.error(getClass(), "Tried to remove a user that was not in the list: " + user.toString());
		
		writeUserList();
		
		append(user + " has left the channel.",
				cs.getChannelColor());
	}
	
	public void channelUser(BNetUser user) {
		if(get(user) == null)
			users.add(user);
		
		writeUserList();
		
		append(user + user.getStatString().toString() + ".",
				cs.getChannelColor());
	}
	
	public void initialize(Connection c) {
		File f = new File("Logs");
		if(!f.exists())
			f.mkdir();
		if(!f.isDirectory()) {
			Out.fatalException(new Exception("Logs is not a directory!"));
		}
		
		cs = ColorScheme.createColorScheme(GlobalSettings.colorScheme);
	}
	
	public void joinedChannel(String channel) {
		this.channel = channel;
		users.clear();
		
		append("Joining channel " + channel + ".",
				cs.getChannelColor());
	}
	public void recieveError(String text) {}
	public void recieveInfo(String text) {}
	
	private String getIcon(int product, int icon, int flags) {
		if((flags & 0x01) != 0)	return "blizrep";
		if((flags & 0x08) != 0)	return "bnetrep";
		if((flags & 0x02) != 0)	return "op";
		if((flags & 0x04) != 0)	return "spkr";
		if((flags & 0x40) != 0)	return "blizguest";
		
		if((flags & 0x20) != 0)	return "squelch";
		
		return HexDump.DWordToPretty((icon == 0) ? icon : product);
	}
	
	private void writeUserList() {
		if(writeUserListRunnable == null)
			writeUserListRunnable = new Runnable() {
				public void run() {
					if(!generationNeeded)
						return;
					generationNeeded = false;
					
					try {
						File f = new File("html/userlist.html");
						DataOutputStream fos = new DataOutputStream(new FileOutputStream(f));
						
						fos.write("<table><tr><td colspan=\"4\"><b>".getBytes());
						if(channel != null)
							fos.write(channel.getBytes());
						fos.write("</b> (".getBytes());
						fos.write(Integer.toString(users.size()).getBytes());
						fos.write(")</td></tr>".getBytes());
						
						Collections.sort(users, new SortOrderComparator());

						for(BNetUser ui : users.toArray(new BNetUser[users.size()])) {
							StatString ss = ui.getStatString();
							String product = getIcon(ss.getProduct(), ss.getIcon(), ui.getFlags());
							
							fos.write("<tr>".getBytes());
							fos.write(("<td><img src=\"images/" + product + ".jpg\"></td>").getBytes());
							fos.write(("<td>" + ui.getFullLogonName() + "</td>").getBytes());
							fos.write(("<td>" + ui.getPing() + "ms</td>").getBytes());
							fos.write("</tr>".getBytes());
						}
			
						fos.close();
					} catch (Exception e) {
						Out.exception(e);
					}
				}
			};
		
		generationNeeded = true;
		SwingUtilities.invokeLater(writeUserListRunnable);
	}

	public void friendsList(FriendEntry[] entries) {}
	public void friendsUpdate(FriendEntry friend) {}
	public void friendsAdd(FriendEntry friend) {}
	public void friendsPosition(byte oldPosition, byte newPosition) {}
	public void friendsRemove(byte entry) {}
	
	public void logonRealmEx(int[] MCPChunk1, int ip, int port, int[] MCPChunk2, String uniqueName) {}
	public void queryRealms2(String[] realms) {}

	public void parseCommand(BNetUser user, String command, String param, boolean wasWhispered) {}

	public void clanMOTD(Object cookie, String text) {}
	public void clanMemberList(ClanMember[] members) {}
	public void clanMemberRemoved(String username) {}
	public void clanMemberStatusChange(ClanMember member) {}
	public void clanMemberRankChange(byte oldRank, byte newRank, String user) {}

	private void logChat(String text) {
		String fName = "Logs/";
		fName += new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		fName += ".log";
		File f = new File(fName);
		try {
			if(!f.exists())
				f.createNewFile();
			// Create a new FOS for appending
			FileOutputStream fos = new FileOutputStream(f, true);
			fos.write(text.getBytes());
			fos.close();
		} catch (Exception e) {
			Out.fatalException(e);
		}
	}
	
	private String getDate() {
		return getColor(cs.getTimeStampColor()) + String.format("[%1$tH:%1$tM:%1$tS] ", new GregorianCalendar());
	}
	
	private String getColor(Color col) {
		String out = "000000";
		out += Integer.toHexString(col.getRGB());
		out = out.substring(out.length() - 6);
		out = "[COLOR:#" + out + "]";
		return out;
	}
	
	private void append(String text, Color col) {
		String out = "\n";
		out += getDate();
		out += getColor(col);
		out += text;
		logChat(out);
	}
	
	private void append2(String text, Color col, String text2, Color col2) {
		String out = "\n";
		out += getDate();
		out += getColor(col);
		out += text;
		if(!col.equals(col2))
			out += getColor(col2);
		out += text2;
		logChat(out);
	}
	
	public void recieveChat(BNetUser user, String text) {
		append2(
				"<" + user + "> ",
				cs.getUserNameColor(user.getFlags()),
				text,
				cs.getChatColor(user.getFlags()));
	}
	
	public void recieveEmote(BNetUser user, String text) {
		append(
				"<" + user + " " + text + "> ",
				cs.getEmoteColor(user.getFlags()));
	}
	
	public void whisperSent(BNetUser user, String text) {
		append2(
			"<To: " + user + "> ",
			cs.getUserNameColor(user.getFlags()),
			text,
			cs.getWhisperColor(user.getFlags()));
	}
	
	public void whisperRecieved(BNetUser user, String text) {
		append2(
			"<From: " + user + "> ",
			cs.getUserNameColor(user.getFlags()),
			text,
			cs.getWhisperColor(user.getFlags()));
	}
}
