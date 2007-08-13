/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.core;

import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import net.bnubot.bot.EventHandler;
import net.bnubot.core.bnftp.BNFTPConnection;
import net.bnubot.core.clan.ClanMember;
import net.bnubot.core.friend.FriendEntry;
import net.bnubot.core.queue.ChatQueue;
import net.bnubot.util.Out;
import net.bnubot.util.TimeFormatter;
import net.bnubot.vercheck.CurrentVersion;
import net.bnubot.vercheck.VersionNumber;

public abstract class Connection extends Thread implements EventHandler {
	protected ConnectionSettings cs;
	protected ChatQueue cq;
	protected LinkedList<EventHandler> eventHandlers = new LinkedList<EventHandler>();
	protected LinkedList<EventHandler> eventHandlers2 = new LinkedList<EventHandler>();
	protected BNetUser myUser = null;
	protected boolean connected = false;
	protected LinkedList<Connection> slaves = new LinkedList<Connection>();
	protected String channelName = null;
	protected long lastAntiIdle;
	
	public static final int MAX_CHAT_LENGTH = 242;

	private int eh_semaphor = 0;
	private int eh2_semaphor = 0;
	
	private void waitForEHSemaphor() {
		while(eh_semaphor > 0) {
			try {Thread.sleep(10);} catch (InterruptedException e) {e.printStackTrace();}
			Thread.yield();
		}
	}
	
	private void waitForEH2Semaphor() {
		while(eh2_semaphor > 0) {
			try {Thread.sleep(10);} catch (InterruptedException e) {e.printStackTrace();}
			Thread.yield();
		}
	}
	
	public Connection(ConnectionSettings cs, ChatQueue cq) {
		this.cs = cs;
		this.cq = cq;
		
		if(cq != null)
			cq.add(this);
	}

	public abstract void joinChannel(String channel) throws Exception;
	public abstract void sendQueryRealms() throws Exception;
	public abstract void sendLogonRealmEx(String realmTitle) throws Exception;
	public abstract void sendClanRankChange(String string, int newRank) throws Exception;
	public abstract void sendClanMOTD(Object cookie) throws Exception;
	public abstract void sendClanSetMOTD(String text) throws Exception;
	public abstract void sendProfile(String user) throws Exception;
	
	public void sendProfile(BNetUser user) throws Exception {
		sendProfile(user.getFullAccountName());
	}
	
	public abstract void reconnect();
	public abstract boolean isOp();
	public abstract int getProductID();
	
	public void addSlave(Connection c) {
		slaves.add(c);
	}
	
	public void addEventHandler(EventHandler e) {
		waitForEHSemaphor();
		
		eventHandlers.add(e);
		e.initialize(this);
	}
	
	public void addSecondaryEventHandler(EventHandler e) {
		waitForEH2Semaphor();
		
		eventHandlers2.add(e);
	}
	
	public void removeEventHandler(EventHandler e) {
		waitForEHSemaphor();
		
		eventHandlers.remove(e);
	}

	public boolean isConnected() {
		return connected;
	}
	
	public void setConnected(boolean c) {
		if(c) {
			String v = cs.isValid();
			if(v != null) {
				recieveError(v);
				return;
			}
		}
		
		connected = c;
		
		if(c)
			bnetConnected();
		else
			bnetDisconnected();
	}
	
	protected long lastChatTime = 0;
	protected int lastChatLen = 0;
	private int increaseDelay(int bytes) {
		long thisTime = new Date().getTime();
		
		if(lastChatTime == 0) {
			lastChatTime = thisTime;
			lastChatLen = bytes;
			return 0;
		}
		
		int delay = checkDelay();
		lastChatTime = thisTime;
		lastChatLen = bytes;
		return delay;
	}

	//I found that sending long strings results in flood at values as high as 20; 25 seems safe
	private static final int BYTE_WEIGHT = 25; //15;
	private static final int PACKET_WEIGHT = 2700;
	private int checkDelay() {
		if(lastChatTime == 0)
			return 0;

		long thisTime = new Date().getTime();
		int delay = (lastChatLen * BYTE_WEIGHT) + PACKET_WEIGHT;
		delay -= (thisTime - lastChatTime);
		if(delay < 0)
			return 0;
		return delay;
	}

	public boolean canSendChat() {
		if(channelName == null)
			return false;
		
		if(myUser == null)
			return false;
		
		return (checkDelay() <= 0);
	}
	
	public void sendChatNow(String text) {
		lastAntiIdle = new Date().getTime();
		
		if(canSendChat())
			increaseDelay(text.length());
		else
			// Fake the bot in to thinking we sent chat in the future
			lastChatTime = new Date().getTime() + increaseDelay(text.length());
	}
	
