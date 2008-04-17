/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import net.bnubot.bot.gui.settings.ConfigurationFrame;
import net.bnubot.bot.gui.settings.OperationCancelledException;
import net.bnubot.core.bncs.ProductIDs;
import net.bnubot.core.botnet.BotNetConnection;
import net.bnubot.settings.ConnectionSettings;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetInputStream;
import net.bnubot.util.BNetUser;
import net.bnubot.util.MirrorSelector;
import net.bnubot.util.Out;
import net.bnubot.util.TimeFormatter;
import net.bnubot.util.UserProfile;
import net.bnubot.util.Wildcard;
import net.bnubot.util.music.MusicController;
import net.bnubot.util.music.MusicControllerFactory;
import net.bnubot.util.task.Task;
import net.bnubot.util.task.TaskManager;
import net.bnubot.vercheck.CurrentVersion;

public abstract class Connection extends Thread {
	public enum ConnectionState {
		DO_NOT_ALLOW_CONNECT,
		ALLOW_CONNECT,
		FORCE_CONNECT,
		CONNECTED;
		
		public boolean canConnect() {
			switch(this) {
			case FORCE_CONNECT:
				return true;
			case ALLOW_CONNECT:
				return GlobalSettings.autoConnect;
			}
			return false;
		}
	}

	protected Socket socket = null;
	protected Socket bnlsSocket = null;

	protected ConnectionSettings cs;
	protected Profile profile;
	protected final Collection<EventHandler> eventHandlers = new ArrayList<EventHandler>();
	protected final Hashtable<String, BNetUser> users = new Hashtable<String, BNetUser>();
	protected BNetUser myUser = null;
	protected ConnectionState connectionState = ConnectionState.ALLOW_CONNECT;
	protected String channelName = null;
	protected int channelFlags = 0;
	protected boolean forceReconnect = false;
	protected boolean initialized = false;
	protected boolean disposed = false;
	
	protected long lastNullPacket;

	protected Task createTask(String title, String currentStep) {
		Task t = TaskManager.createTask(profile.getName() + ": " + title, currentStep);
		currentTasks.add(t);
		return t;
	}
	
	protected Task createTask(String title, int max, String units) {
		Task t = TaskManager.createTask(title, max, units);
		currentTasks.add(t);
		return t;
	}
	
	protected void completeTask(Task t) {
		currentTasks.remove(t);
		t.complete();
	}
	
	protected String getServer() {
		return cs.server;
	}
	
	protected int getPort() {
		return cs.port;
	}
	
	private final List<Task> currentTasks = new LinkedList<Task>();
	@Override
	public void run() {
		if(!(this instanceof BotNetConnection)) {
			// We must initialize the EHs in the Connection thread
			for(EventHandler eh : eventHandlers)
				eh.initialize(this);
		}
		
		initialized = true;
		
		while(!disposed) {
			try {
				for(Task t : currentTasks)
					t.complete();
				currentTasks.clear();
				myUser = null;
				titleChanged();
				
				// Wait until we're supposed to connect
				while(!connectionState.canConnect()) {
					yield();
					sleep(200);
				}
				
				Task connect = createTask("Connecting to " + getServer() + ":" + getPort(), "Verify connection settings validity");
				
				// Check if CS is valid
				while(cs.isValid() != null)
					new ConfigurationFrame(cs);

				// Wait a short time before allowing a reconnect
				waitUntilConnectionSafe(connect);
				
				// Double-check if disposal occured
				if(disposed)
					break;
				
				// Initialize connection to DT server
				initializeConnection(connect);
				
				// Log in
				boolean loggedIn = sendLoginPackets(connect);
				
				// Connection established
				completeTask(connect);
				
				lastNullPacket = System.currentTimeMillis();
				profile.lastAntiIdle = lastNullPacket;
				
				if(loggedIn)
					connectedLoop();
					
				// Connection closed
			} catch(SocketException e) {
			} catch(OperationCancelledException e) {
				disposed = true;
			} catch(Exception e) {
				recieveError("Unhandled " + e.getClass().getSimpleName() + ": " + e.getMessage());
				Out.exception(e);
			}

			disconnect(true);
		}
		
		for(Task t : currentTasks)
			t.complete();
		currentTasks.clear();
		
		getProfile().dispose();
	}

	protected abstract void connectedLoop() throws Exception;
	protected abstract boolean sendLoginPackets(Task connect) throws Exception;
	protected abstract void initializeConnection(Task connect) throws Exception;

