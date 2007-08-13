/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.core.mcp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.bnubot.core.BNetInputStream;
import net.bnubot.core.BNetOutputStream;
import net.bnubot.util.HexDump;
import net.bnubot.util.Out;

public class MCPPacketReader {
	int packetId;
	int packetLength;
	byte data[];
	
	public MCPPacketReader(InputStream rawis, boolean packetLog) throws IOException {
		BNetInputStream is = new BNetInputStream(rawis);
		
		packetLength = is.readWord() & 0x0000FFFF;
		packetId = is.readByte() & 0x000000FF;
		assert(packetLength >= 3);
		
		data = new byte[packetLength-3];
		for(int i = 0; i < packetLength-3; i++) {
			data[i] = is.readByte();
		}
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BNetOutputStream os = new BNetOutputStream(baos);
		os.writeByte(packetId);
		os.writeWord(packetLength);
		os.write(data);
		
		if(packetLog)
			Out.info(MCPPacketReader.class, "RECV MCP\n" + HexDump.hexDump(baos.toByteArray()));
	}
	
	public BNetInputStream getData() {
		return new BNetInputStream(new ByteArrayInputStream(data));
	}
}
