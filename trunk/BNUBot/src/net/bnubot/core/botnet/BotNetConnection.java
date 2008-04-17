/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.botnet;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;

import net.bnubot.core.Connection;
import net.bnubot.core.EventHandler;
import net.bnubot.core.Profile;
import net.bnubot.core.UnsupportedFeatureException;
import net.bnubot.core.bncs.BNCSConnection;
import net.bnubot.core.bncs.ProductIDs;
import net.bnubot.settings.ConnectionSettings;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetInputStream;
import net.bnubot.util.BNetUser;
import net.bnubot.util.HexDump;
import net.bnubot.util.MirrorSelector;
import net.bnubot.util.Out;
import net.bnubot.util.UserProfile;
import net.bnubot.util.task.Task;

/**
 * @author sanderson
 *
 */
public class BotNetConnection extends Connection {
	private BNCSConnection master;
	
	private HashMap<Integer, BotNetUser> users = new HashMap<Integer, BotNetUser>();
	
	private InputStream bnInputStream = null;
	private DataOutputStream bnOutputStream = null;

	private int botNetServerRevision = 0;
	private int botNetCommunicationRevision = 0;

	public BotNetConnection(BNCSConnection master, ConnectionSettings cs, Profile p) {
		super(cs, p);
		this.master = master;
	}
	
	@Override
	protected String getServer() {
		return GlobalSettings.botNetServer;
	}

	@Override
	protected int getPort() {
		return GlobalSettings.botNetPort;
	}
	
	protected void initializeConnection(Task connect) throws Exception {
		botNetServerRevision = 0;
		botNetCommunicationRevision = 0;
		
		// Set up BotNet
		connect.updateProgress("Connecting to BotNet");
		int port = getPort();
		InetAddress address = MirrorSelector.getClosestMirror(getServer(), port);
		recieveInfo("Connecting to " + address + ":" + port + ".");
		socket = new Socket(address, port);
		socket.setKeepAlive(true);
		bnInputStream = socket.getInputStream();
		bnOutputStream = new DataOutputStream(socket.getOutputStream());
		
		// Connected
		connectionState = ConnectionState.CONNECTED;
		connect.updateProgress("Connected");
	}