	public String cleanText(String text) {
		//Remove all chars under 0x20
		byte[] data = text.getBytes();
		text = "";
		for(int i = 0; i < data.length; i++) {
			if(data[i] >= 0x20)
				text += (char)data[i];
		}
		
		boolean somethingDone = true;
		while(somethingDone) {
			somethingDone = false;
			
			if(text.indexOf('%') == -1)
				break;
			
			int i = text.indexOf("%uptime%");
			if(i != -1) {
				somethingDone = true;
				
				String first = text.substring(0, i);
				String last = text.substring(i + 8);
				
				long uptime = java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();
				text = first + TimeFormatter.formatTime(uptime) + last;
			}
			
			i = text.indexOf("%trigger%");
			if(i != -1) {
				somethingDone = true;
				
				String first = text.substring(0, i);
				String last = text.substring(i + 9);
				text = first + cs.trigger + last;
			}
			
			i = text.indexOf("%version%");
			if(i != -1) {
				somethingDone = true;
				
				String first = text.substring(0, i);
				String last = text.substring(i + 9);
				text = first + CurrentVersion.version() + last;
			}
		}
		return text;
	}
	
	public void sendChat(String text) {
		if(text == null)
			return;
		if(text.length() == 0)
			return;
		if(!isConnected())
			return;
		
		if(text.charAt(0) == '/') {
			String[] command = text.substring(1).split(" ", 3);
			switch(command[0].charAt(0)) {
			case 'c':
				if(command[0].equals("cmd")) {
					if(command.length == 1)
						break;
					
					String params = null;
					if(command.length > 2)
						params = command[2];

					Iterator<EventHandler> it = eventHandlers.iterator();
					while(it.hasNext())
						it.next().parseCommand(myUser, command[1], params, true);
					
					return;
				}
				break;
			case 'p':
				if(command[0].equals("profile")) {
					try {
						if((command.length < 2) || (command[1].length() == 0))
							sendProfile(myUser);
						else
							sendProfile(command[1]);
					} catch(Exception e) {
						e.printStackTrace();
					}
					return;
				}
				break;
			}
		}
		
		text = cleanText(text);
		
		if(text.length() == 0)
			return;

		if(canSendChat() && !cs.enableFloodProtect) {
			sendChatNow(text);
		} else {
			cq.enqueue(text, cs.enableFloodProtect);
		}
	}
	
	public void sendChat(BNetUser to, String text, boolean forceWhisper) {
		if(text == null)
			return;

		text = cleanText(text);
		
		if(myUser.equals(to))
			recieveInfo(text);
		else {
			String prefix;
			if(cs.whisperBack || forceWhisper) {
				prefix = "/w " + to.getFullLogonName() + " [BNU";
				VersionNumber cv = CurrentVersion.version();
				if(cv.isAlpha())
					prefix += " alpha";
				else if(cv.isBeta())
					prefix += " beta";
				else if(cv.isReleaseCandidate())
					prefix += " RC";
				prefix += "] ";
			} else
				prefix = to.getShortPrettyName() + ": ";
			
			//Split up the text in to appropriate sized pieces
			int pieceSize = MAX_CHAT_LENGTH - prefix.length();
			for(int i = 0; i < text.length(); i += pieceSize) {
				String piece = prefix + text.substring(i);
				if(piece.length() > MAX_CHAT_LENGTH)
					piece = piece.substring(0, MAX_CHAT_LENGTH);
				sendChat(piece);
			}
		}
	}
	
	public File downloadFile(String filename) {
		return BNFTPConnection.downloadFile(cs, filename);
	}
	
	public ConnectionSettings getConnectionSettings() {
		return cs;
	}

	public BNetUser getMyUser() {
		return myUser;
	}
	
	/*
	 * EventHandler methods follow
	 * 
	 */

	public void initialize(Connection c) {
		new UnsupportedOperationException();
	}
	
	public synchronized void bnetConnected() {
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().bnetConnected();
	}
	
	public synchronized void bnetDisconnected() {
		channelName = null;
		myUser = null;
		
		eh_semaphor++;
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().bnetDisconnected();
		eh_semaphor--;
	}

	public void parseCommand(BNetUser user, String command, String param, boolean wasWhispered) {
		eh_semaphor++;
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().parseCommand(user, command, param, wasWhispered);
		eh_semaphor--;
	}

	public synchronized void titleChanged() {
		eh_semaphor++;
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().titleChanged();
		eh_semaphor--;
	}
	
	public synchronized void joinedChannel(String channel) {
		channelName = channel;

		eh_semaphor++;
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().joinedChannel(channel);
		eh_semaphor--;

		eh2_semaphor++;
		Iterator<Connection> it2 = slaves.iterator();
		while(it2.hasNext())
			try {
				Connection c = it2.next();
				Out.debug(Connection.class, "Telling [" + c.toString() + "] to join " + channel);
				c.joinChannel(channel);
			} catch (Exception e) {
				e.printStackTrace();
			}
		eh2_semaphor--;
	}
	
