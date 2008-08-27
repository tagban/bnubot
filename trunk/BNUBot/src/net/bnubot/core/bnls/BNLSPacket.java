/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.core.bnls;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;

import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetOutputStream;
import net.bnubot.util.Out;
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
		byte data[] = ((ByteArrayOutputStream)this.out).toByteArray();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BNetOutputStream sckout = new BNetOutputStream(baos);

		try {
			sckout.writeWord(data.length + 3);
			sckout.writeByte(packetId.ordinal());
			sckout.write(data);
		} catch(IOException e) {
			Out.fatalException(e);
		}

		data = baos.toByteArray();

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
