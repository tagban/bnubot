/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.digitaltext;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;

import net.bnubot.bot.gui.settings.ConfigurationFrame;
import net.bnubot.bot.gui.settings.OperationCancelledException;
import net.bnubot.core.ChatQueue;
import net.bnubot.core.Connection;
import net.bnubot.core.EventHandler;
import net.bnubot.core.Profile;
import net.bnubot.core.UnsupportedFeatureException;
import net.bnubot.core.bncs.ProductIDs;
import net.bnubot.settings.ConnectionSettings;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetInputStream;
import net.bnubot.util.BNetUser;
import net.bnubot.util.HexDump;
import net.bnubot.util.MirrorSelector;
import net.bnubot.util.Out;
import net.bnubot.util.task.Task;
import net.bnubot.util.task.TaskManager;

public class DTConnection extends Connection {
	protected InputStream dtInputStream = null;
	protected DataOutputStream dtOutputStream = null;
	
	protected long lastNullPacket;
	protected long lastEntryForced;
	
	private final List<Task> currentTasks = new LinkedList<Task>();

	public DTConnection(ConnectionSettings cs, ChatQueue cq, Profile p) {
		super(cs, cq, p);
	}
	
	private boolean canConnect() {
		switch(connectionState) {
		case FORCE_CONNECT:
			return true;
		case ALLOW_CONNECT:
			return GlobalSettings.autoConnect;
		}
		return false;
	}

	public void run() {
		// We must initialize the EHs in the Connection thread
		for(EventHandler eh : eventHandlers)
			eh.initialize(this);
		for(EventHandler eh : eventHandlers2)
			eh.initialize(this);
		
		initialized = true;
		
		while(!disposed) {
			try {
				for(Task t : currentTasks)
					t.complete();
				currentTasks.clear();
				myUser = null;
				titleChanged();
				
				// Wait until we're supposed to connect
				while(!canConnect()) {
					yield();
					sleep(200);
				}
				
				Task connect = createTask("Connecting to DigitalText", "Verify connection settings validity");
				
				// Check if CS is valid
				while(cs.isValid() != null)
					new ConfigurationFrame(cs);

				// Wait a short time before allowing a reconnect
				waitUntilConnectionSafe(connect);
				
				// Double-check if disposal occured
				if(disposed)
					break;
				
				// Initialize connection to DT server
				initializeDT(connect);
				
				// Log in
				boolean loggedIn = sendLoginPackets(connect);
				
				// Connection established
				completeTask(connect);
				if(loggedIn)
					connectedLoop();
					
				// Connection closed
			} catch(SocketException e) {
			} catch (OperationCancelledException e) {
				disposed = true;
			} catch(Exception e) {
				recieveError("Unhandled " + e.getClass().getSimpleName() + ": " + e.getMessage());
				Out.exception(e);
			}

			try { disconnect(true); } catch (Exception e) { }
		}
		
		for(Task t : currentTasks)
			t.complete();
		currentTasks.clear();
		
		getProfile().dispose();
	}

	/**
	 * Initialize the connection, send game id
	 * @throws Exception
	 */
	private void initializeDT(Task connect) throws Exception {
		// Set up DT
		connect.updateProgress("Connecting to DigitalText");
		InetAddress address = MirrorSelector.getClosestMirror(cs.server, cs.port);
		recieveInfo("Connecting to " + address + ":" + cs.port + ".");
		socket = new Socket(address, cs.port);
		socket.setKeepAlive(true);
		dtInputStream = socket.getInputStream();
		dtOutputStream = new DataOutputStream(socket.getOutputStream());
		
		// Connected
		connectionState = ConnectionState.CONNECTED;
		connect.updateProgress("Connected");
	}
	
