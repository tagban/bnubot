/**
 * This file is distributed under the GPL 
 * $Id$
 */
package net.bnubot.core.bnls;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;

import net.bnubot.core.ConnectionSettings;
import net.bnubot.util.BNetOutputStream;
import net.bnubot.util.HexDump;
import net.bnubot.util.Out;

public class BNLSPacket extends BNetOutputStream {
	byte packetId;

	public BNLSPacket(byte packetId) {
		super(new ByteArrayOutputStream());
		this.packetId = packetId;
	}
	
	public void SendPacket(OutputStream out) throws IOException, SocketException {
		byte data[] = ((ByteArrayOutputStream)this.out).toByteArray();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BNetOutputStream sckout = new BNetOutputStream(baos);
		
		try {
			sckout.writeWord(data.length + 3);
			sckout.writeByte(packetId);
			sckout.write(data);
		} catch(IOException e) {
			Out.fatalException(e);
		}
		
		data = baos.toByteArray();
		
		if(ConnectionSettings.packetLog) {
			if(Out.isDebug())
				Out.debugAlways(getClass(), "SEND\n" + HexDump.hexDump(data));
			else
				Out.debugAlways(getClass(), "SEND 0x" + Integer.toHexString(packetId));
		}
		
		out.write(data);
		out.flush();
	}
}
