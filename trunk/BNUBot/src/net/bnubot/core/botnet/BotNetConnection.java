/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.botnet;

import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;

import net.bnubot.core.Connection;
import net.bnubot.core.EventHandler;
import net.bnubot.core.Profile;
import net.bnubot.core.bncs.BNCSConnection;
import net.bnubot.core.bncs.ProductIDs;
import net.bnubot.logging.Out;
import net.bnubot.settings.ConnectionSettings;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetInputStream;
import net.bnubot.util.BNetUser;
import net.bnubot.util.ByteArray;
import net.bnubot.util.MirrorSelector;
import net.bnubot.util.crypto.GenericCrypto;
import net.bnubot.util.crypto.HexDump;
import net.bnubot.util.task.Task;

/**
 * @author scotta
 */
public class BotNetConnection extends Connection {
	private static final String BOTNET_TYPE = "BotNet";

	private BNCSConnection master;

	private HashMap<Integer, BotNetUser> users = new HashMap<Integer, BotNetUser>();
	private boolean userInit = false;

	private BNetInputStream bnInputStream = null;
	private DataOutputStream bnOutputStream = null;

	private int serverRevision = 0;
	private int communicationRevision = 0;

	public BotNetConnection(BNCSConnection master, ConnectionSettings cs, Profile p) {
		super(cs, p);
		this.master = master;
	}

	@Override
	public String getDisplayType() {
		return BOTNET_TYPE;
	}

	@Override
	public String getServerType() {
		return BOTNET_TYPE;
	}

	@Override
	protected boolean isPrimaryConnection() {
		return true;
	}

	@Override
	protected String getServer() {
		return GlobalSettings.botNetServer;
	}

	@Override
	protected int getPort() {
		return GlobalSettings.botNetPort;
	}

	@Override
	protected void initializeConnection(Task connect) throws Exception {
		serverRevision = 0;
		communicationRevision = 0;

		// Set up BotNet
		connect.updateProgress("Connecting to BotNet");
		int port = getPort();
		InetAddress address = MirrorSelector.getClosestMirror(getServer(), port);
		dispatchRecieveInfo("Connecting to " + address + ":" + port + ".");
		socket = new Socket(address, port);
		socket.setKeepAlive(true);
		bnInputStream = new BNetInputStream(socket.getInputStream());
		bnOutputStream = new DataOutputStream(socket.getOutputStream());

		// Connected
		connect.updateProgress("Connected");
	}

	@Override
	protected boolean sendLoginPackets(Task connect) throws Exception {
		//sendLogon("RivalBot", "b8f9b319f223ddcc38");
		sendLogon("EternalChat", "das93kajfdsklah3");

		boolean loggedon = false;

		while(isConnected() && !socket.isClosed() && !disposed) {
			if(bnInputStream.available() > 0) {
				BotNetPacketReader pr = new BotNetPacketReader(bnInputStream);
				BNetInputStream is = pr.getData();

				eventHandlers.clear();
				eventHandlers.addAll(master.getEventHandlers());

				switch(pr.packetId) {
				case PACKET_BOTNETVERSION: {
					serverRevision = is.readDWord();
					Out.debug(getClass(), "BotNet server version is " + serverRevision);
					sendBotNetVersion(1, 1);
					break;
				}
				case PACKET_LOGON: {
					int result = is.readDWord();
					switch(result) {
					case 0:
						dispatchRecieveError("Logon failed!");
						disconnect(ConnectionState.LONG_PAUSE_BEFORE_CONNECT);
						return false;
					case 1:
						dispatchRecieveInfo("Logon success!");
						loggedon = true;
						if(communicationRevision != 0)
							return true;
						break;
					default:
						dispatchRecieveError("Unknown PACKET_LOGON result 0x" + Integer.toHexString(result));
						disconnect(ConnectionState.LONG_PAUSE_BEFORE_CONNECT);
						return false;
					}
					break;
				}
				case PACKET_CHANGEDBPASSWORD: {
					// Server is acknowledging the communication version
					communicationRevision = is.readDWord();
					Out.debug(getClass(), "BotNet communication version is " + communicationRevision);
					if(loggedon)
						return true;
					break;
				}
				default:
					Out.debugAlways(getClass(), "Unexpected packet " + pr.packetId.name() + "\n" + HexDump.hexDump(pr.data));
					break;
				}
			} else {
				sleep(200);
				yield();
			}
		}

		return false;
	}