	/**
	 * Do the login work up to SID_ENTERCHAT
	 * @throws Exception
	 */
	private boolean sendLoginPackets(Task connect) throws Exception {
		DTPacket p = new DTPacket(DTPacketId.PKT_LOGON);
		p.writeNTString(cs.username);
		p.writeNTString(cs.password);
		p.SendPacket(dtOutputStream);
		
		while(isConnected() && !socket.isClosed()) {
			if(dtInputStream.available() > 0) {
				DTPacketReader pr;
				pr = new DTPacketReader(dtInputStream);
				BNetInputStream is = pr.getData();
				
				switch(pr.packetId) {
				case PKT_LOGON: {
					/* (BYTE)	 Status
					 * 
					 * Status codes:
					 * 0x00 - (C) Passed
					 * 0x01 - (A) Failed
					 * 0x02 - (C) Account created
					 * 0x03 - (A) Account online.
					 * Other: Unknown (failure).
					 */
					int status = is.readByte();
					switch(status) {
					case 0x00:
						recieveInfo("Login accepted.");
						break;
					case 0x02:
						recieveInfo("Login created.");
						break;
					default:
						recieveError("Unknown PKT_LOGON status 0x" + Integer.toHexString(status));
						disconnect(false);
						break;
					}
					
					// We are officially logged in!
					sendJoinChannel("x86");
					
					myUser = new BNetUser(cs.username, cs.myRealm);
					titleChanged();
					return true;
				}
				
				default:
					Out.debugAlways(getClass(), "Unexpected packet " + pr.packetId.name() + "\n" + HexDump.hexDump(pr.data));
					break;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * This method is the main loop after recieving SID_ENTERCHAT
	 * @throws Exception
	 */
	private void connectedLoop() throws Exception {
		lastNullPacket = System.currentTimeMillis();
		lastEntryForced = lastNullPacket;
		profile.lastAntiIdle = lastNullPacket;
		
		while(isConnected() && !socket.isClosed() && !disposed) {
			long timeNow = System.currentTimeMillis();
			
			//Send null packets every 30 seconds
			if(true) {
				long timeSinceNullPacket = timeNow - lastNullPacket;
				//Wait 30 seconds
				timeSinceNullPacket /= 1000;
				if(timeSinceNullPacket > 30) {
					lastNullPacket = timeNow;
					DTPacket p = new DTPacket(DTPacketId.PKT_UNKNOWN_0x00);
					p.SendPacket(dtOutputStream);
				}
			}
			
			//Send anti-idles every 5 minutes
			if((channelName != null) && cs.enableAntiIdle) {
				synchronized(profile) {
					long timeSinceAntiIdle = timeNow - profile.lastAntiIdle;
					
					//Wait 5 minutes
					timeSinceAntiIdle /= 1000;
					timeSinceAntiIdle /= 60;
					if(timeSinceAntiIdle >= cs.antiIdleTimer) {
						profile.lastAntiIdle = timeNow;
						queueChatHelper(getAntiIdle(), false);
					}
				}
			}
			
			if(dtInputStream.available() > 0) {
				DTPacketReader pr;
				pr = new DTPacketReader(dtInputStream);
				//BNetInputStream is = pr.getData();
				
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
	
	public boolean isOp() {
		Integer myFlags = myUser.getFlags();
		if(myFlags == null)
			return false;
		return (myFlags & 0x02) == 0x02;
	}

	/**
	 * Send SID_JOINCHANNEL
	 */
	public void sendJoinChannel(String channel) throws Exception {
		DTPacket p = new DTPacket(DTPacketId.PKT_ENTERCHANNEL);
		p.writeNTString(channel);
		p.SendPacket(dtOutputStream);
	}
	
	public void queueChatHelper(String text, boolean allowCommands) {
		text = cleanText(text);
		
		try {
			if(text.charAt(0) == '/') {
				if(text.substring(1, 3).equals("j ")) {
					sendJoinChannel(text.substring(3));
					return;
				}
				if(text.substring(1, 6).equals("join ")) {
					sendJoinChannel(text.substring(6));
					return;
				}
			}
		} catch(Exception e) {}
		
		super.queueChatHelper(text, allowCommands);
	}
	
	/**
	 * Send SID_CHATCOMMAND
	 */
	public void sendChatCommand(String text) {
		super.sendChatCommand(text);
		
		//Write the packet
		try {
			DTPacket p = new DTPacket(DTPacketId.PKT_CHANNELCHAT);
			p.writeNTString(text);
			p.SendPacket(dtOutputStream);
		} catch(IOException e) {
			Out.exception(e);
			disconnect(true);
			return;
		}
	}
	
	public String toString() {
		if(myUser == null)
			return toShortString();
		
		String out = myUser.getShortLogonName();
		if(channelName != null)
			out += " - [ #" + channelName + " ]";
		return out;
	}
	
	public String toShortString() {
		if(cs.isValid() == null)
			return cs.username + "@" + cs.myRealm;
		
		return profile.getName();
	}
	
	private Task createTask(String title, String currentStep) {
		Task t = TaskManager.createTask(profile.getName() + ": " + title, currentStep);
		currentTasks.add(t);
		return t;
	}
	
	/*private Task createTask(String title, int max, String units) {
		Task t = TaskManager.createTask(title, max, units);
		currentTasks.add(t);
		return t;
	}*/
	
	private void completeTask(Task t) {
		currentTasks.remove(t);
		t.complete();
	}

	public int getProductID() {
		return ProductIDs.PRODUCT_CHAT;
	}
	
	public void sendClanInvitation(Object cookie, String user) throws Exception { throw new UnsupportedFeatureException(null); }
	public void sendClanMOTD(Object cookie) throws Exception { throw new UnsupportedFeatureException(null); }
	public void sendClanRankChange(Object cookie, String user, int newRank) throws Exception { throw new UnsupportedFeatureException(null); }
	public void sendClanSetMOTD(String text) throws Exception { throw new UnsupportedFeatureException(null); }
	public void sendLeaveChat() throws Exception { throw new UnsupportedFeatureException(null); }
	public void sendLogonRealmEx(String realmTitle) throws Exception { throw new UnsupportedFeatureException(null); }
	public void sendQueryRealms2() throws Exception { throw new UnsupportedFeatureException(null); }
	public void sendReadUserData(String user) throws Exception { throw new UnsupportedFeatureException(null); }
}