	private static Hashtable<String, Long> connectionTimes = new Hashtable<String, Long>();
	/**
	 * Wait until it is safe to connect to the server
	 */
	protected void waitUntilConnectionSafe(Task connect) {
		long waitUntil;
		String server = getServer();
		synchronized(connectionTimes) {
			Long lastConnectionTime = connectionTimes.get(server);
			if(lastConnectionTime == null) {
				connectionTimes.put(server, System.currentTimeMillis());
				return;
			}
			
			waitUntil = lastConnectionTime + 15000;
			connectionTimes.put(server, waitUntil);
		}

		final String status = "Stalling to avoid flood: ";
		while(!disposed) {
			long timeLeft = waitUntil - System.currentTimeMillis();
			if(timeLeft <= 0)
				break;
			
			connect.updateProgress(status + TimeFormatter.formatTime(timeLeft, false));
			
			try { sleep(100); } catch (InterruptedException e1) {}
			yield();
		}
	}
	
	public int getIp() {
		return BNetInputStream.readDWord(socket.getInetAddress().getAddress(), 0);
	}

	private static List<String> antiIdles = new ArrayList<String>();
	protected String getAntiIdle() {
		if(antiIdles.size() == 0) {
			BufferedReader is = null;
			try {
				File f = new File("anti-idle.txt");
				if(!f.exists()) {
					f.createNewFile();
					
					FileWriter os = new FileWriter(f);
					os.write("# Enter anti-idle messages in this file.\r\n");
					os.write("# \r\n");
					os.write("# Lines beginning with '#' are regarded as comments\r\n");
					os.write("# \r\n");
					os.write("\r\n");
					os.close();
				}
				is = new BufferedReader(new FileReader(f));
			} catch (Exception e) {
				Out.fatalException(e);
			}
			
			do {
				String line = null;
				try {
					line = is.readLine();
				} catch (IOException e) {
					Out.fatalException(e);
				}
				if(line == null)
					break;
				
				line = line.trim();
				if(line.length() == 0)
					continue;
				
				if(line.charAt(0) != '#')
					antiIdles.add(line);
			} while(true);
			
			try { is.close(); } catch (Exception e) {}
		}
		
		//grab one
		int i = antiIdles.size();
		if(i == 0)
			return cs.antiIdle;
		i = (int)Math.floor(Math.random() * i);
		return antiIdles.remove(i);
	}
	
	public Collection<EventHandler> getEventHandlers() {
		return Collections.synchronizedCollection(eventHandlers);
	}
	
	public Collection<BNetUser> getUsers() {
		return users.values();
	}
	
	public List<BNetUser> getSortedUsers() {
		List<BNetUser> x = new ArrayList<BNetUser>(getUsers());
		ChannelListPriority.sort(x);
		return x;
	}
	
	private BNetUser getUser(BNetUser u) {
		return users.get(u.getFullLogonName().toLowerCase());
	}

	private void checkAddUser(BNetUser user) {
		if(getUser(user) == null)
			users.put(user.getFullLogonName().toLowerCase(), user);
	}

	private void removeUser(BNetUser user) {
		if(users.remove(user.getFullLogonName().toLowerCase()) == null)
			Out.error(getClass(), "Tried to remove a user that was not in the list: " + user.toString());
	}
	
