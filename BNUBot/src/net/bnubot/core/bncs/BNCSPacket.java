/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.bncs;

import java.io.IOException;

import net.bnubot.core._super._Packet;
import net.bnubot.util.BNetOutputStream;

/**
 * @author scotta
 */
public class BNCSPacket extends _Packet<BNCSConnection, BNCSPacketId> {
	public BNCSPacket(BNCSConnection c, BNCSPacketId packetId) {
		super(c, packetId);
	}

	@Override
	protected void buildPacket(BNCSPacketId packetId, byte[] data, BNetOutputStream sckout) throws IOException  {
		sckout.writeByte(0xFF);
		sckout.writeByte(packetId.ordinal());
		sckout.writeWord(data.length + 4);
		sckout.write(data);
	}
}