	@Override
	protected void connectedLoop() throws Exception {
		{
			String user = GlobalSettings.botNetUsername;
			String pass = GlobalSettings.botNetPassword;
			if((user != null) && (pass != null) && (user.length() > 0) && (pass.length() > 0))
				sendAccount(0, user, pass, null);
		}
		sendStatusUpdate();
		sendUserInfo();

		while(isConnected() && !socket.isClosed() && !disposed) {
			if(bnInputStream.available() > 0) {
				BotNetPacketReader pr = new BotNetPacketReader(bnInputStream);
				BNetInputStream is = pr.getData();

				eventHandlers.clear();
				eventHandlers.addAll(master.getEventHandlers());

				switch(pr.packetId) {
				case PACKET_IDLE: {
					sendIdle();
					break;
				}
				case PACKET_STATSUPDATE: {
					int result = is.readDWord();
					switch(result) {
					case 0:
						dispatchRecieveError("Status update failed");
						break;
					case 1:
						// Success
						break;
					default:
						dispatchRecieveError("Unknown PACKET_LOGON result 0x" + Integer.toHexString(result));
						disconnect(ConnectionState.LONG_PAUSE_BEFORE_CONNECT);
						return;
					}
					break;
				}
				case PACKET_ACCOUNT: {
					int command = is.readDWord();
					int result = is.readDWord();
					switch(result) {
					case 0:
						switch(command) {
						case 0:
							dispatchRecieveError("Account logon failed");
							break;
						case 1:
							dispatchRecieveError("Password change failed");
							break;
						case 2:
							dispatchRecieveError("Account create failed");
							break;
						default:
							dispatchRecieveError("Unknown PACKET_ACCOUNT command 0x" + Integer.toHexString(command));
							break;
						}
						dispatchRecieveError("Status update failed");
						break;
					case 1:
						// Success
						break;
					default:
						dispatchRecieveError("Unknown PACKET_ACCOUNT result 0x" + Integer.toHexString(result));
						disconnect(ConnectionState.LONG_PAUSE_BEFORE_CONNECT);
						return;
					}
					break;
				}
				case PACKET_USERINFO: {
					if(pr.data.length == 0) {
						userInit = false;
						break;
					}

					int number = is.readDWord();
					int dbflag = 0, ztff = 0;
					if(serverRevision >= 4) {
						dbflag = is.readDWord();
						ztff = is.readDWord();
					}
					String name = is.readNTString();

					BotNetUser user = new BotNetUser(this, number, name);
					user.dbflag = dbflag;
					user.ztff = ztff;

					user.channel = is.readNTString();
					user.server = is.readDWord();
					if(serverRevision >= 2)
						user.account = is.readNTString();
					if(serverRevision >= 3)
						user.database = is.readNTString();

					if(myUser == null)
						myUser = user;

					if(userInit)
						dispatchBotnetUserOnline(user);
					else
						dispatchBotnetUserStatus(user);
					//recieveInfo(user.toStringEx());
					break;
				}
				case PACKET_USERLOGGINGOFF: {
					int number = is.readDWord();
					dispatchBotnetUserLogoff(number);
					break;
				}
				case PACKET_BOTNETCHAT: {
					int command = is.readDWord();
					int action = is.readDWord();
					BotNetUser user = users.get(is.readDWord());
					ByteArray data = new ByteArray(is.readNTBytes());

					switch(command) {
					case 0: //broadcast
						// TODO: change this to recieveBroadcast()
						dispatchRecieveChat(user, data);
						break;
					case 1: // chat
						if(action == 0)
							dispatchRecieveChat(user, data);
						else
							dispatchRecieveEmote(user, data.toString());
						break;
					case 2: //whisper
						dispatchWhisperRecieved(user, data.toString());
						break;
					default:
						dispatchRecieveError("Unknown PACKET_BOTNETCHAT command 0x" + Integer.toHexString(command));
						disconnect(ConnectionState.LONG_PAUSE_BEFORE_CONNECT);
						break;
					}
					break;
				}

				case PACKET_COMMAND: {
					// PROTOCOL VIOLATION!
					int err = is.readDWord();
					byte id = is.readByte();
					int lenOffending = is.readWord();
					int lenUnprocessed = is.readWord();
					dispatchRecieveError("Protocol violation: err=" + err + ", packet=" + BotNetPacketId.values()[id].name() + ", offending packet len=" + lenOffending + ", unprocessed data len=" + lenUnprocessed);
					disconnect(ConnectionState.LONG_PAUSE_BEFORE_CONNECT);
					break;
				}
				default:
					Out.debugAlways(getClass(), "Unexpected packet " + pr.packetId.name() + "\n" + HexDump.hexDump(pr.data));
					break;
				}
			} else {
				sleep(200);
				yield();
			}
		}
	}

