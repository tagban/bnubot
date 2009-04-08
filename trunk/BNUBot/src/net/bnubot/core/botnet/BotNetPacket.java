/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.botnet;

import java.io.IOException;

import net.bnubot.core._super._Packet;
import net.bnubot.util.BNetOutputStream;

/**
 * @author scotta
 */
public class BotNetPacket extends _Packet<BotNetConnection, BotNetPacketId> {
	public BotNetPacket(BotNetConnection c, BotNetPacketId packetId) {
		super(c, packetId);
	}

	@Override
	protected void buildPacket(BotNetPacketId packetId, byte[] data, BNetOutputStream sckout) throws IOException  {
		sckout.writeByte(0x01);
		sckout.writeByte(packetId.ordinal());
		sckout.writeWord(data.length + 4);
		sckout.write(data);
	}
}
