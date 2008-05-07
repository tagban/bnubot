/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.botnet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;

import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetOutputStream;
import net.bnubot.util.Out;
import net.bnubot.util.crypto.HexDump;

public class BotNetPacket extends BNetOutputStream {
	BotNetPacketId packetId;

	public BotNetPacket(BotNetPacketId packetId) {
		super(new ByteArrayOutputStream());
		this.packetId = packetId;
	}

	public void SendPacket(OutputStream out) throws IOException, SocketException {
		byte data[] = ((ByteArrayOutputStream)this.out).toByteArray();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BNetOutputStream sckout = new BNetOutputStream(baos);

		try {
			sckout.writeByte(0x01);
			sckout.writeByte(packetId.ordinal());
			sckout.writeWord(data.length + 4);
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
