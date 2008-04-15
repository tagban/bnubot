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
	
	protected void initializeConnection(Task connect) throws Exception {
		botNetServerRevision = 0;
		
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
				case PACKET_BOTNETVERSION: {
					botNetServerRevision = is.readDWord();
					
					BotNetPacket p = new BotNetPacket(BotNetPacketId.PACKET_BOTNETVERSION);
					p.writeDWord(1);
					p.writeDWord(1);
					p.SendPacket(bnOutputStream);
					
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

	protected void connectedLoop() throws Exception {
		BotNetPacket p = new BotNetPacket(BotNetPacketId.PACKET_USERINFO);
		p.SendPacket(bnOutputStream);
		
		while(isConnected() && !socket.isClosed() && !disposed) {
			if(bnInputStream.available() > 0) {
				BotNetPacketReader pr = new BotNetPacketReader(bnInputStream);
				//BNetInputStream is = pr.getInputStream();
				
				switch(pr.packetId) {
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
