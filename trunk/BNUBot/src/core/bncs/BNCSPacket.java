package core.bncs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import core.BNetOutputStream;

public class BNCSPacket extends BNetOutputStream {
	byte packetId;

	public BNCSPacket(byte packetId) {
		super(new ByteArrayOutputStream());
		this.packetId = packetId;
	}
	
	public void SendPacket(OutputStream out) {
		byte data[] = ((ByteArrayOutputStream)this.out).toByteArray();
		//BNCSOutputStream sckout = new BNCSOutputStream(out);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BNetOutputStream sckout = new BNetOutputStream(baos);
		
		try {
			sckout.writeByte(0xFF);
			sckout.writeByte(packetId);
			sckout.writeWord(data.length + 4);
			sckout.write(data);
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		data = baos.toByteArray();
		
		//System.out.println("SEND\n" + util.HexDump.hexDump(data));
		try {
			out.write(data);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