	@Override
	public ProductIDs getProductID() {
		return ProductIDs.CHAT;
	}

	@Override
	public boolean isOp() {
		return false;
	}

	public void processCommand(String text) {
		try {
			String[] commands = text.split(" ", 3);
			if(commands[0].equals("whisper")) {
				if(commands.length != 3) {
					dispatchRecieveError("Invalid use of whisper");
					return;
				}

				BotNetUser target = getUser(commands[1]);
				if(target == null) {
					dispatchRecieveError("Invalid whisper target");
					return;
				}

				sendWhisper(target, commands[2]);
				return;
			} else if(commands[0].equals("chat")) {
				sendChat(false, text.substring(5));
				return;
			} else if(commands[0].equals("emote")) {
				sendChat(false, text.substring(6));
				return;
			} else if(commands[0].equals("broadcast")) {
				sendBroadcast(text.substring(10));
				return;
			}

			dispatchRecieveError("Invalid BotNet command: " + text);
		} catch(Exception e) {
			Out.exception(e);
		}
	}

	/**
	 * @param string
	 * @return
	 */
	private BotNetUser getUser(String string) {
		if(string.charAt(0) == '%')
			return users.get(Integer.parseInt(string.substring(1)));
		return null;
	}

	/**
	 * Broadcast text
	 * @param text Text to send
	 * @throws Exception
	 */
	public void sendBroadcast(String text) throws Exception {
		sendBotNetChat(0, false, 0, text);
		super.dispatchRecieveEmote(myUser, "TODO: dispatchRecieveBroadcast()");
	}

	/**
	 * Talk on the database
	 * @param emote True if this is an emote
	 * @param text Text to send
	 * @throws Exception
	 */
	public void sendChat(boolean emote, String text) throws Exception {
		sendBotNetChat(1, emote, 0, text);
		super.dispatchRecieveChat(myUser, new ByteArray(text));
	}

	/**
	 * Send a whisper
	 * @param target User to whisper
	 * @param text Text to send
	 * @throws Exception
	 */
	public void sendWhisper(BotNetUser target, String text) throws Exception {
		sendBotNetChat(2, false, target.number, text);
		super.dispatchWhisperSent(target, text);
	}


	/*
	 * Sending packets
	 *
	 */

	/**
	 * Send PACKET_LOGON
	 * @param user
	 * @param pass
	 * @throws Exception
	 */
	private void sendLogon(String user, String pass) throws Exception {
		BotNetPacket p = new BotNetPacket(this, BotNetPacketId.PACKET_LOGON);
		p.writeNTString(user);
		p.writeNTString(pass);
		p.sendPacket(bnOutputStream);
	}

	/**
	 * Send PACKET_ACCOUNT
	 * @param command 0: login, 1: change password, 2: account create
	 * @param username Username to use
	 * @param password Current password to use
	 * @param newPassword New password (used for command 1 only)
	 * @throws Exception If an error occurred
	 */
	private void sendAccount(int command, String username, String password, String newPassword) throws Exception {
		BotNetPacket p = new BotNetPacket(this, BotNetPacketId.PACKET_ACCOUNT);
		p.writeDWord(command);
		p.writeNTString(username);
		p.writeNTString(password);
		switch(command) {
		case 0: // login
			break;
		case 1: // change password
			p.writeNTString(newPassword);
			break;
		case 2: // account create
			break;
		default:
			throw new IllegalStateException("Unknown PACKET_ACCOUNT command 0x" + Integer.toHexString(command));
		}
		p.sendPacket(bnOutputStream);
	}

