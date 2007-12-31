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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.bnubot.core.clan.ClanMember;
import net.bnubot.core.friend.FriendEntry;
import net.bnubot.settings.ConnectionSettings;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetUser;
import net.bnubot.util.MirrorSelector;
import net.bnubot.util.Out;
import net.bnubot.util.TimeFormatter;
import net.bnubot.util.Wildcard;
import net.bnubot.vercheck.CurrentVersion;
import net.bnubot.vercheck.ReleaseType;

public abstract class Connection extends Thread {
	public enum ConnectionState {
		DO_NOT_ALLOW_CONNECT,
		ALLOW_CONNECT,
		FORCE_CONNECT,
		CONNECTED
	}

	protected Socket bnlsSocket = null;
	protected Socket socket = null;

	protected ConnectionSettings cs;
	protected ChatQueue chatQueue;
	protected Profile profile;
	protected List<EventHandler> eventHandlers = new ArrayList<EventHandler>();
	protected List<EventHandler> eventHandlers2 = new ArrayList<EventHandler>();
	protected BNetUser myUser = null;
	protected final List<BNetUser> users = new LinkedList<BNetUser>();
	protected ConnectionState connectionState = ConnectionState.ALLOW_CONNECT;
	protected List<Connection> slaves = new ArrayList<Connection>();
	protected String channelName = null;
	protected boolean forceReconnect = false;
	protected boolean initialized = false;
	protected boolean disposed = false;

	public static final int MAX_CHAT_LENGTH = 242;
	
	public List<EventHandler> getEventHandlers() {
		return Collections.synchronizedList(eventHandlers);
	}
	
	public List<BNetUser> getUsers() {
		return users;
	}
	
	private BNetUser getUser(BNetUser u) {
		for(BNetUser ui : users) {
			if(u.equals(ui))
				return ui;
		}
		return null;
	}
	
	/**
	 * Find users according to TC rules
	 */
	public List<String> findUsersForTabComplete(String containing) {
		containing = containing.toLowerCase();
		
		List<String> ret = new ArrayList<String>(users.size());
		for(BNetUser user : users) {
			String u = user.getShortLogonName();
			if(GlobalSettings.tabCompleteMode.beginsWithMode()) {
				if(u.toLowerCase().startsWith(containing))
					ret.add(u);
			} else {
				if(u.toLowerCase().contains(containing))
					ret.add(u);
			}
		}
		return ret;
	}
	
	/**
	 * Find users according to wildcard rules
	 */
	public List<BNetUser> findUsersWildcard(String pattern, BNetUser perspective) {
		pattern = pattern.toLowerCase();
		
		List<BNetUser> ret = new ArrayList<BNetUser>(users.size());
		for(BNetUser user : users) {
			String u = user.getShortLogonName(perspective).toLowerCase();
			if(Wildcard.matches(pattern, u))
				ret.add(user);
		}
		return ret;
	}
	
	/**
	 * Find a user
	 */
	public BNetUser findUser(String pattern, BNetUser perspective) {
		for(BNetUser user : users) {
			String u = user.getShortLogonName(perspective);
			if(pattern.compareToIgnoreCase(u) == 0)
				return user;
		}
		return null;
	}

	public Connection(ConnectionSettings cs, ChatQueue cq, Profile p) {
		super(Connection.class.getSimpleName());

		this.cs = cs;
		this.chatQueue = cq;
		this.profile = p;

		if(cq != null)
			cq.add(this);
	}

	public abstract String toShortString();

	public abstract void sendJoinChannel(String channel) throws Exception;
	public abstract void sendQueryRealms2() throws Exception;
	public abstract void sendLogonRealmEx(String realmTitle) throws Exception;
	public abstract void sendClanInvitation(Object cookie, String user) throws Exception;
	public abstract void sendClanRankChange(Object cookie, String user, int newRank) throws Exception;
	public abstract void sendClanMOTD(Object cookie) throws Exception;
	public abstract void sendClanSetMOTD(String text) throws Exception;
	public abstract void sendReadUserData(String user) throws Exception;

