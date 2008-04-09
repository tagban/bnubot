/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.bncs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetOutputStream;
import net.bnubot.util.HexDump;
import net.bnubot.util.Out;

public class BNCSPacket extends BNetOutputStream {
	BNCSPacketId packetId;

	public BNCSPacket(BNCSPacketId packetId) {
		super(new ByteArrayOutputStream());
		this.packetId = packetId;
	}
	
	private static void debug(BNCSPacketId packetId, byte[] data) {
		String msg = "SEND " + packetId.name();
		if(Out.isDebug())
			msg += "\n" + HexDump.hexDump(data);
		Out.debugAlways(BNCSPacket.class, msg);
	}
	
	public void debug() throws IOException {
		debug(packetId, getData());
	}

	public void SendPacket(OutputStream out) throws IOException {
		byte[] data;
		try {
			data = getData();
		} catch(Exception e) {
			Out.exception(e);
			return;
		}

		if(GlobalSettings.packetLog)
			debug(packetId, data);

		out.write(data);
		out.flush();
	}

	/**
	 * @return
	 */
	private byte[] getData() throws IOException {
		byte data[] = ((ByteArrayOutputStream)this.out).toByteArray();
		//BNCSOutputStream sckout = new BNCSOutputStream(out);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BNetOutputStream sckout = new BNetOutputStream(baos);

		if(packetId == BNCSPacketId.SID_CHATCOMMAND) {
			if(data.length > 0xFB)
				throw new IOException("Chat command is too long; ignoring.");
			if(data[data.length-1] != 0x00)
				throw new IOException("Chat command is not null terminated; ignoring.");
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
		return data;
	}
}
