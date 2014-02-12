/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.digitaltext;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import net.bnubot.core.Connection;
import net.bnubot.core.Profile;
import net.bnubot.core.bncs.ProductIDs;
import net.bnubot.logging.Out;
import net.bnubot.settings.ConnectionSettings;
import net.bnubot.util.BNetInputStream;
import net.bnubot.util.BNetUser;
import net.bnubot.util.ByteArray;
import net.bnubot.util.MirrorSelector;
import net.bnubot.util.StatString;
import net.bnubot.util.crypto.HexDump;
import net.bnubot.util.task.Task;

/**
 * @author scotta
 */
public class DTConnection extends Connection {
	private static final String DT_TYPE = "DigitalText";
	private BNetInputStream dtInputStream = null;
	private DataOutputStream dtOutputStream = null;

	public DTConnection(ConnectionSettings cs, Profile p) {
		super(cs, p);
	}

	@Override
	public String getServerType() {
		return DT_TYPE;
	}

	/**
	 * Initialize the connection, send game id
	 * @throws Exception
	 */
	@Override
	protected void initializeConnection(Task connect) throws Exception {
		// Set up DT
		connect.updateProgress("Connecting to DigitalText");
		int port = getPort();
		InetAddress address = MirrorSelector.getClosestMirror(getServer(), port);
		dispatchRecieveInfo("Connecting to " + address + ":" + port + ".");
		socket = new Socket(address, port);
		socket.setKeepAlive(true);
		dtInputStream = new BNetInputStream(socket.getInputStream());
		dtOutputStream = new DataOutputStream(socket.getOutputStream());

		// Connected
		connect.updateProgress("Connected");
	}

	/**
	 * Do the login work up to SID_ENTERCHAT
	 * @throws Exception
	 */
	@Override
	protected boolean sendLoginPackets(Task connect) throws Exception {
		DTPacket p = new DTPacket(this, DTPacketId.PKT_LOGON);
		p.writeNTString(cs.username);
		p.writeNTString(cs.password);
		p.sendPacket(dtOutputStream);

		while(isConnected() && !socket.isClosed() && !disposed) {
			if(dtInputStream.available() <= 0) {
				sleep(200);
			} else {
				DTPacketReader pr = new DTPacketReader(dtInputStream);
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
						dispatchRecieveInfo("Login accepted.");
						break;
					case 0x01:
						dispatchRecieveError("Login failed");
						disconnect(ConnectionState.LONG_PAUSE_BEFORE_CONNECT);
						break;
					case 0x02:
						dispatchRecieveInfo("Login created.");
						break;
					case 0x03:
						dispatchRecieveError("That account is already logged in.");
						disconnect(ConnectionState.LONG_PAUSE_BEFORE_CONNECT);
						break;
					default:
						dispatchRecieveError("Unknown PKT_LOGON status 0x" + Integer.toHexString(status));
						disconnect(ConnectionState.LONG_PAUSE_BEFORE_CONNECT);
						break;
					}

					if(!isConnected())
						break;

					// We are officially logged in!
					sendJoinChannel("x86");

					myUser = new BNetUser(this, cs.username, cs.myRealm);
					dispatchTitleChanged();
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
	@Override
	protected void connectedLoop() throws Exception {
		while(isConnected() && !socket.isClosed() && !disposed) {
			long timeNow = System.currentTimeMillis();

			//Send null packets every 30 seconds
			if(true) {
				long timeSinceNullPacket = timeNow - lastNullPacket;
				//Wait 30 seconds
				timeSinceNullPacket /= 1000;
				if(timeSinceNullPacket > 30) {
					lastNullPacket = timeNow;
					DTPacket p = new DTPacket(this, DTPacketId.PKT_NULL);
					p.sendPacket(dtOutputStream);
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
						sendChatInternal(getAntiIdle());
					}
				}
			}

			if(dtInputStream.available() <= 0) {
				sleep(200);
			} else {
				DTPacketReader pr = new DTPacketReader(dtInputStream);
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

					dispatchJoinedChannel(channel, flags);
					dispatchRecieveInfo(motd);
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

						dispatchChannelUser(findCreateBNUser(username, flags));
					}
					break;
				}

				case PKT_USERUPDATE: {
					/* (CString)	Username
					 * (UInt32)	Flags
					 */
					String username = is.readNTString();
					int flags = is.readDWord();

					dispatchChannelUser(findCreateBNUser(username, flags));
					break;
				}

				case PKT_CHANNELJOIN: {
					/* (CString)	Username
					 * (UInt32)	Flags
					 */
					String username = is.readNTString();
					int flags = is.readDWord();

					dispatchChannelJoin(findCreateBNUser(username, flags));
					break;
				}

				case PKT_CHANNELLEAVE: {
					/* (CString)	Username
					 */
					String username = is.readNTString();

					dispatchChannelLeave(findCreateBNUser(username, null));
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
					ByteArray text = new ByteArray(is.readNTBytes());

					// Get a BNetUser object for the user
					BNetUser user = null;
					if(myUser.equals(username))
						user = myUser;
					else
						user = getCreateBNetUser(username, myUser);

					switch(chatType) {
					case 0x00: // Normal
					case 0x01: // Self talking
						dispatchRecieveChat(user, text);
						break;
					case 0x02: // Whisper to
						dispatchWhisperSent(user, text.toString());
						break;
					case 0x03: // Whisper from
						dispatchWhisperRecieved(user, text.toString());
						break;
					case 0x04: // Emote
					case 0x05: // Self Emote
						dispatchRecieveEmote(user, text.toString());
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
						dispatchRecieveInfo(text);
						break;
					case 0x01:
						dispatchRecieveError(text);
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
		BNetUser user = getCreateBNetUser(username, myUser);

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

	@Override
	public boolean isOp() {
		return (myUser.getFlags() & 0x02) == 0x02;
	}

	/**
	 * Send SID_JOINCHANNEL
	 */
	@Override
	public void sendJoinChannel(String channel) throws Exception {
		DTPacket p = new DTPacket(this, DTPacketId.PKT_ENTERCHANNEL);
		p.writeNTString(channel);
		p.sendPacket(dtOutputStream);
	}

	/**
	 * Send SID_JOINCHANNEL with create channel flag
	 */
	@Override
	public void sendJoinChannel2(String channel) throws Exception {
		sendJoinChannel(channel);
	}

	/**
	 * Send SID_CHATCOMMAND
	 */
	@Override
	public void sendChatCommand(ByteArray text) {
		super.sendChatCommand(text);

		//Write the packet
		try {
			DTPacket p = new DTPacket(this, DTPacketId.PKT_CHANNELCHAT);
			p.writeNTString(text);
			p.sendPacket(dtOutputStream);
		} catch(IOException e) {
			Out.exception(e);
			disconnect(ConnectionState.ALLOW_CONNECT);
			return;
		}
	}

	@Override
	public String toString() {
		if(myUser == null)
			return toShortString();

		String out = myUser.getShortLogonName();
		if(channelName != null)
			out += " - [ #" + channelName + " ]";
		return out;
	}

	@Override
	public ProductIDs getProductID() {
		return ProductIDs.CHAT;
	}
}
