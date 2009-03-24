/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.digitaltext;

import java.io.IOException;

import net.bnubot.core._super._Packet;
import net.bnubot.util.BNetOutputStream;

/**
 * @author scotta
 */
public class DTPacket extends _Packet<DTConnection, DTPacketId> {

	public DTPacket(DTPacketId packetId) {
		super(packetId);
	}

	@Override
	protected void buildPacket(int packetId, byte[] data, BNetOutputStream sckout) throws IOException {
		sckout.writeByte(0xF4);
		sckout.writeByte(packetId);
		sckout.writeWord(data.length + 4);
		sckout.write(data);
	}
}
