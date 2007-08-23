/**
 * This file is distributed under the GPL 
 * $Id$
 */
package net.bnubot.core.bnls;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;

import net.bnubot.core.BNetOutputStream;
import net.bnubot.util.HexDump;
import net.bnubot.util.Out;

public class BNLSPacket extends BNetOutputStream {
	byte packetId;

	public BNLSPacket(byte packetId) {
		super(new ByteArrayOutputStream());
		this.packetId = packetId;
	}
	
	public void SendPacket(OutputStream out, boolean packetLog) throws IOException, SocketException {
		byte data[] = ((ByteArrayOutputStream)this.out).toByteArray();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BNetOutputStream sckout = new BNetOutputStream(baos);
		
		try {
			sckout.writeWord(data.length + 3);
			sckout.writeByte(packetId);
			sckout.write(data);
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		data = baos.toByteArray();
		
		if(packetLog)
			Out.info(this.getClass(), "SEND\n" + HexDump.hexDump(data));
		
		out.write(data);
		out.flush();
	}
}
