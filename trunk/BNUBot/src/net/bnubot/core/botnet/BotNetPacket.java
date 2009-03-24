/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.botnet;

import java.io.IOException;

import net.bnubot.core._super._Packet;
import net.bnubot.util.BNetOutputStream;

public class BotNetPacket extends _Packet<BotNetConnection, BotNetPacketId> {
	public BotNetPacket(BotNetPacketId packetId) {
		super(packetId);
	}

	@Override
	protected void buildPacket(int packetId, byte[] data, BNetOutputStream sckout) throws IOException  {
		sckout.writeByte(0x01);
		sckout.writeByte(packetId);
		sckout.writeWord(data.length + 4);
		sckout.write(data);
	}
}
