/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.bncs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;

import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetOutputStream;
import net.bnubot.util.Out;
import net.bnubot.util.crypto.HexDump;

public class BNCSPacket extends BNetOutputStream {
	BNCSPacketId packetId;

	public BNCSPacket(BNCSPacketId packetId) {
		super(new ByteArrayOutputStream());
		this.packetId = packetId;
	}

	public void sendPacket(OutputStream out) throws IOException, SocketException {
		byte data[] = ((ByteArrayOutputStream)this.out).toByteArray();
		//BNCSOutputStream sckout = new BNCSOutputStream(out);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BNetOutputStream sckout = new BNetOutputStream(baos);

		if(packetId == BNCSPacketId.SID_CHATCOMMAND) {
			if(data.length > 0xFB) {
				Out.error(getClass(), "Chat command is too long; ignoring. Len=" + data.length);
				return;
			}
			if(data[data.length-1] != 0x00) {
				Out.error(getClass(), "Chat command is not null terminated; ignoring.");
				return;
			}
		}

		try {
			sckout.writeByte(0xFF);
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
