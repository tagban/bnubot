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
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import net.bnubot.bot.gui.settings.ConfigurationFrame;
import net.bnubot.bot.gui.settings.OperationCancelledException;
import net.bnubot.core.bncs.BNCSConnection;
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
import net.bnubot.util.crypto.GenericCrypto;
import net.bnubot.util.music.MusicController;
import net.bnubot.util.music.MusicControllerFactory;
import net.bnubot.util.task.Task;
import net.bnubot.util.task.TaskManager;
import net.bnubot.vercheck.CurrentVersion;

public abstract class Connection extends Thread {
	public enum ConnectionState {
		DO_NOT_ALLOW_CONNECT,
		LONG_PAUSE_BEFORE_CONNECT,
		ALLOW_CONNECT,
		FORCE_CONNECT,
		CONNECTING,
		CONNECTED;

		public boolean canConnect(long timeWaiting) {
			switch(this) {
			case FORCE_CONNECT:
				return true;
			case ALLOW_CONNECT:
				return GlobalSettings.autoConnect;
			case LONG_PAUSE_BEFORE_CONNECT:
				return timeWaiting > (1000l * 60l);
			}
			return false;
		}
	}

	protected Socket socket = null;
	protected Socket bnlsSocket = null;

	protected ConnectionSettings cs;
	protected Profile profile;
	protected final Collection<EventHandler> eventHandlers = new ArrayList<EventHandler>();
	protected final Collection<BNetUser> users = new LinkedList<BNetUser>();
	protected BNetUser myUser = null;
	protected ConnectionState connectionState = ConnectionState.ALLOW_CONNECT;
	protected String channelName = null;
	protected int channelFlags = 0;
	protected boolean forceReconnect = false;
	protected boolean initialized = false;
	protected boolean disposed = false;

	protected long lastNullPacket;

	protected AcceptOrDecline lastAcceptDecline = null;
	protected int enabledCryptos = 0;

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
	public final void run() {
		// We must initialize the EHs in the Connection thread
		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.initialize(this);
			initialized = true;
		}