	/**
	 * Find users according to TC rules
	 */
	public List<String> findUsersForTabComplete(String containing) {
		containing = containing.toLowerCase();
		
		List<String> ret = new ArrayList<String>(users.size());
		for(BNetUser user : getUsers()) {
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
		for(BNetUser user : getUsers()) {
			String u = user.getShortLogonName(perspective).toLowerCase();
			if(Wildcard.matches(pattern, u))
				ret.add(user);
		}
		return ret;
	}

	public Connection(ConnectionSettings cs, Profile p) {
		super(Connection.class.getSimpleName() + "-" + cs.botNum);

		this.cs = cs;
		this.profile = p;

		p.getChatQueue().add(this);
	}

	public String toShortString() {
		if(cs.isValid() == null)
			return cs.username + "@" + cs.myRealm;
		
		return profile.getName();
	}

	public abstract void sendLeaveChat() throws Exception;
	public abstract void sendJoinChannel(String channel) throws Exception;
	public abstract void sendJoinChannel2(String channel) throws Exception;
	public abstract void sendQueryRealms2() throws Exception;
	public abstract void sendLogonRealmEx(String realmTitle) throws Exception;
	public abstract void sendClanInvitation(Object cookie, String user) throws Exception;
	public abstract void sendClanRankChange(Object cookie, String user, int newRank) throws Exception;
	public abstract void sendClanMOTD(Object cookie) throws Exception;
	public abstract void sendClanSetMOTD(String text) throws Exception;
	public abstract void sendReadUserData(String user) throws Exception;
	public abstract void sendWriteUserData(UserProfile profile) throws Exception;

	public void sendProfile(BNetUser user) throws Exception {
		sendReadUserData(user.getFullAccountName());
	}

	public void reconnect() {
		disconnect(true);
		connectionState = ConnectionState.FORCE_CONNECT;
	}
	
	public abstract boolean isOp();
	public abstract ProductIDs getProductID();

	public void addEventHandler(EventHandler e) {
		synchronized(eventHandlers) {
			eventHandlers.add(e);
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
		if(!isConnected() && allowReconnect)
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

	protected void sendChatCommand(String text) {
		profile.lastAntiIdle = System.currentTimeMillis();

		if(canSendChat())
			increaseDelay(text.length());
		else
			// Fake the bot in to thinking we sent chat in the future
			lastChatTime = System.currentTimeMillis() + increaseDelay(text.length());
	}

	public String cleanText(String text, boolean enableKeywords) {
		//Remove all chars under 0x20
		byte[] data = text.getBytes();
		text = "";
		for(byte element : data) {
			if(element >= 0x20)
				text += (char)element;
		}

		boolean somethingDone = true;
		while(somethingDone && enableKeywords) {
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

			i = text.indexOf("%mp3%");
			if(i != -1) {
				String mp3 = null;
				try {
					MusicController musicController = MusicControllerFactory.getMusicController();
					mp3 = musicController.getCurrentlyPlaying();
				} catch(Exception e) {
					e.printStackTrace();
				}
				
				if(mp3 == null)
					mp3 = "[Music Player Error]";

				somethingDone = true;
				String first = text.substring(0, i);
				String last = text.substring(i + 5);
				text = first + mp3 + last;
			}
		}
		return text;
	}

	/**
	 * Helper function to send chat to battle.net
	 * @param text The whole text to be sent out
	 * @param allowCommands Enables internal bot commands (/cmd, /profile, etc)
	 */
	public void sendChat(String text, boolean allowCommands) {
		sendChat(null, text, allowCommands, true);
	}
	
	public static final int MAX_CHAT_LENGTH = 200;
	/**
	 * Helper function to send chat to battle.net
	 * @param prefix A String to prepend each time the text is split (ex: "/w BNU-Camel ")
	 * @param text The whole text to be sent out
	 * @param allowCommands Enables internal bot commands (/cmd, /profile, etc)
	 * @param enableKeywords Enable keywords (%uptime%, %trigger%, %mp3%, etc)
	 */
	public void sendChat(String prefix, String text, boolean allowCommands, boolean enableKeywords) {
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
					parseCommand(myUser, postSlash.substring(1), false);
					return;
				case 'c':
					if(command[0].equals("cmd")) {
						if(command.length == 2) {
							parseCommand(myUser, command[1], true);
							return;
						}
					}
					break;
				case 'j':
					try {
						if(command[0].equals("j")) {
							sendJoinChannel(command[1]);
							return;
						}
						if(command[0].equals("join")) {
							sendJoinChannel(command[1]);
							return;
						}
						if(command[0].equals("join2")) {
							sendJoinChannel2(command[1]);
							return;
						}
					} catch(Exception e) {
						Out.exception(e);
						break ALLOWCOMMANDS;
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
				case 'q':
					if(command[0].equals("quote")) {
						sendChat(prefix, command[1], false, false);
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
		text = cleanText(text, enableKeywords);

		if(text.length() == 0)
			return;
		
		//Split up the text in to appropriate sized pieces
		int pieceSize = MAX_CHAT_LENGTH - (prefix == null ? 0 : prefix.length());
		ChatQueue cq = profile.getChatQueue();
		for(int i = 0; i < text.length(); i += pieceSize) {
			String piece = (prefix == null ? "" : prefix) + (i > 0 ? "..." : "") + text.substring(i);
			if(i > 0)
				i -= 3;
			if(piece.length() > MAX_CHAT_LENGTH) {
				piece = piece.substring(0, MAX_CHAT_LENGTH - 3) + "...";
				i -= 3;
			}

			cq.enqueue(this, piece);
		}
	}

	/**
	 * Download a file using BNFTP
	 * @param filename The file's name
	 * @return The File, or null if there was an error
	 */
	public File downloadFile(String filename) {
		return BNFTPConnection.downloadFile(cs, filename);
	}

	public ConnectionSettings getConnectionSettings() {
		return cs;
	}

	public BNetUser getMyUser() {
		return myUser;
	}
	
	public Profile getProfile() {
		return profile;
	}
	
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * Destroy and cleanup this Connection
	 */
	public void dispose() {
		disposed = true;
		disconnect(false);
		
		synchronized(eventHandlers) {
			for(EventHandler e : eventHandlers)
				removeEventHandler(e);
		}
	}

	/**
	 * Determine if this Connection is the primary for a Profile
	 * @return true if the connection is primary
	 */
	protected boolean isPrimaryConnection() {
		return equals(profile.getPrimaryConnection());
	}

	/**
	 * Get the channel this Connection is in
	 * @return The channel
	 */
	public String getChannel() {
		return channelName;
	}
	
	/**
	 * Look for a BNetUser in the user list.
	 * @param user "User[#N][@Realm]"
	 * @param perspective The BNetUser whose realm to use if none is specified
	 * @return A BNetUser representing the user, or null if none is found
	 */
	public BNetUser findUser(String user, BNetUser perspective) {
		if(user.indexOf('@') == -1)
			user += '@' + perspective.getRealm();
		return users.get(user.toLowerCase());
	}

	/**
	 * Look for a BNetUser in the user list. If it doesn't exist, create a new one
	 * @param user "User[#N][@Realm]"
	 * @param perspective The BNetUser whose realm to use if none is specified
	 * @return A BNetUser representing the user
	 */
	public BNetUser getCreateBNetUser(String user, BNetUser perspective) {
		BNetUser x = findUser(user, perspective);
		if(x != null)
			return x;
		return new BNetUser(this, user, perspective);
	}
	
	/**
	 * Clear all text waiting to send in the ChatQueue
	 */
	public void clearQueue() {
		profile.getChatQueue().clear();
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
		channelFlags = 0;
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
	}

	public void joinedChannel(String channel, int flags) {
		if(!isPrimaryConnection())
			return;
		
		channelName = channel;
		channelFlags = flags;
		users.clear();

		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.joinedChannel(this, channel);
		}
		
		for(Connection c : profile.getConnections()) {
			if(equals(c))
				continue;
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
		checkAddUser(user);

		if(!isPrimaryConnection())
			return;
		
		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.channelUser(this, user);
		}
	}

	public void channelJoin(BNetUser user) {
		checkAddUser(user);

		if(!isPrimaryConnection())
			return;
		
		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.channelJoin(this, user);
		}
	}

	public void channelLeave(BNetUser user) {
		removeUser(user);
		
		if(!isPrimaryConnection())
			return;
		
		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.channelLeave(this, user);
		}
	}

	public void recieveChat(String type, BNetUser user, String text) {
		if(!isPrimaryConnection())
			return;
		
		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.recieveChat(this, type, user, text);
		}
		
		if((text == null) || (text.length() == 0))
			return;
		
		if((text.charAt(0) == getTrigger()) || text.equals("?trigger"))
			parseCommand(user, text.substring(1), GlobalSettings.whisperBack);
	}

	public void recieveEmote(String type, BNetUser user, String text) {
		if(!isPrimaryConnection())
			return;
		
		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.recieveEmote(this, type, user, text);
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
		
		if(GlobalSettings.autoRejoin
		&& (myUser != null)
		&& text.startsWith(myUser.getShortLogonName() + " was kicked out of the channel")) {
			final String oldChannel = channelName;
			recieveError("Will auto-rejoin in 5 seconds...");
			new Thread() {
				@Override
				public void run() {
					try {
						sleep(5000);
						if(channelName.equalsIgnoreCase("The Void"))
							sendJoinChannel(oldChannel);
						else
							recieveError("Auto-rejoin cancelled.");
					} catch(Exception e) {
						Out.exception(e);
					}
				}}.start();
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
	}

	public void whisperSent(String type, BNetUser user, String text) {
		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.whisperSent(this, type, user, text);
		}
	}

	public char getTrigger() {
		return getConnectionSettings().trigger.charAt(0);
	}

	public void whisperRecieved(String type, BNetUser user, String text) {
		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.whisperRecieved(this, type, user, text);
		}

		if((text == null) || (text.length() == 0))
			return;
		if(text.charAt(0) == getTrigger())
			text = text.substring(1);
		parseCommand(user, text, true);
	}
}
