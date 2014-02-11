/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.core.bnls;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.bnubot.logging.Out;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetInputStream;
import net.bnubot.util.BNetOutputStream;
import net.bnubot.util.crypto.HexDump;

/**
 * @author scotta
 */
public class BNLSPacketReader {
	public BNLSPacketId packetId;
	int packetLength;
	byte data[];

	public BNLSPacketReader(InputStream rawis) throws IOException {
		// Operates on a socket, which we don't want to close; suppress the warning
		@SuppressWarnings("resource")
		BNetInputStream is = new BNetInputStream(rawis);

		packetLength = is.readWord() & 0x0000FFFF;
		packetId = BNLSPacketId.values()[is.readByte() & 0x000000FF];
		assert(packetLength >= 3);

		data = new byte[packetLength-3];
		for(int i = 0; i < packetLength-3; i++)
			data[i] = is.readByte();

		if(GlobalSettings.packetLog) {
			String msg = "RECV " + packetId.name();
			if(Out.isDebug()) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				try (BNetOutputStream os = new BNetOutputStream(baos)) {
					os.writeWord(packetLength);
					os.writeByte(packetId.ordinal());
					os.write(data);
				}
				msg += "\n" + HexDump.hexDump(baos.toByteArray());
			}
			Out.debugAlways(getClass(), msg);
		}
	}

	public byte[] getData() {
		return data;
	}

	public BNetInputStream getInputStream() {
		return new BNetInputStream(new ByteArrayInputStream(data));
	}
}
