/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.core.bncs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;

import net.bnubot.settings.ConnectionSettings;
import net.bnubot.util.BNetOutputStream;
import net.bnubot.util.HexDump;
import net.bnubot.util.Out;

public class BNCSPacket extends BNetOutputStream {
	BNCSCommandIDs packetId;

	public BNCSPacket(BNCSCommandIDs packetId) {
		super(new ByteArrayOutputStream());
		this.packetId = packetId;
	}

	public void SendPacket(OutputStream out) throws IOException, SocketException {
		byte data[] = ((ByteArrayOutputStream)this.out).toByteArray();
		//BNCSOutputStream sckout = new BNCSOutputStream(out);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BNetOutputStream sckout = new BNetOutputStream(baos);

		if(packetId == BNCSCommandIDs.SID_CHATCOMMAND) {
			if(data.length > 0xFB) {
				Out.error(getClass(), "Chat command is too long; ignoring.");
				return;
			}
			if(data[data.length-1] != 0x00) {
				Out.error(getClass(), "Chat command is not null terminated; ignoring.");
				return;
			}
		}

		try {
			sckout.writeByte(0xFF);
			sckout.writeByte(packetId.getId());
			sckout.writeWord(data.length + 4);
			sckout.write(data);
		} catch(IOException e) {
			Out.fatalException(e);
		}

		data = baos.toByteArray();

		if(ConnectionSettings.packetLog) {
			if(Out.isDebug())
				Out.debugAlways(getClass(), "SEND " + packetId.name() + "\n" + HexDump.hexDump(data));
			else
				Out.debugAlways(getClass(), "SEND " + packetId.name());
		}

		out.write(data);
		out.flush();
	}
}