		while(!disposed) {
			try {
				for(Task t : currentTasks)
					t.complete();
				currentTasks.clear();
				myUser = null;
				dispatchTitleChanged();

				// Wait until we're supposed to connect
				long waitStartTime = System.currentTimeMillis();
				while(!connectionState.canConnect(System.currentTimeMillis() - waitStartTime)) {
					yield();
					sleep(200);
				}

				connectionState = ConnectionState.CONNECTING;
				dispatchTitleChanged();
				Task connect = createTask("Connecting to " + getServer() + ":" + getPort(), "Verify connection settings validity");

				// Check if CS is valid
				while(cs.isValid() != null)
					new ConfigurationFrame(cs);

				// Wait a short time before allowing a reconnect
				waitUntilConnectionSafe(connect);
				if(!connectionState.equals(ConnectionState.CONNECTING))
					continue;

				// Double-check if disposal occured
				if(disposed)
					break;

				// Initialize connection to DT server
				initializeConnection(connect);
				connectionState = ConnectionState.CONNECTED;
				dispatchTitleChanged();

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
				dispatchRecieveError("Unhandled " + e.getClass().getSimpleName() + ": " + e.getMessage());
				Out.exception(e);
			}

			disconnect(ConnectionState.ALLOW_CONNECT);
			try {
				sleep(5000);
			} catch (Exception e) {}
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

			if(!connectionState.equals(ConnectionState.CONNECTING))
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

	/**
	 * It is imperative that you synchronize on this collection<br>
	 * <code>
	 * Collection<EventHandler> ehs = [Connection].getEventHandlers();
	 * synchronized(ehs) {
	 * 		...
	 * }
	 * </code>
	 * @return A synchronized Collection<EventHandler>
	 */
	public Collection<EventHandler> getEventHandlers() {
		return eventHandlers;
	}

	public Collection<BNetUser> getUsers() {
		return users;
	}

	public List<BNetUser> getSortedUsers() {
		List<BNetUser> x = new ArrayList<BNetUser>(getUsers());
		ChannelListPriority.sort(x);
		return x;
	}

	private BNetUser getUser(BNetUser u) {
		for(BNetUser user : users)
			if(user.equals(u))
				return user;
		return null;
	}

	private void checkAddUser(BNetUser user) {
		if(getUser(user) == null)
			users.add(user);
	}

	private void removeUser(BNetUser user) {
		if(!users.remove(user))
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
		if(myUser != null)
			return myUser.getFullLogonName();

		if(cs.isValid() == null)
			return cs.username + "@" + cs.myRealm + " " + connectionState.name();

		return profile.getName();
	}

	public void sendLeaveChat() throws Exception { throw new UnsupportedFeatureException(null); }
	public void sendJoinChannel(String channel) throws Exception { throw new UnsupportedFeatureException(null); }
	public void sendJoinChannel2(String channel) throws Exception { throw new UnsupportedFeatureException(null); }
	public void sendQueryRealms2() throws Exception { throw new UnsupportedFeatureException(null); }
	public void sendLogonRealmEx(String realmTitle) throws Exception { throw new UnsupportedFeatureException(null); }

	public void sendClanFindCandidates(Object cookie, int clanTag) throws Exception { throw new UnsupportedFeatureException(null); }
	public void sendClanInviteMultiple(Object cookie, String clanName, int clanTag, List<String> invitees) throws Exception { throw new UnsupportedFeatureException(null); }
	public void sendClanCreationInvitation(int cookie, int clanTag, String inviter, int response) throws Exception { throw new UnsupportedFeatureException(null); }
	public void sendClanInvitation(Object cookie, String user) throws Exception { throw new UnsupportedFeatureException(null); }

	public void sendClanRankChange(Object cookie, String user, int newRank) throws Exception { throw new UnsupportedFeatureException(null); }
	public void sendClanMOTD(Object cookie) throws Exception { throw new UnsupportedFeatureException(null); }
	public void sendClanSetMOTD(String text) throws Exception { throw new UnsupportedFeatureException(null); }

	public void sendReadUserData(String user) throws Exception { throw new UnsupportedFeatureException(null); }
	public void sendWriteUserData(UserProfile profile) throws Exception { throw new UnsupportedFeatureException(null); }

	public void sendProfile(BNetUser user) throws Exception {
		sendReadUserData(user.getFullAccountName());
	}

	public abstract boolean isOp();
	public abstract ProductIDs getProductID();
	public String getDisplayType() {
		return null;
	}
	public abstract String getServerType();

	public void addEventHandler(EventHandler e) {
		synchronized(eventHandlers) {
			eventHandlers.add(e);
			if(initialized)
				e.initialize(this);
		}
	}

	public boolean isConnected() {
		return (connectionState == ConnectionState.CONNECTED);
	}

	private boolean isConnecting() {
		return (connectionState == ConnectionState.CONNECTING);
	}

	public void setBNLSConnected(boolean c) throws IOException {
		if(bnlsSocket != null) {
			bnlsSocket.close();
			bnlsSocket = null;
		}

		if(c) {
			InetAddress address = MirrorSelector.getClosestMirror(GlobalSettings.bnlsServer, GlobalSettings.bnlsPort);
			dispatchRecieveInfo("Connecting to " + address + ":" + GlobalSettings.bnlsPort + ".");
			bnlsSocket = new Socket(address, GlobalSettings.bnlsPort);
			bnlsSocket.setKeepAlive(true);
		}
	}

	public void connect() {
		if(isConnected() || isConnecting())
			return;

		try {
			if(socket != null) {
				socket.close();
				socket = null;
			}

			String v = cs.isValid();
			if(v != null) {
				dispatchRecieveError(v);
				return;
			}
		} catch(IOException e) {
			Out.fatalException(e);
		}

		connectionState = ConnectionState.FORCE_CONNECT;
		dispatchConnected();
	}

	public void reconnect() {
		disconnect(ConnectionState.FORCE_CONNECT);
	}

	public void disconnect(ConnectionState newState) {
		switch(connectionState) {
		case CONNECTED:
		case CONNECTING:
			break;
		default:
			switch(newState) {
			case ALLOW_CONNECT:
			case LONG_PAUSE_BEFORE_CONNECT:
				return;
			}
		}

		connectionState = newState;

		try {
			if(socket != null) {
				socket.close();
				socket = null;
				dispatchDisconnected();
			}
		} catch(IOException e) {
			Out.fatalException(e);
		}
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
		char[] data = text.toCharArray();
		text = "";
		for(char element : data) {
			if(element >= 0x20)
				text += element;
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
	 */
	public void sendChat(String text) {
		sendChat(text, 0);
	}

	/**
	 * Helper function to send chat to battle.net
	 * @param text The whole text to be sent out
	 * @param priority The priority with which to queue the chat
	 */
	public void sendChat(String text, int priority) {
		sendChat(null, text, false, true, priority);
	}

	/**
	 * Helper function to send chat to battle.net, or parse internal commands
	 * such as /cmd, /profile, /botnet, etc
	 * @param text The whole text to be sent out
	 */
	public void sendChatInternal(String text) {
		sendChat(null, text, true, true, 0);
	}

	public static final int MAX_CHAT_LENGTH = 200;
	/**
	 * Helper function to send chat to battle.net
	 * @param prefix A String to prepend each time the text is split (ex: "/w BNU-Camel ")
	 * @param text The whole text to be sent out
	 * @param allowCommands Enables internal bot commands (/cmd, /profile, etc)
	 * @param enableKeywords Enable keywords (%uptime%, %trigger%, %mp3%, etc)
	 */
	public void sendChat(String prefix, String text, boolean allowCommands, boolean enableKeywords, int priority) {
		if(text == null)
			return;
		if(text.length() == 0)
			return;

		ALLOWCOMMANDS: if(allowCommands) {
			if(text.charAt(0) == '/') {
				String postSlash = text.substring(1);
				String[] command = postSlash.split(" ", 2);
				switch(command[0].charAt(0)) {
				case '/':
					if(myUser != null)
						dispatchParseCommand(myUser, postSlash.substring(1), false);
					return;
				case 'a':
					if(command[0].equals("accept"))
						try {
							lastAcceptDecline.accept();
							lastAcceptDecline = null;
							return;
						} catch(Exception e) {}
					break;
				case 'b':
					if(command[0].equals("b64") || command[0].equals("base64")) {
						enabledCryptos ^= GenericCrypto.CRYPTO_BASE64;
						dispatchRecieveInfo("Base64 crypto " + (((enabledCryptos & GenericCrypto.CRYPTO_BASE64) != 0) ? "enabled" : "disabled"));
						return;
					}
					if(command[0].equals("botnet") && (this instanceof BNCSConnection)) {
						BotNetConnection botnet = ((BNCSConnection)this).getBotNet();
						if(botnet != null) {
							botnet.processCommand(text.substring(8));
							return;
						}
					}
					break;
				case 'c':
					if(command[0].equals("cmd")) {
						if(command.length == 2) {
							BNetUser user = myUser;
							if(user == null)
								user = new BNetUser(this, cs.username, cs.getMyRealm());
							dispatchParseCommand(user, command[1], true);
							return;
						}
					}
					break;
				case 'd':
					if(command[0].equals("decline"))
						try {
							lastAcceptDecline.decline();
							lastAcceptDecline = null;
							return;
						} catch(Exception e) {}
					if(command[0].equals("dm")) {
						enabledCryptos ^= GenericCrypto.CRYPTO_DM;
						dispatchRecieveInfo("DM crypto " + (((enabledCryptos & GenericCrypto.CRYPTO_DM) != 0) ? "enabled" : "disabled"));
						return;
					}
					break;
				case 'h':
					if(command[0].equals("hex")) {
						enabledCryptos ^= GenericCrypto.CRYPTO_HEX;
						dispatchRecieveInfo("Hex crypto " + (((enabledCryptos & GenericCrypto.CRYPTO_HEX) != 0) ? "enabled" : "disabled"));
						return;
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
				case 'm':
					if(command[0].equals("mc")) {
						enabledCryptos ^= GenericCrypto.CRYPTO_MC;
						dispatchRecieveInfo("MC crypto " + (((enabledCryptos & GenericCrypto.CRYPTO_MC) != 0) ? "enabled" : "disabled"));
						return;
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
						sendChat(prefix, command[1], false, false, priority);
						return;
					}
					break;
				case 'r':
					if(command[0].equals("reverse")) {
						enabledCryptos ^= GenericCrypto.CRYPTO_REVERSE;
						dispatchRecieveInfo("Reverse crypto " + (((enabledCryptos & GenericCrypto.CRYPTO_REVERSE) != 0) ? "enabled" : "disabled"));
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
					BNetUser user = myUser;
					if(user == null)
						user = new BNetUser(this, cs.username, cs.getMyRealm());
					if(dispatchParseCommand(user, postSlash, true))
						return;
				}
			}
		}

		if(!isConnected()) {
			dispatchRecieveError("Can not send chat: " + text);
			return;
		}

//		try {
//			text = new String(text.getBytes(), "UTF-8");
//		} catch (UnsupportedEncodingException e) {}
		text = cleanText(text, enableKeywords);

		if(text.length() == 0)
			return;

		if(enabledCryptos != 0)
			text = new String(GenericCrypto.encode(text, enabledCryptos));

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


			cq.enqueue(this, piece, priority);
		}
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
		disconnect(ConnectionState.DO_NOT_ALLOW_CONNECT);

		synchronized(eventHandlers) {
			for(EventHandler e : new ArrayList<EventHandler>(eventHandlers)) {
				eventHandlers.remove(e);
				e.disable(this);
			}
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

	public char getTrigger() {
		return getConnectionSettings().trigger.charAt(0);
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
		for(BNetUser u : users)
			if(u.equals(user))
				return u;
		return null;
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
	protected void dispatchConnected() {
		users.clear();

		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.bnetConnected(this);
		}
	}

	protected void dispatchDisconnected() {
		channelName = null;
		channelFlags = 0;
		users.clear();
		myUser = null;

		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.bnetDisconnected(this);
		}
	}

	public boolean dispatchParseCommand(BNetUser user, String command, boolean whisperBack) {
		int i = command.indexOf(';');
		if(i != -1) {
			String c1 = command.substring(0, i);
			String c2 = command.substring(i + 1);
			while((c2.length() > 0) && (c2.charAt(0) == ' '))
				c2 = c2.substring(1);

			boolean ret = dispatchParseCommand(user, c1, whisperBack);
			ret |= dispatchParseCommand(user, c2, whisperBack);
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

	protected void dispatchTitleChanged() {
		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.titleChanged(this);
		}
	}

	protected void dispatchJoinedChannel(String channel, int flags) {
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
			if(c instanceof BotNetConnection)
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

	protected void dispatchChannelUser(BNetUser user) {
		checkAddUser(user);

		if(!isPrimaryConnection())
			return;

		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.channelUser(this, user);
		}
	}

	protected void dispatchChannelJoin(BNetUser user) {
		checkAddUser(user);

		if(!isPrimaryConnection())
			return;

		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.channelJoin(this, user);
		}
	}

	protected void dispatchChannelLeave(BNetUser user) {
		removeUser(user);

		if(!isPrimaryConnection())
			return;

		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.channelLeave(this, user);
		}
	}

	protected void dispatchRecieveChat(BNetUser user, String text) {
		if(!isPrimaryConnection())
			return;

		text = GenericCrypto.decode(text.toCharArray());

		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.recieveChat(this, user, text);
		}

		if((text == null) || (text.length() == 0))
			return;

		if((text.charAt(0) == getTrigger()) || text.equals("?trigger"))
			dispatchParseCommand(user, text.substring(1), GlobalSettings.whisperBack);
	}

	protected void dispatchRecieveEmote(BNetUser user, String text) {
		if(!isPrimaryConnection())
			return;

		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.recieveEmote(this, user, text);
		}
	}

	public void dispatchRecieveDebug(String text) {
		if(text == null)
			return;
		if(text.length() == 0)
			return;

		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.recieveDebug(this, text);
		}
	}

	public void dispatchRecieveServerInfo(String text) {
		if(text == null)
			return;
		if(text.length() == 0)
			return;

		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.recieveServerInfo(this, text);
		}

		if(GlobalSettings.autoRejoin
		&& (myUser != null)
		&& text.startsWith(myUser.getShortLogonName() + " was kicked out of the channel")) {
			final String oldChannel = channelName;
			dispatchRecieveError("Will auto-rejoin in 5 seconds...");
			new Thread() {
				@Override
				public void run() {
					try {
						sleep(5000);
						if(channelName.equalsIgnoreCase("The Void"))
							sendJoinChannel(oldChannel);
						else
							dispatchRecieveError("Auto-rejoin cancelled.");
					} catch(Exception e) {
						Out.exception(e);
					}
				}}.start();
		}
	}

	public void dispatchRecieveInfo(String text) {
		if(text == null)
			return;
		if(text.length() == 0)
			return;

		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.recieveInfo(this, text);
		}
	}

	public void dispatchRecieveServerError(String text) {
		if(text == null)
			return;
		if(text.length() == 0)
			return;

		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.recieveServerError(this, text);
		}
	}

	public void dispatchRecieveError(String text) {
		if(text == null)
			return;
		if(text.length() == 0)
			return;

		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.recieveError(this, text);
		}
	}

	protected void dispatchWhisperSent(BNetUser user, String text) {
		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.whisperSent(this, user, text);
		}
	}

	protected void dispatchWhisperRecieved(BNetUser user, String text) {
		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.whisperRecieved(this, user, text);
		}

		if((text == null) || (text.length() == 0))
			return;
		if(text.charAt(0) == getTrigger())
			text = text.substring(1);
		dispatchParseCommand(user, text, true);
	}
}
