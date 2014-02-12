/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.core._super;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import net.bnubot.core.bncs.BNCSChatEventId;
import net.bnubot.core.bncs.BNCSPacketId;
import net.bnubot.logging.Out;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetInputStream;
import net.bnubot.util.crypto.HexDump;

/**
 * @param <C> The <code>net.bnubot.core.Connection</code> type for this packet
 * @param <P> The <code>net.bnubot.core._super._PacketId</code> type for this packet
 * @author scotta
 */
public abstract class _PacketReader<P extends _PacketId<?>> {
	public P packetId;
	public int packetLength;
	public byte data[];

	public _PacketReader(BNetInputStream bnis) throws IOException {
		parse(bnis);

		if(GlobalSettings.packetLog) {
			String msg = "RECV " + packetId.name();
			if(packetId == BNCSPacketId.SID_CHATEVENT)
				msg += " " + BNCSChatEventId.values()[BNetInputStream.readDWord(data, 0)].name();
			if(Out.isDebug())
				msg += "\n" + HexDump.hexDump(data);
			Out.debugAlways(getClass(), msg);
		}

	}

	protected abstract void parse(BNetInputStream is) throws IOException;

	public BNetInputStream getData() {
		return new BNetInputStream(new ByteArrayInputStream(data));
	}
}
