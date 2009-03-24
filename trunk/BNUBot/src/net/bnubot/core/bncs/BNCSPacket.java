/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.bncs;

import java.io.IOException;

import net.bnubot.core._super._Packet;
import net.bnubot.util.BNetOutputStream;

public class BNCSPacket extends _Packet<BNCSConnection, BNCSPacketId> {
	public BNCSPacket(BNCSPacketId packetId) {
		super(packetId);
	}

	@Override
	protected void buildPacket(int packetId, byte[] data, BNetOutputStream sckout) throws IOException  {
		sckout.writeByte(0xFF);
		sckout.writeByte(packetId);
		sckout.writeWord(data.length + 4);
		sckout.write(data);
	}
}