	protected boolean sendLoginPackets(Task connect) throws Exception {
		sendLogon("RivalBot", "b8f9b319f223ddcc38");
		
		while(isConnected() && !socket.isClosed() && !disposed) {
			if(bnInputStream.available() > 0) {
				BotNetPacketReader pr = new BotNetPacketReader(bnInputStream);
				BNetInputStream is = pr.getInputStream();
				
				switch(pr.packetId) {
				case PACKET_BOTNETVERSION: {
					botNetServerRevision = is.readDWord();
					recieveInfo("BotNet server version is " + botNetServerRevision);
					sendBotNetVersion(1, 1);
					break;
				}
				case PACKET_LOGON: {
					int result = is.readDWord();
					switch(result) {
					case 0:
						recieveError("Logon failed!");
						disconnect(false);
						return false;
					case 1:
						recieveInfo("Logon success!");
						return true;
					default:
						recieveError("Unknown PACKET_LOGON result 0x" + Integer.toHexString(result));
						disconnect(false);
						return false;
					}
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

	protected void connectedLoop() throws Exception {
		sendStatusUpdate();
		sendUserInfo();
		boolean userInit = true;
		
		while(isConnected() && !socket.isClosed() && !disposed) {
			if(bnInputStream.available() > 0) {
				BotNetPacketReader pr = new BotNetPacketReader(bnInputStream);
				BNetInputStream is = pr.getInputStream();
				
				switch(pr.packetId) {
				case PACKET_CHANGEDBPASSWORD: {
					// Server is acknowledging the communication version
					botNetCommunicationRevision = is.readDWord();
					recieveInfo("BotNet communication version is " + botNetCommunicationRevision);
					break;
				}
				case PACKET_IDLE: {
					sendIdle();
					break;
				}
				case PACKET_STATSUPDATE: {
					int result = is.readDWord();
					switch(result) {
					case 0:
						recieveError("Status update failed");
						break;
					case 1:
						// Success
						break;
					default:
						recieveError("Unknown PACKET_LOGON result 0x" + Integer.toHexString(result));
						disconnect(false);
						return;
					}
					break;
				}
				case PACKET_USERINFO: {
					if(pr.data.length == 0) {
						userInit = false;
						break;
					}
					
					BotNetUser user = new BotNetUser();
					user.number = is.readDWord();
					if(botNetServerRevision >= 4) {
						user.dbflag = is.readDWord();
						user.ztff = is.readDWord();
					}
					user.name = is.readNTString();
					user.channel = is.readNTString();
					user.server = is.readDWord();
					if(botNetServerRevision >= 2)
						user.account = is.readNTString();
					if(botNetServerRevision >= 3)
						user.database = is.readNTString();
					

					if(userInit)
						botnetUserOnline(user);
					else
						botnetUserStatus(user);
					//recieveInfo(user.toStringEx());
					break;
				}
				case PACKET_USERLOGGINGOFF: {
					int number = is.readDWord();
					botnetUserLogoff(number);
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

	public ProductIDs getProductID() {
		return ProductIDs.CHAT;
	}

	public boolean isOp() {
		return false;
	}
	
	@Override
	public void bnetConnected() {
		users.clear();
		
		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.botnetConnected(this);
		}
	}
	
	@Override
	public void bnetDisconnected() {
		users.clear();
		myUser = null;

		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.botnetDisconnected(this);
		}
	}

	public void botnetUserOnline(BotNetUser user) {
		users.put(user.number, user);
		
		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.botnetUserOnline(this, user);
		}
	}

	public void botnetUserStatus(BotNetUser user) {
		users.put(user.number, user);
		
		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.botnetUserStatus(this, user);
		}
	}
	
	private void botnetUserLogoff(int number) {
		BotNetUser user = users.remove(number);
		
		synchronized(eventHandlers) {
			for(EventHandler eh : eventHandlers)
				eh.botnetUserLogoff(this, user);
		}
	}
	
	/**
	 * Send PACKET_LOGON
	 * @param user
	 * @param pass
	 * @throws Exception
	 */
	private void sendLogon(String user, String pass) throws Exception {
		BotNetPacket p = new BotNetPacket(BotNetPacketId.PACKET_LOGON);
		p.writeNTString(user);
		p.writeNTString(pass);
		p.SendPacket(bnOutputStream);
	}
	
	/**
	 * Send PACKET_BOTNETVERSION
	 * @param x
	 * @param y
	 * @throws Exception
	 */
	private void sendBotNetVersion(int x, int y) throws Exception {
		BotNetPacket p = new BotNetPacket(BotNetPacketId.PACKET_BOTNETVERSION);
		p.writeDWord(x);
		p.writeDWord(y);
		p.SendPacket(bnOutputStream);
	}
	
	/**
	 * Send PACKET_IDLE
	 * @throws Exception
	 */
	private void sendIdle() throws Exception {
		BotNetPacket p = new BotNetPacket(BotNetPacketId.PACKET_IDLE);
		p.SendPacket(bnOutputStream);
	}
	
	/**
	 * Send PACKET_STATUSUPDATE
	 * @throws Exception
	 */
	public void sendStatusUpdate() throws Exception {
		BNetUser user = master.getMyUser();
		String channel = master.getChannel();
		int ip = -1;
		if(channel == null)
			channel = "<Not Logged On>";
		else
			ip = master.getIp();
		
		sendStatusUpdate(
				(user == null) ? "BNUBot2" : user.getShortLogonName(),
				channel,
				ip,
				"PubEternalChat",
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
		BotNetPacket p = new BotNetPacket(BotNetPacketId.PACKET_STATSUPDATE);
		p.writeNTString(username);
		p.writeNTString(channel);
		p.writeDWord(ip); // bnet ip address
		p.writeNTString(database); // database
		p.writeDWord(cycling ? 1 : 0); // cycling?
		p.SendPacket(bnOutputStream);
	}
	
	/**
	 * Send PACKET_USERINFO
	 * @throws Exception
	 */
	public void sendUserInfo() throws Exception {
		BotNetPacket p = new BotNetPacket(BotNetPacketId.PACKET_USERINFO);
		p.SendPacket(bnOutputStream);
	}

	public void sendClanInvitation(Object cookie, String user) throws Exception { throw new UnsupportedFeatureException(null); }
	public void sendClanMOTD(Object cookie) throws Exception { throw new UnsupportedFeatureException(null); }
	public void sendClanRankChange(Object cookie, String user, int newRank)throws Exception { throw new UnsupportedFeatureException(null); }
	public void sendClanSetMOTD(String text) throws Exception { throw new UnsupportedFeatureException(null); }
	public void sendJoinChannel(String channel) throws Exception { throw new UnsupportedFeatureException(null); }
	public void sendJoinChannel2(String channel) throws Exception { throw new UnsupportedFeatureException(null); }
	public void sendLeaveChat() throws Exception { throw new UnsupportedFeatureException(null); }
	public void sendLogonRealmEx(String realmTitle) throws Exception { throw new UnsupportedFeatureException(null); }
	public void sendQueryRealms2() throws Exception { throw new UnsupportedFeatureException(null); }
	public void sendReadUserData(String user) throws Exception { throw new UnsupportedFeatureException(null); }
	public void sendWriteUserData(UserProfile profile) throws Exception { throw new UnsupportedFeatureException(null); }
}