	public void sendProfile(BNetUser user) throws Exception {
		sendReadUserData(user.getFullAccountName());
	}

	public void reconnect() {
		disconnect(true);
		connectionState = ConnectionState.FORCE_CONNECT;
	}
	
	public abstract boolean isOp();
	public abstract int getProductID();

	public void addSlave(Connection c) {
		slaves.add(c);
	}
	public void addEventHandler(EventHandler e) {
		synchronized(eventHandlers) {
			eventHandlers.add(e);
			if(initialized)
				e.initialize(this);
		}
	}
	public void addSecondaryEventHandler(EventHandler e) {
		synchronized(eventHandlers2) {
			eventHandlers2.add(e);
			if(initialized)
				e.initialize(this);
		}
	}

	public void removeEventHandler(EventHandler e) {
		synchronized(eventHandlers) {
			eventHandlers.remove(e);
		}
		e.disable(this);
	}

	public boolean isConnected() {
		return (connectionState == ConnectionState.CONNECTED);
	}

	public void setBNLSConnected(boolean c) throws IOException {
		if(bnlsSocket != null) {
			bnlsSocket.close();
			bnlsSocket = null;
		}
		
		if(c) {
			InetAddress address = MirrorSelector.getClosestMirror(GlobalSettings.bnlsServer, GlobalSettings.bnlsPort);
			recieveInfo("Connecting to " + address + ":" + GlobalSettings.bnlsPort + ".");
			bnlsSocket = new Socket(address, GlobalSettings.bnlsPort);
			bnlsSocket.setKeepAlive(true);
		}
	}
	
	public void connect() {
		if(isConnected())
			return;

		try {
			if(socket != null) {
				socket.close();
				socket = null;
			}
			
			String v = cs.isValid();
			if(v != null) {
				recieveError(v);
				return;
			}
		} catch(IOException e) {
			Out.fatalException(e);
		}

		connectionState = ConnectionState.FORCE_CONNECT;
		bnetConnected();
	}
	
