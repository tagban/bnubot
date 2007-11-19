/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.mcp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetOutputStream;
import net.bnubot.util.HexDump;
import net.bnubot.util.Out;

public class MCPPacket extends BNetOutputStream {
	byte packetId;

	public MCPPacket(byte packetId) {
		super(new ByteArrayOutputStream());
		this.packetId = packetId;
	}
	
	public void SendPacket(OutputStream out) {
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

		if(GlobalSettings.packetLog) {
			if(Out.isDebug())
				Out.debugAlways(getClass(), "SEND\n" + HexDump.hexDump(data));
			else
				Out.debugAlways(getClass(), "SEND 0x" + Integer.toHexString(packetId));
		}
		
		try {
			out.write(data);
			out.flush();
		} catch (Exception e) {
			Out.fatalException(e);
		}
	}
}
