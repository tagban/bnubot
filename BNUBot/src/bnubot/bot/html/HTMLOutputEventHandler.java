package bnubot.bot.html;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import bnubot.bot.EventHandler;
import bnubot.core.BNetUser;
import bnubot.core.Connection;
import bnubot.core.StatString;
import bnubot.core.clan.ClanMember;
import bnubot.core.friend.FriendEntry;
import bnubot.util.HexDump;

public class HTMLOutputEventHandler implements EventHandler {
	private class UserInfo {
		BNetUser user;
		int flags;
		int ping;
		StatString statstr;
	}
	String channel;
	boolean generationNeeded;
	Runnable writeUserListRunnable = null;
	
	ArrayList<UserInfo> users;
	
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

	public void channelJoin(BNetUser user, int flags, int ping, StatString statstr) {
		UserInfo ui = new UserInfo();
		users.add(ui);
		ui.user = user;
		ui.flags = flags;
		ui.ping = ping;
		ui.statstr = statstr;
		
		writeUserList();
	}
	
	public void channelLeave(BNetUser user, int flags, int ping, StatString statstr) {
		if(!users.remove(get(user)))
			System.err.println("Tried to remove a user that was not in the list: " + user.toString());
		
		writeUserList();
	}
	
	public void channelUser(BNetUser user, int flags, int ping, StatString statstr) {
		UserInfo ui = get(user);
		if(ui == null) {
			ui = new UserInfo();
			users.add(ui);
		}
		ui.user = user;
		ui.flags = flags;
		ui.ping = ping;
		ui.statstr = statstr;
		
		writeUserList();
	}
	
	public void initialize(Connection c) {}
	public void joinedChannel(String channel) {
		this.channel = channel;
		users.clear();
	}
	public void recieveChat(BNetUser user, int flags, int ping, String text) {}
	public void recieveEmote(BNetUser user, int flags, int ping, String text) {}
	public void recieveError(String text) {}
	public void recieveInfo(String text) {}
	public void whisperRecieved(BNetUser user, int flags, int ping, String text) {}
	public void whisperSent(BNetUser user, int flags, int ping, String text) {}
	
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
							String product = getIcon(ui.statstr.getProduct(), ui.statstr.getIcon(), ui.flags);
							
							out += "<tr>";
							out += "<td><img src=\"images/" + product + ".jpg\"></td>";
							out += "<td>" + ui.user + "</td>";
							out += "<td>" + ui.ping + "ms</td>";
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
	public void friendsUpdate(byte entry, byte location, byte status, int product, String locationName) {}
	public void clanMemberRankChange(byte oldRank, byte newRank, String user) {}
	public void clanMemberList(ClanMember[] members) {}
	public void clanMOTD(Object cookie, String text) {}
	public void logonRealmEx(int[] MCPChunk1, int ip, int port, int[] MCPChunk2, String uniqueName) {}
	public void queryRealms2(String[] realms) {}
}
