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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.bnubot.bot.gui.settings.ConfigurationFrame;
import net.bnubot.bot.gui.settings.OperationCancelledException;
import net.bnubot.bot.gui.wizard.NewProfileWizard;
import net.bnubot.core.bncs.BNCSConnection;
import net.bnubot.core.bncs.ProductIDs;
import net.bnubot.core.botnet.BotNetConnection;
import net.bnubot.db.conf.DatabaseContext;
import net.bnubot.logging.Out;
import net.bnubot.logging.OutputHandler;
import net.bnubot.settings.ConnectionSettings;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetInputStream;
import net.bnubot.util.BNetUser;
import net.bnubot.util.ByteArray;
import net.bnubot.util.MirrorSelector;
import net.bnubot.util.TimeFormatter;
import net.bnubot.util.UserProfile;
import net.bnubot.util.Wildcard;
import net.bnubot.util.crypto.GenericCrypto;
import net.bnubot.util.music.MusicController;
import net.bnubot.util.music.MusicControllerFactory;
import net.bnubot.util.task.Task;
import net.bnubot.util.task.TaskManager;
import net.bnubot.vercheck.CurrentVersion;

/**
 * @author scotta
 */
public abstract class Connection extends Thread implements OutputHandler {
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
				// Wait 1 minute
				return timeWaiting > (1000l * 60l);
			}
			return false;
		}
	}

	protected Socket socket = null;

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
	public int enabledCryptos = 0;

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

	protected void clearTasks() {
		for(Task t : currentTasks)
			t.complete();
		currentTasks.clear();
	}

	/**
	 * Create a socket, and if necessary, connect via SOCKS4
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	protected Socket makeSocket(String address, int port) throws UnknownHostException, IOException {
		Socket s;
		if(GlobalSettings.socksEnabled) {
			InetAddress s4_addr = MirrorSelector.getClosestMirror(GlobalSettings.socksHost, GlobalSettings.socksPort);
			int s4_port = GlobalSettings.socksPort;

			switch(GlobalSettings.socksType) {
			case SOCKS4:
				InetAddress addr = MirrorSelector.getClosestMirror(address, port);
				dispatchRecieveInfo("Connecting to " + addr + ":" + port + " via SOCKS4 proxy " + s4_addr + ":" + s4_port + ".");
				s = new SOCKS4ProxySocket(
					s4_addr, s4_port,
					addr, port);
				break;
			case SOCKS4a:
				dispatchRecieveInfo("Connecting to " + address + ":" + port + " via SOCKS4a proxy " + s4_addr + ":" + s4_port + ".");
				s = new SOCKS4ProxySocket(
					s4_addr, s4_port,
					address, port);
				break;
			default:
				throw new IOException("Unknown SOCKS type: " + GlobalSettings.socksType);
			}
		} else {
			InetAddress addr = MirrorSelector.getClosestMirror(address, port);
			dispatchRecieveInfo("Connecting to " + addr + ":" + port + ".");
			s = new Socket(addr, port);
		}
		s.setKeepAlive(true);
		return s;
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
				clearTasks();
				myUser = null;
				dispatchTitleChanged();

				if(!connectionState.canConnect(0)) {
					Out.debug(getClass(), "Pausing to meet connection criteria for " + connectionState.toString());

					// Wait until we're supposed to connect
					long waitStartTime = System.currentTimeMillis();
					while(!connectionState.canConnect(System.currentTimeMillis() - waitStartTime)) {
						yield();
						sleep(200);
					}
				}

				connectionState = ConnectionState.CONNECTING;
				dispatchTitleChanged();
				Task connect = createTask("Connecting to " + getServer() + ":" + getPort(), "Verify connection settings validity");

				if(!cs.isInitialized()) {
					if(PluginManager.getEnableGui()) {
						new NewProfileWizard(cs).displayAndBlock();
					}
				}

				// Check if CS is valid
				while(cs.isValid() != null) {
					if(PluginManager.getEnableGui()) {
						new ConfigurationFrame(cs);
					} else {
						// TODO: handle this somehow
						throw new RuntimeException("Invalid configuration in bot #" + cs.botNum + " " + cs.isValid());
					}
				}

				// Wait a short time before allowing a reconnect
				waitUntilConnectionSafe(connect);
				if(!isConnecting())
					continue;

				// Double-check if disposal occured
				if(disposed)
					break;

				// Initialize connection to DT server
				initializeConnection(connect);
				if(!isConnecting())
					continue;
				connectionState = ConnectionState.CONNECTED;
				dispatchTitleChanged();

				// Log in
				boolean loggedIn = sendLoginPackets(connect);
				if(!isConnected())
					continue;

				// Connection established
				completeTask(connect);

				lastNullPacket = System.currentTimeMillis();
				profile.lastAntiIdle = lastNullPacket;

				if(loggedIn)
					connectedLoop();

				// Connection closed gracefully
				clearTasks();
				// If we're already in a disconnected state, disconnect() has no effect
				disconnect(ConnectionState.ALLOW_CONNECT);
				if(connectionState == ConnectionState.ALLOW_CONNECT) {
					// Disconnect which did not cause an error; wait a minimum of five seconds before reconnecting
					try {
						sleep(5000);
					} catch (Exception e) {}
				}
			} catch(OperationCancelledException e) {
				// Dispose the connection
				disposed = true;
				disconnect(ConnectionState.LONG_PAUSE_BEFORE_CONNECT);
			} catch(UnknownHostException e) {
				dispatchRecieveError("Unknown host: " + e.getMessage());
				disconnect(ConnectionState.LONG_PAUSE_BEFORE_CONNECT);
			} catch(IOException e) {
				dispatchRecieveError(e.getMessage());
				disconnect(ConnectionState.LONG_PAUSE_BEFORE_CONNECT);
			} catch(Exception e) {
				dispatchRecieveError("Unhandled " + e.getClass().getSimpleName() + ": " + e.getMessage());
				e.printStackTrace();
				Out.exception(e);
				disconnect(ConnectionState.LONG_PAUSE_BEFORE_CONNECT);
			}
		}

		clearTasks();

		getProfile().dispose();
	}

	protected abstract void connectedLoop() throws Exception;
	protected abstract boolean sendLoginPackets(Task connect) throws Exception;
	protected abstract void initializeConnection(Task connect) throws Exception;

	private static Map<String, Long> connectionTimes = new HashMap<String, Long>();
	/**
	 * Wait until it is safe to connect to the server
	 */
	protected void waitUntilConnectionSafe(Task connect) {
		long waitUntil;
		long totalTime;
		String server = getServer();
		synchronized(connectionTimes) {
			long now = System.currentTimeMillis();

			Long lastConnectionTime = connectionTimes.get(server);
			if(lastConnectionTime == null) {
				connectionTimes.put(server, now);
				return;
			}

			waitUntil = lastConnectionTime + 15000;
			totalTime = waitUntil - now;
			connectionTimes.put(server, waitUntil);
		}

		connect.setDeterminate((int)totalTime, "ms");

		final String status = "Stalling to avoid flood";
		while(!disposed) {
			long timeLeft = waitUntil - System.currentTimeMillis();
			if(timeLeft <= 0)
				break;

			if(!connectionState.equals(ConnectionState.CONNECTING))
				break;

			connect.setProgress((int) (totalTime - timeLeft), status);

			try { sleep(100); } catch (InterruptedException e1) {}
			yield();
		}

		connect.setIndeterminate();
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
	 * @param containing the string to look for in username
	 * @return a list of users according to TC rules
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
	 * @param pattern the wildcard-enabled search string
	 * @param perspective whose perspective to search from
	 * @return a list of the matched users
	 */
	public Collection<BNetUser> findUsersWildcard(String pattern, BNetUser perspective) {
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

	public void sendFriendsList() throws Exception { throw new UnsupportedFeatureException(null); }

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

	public void removeEventHandler(EventHandler e) {
		synchronized(eventHandlers) {
			e.disable(this);
			eventHandlers.remove(e);
		}
	}

	public boolean isConnected() {
		return (connectionState == ConnectionState.CONNECTED);
	}

	private boolean isConnecting() {
		return (connectionState == ConnectionState.CONNECTING);
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
			// Allow change to any state when in a connected state
			break;
		default:
			// Already disconnected; only accept FORCE_CONNECT (user-initiated)
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

	protected void sendChatCommand(ByteArray text) {
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

	/**
	 * Helper function to send chat to battle.net
	 * @param prefix A String to prepend each time the text is split.
	 * <p><code>"/w *BNU-Camel@Azeroth "</code></p>
	 * @param text The whole text to be sent out
	 * @param allowCommands Enables internal bot commands (/cmd, /profile, etc)
	 * @param enableKeywords Enable keywords (%uptime%, %trigger%, %mp3%, etc)
	 * @param priority the priority to send to the ChatQueue
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
				if(command[0].length() == 0)
					break ALLOWCOMMANDS;
				switch(command[0].charAt(0)) {
				case '/':
					Profile.internalParseCommand(this, postSlash.substring(1), false);
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
							Profile.internalParseCommand(this, command[1], true);
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
					if(Profile.internalParseCommand(this, postSlash, true))
						return;
				}
			}
		} // if(allowCommands)

		if(!isConnected()) {
			dispatchRecieveError("Can not send chat: " + text);
			return;
		}

		text = cleanText(text, enableKeywords);

		if(prefix == null) {
			// Detect /w user and /f m, and make that the prefix
			if(text.startsWith("/w ")) {
				int i = text.indexOf(' ', 4) + 1;
				if(i != 0) {
					prefix = text.substring(0, i);
					text = text.substring(i);
				}
			} else if(text.startsWith("/f m ")) {
				prefix = "/f m ";
				text = text.substring(5);
			}
		}

		if(text.length() == 0)
			return;

		ByteArray baPrefix = null;
		if(prefix != null)
			baPrefix = new ByteArray(prefix);
		enqueueChat(baPrefix, text, priority);
	}

	public static final int MAX_CHAT_LENGTH = 200;
	/**
	 * @param prefix Prepend this before each line of text.
	 * <p><code>new ByteArray("/w *BNU-Camel@Azeroth ")</code></p>
	 * @param text The string to split up and send; unicode is okay.
	 * <p><code>"Hello!"</code></p>
	 * @param priority
	 */
	private void enqueueChat(ByteArray prefix, String text, int priority) {
		//Split up the text in to appropriate sized pieces
		int pieceSize = MAX_CHAT_LENGTH;
		if(prefix != null)
			pieceSize -= prefix.length();
		if(enabledCryptos != 0) {
			if((enabledCryptos & GenericCrypto.CRYPTO_REVERSE) != 0)
				pieceSize--; // Reverse has a prefix
			if((enabledCryptos & GenericCrypto.CRYPTO_MC) != 0)
				pieceSize--; // MC has a prefix
			if((enabledCryptos & GenericCrypto.CRYPTO_DM) != 0)
				pieceSize = (pieceSize - 1) / 2; // DM doubles in size and has a prefix
			if((enabledCryptos & GenericCrypto.CRYPTO_HEX) != 0)
				pieceSize = (pieceSize - 1) / 2; // Hex doubles in size and has a prefix
			if((enabledCryptos & GenericCrypto.CRYPTO_BASE64) != 0)
				pieceSize = (pieceSize - 1) * 3 / 4; // B64 increases 33% and has a prefix
		}

		enqueueChat(prefix, text, priority, pieceSize, 0);
	}

	/**
	 * @param prefix Prepend this before each line of text.
	 * <p><code>new ByteArray("/w *BNU-Camel@Azeroth ")</code></p>
	 * @param text The string to split up and send; unicode is okay.
	 * <p><code>"Hello!"</code></p>
	 * @param priority
	 * @param pieceSize
	 * @param cq
	 * @param current_position
	 */
	private void enqueueChat(ByteArray prefix, String text, int priority, int pieceSize, int current_position) {
		// Count the number of unicode characters removed from text
		int chars_pulled = pieceSize;
		// Count the number of characters remaining from the current position
		int chars_left = text.length() - current_position;
		if(chars_pulled > chars_left)
			chars_pulled = chars_left;

		// Determine whether to prepend ellipsies
		boolean firstPiece = (current_position == 0);

		// This is the pieceSize accomadating for the unprepended ellipsis
		int maxByteCount = (firstPiece ? pieceSize : pieceSize - 3);

		ByteArray piece = new ByteArray(text.substring(current_position, current_position + chars_pulled));
		while(piece.length() > maxByteCount) {
			// Estimate that we need to remove half the overflow, since most unicode chars are 2-bytes
			int deltaEstimate = (piece.length() - pieceSize + 1) / 2;
			// Don't remove less than 1 character
			if(deltaEstimate < 1)
				deltaEstimate = 1;
			Out.debug(getClass(), "deltaEstimate=" + deltaEstimate);
			chars_pulled -= deltaEstimate;
			piece = new ByteArray(text.substring(current_position, current_position + chars_pulled));
		}

		boolean lastPiece = (current_position + chars_pulled >= text.length());
		if(!lastPiece) {
			// This is not the last piece; append ellipsis
			chars_pulled -= 3;
			piece = new ByteArray(text.substring(current_position, current_position + chars_pulled)).concat("...".getBytes());
		}

		if(!firstPiece) {
			// This is not the first piece; prepend ellipsis
			piece = new ByteArray("...".getBytes()).concat(piece);
		}

		// Cryptos
		if(enabledCryptos != 0)
			piece = GenericCrypto.encode(piece, enabledCryptos);

		// Prepend the prefix
		if(prefix != null)
			piece = prefix.concat(piece);

		profile.getChatQueue().enqueue(this, piece, priority);
		if(!lastPiece)
			enqueueChat(prefix, text, priority, pieceSize, current_position + chars_pulled);
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

	protected void dispatchEnterChat(BNetUser user) {
		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.enterChat(this, user);
		}
	}

	private void dispatchParseCommand(BNetUser user, String command, boolean whisperBack) {
		if(DatabaseContext.getContext() == null)
			return;

		int i = command.indexOf(';');
		if(i != -1) {
			String c1 = command.substring(0, i);
			String c2 = command.substring(i + 1);
			while((c2.length() > 0) && (c2.charAt(0) == ' '))
				c2 = c2.substring(1);

			Profile.parseCommand(this, user, c1, whisperBack);
			dispatchParseCommand(user, c2, whisperBack);
			return;
		}

		Profile.parseCommand(this, user, command, whisperBack);
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

	protected void dispatchRecieveChat(BNetUser user, ByteArray data) {
		if(!isPrimaryConnection())
			return;

		// Try to remove encryption
		try {
			data = GenericCrypto.decode(data);
		} catch(Exception e) {
			Out.exception(e);
		}

		// Convert to string
		String text = data.toString();
		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.recieveChat(this, user, text);
		}

		if(text.length() == 0)
			return;

		if((text.charAt(0) == getTrigger()) || text.equalsIgnoreCase("?trigger"))
			dispatchParseCommand(user, text.substring(1), GlobalSettings.whisperBack);
	}

	protected void dispatchRecieveBroadcast(String username, int flags, String text) {
		if(!isPrimaryConnection())
			return;

		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.recieveBroadcast(this, username, flags, text);
		}
	}

	protected void dispatchRecieveEmote(BNetUser user, String text) {
		if(!isPrimaryConnection())
			return;

		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.recieveEmote(this, user, text);
		}
	}

	@Override
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

	@Override
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

	@Override
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