	public void disconnect(boolean allowReconnect) {
		if(!isConnected())
			return;

		try {
			if(socket != null) {
				socket.close();
				socket = null;
			}
		} catch(IOException e) {
			Out.fatalException(e);
		}

		connectionState = allowReconnect ? ConnectionState.ALLOW_CONNECT : ConnectionState.DO_NOT_ALLOW_CONNECT;
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

	public void sendChatCommand(String text) {
		profile.lastAntiIdle = System.currentTimeMillis();

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

	public void queueChatHelper(String text, boolean allowCommands) {
		if(text == null)
			return;
		if(text.length() == 0)
			return;
		if(!isConnected())
			return;
		
		ALLOWCOMMANDS: if(allowCommands) {
			if(text.charAt(0) == '/') {
				String postSlash = text.substring(1);
				String[] command = postSlash.split(" ", 2);
				switch(command[0].charAt(0)) {
				case '/':
					parseCommand(myUser, postSlash, false);
					return;
				case 'c':
					if(command[0].equals("cmd")) {
						if(command.length == 2) {
							parseCommand(myUser, command[1], true);
							return;
						}
					}
					break;
				case 'p':
					if(command[0].equals("profile")) {
						try {
							if((command.length < 2) || (command[1].length() == 0))
								sendProfile(myUser);
							else
								sendReadUserData(command[1]);
						} catch(Exception e) {
							Out.exception(e);
						}
						return;
					}
					break;
				case 'w':
					if(command[0].equals("whoami"))
						break ALLOWCOMMANDS;
					if(command[0].equals("whois"))
						break ALLOWCOMMANDS;
					break;
				default:
				}
				
				if(postSlash.length() > 0) {
					if(parseCommand(myUser, postSlash, true))
						return;
				}
			}
		}

		try {
			text = new String(text.getBytes(), "UTF-8");
		} catch (UnsupportedEncodingException e) {}
		text = cleanText(text);

		if(text.length() == 0)
			return;

		if(canSendChat() && !GlobalSettings.enableFloodProtect) {
			sendChatCommand(text);
		} else {
			chatQueue.enqueue(text, GlobalSettings.enableFloodProtect);
		}
	}
	public void sendChat(BNetUser to, String text, boolean whisperBack) {
		if(text == null)
			return;

		text = cleanText(text);

		boolean isMyUser = false;
		if(myUser != null)
			isMyUser = myUser.equals(to);
		
		if(whisperBack && isMyUser)
			recieveInfo(text);
		else {
			String prefix;
			if(whisperBack || isMyUser) {
				if(whisperBack)
					prefix = "/w " + to.getFullLogonName() + " ";
				else
					prefix = "";
				
				prefix += "[BNU";
				ReleaseType rt = CurrentVersion.version().getReleaseType();
				if(rt.isAlpha())
					prefix += " Alpha";
				else if(rt.isBeta())
					prefix += " Beta";
				else if(rt.isReleaseCandidate())
					prefix += " RC";
				prefix += "] ";
			} else {
				prefix = to.getShortPrettyName() + ": ";
			}

			//Split up the text in to appropriate sized pieces
			int pieceSize = MAX_CHAT_LENGTH - prefix.length();
			for(int i = 0; i < text.length(); i += pieceSize) {
				String piece = prefix + text.substring(i);
				if(piece.length() > MAX_CHAT_LENGTH)
					piece = piece.substring(0, MAX_CHAT_LENGTH);
				queueChatHelper(piece, false);
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
	
	public boolean isInitialized() {
		return initialized;
	}

	public void dispose() {
		disposed = true;
		disconnect(false);
		
		for(EventHandler e : eventHandlers)
			removeEventHandler(e);
	}

	/*
	 * EventHandler methods follow
	 * 
	 */
	public void bnetConnected() {
		users.clear();
		
		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.bnetConnected(this);
		}
	}

	public void bnetDisconnected() {
		channelName = null;
		users.clear();
		myUser = null;

		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.bnetDisconnected(this);
		}
	}

	public boolean parseCommand(BNetUser user, String command, boolean whisperBack) {
		int i = command.indexOf(';');
		if(i != -1) {
			String c1 = command.substring(0, i);
			String c2 = command.substring(i + 1);
			while(c2.charAt(0) == ' ')
				c2 = c2.substring(1);
			
			boolean ret = parseCommand(user, c1, whisperBack);
			ret |= parseCommand(user, c2, whisperBack);
			return ret;
		}

		Out.info(Connection.class, user.toString() + ": " + command + " [" + whisperBack + "]");
		
		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers) {
				if(eh.parseCommand(this, user, command, whisperBack))
					return true;
			}
		}
		return false;
	}

	public void titleChanged() {
		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.titleChanged(this);
		}

		synchronized(eventHandlers2) {
			for(EventHandler eh : eventHandlers2)
				eh.titleChanged(this);
		}
	}

	public void joinedChannel(String channel) {
		channelName = channel;
		users.clear();

		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.joinedChannel(this, channel);
		}

