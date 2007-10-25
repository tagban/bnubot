/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.core;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;

import net.bnubot.core.clan.ClanMember;
import net.bnubot.core.friend.FriendEntry;
import net.bnubot.settings.ConnectionSettings;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetUser;
import net.bnubot.util.MirrorSelector;
import net.bnubot.util.Out;
import net.bnubot.util.TimeFormatter;
import net.bnubot.vercheck.CurrentVersion;
import net.bnubot.vercheck.ReleaseType;

public abstract class Connection extends Thread implements EventHandler {
	protected Socket bnlsSocket = null;
	protected Socket socket = null;

	protected ConnectionSettings cs;
	protected ChatQueue chatQueue;
	protected Profile profile;
	protected LinkedList<EventHandler> eventHandlers = new LinkedList<EventHandler>();
	protected LinkedList<EventHandler> eventHandlers2 = new LinkedList<EventHandler>();
	protected BNetUser myUser = null;
	protected boolean connected = false;
	protected LinkedList<Connection> slaves = new LinkedList<Connection>();
	protected String channelName = null;
	protected long lastAntiIdle;
	protected boolean forceReconnect = false;

	public static final int MAX_CHAT_LENGTH = 242;

	private int eh_semaphore = 0;
	private int eh2_semaphore = 0;
	
	public LinkedList<EventHandler> getEventHandlers() {
		return eventHandlers;
	}

	private void waitForEHsemaphore() {
		while(eh_semaphore > 0) {
			try {Thread.sleep(10);} catch (InterruptedException e) {Out.exception(e);}
			Thread.yield();
		}
	}

	private void waitForEH2semaphore() {
		while(eh2_semaphore > 0) {
			try {Thread.sleep(10);} catch (InterruptedException e) {Out.exception(e);}
			Thread.yield();
		}
	}

	public Connection(ConnectionSettings cs, ChatQueue cq, Profile p) {
		super(Connection.class.getSimpleName());

		this.cs = cs;
		this.chatQueue = cq;
		this.profile = p;

		if(cq != null)
			cq.add(this);
	}

