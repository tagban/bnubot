/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.core.bnls;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;

import net.bnubot.logging.Out;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetOutputStream;
import net.bnubot.util.crypto.HexDump;

/**
 * @author scotta
 */
public class BNLSPacket extends BNetOutputStream {
	BNLSPacketId packetId;

	public BNLSPacket(BNLSPacketId packetId) {
		super(new ByteArrayOutputStream());
		this.packetId = packetId;
	}

	public void sendPacket(OutputStream out) throws IOException, SocketException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try (BNetOutputStream sckout = new BNetOutputStream(baos)) {
			byte raw_data[] = ((ByteArrayOutputStream)this.out).toByteArray();
			sckout.writeWord(raw_data.length + 3);
			sckout.writeByte(packetId.ordinal());
			sckout.write(raw_data);
		} catch(IOException e) {
			Out.fatalException(e);
		}

		byte data[] = baos.toByteArray();

		if(GlobalSettings.packetLog) {
			String msg = "SEND " + packetId.name();
			if(Out.isDebug())
				msg += "\n" + HexDump.hexDump(data);
			Out.debugAlways(getClass(), msg);
		}

		out.write(data);
		out.flush();
	}
}
