package bnubot.bot.html;

import java.awt.Color;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.SwingUtilities;

import bnubot.bot.EventHandler;
import bnubot.bot.gui.ColorScheme.ColorScheme;
import bnubot.core.BNetUser;
import bnubot.core.Connection;
import bnubot.core.StatString;
import bnubot.core.clan.ClanMember;
import bnubot.core.friend.FriendEntry;
import bnubot.util.HexDump;

public class HTMLOutputEventHandler implements EventHandler {
	private class UserInfo {
		BNetUser user;
		StatString statstr;
	}
	private String channel = null;
	private boolean generationNeeded = false;
	private Runnable writeUserListRunnable = null;
	private ColorScheme cs = null;
	
	private ArrayList<UserInfo> users;
	
	private UserInfo get(BNetUser u) {
		for(UserInfo ui : users.toArray(new UserInfo[users.size()])) {
			if(u.equals(ui.user))
				return ui;
		}
		return null;
	}

	public void bnetConnected() {
		users = new ArrayList<UserInfo>();
		File f = new File("html");
		f.mkdir();
	}
	
	public void bnetDisconnected() {
		users.clear();
		writeUserList();
	}

	public void titleChanged() {}

	public void channelJoin(BNetUser user, StatString statstr) {
		UserInfo ui = new UserInfo();
		users.add(ui);
		ui.user = user;
		ui.statstr = statstr;
		
		writeUserList();
	}
	
	public void channelLeave(BNetUser user) {
		if(!users.remove(get(user)))
			System.err.println("Tried to remove a user that was not in the list: " + user.toString());
		
		writeUserList();
	}
	
	public void channelUser(BNetUser user, StatString statstr) {
		UserInfo ui = get(user);
		if(ui == null) {
			ui = new UserInfo();
			users.add(ui);
		}
		ui.user = user;
		ui.statstr = statstr;
		
		writeUserList();
	}
	
	public void initialize(Connection c) {
		File f = new File("Logs");
		if(!f.exists())
			f.mkdir();
		if(!f.isDirectory()) {
			new Exception("Logs is not a directory!").printStackTrace();
			System.exit(1);
		}
		
		cs = ColorScheme.createColorScheme(c.getConnectionSettings().colorScheme);
	}
	
	public void joinedChannel(String channel) {
		this.channel = channel;
		users.clear();
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
					try {
						if(!generationNeeded)
							return;
						generationNeeded = false;
						
						File f = new File("html/userlist.html");
					
						String out = "<table><tr><td colspan=\"4\"><b>";
						out += channel;
						out += "</b> (";
						out += users.size();
						out += ")</td></tr>";

						for(UserInfo ui : users.toArray(new UserInfo[users.size()])) {
							String product = getIcon(ui.statstr.getProduct(), ui.statstr.getIcon(), ui.user.getFlags());
							
							out += "<tr>";
							out += "<td><img src=\"images/" + product + ".jpg\"></td>";
							out += "<td>" + ui.user + "</td>";
							out += "<td>" + ui.user.getPing() + "ms</td>";
							out += "</tr>";
						}
			
						DataOutputStream fos = new DataOutputStream(new FileOutputStream(f));
						fos.write(out.getBytes());
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
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
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private String getDate() {
		return String.format("[%1$tH:%1$tM:%1$tS] ", new GregorianCalendar());
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
