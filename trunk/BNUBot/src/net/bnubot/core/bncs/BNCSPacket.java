/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.bncs;

import net.bnubot.core._super._Packet;

/**
 * @author scotta
 */
public class BNCSPacket extends _Packet<BNCSConnection, BNCSPacketId> {
	public BNCSPacket(BNCSConnection c, BNCSPacketId packetId) {
		super(c, packetId);
	}

	@Override
	protected byte[] buildPacket(BNCSPacketId packetId, byte[] data) {
		byte[] out = new byte[data.length+4];
		out[0] = (byte) 0xFF;
		out[1] = (byte) packetId.ordinal();
		out[2] = (byte) ((data.length + 4) & 0x00FF);
		out[3] = (byte) (((data.length + 4) & 0xFF00) >> 8);
		System.arraycopy(data, 0, out, 4, data.length);
		return out;
	}
}