	/**
	 * Send PACKET_BOTNETVERSION
	 * @param x
	 * @param y
	 * @throws Exception
	 */
	private void sendBotNetVersion(int x, int y) throws Exception {
		BotNetPacket p = new BotNetPacket(this, BotNetPacketId.PACKET_BOTNETVERSION);
		p.writeDWord(x);
		p.writeDWord(y);
		p.sendPacket(bnOutputStream);
	}

	/**
	 * Send PACKET_IDLE
	 * @throws Exception
	 */
	private void sendIdle() throws Exception {
		BotNetPacket p = new BotNetPacket(this, BotNetPacketId.PACKET_IDLE);
		p.sendPacket(bnOutputStream);
	}

	/**
	 * Send PACKET_STATUSUPDATE
	 * @throws Exception
	 */
	public void sendStatusUpdate() throws Exception {
		if(!isConnected()) {
			connect();
			return;
		}

		BNetUser user = master.getMyUser();
		String channel = master.getChannel();
		int ip = -1;
		if(channel == null)
			channel = "<Not Logged On>";
		else
			ip = master.getIp();

		if((myUser != null) && (myUser instanceof BotNetUser)) {
			BotNetUser me = (BotNetUser)myUser;
			me.name = GlobalSettings.botNetUsername;
			if((me.name == null) || (me.name.length() == 0))
				me.name = "BNUBot2";
			if(user != null)
				me.name = user.getShortLogonName();
			me.channel = channel;
			me.server = ip;
			me.database = GlobalSettings.botNetDatabase;
			dispatchBotnetUserStatus(me);
		}

		sendStatusUpdate(
				(user == null) ? "BNUBot2" : user.getShortLogonName(),
				channel,
				ip,
				GlobalSettings.botNetDatabase + " " + GlobalSettings.botNetDatabasePassword,
				false);
	}

	/**
	 * Send PACKET_STATUSUPDATE
	 * @param username
	 * @param channel
	 * @param ip
	 * @param database
	 * @param cycling
	 * @throws Exception
	 */
	private void sendStatusUpdate(String username, String channel, int ip, String database, boolean cycling) throws Exception {
		BotNetPacket p = new BotNetPacket(this, BotNetPacketId.PACKET_STATSUPDATE);
		p.writeNTString(username);
		p.writeNTString(channel);
		p.writeDWord(ip); // bnet ip address
		p.writeNTString(database); // database
		p.writeDWord(cycling ? 1 : 0); // cycling?
		p.sendPacket(bnOutputStream);
	}

	/**
	 * Send PACKET_USERINFO
	 * @throws Exception
	 */
	public void sendUserInfo() throws Exception {
		BotNetPacket p = new BotNetPacket(this, BotNetPacketId.PACKET_USERINFO);
		p.sendPacket(bnOutputStream);

		userInit = true;
		myUser = null;
	}

	/**
	 * Send PACKET_BOTNETCHAT
	 * @param command 0=broadcast, 1=database chat, 2=whisper
	 * @param emote True if this is an emote
	 * @param target The id of the person to whisper (command 2)
	 * @param text The text to send
	 */
	private void sendBotNetChat(int command, boolean emote, int target, String message) throws Exception {
		if(message.length() > 496)
			throw new IllegalStateException("Chat length too long");

		byte[] crypt = GenericCrypto.encode(new ByteArray(message), master.enabledCryptos).getBytes();

		BotNetPacket p = new BotNetPacket(this, BotNetPacketId.PACKET_BOTNETCHAT);
		p.writeDWord(command);
		p.writeDWord(emote ? 1 : 0);
		p.writeDWord(target);
		p.writeNTString(crypt);
		p.sendPacket(bnOutputStream);
	}

	/*
	 * Event dispatch
	 *
	 */

	@Override
	public void dispatchConnected() {
		users.clear();

		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.botnetConnected(this);
		}
	}

	@Override
	public void dispatchDisconnected() {
		users.clear();
		myUser = null;

		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.botnetDisconnected(this);
		}
	}

	public void dispatchBotnetUserOnline(BotNetUser user) {
		users.put(user.number, user);

		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.botnetUserOnline(this, user);
		}
	}

	public void dispatchBotnetUserStatus(BotNetUser user) {
		users.put(user.number, user);

		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.botnetUserStatus(this, user);
		}
	}

	private void dispatchBotnetUserLogoff(int number) {
		BotNetUser user = users.remove(number);

		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.botnetUserLogoff(this, user);
		}
	}
}
