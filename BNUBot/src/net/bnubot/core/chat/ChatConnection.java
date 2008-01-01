/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.chat;

import java.net.Socket;

import net.bnubot.core.ChatQueue;
import net.bnubot.core.Connection;
import net.bnubot.core.Profile;
import net.bnubot.core.UnsupportedFeatureException;
import net.bnubot.core.bncs.ProductIDs;
import net.bnubot.settings.ConnectionSettings;
import net.bnubot.util.BNetInputStream;
import net.bnubot.util.BNetOutputStream;
import net.bnubot.util.Out;

public class ChatConnection extends Connection {
	protected Socket s;
	protected BNetInputStream is;
	protected BNetOutputStream os;
	
	public ChatConnection(ConnectionSettings cs, ChatQueue cq, Profile p) {
		super(cs, cq, p);
	}
	
	public void run() {
		try {
			s = new Socket(cs.bncsServer, cs.port);
			is = new BNetInputStream(s.getInputStream());
			os = new BNetOutputStream(s.getOutputStream());
			
			//Chat
			//os.writeByte(0x03);
			//os.writeByte(0x04);
			os.writeBytes("c" + cs.username + "\n" + cs.password + "\n");

			os.writeBytes("/join open tech support\n");
			
			Out.info(getClass(), "Connected to " + cs.bncsServer + ":" + cs.port);
			
			
			os.writeNTString(cs.username);
			
			while(s.isConnected()) {
				if(is.available() > 0) {
					byte b = is.readByte();
					Out.info(getClass(), Character.toString((char)b));
				} else {
					yield();
					sleep(200);
				}
			}
			
			Out.info(getClass(), "Disconnected");
			
			s.close();
		} catch (Exception e) {
			Out.fatalException(e);
		}
	}

	@Override
	public boolean isOp() {
		return false;
	}

	@Override
	public void sendLeaveChat() throws Exception {
	}

	@Override
	public void sendJoinChannel(String channel) throws Exception {
	}

	@Override
	public void reconnect() {
	}

	@Override
	public void sendClanMOTD(Object cookie) throws Exception {
		throw new UnsupportedFeatureException("Chat clients can not use clans");
	}

	@Override
	public void sendClanInvitation(Object cookie, String user) throws Exception {
		throw new UnsupportedFeatureException("Chat clients can not use clans");
		
	}

	@Override
	public void sendClanRankChange(Object cookie, String user, int newRank) throws Exception {
		throw new UnsupportedFeatureException("Chat clients can not use clans");
		
	}

	@Override
	public void sendClanSetMOTD(String text) throws Exception {
		throw new UnsupportedFeatureException("Chat clients can not use clans");
	}

	@Override
	public int getProductID() {
		return ProductIDs.PRODUCT_CHAT;
	}

	@Override
	public void sendLogonRealmEx(String realmTitle) throws Exception {
		throw new UnsupportedFeatureException("Chat clients can not use realms");
	}

	@Override
	public void sendQueryRealms2() throws Exception {
		throw new UnsupportedFeatureException("Chat clients can not use realms");
	}

	@Override
	public void sendReadUserData(String user) throws Exception {
		throw new UnsupportedFeatureException("Chat clients can not request profiles");
	}

	@Override
	public String toShortString() {
		return toString();
	}

}
