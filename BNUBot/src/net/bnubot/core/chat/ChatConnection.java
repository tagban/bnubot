/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.chat;

import java.net.Socket;

import net.bnubot.core.Connection;
import net.bnubot.core.Profile;
import net.bnubot.core.bncs.ProductIDs;
import net.bnubot.logging.Out;
import net.bnubot.settings.ConnectionSettings;
import net.bnubot.util.BNetInputStream;
import net.bnubot.util.BNetOutputStream;
import net.bnubot.util.task.Task;

/**
 * @author scotta
 */
public class ChatConnection extends Connection {
	private static final String CHAT_TYPE = "Chat";
	private Socket s;
	private BNetInputStream is;
	private BNetOutputStream os;

	public ChatConnection(ConnectionSettings cs, Profile p) {
		super(cs, p);
	}

	@Override
	public String getServerType() {
		return CHAT_TYPE;
	}

	/*public void run() {
		try {
			s = new Socket(cs.server, cs.port);
			is = new BNetInputStream(s.getInputStream());
			os = new BNetOutputStream(s.getOutputStream());

			//Chat
			//os.writeByte(0x03);
			//os.writeByte(0x04);
			os.writeBytes("c" + cs.username + "\n" + cs.password + "\n");

			os.writeBytes("/join open tech support\n");

			Out.info(getClass(), "Connected to " + cs.server + ":" + cs.port);


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
	}*/

	@Override
	protected void initializeConnection(Task connect) throws Exception {
		s = new Socket(getServer(), getPort());
		is = new BNetInputStream(s.getInputStream());
		os = new BNetOutputStream(s.getOutputStream());
		//Chat
		//os.writeByte(0x03);
		//os.writeByte(0x04);
	}

	@Override
	protected boolean sendLoginPackets(Task connect) throws Exception {
		os.writeBytes("c" + cs.username + "\n" + cs.password + "\n");
		return false;
	}

	@Override
	protected void connectedLoop() throws Exception {
		while(s.isConnected() && !disposed) {
			if(is.available() <= 0) {
				yield();
				sleep(200);
			} else {
				byte b = is.readByte();
				Out.info(getClass(), Character.toString((char)b));
			}
		}
	}

	@Override
	public boolean isOp() {
		return false;
	}

	@Override
	public ProductIDs getProductID() {
		return ProductIDs.CHAT;
	}

}