		synchronized(eventHandlers2) {
			for(Connection c : slaves)
				try {
					if(c.myUser != null) {
						Out.info(getClass(), "[" + myUser.getFullLogonName() + "] Telling [" + c.myUser.getFullLogonName() + "] to join " + channel);
						c.sendJoinChannel(channel);
					}
				} catch (Exception e) {
					Out.exception(e);
				}
		}
	}

	public void channelUser(BNetUser user) {
		if(getUser(user) == null) {
			users.add(user);
			ChannelListPriority.sort(users);
		}

		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.channelUser(this, user);
		}
	}

	public void channelJoin(BNetUser user) {
		if(getUser(user) == null) {
			users.add(user);
			ChannelListPriority.sort(users);
		}

		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.channelJoin(this, user);
		}
	}

	public void channelLeave(BNetUser user) {
		if(!users.remove(getUser(user)))
			Out.error(getClass(), "Tried to remove a user that was not in the list: " + user.toString());

		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.channelLeave(this, user);
		}
	}

	public void recieveChat(BNetUser user, String text) {
		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.recieveChat(this, user, text);
		}
	}

	public void recieveEmote(BNetUser user, String text) {
		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.recieveEmote(this, user, text);
		}
	}

	public void recieveDebug(String text) {
		if(text == null)
			return;
		if(text.length() == 0)
			return;

		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.recieveDebug(this, text);
		}

		synchronized(eventHandlers2) {
			for(EventHandler eh : eventHandlers2)
				eh.recieveDebug(this, text);
		}
	}

	public void recieveInfo(String text) {
		if(text == null)
			return;
		if(text.length() == 0)
			return;

		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.recieveInfo(this, text);
		}

		synchronized(eventHandlers2) {
			for(EventHandler eh : eventHandlers2)
				eh.recieveInfo(this, text);
		}
	}

	public void recieveError(String text) {
		if(text == null)
			return;
		if(text.length() == 0)
			return;

		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.recieveError(this, text);
		}

		synchronized(eventHandlers2) {
			for(EventHandler eh : eventHandlers2)
				eh.recieveError(this, text);
		}
	}

	public void whisperSent(BNetUser user, String text) {
		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.whisperSent(this, user, text);
		}

		synchronized(eventHandlers2) {
			for(EventHandler eh : eventHandlers2)
				eh.whisperSent(this, user, text);
		}
	}

	public void whisperRecieved(BNetUser user, String text) {
		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.whisperRecieved(this, user, text);
		}

		synchronized(eventHandlers2) {
			for(EventHandler eh : eventHandlers2)
				eh.whisperRecieved(this, user, text);
		}
	}

	// Realms

	public void queryRealms2(String[] realms) {
		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.queryRealms2(this, realms);
		}
	}

	public void logonRealmEx(int[] MCPChunk1, int ip, int port, int[] MCPChunk2, String uniqueName) {
		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.logonRealmEx(this, MCPChunk1, ip, port, MCPChunk2, uniqueName);
		}
	}

	// Friends

	public void friendsList(FriendEntry[] entries) {
		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.friendsList(this, entries);
		}
	}

	public void friendsUpdate(FriendEntry friend) {
		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.friendsUpdate(this, friend);
		}
	}

	public void friendsAdd(FriendEntry friend) {
		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.friendsAdd(this, friend);
		}
	}

	public void friendsRemove(byte entry) {
		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.friendsRemove(this, entry);
		}
	}

	public void friendsPosition(byte oldPosition, byte newPosition) {
		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.friendsPosition(this, oldPosition, newPosition);
		}
	}

	// Clan

	public void clanMOTD(Object cookie, String text) {
		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.clanMOTD(this, cookie, text);
		}
	}

	public void clanMemberList(ClanMember[] members) {
		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.clanMemberList(this, members);
		}
	}

	public void clanMemberRemoved(String username) {
		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.clanMemberRemoved(this, username);
		}
	}

	public void clanMemberRankChange(byte oldRank, byte newRank, String user) {
		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.clanMemberRankChange(this, oldRank, newRank, user);
		}
	}

	public void clanMemberStatusChange(ClanMember member) {
		synchronized(eventHandlers) {
		for(EventHandler eh : eventHandlers)
			eh.clanMemberStatusChange(this, member);
		}
	}

	public String getChannel() {
		return channelName;
	}
	
	public BNetUser getBNetUser(String user) {
		for(BNetUser u : users) {
			if(u.equals(user))
				return u;
		}
		return null;
	}

	public BNetUser getBNetUser(String user, BNetUser myRealm) {
		BNetUser out = getBNetUser(user);
		if(out != null)
			return out.toPerspective(myRealm);
		return new BNetUser(user, myRealm.getFullLogonName());
	}
}
