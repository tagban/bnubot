package bnubot.core;

import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import bnubot.bot.EventHandler;
import bnubot.core.bnftp.BNFTPConnection;
import bnubot.core.clan.ClanMember;
import bnubot.core.friend.FriendEntry;
import bnubot.core.queue.ChatQueue;

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
	
	public static final int MAX_CHAT_LENGTH = 250;
	
	
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
	
	public abstract void reconnect();
	public abstract boolean isOp();
	public abstract int getProductID();
	
	public void addSlave(Connection c) {
		slaves.add(c);
	}
	
	public void addEventHandler(EventHandler e) {
		eventHandlers.add(e);
		e.initialize(this);
	}
	
	public void addSecondaryEventHandler(EventHandler e) {
		eventHandlers2.add(e);
	}
	
	public void removeEventHandler(EventHandler ee) {
		eventHandlers.remove(ee);
	}

	public boolean isConnected() {
		return connected;
	}
	
	public void setConnected(boolean c) {
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
		
		if(text.indexOf('%') != -1) {
			int i = text.indexOf("%uptime%");
			if(i != -1) {
				String first = text.substring(0, i);
				String last = text.substring(i + 8);
				
				//long uptime = new Date().getTime();
				long uptime = java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();
				uptime /= 1000;
				text = Long.toString(uptime % 60) + "s";
				uptime /= 60;
				if(uptime > 0) {
					text = Long.toString(uptime % 60) + "m " + text;
					uptime /= 60;
					if(uptime > 0) {
						text = Long.toString(uptime % 24) + "h " + text;
						uptime /= 24;
						if(uptime > 0)
							text = Long.toString(uptime) + "d " + text;
					}
				}
				text = first + text + last;
			}
			
			i = text.indexOf("%trigger%");
			if(i != -1) {
				String first = text.substring(0, i);
				String last = text.substring(i + 9);
				text = first + cs.trigger + last;
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
		
		text = cleanText(text);
		
		if(text.length() == 0)
			return;

		if(canSendChat()) {
			sendChatNow(text);
		} else {
			if(cq == null) {
				new Exception("cq == null").printStackTrace();
				System.exit(1);
			} else {
				cq.enqueue(text);
			}
		}
	}
	
	public void sendChat(BNetUser to, String text) {
		if(text == null)
			return;

		text = cleanText(text);
		
		if(myUser.equals(to))
			recieveInfo(text);
		else {
			String prefix = "/w " + to.getFullLogonName() + " ";
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
		
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().bnetDisconnected();
	}
	
	public synchronized void joinedChannel(String channel) {
		channelName = channel;
		
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().joinedChannel(channel);
		
		Iterator<Connection> it2 = slaves.iterator();
		while(it2.hasNext())
			try {
				Connection c = it2.next();
				System.out.println("Telling [" + c.toString() + "] to join " + channel);
				c.joinChannel(channel);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	public synchronized void channelUser(BNetUser user, int flags, int ping, StatString statstr) {
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().channelUser(user, flags, ping, statstr);
	}
	
	public synchronized void channelJoin(BNetUser user, int flags, int ping, StatString statstr) {
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().channelJoin(user, flags, ping, statstr);
	}
	
	public synchronized void channelLeave(BNetUser user, int flags, int ping, StatString statstr) {
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().channelLeave(user, flags, ping, statstr);
	}

	public synchronized void recieveChat(BNetUser user, int flags, int ping, String text) {
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().recieveChat(user, flags, ping, text);
	}

	public synchronized void recieveEmote(BNetUser user, int flags, int ping, String text) {
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().recieveEmote(user, flags, ping, text);
	}

	public synchronized void recieveInfo(String text) {
		if(text == null)
			return;
		if(text.length() == 0)
			return;
		
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().recieveInfo(text);
		
		it = eventHandlers2.iterator();
		while(it.hasNext())
			it.next().recieveInfo(text);
	}

	public synchronized void recieveError(String text) {
		if(text == null)
			return;
		if(text.length() == 0)
			return;
		
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().recieveError(text);
		
		it = eventHandlers2.iterator();
		while(it.hasNext())
			it.next().recieveError(text);
	}
	
	public synchronized void whisperSent(BNetUser user, int flags, int ping, String text) {
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().whisperSent(user, flags, ping, text);
		
		it = eventHandlers2.iterator();
		while(it.hasNext())
			it.next().whisperSent(user, flags, ping, text);
	}
	
	public synchronized void whisperRecieved(BNetUser user, int flags, int ping, String text) {
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().whisperRecieved(user, flags, ping, text);
		
		it = eventHandlers2.iterator();
		while(it.hasNext())
			it.next().whisperRecieved(user, flags, ping, text);
	}
	
	// Realms
	
	public synchronized void queryRealms2(String[] realms) {
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().queryRealms2(realms);
	}
	
	public synchronized void logonRealmEx(int[] MCPChunk1, int ip, int port, int[] MCPChunk2, String uniqueName) {
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().logonRealmEx(MCPChunk1, ip, port, MCPChunk2, uniqueName);
	}
	
	// Friends

	public synchronized void friendsList(FriendEntry[] entries) {
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().friendsList(entries);
	}
	
	public synchronized void friendsUpdate(byte entry, byte location, byte status, int product, String locationName) {
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().friendsUpdate(entry, location, status, product, locationName);
	}
	
	// Clan

	public synchronized void clanMemberList(ClanMember[] members) {
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().clanMemberList(members);
	}
	
	public synchronized void clanMemberRankChange(byte oldRank, byte newRank, String user) {
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().clanMemberRankChange(oldRank, newRank, user);
	}
	
	public synchronized void clanMOTD(Object cookie, String text) {
		Iterator<EventHandler> it = eventHandlers.iterator();
		while(it.hasNext())
			it.next().clanMOTD(cookie, text);
	}
}
