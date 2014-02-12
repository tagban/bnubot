/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.bncs;

import java.io.IOException;

import net.bnubot.core._super._PacketReader;
import net.bnubot.util.BNetInputStream;

/**
 * @author scotta
 */
public class BNCSPacketReader extends _PacketReader<BNCSPacketId> {

	public BNCSPacketReader(BNetInputStream rawis) throws IOException {
		super(rawis);
	}

	@Override
	protected void parse(BNetInputStream is) throws IOException {
		packetId = BNCSPacketId.values()[is.readByte() & 0x000000FF];
		packetLength = is.readWord() & 0x0000FFFF;
		assert(packetLength >= 4);

		data = new byte[packetLength-4];
		for(int i = 0; i < packetLength-4; i++) {
			data[i] = is.readByte();
		}
	}
}
