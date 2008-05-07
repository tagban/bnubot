/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.mcp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetInputStream;
import net.bnubot.util.BNetOutputStream;
import net.bnubot.util.Out;
import net.bnubot.util.crypto.HexDump;

public class MCPPacketReader {
	MCPPacketID packetId;
	int packetLength;
	byte data[];

	public MCPPacketReader(InputStream rawis) throws IOException {
		BNetInputStream is = new BNetInputStream(rawis);

		packetLength = is.readWord() & 0x0000FFFF;
		packetId = MCPPacketID.values()[is.readByte() & 0x000000FF];
		assert(packetLength >= 3);

		data = new byte[packetLength-3];
		for(int i = 0; i < packetLength-3; i++) {
			data[i] = is.readByte();
		}

		if(GlobalSettings.packetLog) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			BNetOutputStream os = new BNetOutputStream(baos);
			os.writeByte(packetId.ordinal());
			os.writeWord(packetLength);
			os.write(data);

			String msg = "RECV " + packetId.name();
			if(Out.isDebug())
				msg += "\n" + HexDump.hexDump(baos.toByteArray());
			Out.debugAlways(getClass(), msg);
		}
	}

	public BNetInputStream getData() {
		return new BNetInputStream(new ByteArrayInputStream(data));
	}
}