	public synchronized void channelUser(BNetUser user, StatString statstr) {
		eh_semaphor++;
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().channelUser(user, statstr);
		eh_semaphor--;
	}
	
	public synchronized void channelJoin(BNetUser user, StatString statstr) {
		eh_semaphor++;
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().channelJoin(user, statstr);
		eh_semaphor--;
	}
	
	public synchronized void channelLeave(BNetUser user) {
		eh_semaphor++;
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().channelLeave(user);
		eh_semaphor--;
	}

	public synchronized void recieveChat(BNetUser user, String text) {
		eh_semaphor++;
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().recieveChat(user, text);
		eh_semaphor--;
	}

	public synchronized void recieveEmote(BNetUser user, String text) {
		eh_semaphor++;
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().recieveEmote(user, text);
		eh_semaphor--;
	}

	public synchronized void recieveInfo(String text) {
		if(text == null)
			return;
		if(text.length() == 0)
			return;

		eh_semaphor++;
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().recieveInfo(text);
		eh_semaphor--;

		eh2_semaphor++;
		it = eventHandlers2.iterator();
		while(it.hasNext())
			it.next().recieveInfo(text);
		eh2_semaphor--;
	}

	public synchronized void recieveError(String text) {
		if(text == null)
			return;
		if(text.length() == 0)
			return;

		eh_semaphor++;
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().recieveError(text);
		eh_semaphor--;

		eh2_semaphor++;
		it = eventHandlers2.iterator();
		while(it.hasNext())
			it.next().recieveError(text);
		eh2_semaphor--;
	}
	
	public synchronized void whisperSent(BNetUser user, String text) {
		eh_semaphor++;
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().whisperSent(user, text);
		eh_semaphor--;

		eh2_semaphor++;
		it = eventHandlers2.iterator();
		while(it.hasNext())
			it.next().whisperSent(user, text);
		eh2_semaphor--;
	}
	
	public synchronized void whisperRecieved(BNetUser user, String text) {
		eh_semaphor++;
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().whisperRecieved(user, text);
		eh_semaphor--;

		eh2_semaphor++;
		it = eventHandlers2.iterator();
		while(it.hasNext())
			it.next().whisperRecieved(user, text);
		eh2_semaphor--;
	}
	
	// Realms
	
	public synchronized void queryRealms2(String[] realms) {
		eh_semaphor++;
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().queryRealms2(realms);
		eh_semaphor--;
	}
	
	public synchronized void logonRealmEx(int[] MCPChunk1, int ip, int port, int[] MCPChunk2, String uniqueName) {
		eh_semaphor++;
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().logonRealmEx(MCPChunk1, ip, port, MCPChunk2, uniqueName);
		eh_semaphor--;
	}
	
	// Friends

	public synchronized void friendsList(FriendEntry[] entries) {
		eh_semaphor++;
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().friendsList(entries);
		eh_semaphor--;
	}
	
	public synchronized void friendsUpdate(FriendEntry friend) {
		eh_semaphor++;
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().friendsUpdate(friend);
		eh_semaphor--;
	}
	
	public synchronized void friendsAdd(FriendEntry friend) {
		eh_semaphor++;
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().friendsAdd(friend);
		eh_semaphor--;
	}
	
	public void friendsRemove(byte entry) {
		eh_semaphor++;
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().friendsRemove(entry);
		eh_semaphor--;
	}
	
	public void friendsPosition(byte oldPosition, byte newPosition) {
		eh_semaphor++;
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().friendsPosition(oldPosition, newPosition);
		eh_semaphor--;
	}
	
	// Clan
	
	public synchronized void clanMOTD(Object cookie, String text) {
		eh_semaphor++;
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().clanMOTD(cookie, text);
		eh_semaphor--;
	}

	public synchronized void clanMemberList(ClanMember[] members) {
		eh_semaphor++;
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().clanMemberList(members);
		eh_semaphor--;
	}
	
	public synchronized void clanMemberRemoved(String username) {
		eh_semaphor++;
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().clanMemberRemoved(username);
		eh_semaphor--;
	}
	
	public synchronized void clanMemberRankChange(byte oldRank, byte newRank, String user) {
		eh_semaphor++;
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().clanMemberRankChange(oldRank, newRank, user);
		eh_semaphor--;
	}
	
	public synchronized void clanMemberStatusChange(ClanMember member) {
		eh_semaphor++;
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().clanMemberStatusChange(member);
		eh_semaphor--;
	}
}
