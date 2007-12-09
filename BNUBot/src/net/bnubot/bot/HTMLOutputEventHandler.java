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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.SwingUtilities;

import net.bnubot.bot.gui.colors.ColorScheme;
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
	private String channel = null;
	private boolean generationNeeded = false;
	private Runnable writeUserListRunnable = null;
	private ColorScheme cs = null;
	
	private List<BNetUser> users = null;

	public void bnetConnected(Connection source) {
		File f = new File("html");
		f.mkdir();
	}
	
	public void bnetDisconnected(Connection source) {
		writeUserList();
	}

	public void titleChanged(Connection source) {}

	public void channelJoin(Connection source, BNetUser user) {
		writeUserList();
		
		append(source,
			user + " has joined the channel" + user.getStatString().toString() + ".",
			cs.getChannelColor());
	}
	
	public void channelLeave(Connection source, BNetUser user) {
		writeUserList();
		
		append(source,
			user + " has left the channel.",
			cs.getChannelColor());
	}
	
	public void channelUser(Connection source, BNetUser user) {
		writeUserList();
		
		append(source,
			user + user.getStatString().toString() + ".",
			cs.getChannelColor());
	}
	
	public void initialize(Connection c) {
		users = c.getUsers();
		
		File f = new File("Logs");
		if(!f.exists())
			f.mkdir();
		if(!f.isDirectory()) {
			Out.fatalException(new Exception("Logs is not a directory!"));
		}
		
		cs = ColorScheme.createColorScheme(GlobalSettings.colorScheme);
	}
	
	public void joinedChannel(Connection source, String channel) {
		this.channel = channel;
		
		append(source,
			"Joining channel " + channel + ".", cs.getChannelColor());
	}
	public void recieveError(Connection source, String text) {}
	public void recieveInfo(Connection source, String text) {}
	public void recieveDebug(Connection source, String text) {}
	
	private String getIcon(int product, int icon, int flags) {
		if((flags & 0x01) != 0)	return "blizrep";
		if((flags & 0x08) != 0)	return "bnetrep";
		if((flags & 0x02) != 0)	return "op";
		if((flags & 0x04) != 0)	return "spkr";
		if((flags & 0x40) != 0)	return "blizguest";
		
		if((flags & 0x20) != 0)	return "squelch";
		
		if(icon != 0)
			return HexDump.DWordToPretty(icon);
		return HexDump.DWordToPretty(product);
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

						for(BNetUser ui : users) {
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

	public void friendsList(Connection source, FriendEntry[] entries) {}
	public void friendsUpdate(Connection source, FriendEntry friend) {}
	public void friendsAdd(Connection source, FriendEntry friend) {}
	public void friendsPosition(Connection source, byte oldPosition, byte newPosition) {}
	public void friendsRemove(Connection source, byte entry) {}
	
	public void logonRealmEx(Connection source, int[] MCPChunk1, int ip, int port, int[] MCPChunk2, String uniqueName) {}
	public void queryRealms2(Connection source, String[] realms) {}

	public boolean parseCommand(Connection source, BNetUser user, String command, String param, boolean wasWhispered) {return false;}

	public void clanMOTD(Connection source, Object cookie, String text) {}
	public void clanMemberList(Connection source, ClanMember[] members) {}
	public void clanMemberRemoved(Connection source, String username) {}
	public void clanMemberStatusChange(Connection source, ClanMember member) {}
	public void clanMemberRankChange(Connection source, byte oldRank, byte newRank, String user) {}

	private void logChat(Connection source, String text) {
		String fName = "Logs/" + source.getProfile().getName() + "/";
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
		return getColor(cs.getForegroundColor()) + String.format("[%1$tH:%1$tM:%1$tS] ", new GregorianCalendar());
	}
	
	private String getColor(Color col) {
		String out = "000000";
		out += Integer.toHexString(col.getRGB());
		out = out.substring(out.length() - 6);
		out = "[COLOR:#" + out + "]";
		return out;
	}
	
	private void append(Connection source, String text, Color col) {
		String out = "\n";
		out += getDate();
		out += getColor(col);
		out += text;
		logChat(source, out);
	}
	
	private void append2(Connection source, String text, Color col, String text2, Color col2) {
		String out = "\n";
		out += getDate();
		out += getColor(col);
		out += text;
		if(!col.equals(col2))
			out += getColor(col2);
		out += text2;
		logChat(source, out);
	}
	
	public void recieveChat(Connection source, BNetUser user, String text) {
		append2(source,
			"<" + user + "> ",
			cs.getUserNameColor(user.getFlags()),
			text,
			cs.getChatColor(user.getFlags()));
	}

	public void recieveEmote(Connection source, BNetUser user, String text) {
		append(source,
			"<" + user + " " + text + "> ", cs.getEmoteColor(user.getFlags()));
	}

	public void whisperSent(Connection source, BNetUser user, String text) {
		append2(source,
			"<To: " + user + "> ",
			cs.getUserNameColor(user.getFlags()),
			text,
			cs.getWhisperColor(user.getFlags()));
	}

	public void whisperRecieved(Connection source, BNetUser user, String text) {
		append2(source,
			"<From: " + user + "> ",
			cs.getUserNameColor(user.getFlags()),
			text,
			cs.getWhisperColor(user.getFlags()));
	}
}
