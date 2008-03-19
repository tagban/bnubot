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
import net.bnubot.util.BNetInputStream;
import net.bnubot.util.BNetUser;
import net.bnubot.util.HexDump;
import net.bnubot.util.MirrorSelector;
import net.bnubot.util.Out;
import net.bnubot.util.StatString;
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
				while(!connectionState.canConnect()) {
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
					case 0x01:
						recieveError("Login failed");
						disconnect(false);
						break;
					case 0x02:
						recieveInfo("Login created.");
						break;
					case 0x03:
						recieveError("That account is already logged in.");
						disconnect(false);
						break;
					default:
						recieveError("Unknown PKT_LOGON status 0x" + Integer.toHexString(status));
						disconnect(false);
						break;
					}
					
					if(!isConnected())
						break;
					
					// We are officially logged in!
					sendJoinChannel("x86");
					
					myUser = new BNetUser(this, cs.username, cs.myRealm);
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
					DTPacket p = new DTPacket(DTPacketId.PKT_NULL);
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
						queueChatHelper(getAntiIdle(), true);
					}
				}
			}
			
			if(dtInputStream.available() > 0) {
				DTPacketReader pr;
				pr = new DTPacketReader(dtInputStream);
				BNetInputStream is = pr.getData();
				
				switch(pr.packetId) {
				case PKT_ENTERCHANNEL: {
					/* (CString) 	Channel
					 * (UInt32)	Flags
					 * (CString)	MOTD
					 * 
					 * Flags:
					 * 0x01 : Registered
					 * 0x02 : Silent
					 * 0x04 : Admin
					 */
					
					String channel = is.readNTString();
					int flags = is.readDWord();
					String motd = is.readNTString();
					
					joinedChannel(channel, flags);
					recieveInfo(motd);
					break;
				}
				
				case PKT_CHANNELUSERS: {
					/* (Byte)		User Count
					 * (void)		User Data
					 * 
					 * For each user...
					 * 	(CString)	Username
					 * 	(UInt32)	Flags
					 * 
					 * User Flags:
					 * 0x01 : Operator
					 * 0x02 : Ignored
					 * 0x04 : Admin
					 * 0x08 : NetOp
					 */
					int numUsers = is.readByte();
					for(int i = 0; i < numUsers; i++) {
						String username = is.readNTString();
						int flags = is.readDWord();
						
						channelUser(findCreateBNUser(username, flags));
					}
					break;
				}
				
				case PKT_USERUPDATE: {
					/* (CString)	Username
					 * (UInt32)	Flags
					 */
					String username = is.readNTString();
					int flags = is.readDWord();
					
					channelUser(findCreateBNUser(username, flags));
					break;
				}
				
				case PKT_CHANNELJOIN: {
					/* (CString)	Username
					 * (UInt32)	Flags
					 */
					String username = is.readNTString();
					int flags = is.readDWord();
					
					channelJoin(findCreateBNUser(username, flags));
					break;
				}
				
				case PKT_CHANNELLEAVE: {
					/* (CString)	Username
					 */
					String username = is.readNTString();
					
					channelLeave(findCreateBNUser(username, null));
					break;
				}
				
				case PKT_CHANNELCHAT: {
					/* (Byte)		Chat Type
					 * (CString)	From
					 * (CString)	Message
					 * 
					 * Chat Type values:
					 * 0x00 : Normal
					 * 0x01 : Self Talking
					 * 0x02 : Whisper To
					 * 0x03 : Whisper From
					 * 0x04 : Emote
					 * 0x05 : Self Emote
					 */
					int chatType = is.readByte();
					String username = is.readNTString();
					String text = is.readNTString();
					
					// Get a BNetUser object for the user
					BNetUser user = null;
					if(myUser.equals(username))
						user = myUser;
					else
						user = getBNetUser(username);
					if(user == null)
						user = new BNetUser(this, username, cs.myRealm);
					
					switch(chatType) {
					case 0x00: // Normal
					case 0x01: // Self talking
						recieveChat(user, text);
						break;
					case 0x02: // Whisper to
						whisperSent(user, text);
						break;
					case 0x03: // Whisper from
						whisperRecieved(user, text);
						break;
					case 0x04: // Emote
					case 0x05: // Self Emote
						recieveEmote(user, text);
						break;
					default:
						Out.debugAlways(getClass(), "Unexpected chat type 0x" + Integer.toHexString(chatType) + " from " + username + ": " + text);
						break;
					}
					
					break;
				}
				
				case PKT_UNKNOWN_0x22: {
					int unknown = is.readDWord();
					String text = is.readNTString();
					switch(unknown) {
					case 0x00:
						recieveInfo(text);
						break;
					case 0x01:
						recieveError(text);
						break;
					default:
						Out.debugAlways(getClass(), "0x" + Integer.toHexString(unknown) + ": " + text);
						break;
					}
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

	/**
	 * Find or create a BNetUser
	 * @param username The user's name
	 * @param flags Flags to mangle and set
	 * @return BNetUser describing the person
	 */
	private BNetUser findCreateBNUser(String username, Integer userFlags) {
		// Create the BNetUser
		BNetUser user = getBNetUser(username);
		if(user == null)
			user = new BNetUser(this, username, cs.myRealm);
		
		// Flags
		if(userFlags != null) {
			int flags = userFlags.intValue();
			// Make flags look like bnet flags
			int bnflags = 0x10;	// No UDP
			if((flags & 0x10) != 0)	// Voiced -> Speaker
				bnflags |= 0x04;
			if((flags & 0x08) != 0)	// NetOp -> Blizzard Rep
				bnflags |= 0x01;
			if((flags & 0x04) != 0)	// Admin -> B.net Rep
				bnflags |= 0x08;
			if((flags & 0x02) != 0)	// Ignored -> Squelched
				bnflags |= 0x20;
			if((flags & 0x01) != 0)	// Operator
				bnflags |= 0x02;
			user.setFlags(bnflags);
		}
		
		// StatString
		if(user.getStatString() == null)
			user.setStatString(new StatString("TAHC"));
		
		return user;
	}
	
	public boolean isOp() {
		return (myUser.getFlags() & 0x02) == 0x02;
	}

	/**
	 * Send SID_JOINCHANNEL
	 */
	public void sendJoinChannel(String channel) throws Exception {
		DTPacket p = new DTPacket(DTPacketId.PKT_ENTERCHANNEL);
		p.writeNTString(channel);
		p.SendPacket(dtOutputStream);
	}

	/**
	 * Send SID_JOINCHANNEL with create channel flag
	 */
	public void sendJoinChannel2(String channel) throws Exception {
		sendJoinChannel(channel);
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
