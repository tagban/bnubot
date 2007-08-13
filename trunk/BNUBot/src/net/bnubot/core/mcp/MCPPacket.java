/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.core.mcp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import net.bnubot.core.BNetOutputStream;
import net.bnubot.util.HexDump;
import net.bnubot.util.Out;

public class MCPPacket extends BNetOutputStream {
	byte packetId;

	public MCPPacket(byte packetId) {
		super(new ByteArrayOutputStream());
		this.packetId = packetId;
	}
	
	public void SendPacket(OutputStream out, boolean packetLog) {
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
			Out.info(MCPPacket.class, "SEND MCP\n" + HexDump.hexDump(data));
		
		try {
			out.write(data);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
