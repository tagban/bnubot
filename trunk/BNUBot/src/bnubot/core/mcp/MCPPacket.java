package bnubot.core.mcp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import bnubot.core.BNetOutputStream;
import bnubot.util.HexDump;

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
			sckout.writeByte(packetId);
			sckout.writeWord(data.length + 3);
			sckout.write(data);
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		data = baos.toByteArray();
		
		if(packetLog)
			System.out.println("SEND MCP\n" + HexDump.hexDump(data));
		
		try {
			out.write(data);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