	public abstract void joinChannel(String channel) throws Exception;
	public abstract void sendQueryRealms() throws Exception;
	public abstract void sendLogonRealmEx(String realmTitle) throws Exception;
	public abstract void sendClanInvitation(Object cookie, String user) throws Exception;
	public abstract void sendClanRankChange(Object cookie, String user, int newRank) throws Exception;
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
		waitForEHsemaphore();
		eventHandlers.add(e);
		e.initialize(this);
	}

	public void addSecondaryEventHandler(EventHandler e) {
		waitForEH2semaphore();
		eventHandlers2.add(e);
		e.initialize(this);
	}

	public void removeEventHandler(EventHandler e) {
		waitForEHsemaphore();
		eventHandlers.remove(e);
	}

	public boolean isConnected() {
		return connected;
	}

	public void setBNLSConnected(boolean c) throws IOException {
		if(c) {
			if(bnlsSocket == null) {
				InetAddress address = MirrorSelector.getClosestMirror(GlobalSettings.bnlsServer, GlobalSettings.bnlsPort);
				recieveInfo("Connecting to " + address + ":" + GlobalSettings.bnlsPort + ".");
				bnlsSocket = new Socket(address, GlobalSettings.bnlsPort);
				bnlsSocket.setKeepAlive(true);
			}
		} else {
			if(bnlsSocket != null) {
				bnlsSocket.close();
				bnlsSocket = null;
			}
		}
	}

	public void setConnected(boolean c) {
		if(connected == c)
			return;

		try {
			if(c) {
				String v = cs.isValid();
				if(v != null) {
					recieveError(v);
					return;
				}

				if(socket == null) {
					InetAddress address = MirrorSelector.getClosestMirror(cs.bncsServer, cs.port);
					recieveInfo("Connecting to " + address + ":" + cs.port + ".");
					socket = new Socket(address, cs.port);
				}
			} else {
				if(socket != null) {
					socket.close();
					socket = null;
				}
			}
		} catch(IOException e) {
			Out.fatalException(e);
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
		long thisTime = System.currentTimeMillis();

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

		long thisTime = System.currentTimeMillis();
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
		lastAntiIdle = System.currentTimeMillis();

		if(canSendChat())
			increaseDelay(text.length());
		else
			// Fake the bot in to thinking we sent chat in the future
			lastChatTime = System.currentTimeMillis() + increaseDelay(text.length());
	}

	public String cleanText(String text) {
		//Remove all chars under 0x20
		byte[] data = text.getBytes();
		text = "";
		for(byte element : data) {
			if(element >= 0x20)
				text += (char)element;
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
				text = first + GlobalSettings.trigger + last;
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

					for(EventHandler eh : eventHandlers)
						eh.parseCommand(myUser, command[1], params, false);

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
						Out.exception(e);
					}
					return;
				}
				break;
			}
		}

		try {
			text = new String(text.getBytes(), "UTF-8");
		} catch (UnsupportedEncodingException e) {}
		text = cleanText(text);

		if(text.length() == 0)
			return;

		if(canSendChat() && !GlobalSettings.enableFloodProtect) {
			sendChatNow(text);
		} else {
			chatQueue.enqueue(text, GlobalSettings.enableFloodProtect);
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
			if(GlobalSettings.whisperBack || forceWhisper) {
				prefix = "/w " + to.getFullLogonName() + " [BNU";
				ReleaseType rt = CurrentVersion.version().getReleaseType();
				if(rt.isAlpha())
					prefix += " Alpha";
				else if(rt.isBeta())
					prefix += " Beta";
				else if(rt.isReleaseCandidate())
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

	public ChatQueue getChatQueue() {
		return this.chatQueue;
	}
	
	public Profile getProfile() {
		return this.profile;
	}

	/*
	 * EventHandler methods follow
	 * 
	 */

	public void initialize(Connection c) {
		new UnsupportedOperationException();
	}

	public synchronized void bnetConnected() {
		for(EventHandler eh : eventHandlers)
			eh.bnetConnected();
	}

	public synchronized void bnetDisconnected() {
		channelName = null;
		myUser = null;

		eh_semaphore++;
		for(EventHandler eh : eventHandlers)
			eh.bnetDisconnected();
		eh_semaphore--;
	}

	public void parseCommand(BNetUser user, String command, String param, boolean wasWhispered) {
		eh_semaphore++;
		for(EventHandler eh : eventHandlers)
			eh.parseCommand(user, command, param, wasWhispered);
		eh_semaphore--;
	}

	public synchronized void titleChanged() {
		eh_semaphore++;
		for(EventHandler eh : eventHandlers)
			eh.titleChanged();
		eh_semaphore--;
	}

	public synchronized void joinedChannel(String channel) {
		channelName = channel;

		eh_semaphore++;
		for(EventHandler eh : eventHandlers)
			eh.joinedChannel(channel);
		eh_semaphore--;

		eh2_semaphore++;
		for(Connection c : slaves)
			try {
				String name;
				if(c.myUser != null)
					name = c.myUser.getFullLogonName();
				else
					name = c.toString();
				Out.info(getClass(), "[" + myUser.getFullLogonName() + "] Telling [" + name + "] to join " + channel);
				c.joinChannel(channel);
			} catch (Exception e) {
				Out.exception(e);
			}
		eh2_semaphore--;
	}

	public synchronized void channelUser(BNetUser user) {
		eh_semaphore++;
		for(EventHandler eh : eventHandlers)
			eh.channelUser(user);
		eh_semaphore--;
	}

	public synchronized void channelJoin(BNetUser user) {
		eh_semaphore++;
		for(EventHandler eh : eventHandlers)
			eh.channelJoin(user);
		eh_semaphore--;
	}

	public synchronized void channelLeave(BNetUser user) {
		eh_semaphore++;
		for(EventHandler eh : eventHandlers)
			eh.channelLeave(user);
		eh_semaphore--;
	}

	public synchronized void recieveChat(BNetUser user, String text) {
		eh_semaphore++;
		for(EventHandler eh : eventHandlers)
			eh.recieveChat(user, text);
		eh_semaphore--;
	}

	public synchronized void recieveEmote(BNetUser user, String text) {
		eh_semaphore++;
		for(EventHandler eh : eventHandlers)
			eh.recieveEmote(user, text);
		eh_semaphore--;
	}

	public synchronized void recieveInfo(String text) {
		if(text == null)
			return;
		if(text.length() == 0)
			return;

		eh_semaphore++;
		for(EventHandler eh : eventHandlers)
			eh.recieveInfo(text);
		eh_semaphore--;

		eh2_semaphore++;
		for(EventHandler eh : eventHandlers2)
			eh.recieveInfo(text);
		eh2_semaphore--;
	}

	public synchronized void recieveError(String text) {
		if(text == null)
			return;
		if(text.length() == 0)
			return;

		eh_semaphore++;
		for(EventHandler eh : eventHandlers)
			eh.recieveError(text);
		eh_semaphore--;

		eh2_semaphore++;
		for(EventHandler eh : eventHandlers2)
			eh.recieveError(text);
		eh2_semaphore--;
	}

	public synchronized void whisperSent(BNetUser user, String text) {
		eh_semaphore++;
		for(EventHandler eh : eventHandlers)
			eh.whisperSent(user, text);
		eh_semaphore--;

		eh2_semaphore++;
		for(EventHandler eh : eventHandlers2)
			eh.whisperSent(user, text);
		eh2_semaphore--;
	}

	public synchronized void whisperRecieved(BNetUser user, String text) {
		eh_semaphore++;
		for(EventHandler eh : eventHandlers)
			eh.whisperRecieved(user, text);
		eh_semaphore--;

		eh2_semaphore++;
		for(EventHandler eh : eventHandlers2)
			eh.whisperRecieved(user, text);
		eh2_semaphore--;
	}

	// Realms

	public synchronized void queryRealms2(String[] realms) {
		eh_semaphore++;
		for(EventHandler eh : eventHandlers)
			eh.queryRealms2(realms);
		eh_semaphore--;
	}

	public synchronized void logonRealmEx(int[] MCPChunk1, int ip, int port, int[] MCPChunk2, String uniqueName) {
		eh_semaphore++;
		for(EventHandler eh : eventHandlers)
			eh.logonRealmEx(MCPChunk1, ip, port, MCPChunk2, uniqueName);
		eh_semaphore--;
	}

	// Friends

	public synchronized void friendsList(FriendEntry[] entries) {
		eh_semaphore++;
		for(EventHandler eh : eventHandlers)
			eh.friendsList(entries);
		eh_semaphore--;
	}

	public synchronized void friendsUpdate(FriendEntry friend) {
		eh_semaphore++;
		for(EventHandler eh : eventHandlers)
			eh.friendsUpdate(friend);
		eh_semaphore--;
	}

	public synchronized void friendsAdd(FriendEntry friend) {
		eh_semaphore++;
		for(EventHandler eh : eventHandlers)
			eh.friendsAdd(friend);
		eh_semaphore--;
	}

	public void friendsRemove(byte entry) {
		eh_semaphore++;
		for(EventHandler eh : eventHandlers)
			eh.friendsRemove(entry);
		eh_semaphore--;
	}

	public void friendsPosition(byte oldPosition, byte newPosition) {
		eh_semaphore++;
		for(EventHandler eh : eventHandlers)
			eh.friendsPosition(oldPosition, newPosition);
		eh_semaphore--;
	}

	// Clan

	public synchronized void clanMOTD(Object cookie, String text) {
		eh_semaphore++;
		for(EventHandler eh : eventHandlers)
			eh.clanMOTD(cookie, text);
		eh_semaphore--;
	}

	public synchronized void clanMemberList(ClanMember[] members) {
		eh_semaphore++;
		for(EventHandler eh : eventHandlers)
			eh.clanMemberList(members);
		eh_semaphore--;
	}

	public synchronized void clanMemberRemoved(String username) {
		eh_semaphore++;
		for(EventHandler eh : eventHandlers)
			eh.clanMemberRemoved(username);
		eh_semaphore--;
	}

	public synchronized void clanMemberRankChange(byte oldRank, byte newRank, String user) {
		eh_semaphore++;
		for(EventHandler eh : eventHandlers)
			eh.clanMemberRankChange(oldRank, newRank, user);
		eh_semaphore--;
	}

	public synchronized void clanMemberStatusChange(ClanMember member) {
		eh_semaphore++;
		for(EventHandler eh : eventHandlers)
			eh.clanMemberStatusChange(member);
		eh_semaphore--;
	}
}
