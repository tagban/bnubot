/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.botnet;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;

import net.bnubot.core.Connection;
import net.bnubot.core.Profile;
import net.bnubot.core.UnsupportedFeatureException;
import net.bnubot.core.bncs.ProductIDs;
import net.bnubot.settings.ConnectionSettings;
import net.bnubot.util.BNetInputStream;
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
	private InputStream bnInputStream = null;
	private DataOutputStream bnOutputStream = null;

	private int botNetServerRevision = 0;
	private int botNetCommunicationRevision = 0;
	
	protected void initializeConnection(Task connect) throws Exception {
		botNetServerRevision = 0;
		botNetCommunicationRevision = 0;
		
		// Set up BotNet
		connect.updateProgress("Connecting to BotNet");
		InetAddress address = MirrorSelector.getClosestMirror(cs.server, cs.port);
		recieveInfo("Connecting to " + address + ":" + cs.port + ".");
		socket = new Socket(address, cs.port);
		socket.setKeepAlive(true);
		bnInputStream = socket.getInputStream();
		bnOutputStream = new DataOutputStream(socket.getOutputStream());
		
		// Connected
		connectionState = ConnectionState.CONNECTED;
		connect.updateProgress("Connected");
	}

	protected boolean sendLoginPackets(Task connect) throws Exception {
		// Closed-scope for p
		{
			BotNetPacket p = new BotNetPacket(BotNetPacketId.PACKET_LOGON);
			p.writeNTString("RivalBot");
			p.writeNTString("b8f9b319f223ddcc38");
			p.SendPacket(bnOutputStream);
		}
		
		while(isConnected() && !socket.isClosed() && !disposed) {
			if(bnInputStream.available() > 0) {
				BotNetPacketReader pr = new BotNetPacketReader(bnInputStream);
				BNetInputStream is = pr.getInputStream();
				
				switch(pr.packetId) {
				case PACKET_BOTNETVERSION: {
					botNetServerRevision = is.readDWord();
					recieveInfo("BotNet server version is " + botNetServerRevision);
					
					BotNetPacket p = new BotNetPacket(BotNetPacketId.PACKET_BOTNETVERSION);
					p.writeDWord(1);
					p.writeDWord(1);
					p.SendPacket(bnOutputStream);
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
		// Send PACKET_STATSUPDATE
		{
			BotNetPacket p = new BotNetPacket(BotNetPacketId.PACKET_STATSUPDATE);
			p.writeNTString("BNUBot2"); // bnet username
			p.writeNTString("<Not Logged On>"); // channel
			p.writeDWord(-1); // bnet ip address
			p.writeNTString(" "); // database
			p.writeDWord(0); // cycling?
			p.SendPacket(bnOutputStream);
		}
		// Send PACKET_USERINFO
		{
			BotNetPacket p = new BotNetPacket(BotNetPacketId.PACKET_USERINFO);
			p.SendPacket(bnOutputStream);
		}
		
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
					BotNetPacket p = new BotNetPacket(BotNetPacketId.PACKET_IDLE);
					p.SendPacket(bnOutputStream);
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
						recieveInfo("Complete");
						break;
					}
					
					int number = is.readDWord();
					//int dbflag = 0;
					//int ztff = 0;
					if(botNetServerRevision >= 4) {
						/*dbflag =*/ is.readDWord();
						/*ztff =*/ is.readDWord();
					}
					String name = is.readNTString();
					String channel = is.readNTString();
					String server = HexDump.DWordToIP(is.readDWord());
					
					String account = null;
					String database = null;
					if(botNetServerRevision >= 2)
						account = is.readNTString();
					if(botNetServerRevision >= 3)
						database = is.readNTString();
					
					StringBuilder sb = new StringBuilder("User [#");
					sb.append(number).append("] ");
					sb.append(name).append(" in channel [ ");
					sb.append(channel).append(" ] of server [ ");
					sb.append(server).append(" ]");
					if((account != null) && !account.equals(""))
						sb.append(" with account [ ").append(account).append(" ]");
					if((database != null) && !database.equals(""))
						sb.append(" on database [ ").append(database).append(" ]");
					
					recieveInfo(sb.toString());
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

	public BotNetConnection(ConnectionSettings cs, Profile p) {
		super(cs, p);
	}

	public ProductIDs getProductID() {
		return ProductIDs.CHAT;
	}

	public boolean isOp() {
		return false;
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
