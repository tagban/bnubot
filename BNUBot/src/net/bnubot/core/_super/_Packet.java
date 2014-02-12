/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.core._super;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;

import net.bnubot.core.Connection;
import net.bnubot.core.Connection.ConnectionState;
import net.bnubot.core.bncs.BNCSPacketId;
import net.bnubot.logging.Out;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetOutputStream;
import net.bnubot.util.crypto.HexDump;

/**
 * @param <C> The connection type for this packet
 * @param <P> The packetId type for this packet
 * @author scotta
 */
public abstract class _Packet<C extends Connection, P extends _PacketId<C>> extends BNetOutputStream {
	private final C c;
	private final P packetId;

	public _Packet(C c, P packetId) {
		super(new ByteArrayOutputStream());
		this.c = c;
		this.packetId = packetId;
	}

	public void sendPacket(OutputStream out) throws IOException, SocketException {
		byte data[] = ((ByteArrayOutputStream)this.out).toByteArray();

		if(packetId == BNCSPacketId.SID_CHATCOMMAND) {
			if(data.length > 0xFB) {
				Out.error(getClass(), "Chat command is too long; ignoring. Len=" + data.length);
				return;
			}
			if(data[data.length-1] != 0x00) {
				Out.error(getClass(), "Chat command is not null terminated; ignoring.");
				return;
			}
		}

		data = buildPacket(packetId, data);

		if(GlobalSettings.packetLog) {
			String msg = "SEND " + packetId.name();
			if(Out.isDebug())
				msg += "\n" + HexDump.hexDump(data);
			Out.debugAlways(getClass(), msg);
		}

		try {
			out.write(data);
			out.flush();
		} catch(SocketException e) {
			c.disconnect(ConnectionState.LONG_PAUSE_BEFORE_CONNECT);
		}
	}

	protected abstract byte[] buildPacket(P packetId, byte[] data);
}
