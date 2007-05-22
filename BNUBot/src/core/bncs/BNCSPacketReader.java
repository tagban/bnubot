package core.bncs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BNCSPacketReader {
	int packetId;
	int packetLength;
	byte data[];
	
	public BNCSPacketReader(InputStream rawis) throws IOException {
		BNCSInputStream is = new BNCSInputStream(rawis);
		
		byte magic;
		do {
			magic = is.readByte();
		} while(magic != (byte)0xFF);
		
		packetId = is.readByte();
		packetLength = is.readWord();
		assert(packetLength >= 4);
		
		data = new byte[packetLength-4];
		for(int i = 0; i < packetLength-4; i++) {
			data[i] = is.readByte();
		}
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BNCSOutputStream os = new BNCSOutputStream(baos);
		os.writeByte(0xFF);
		os.writeByte(packetId);
		os.writeWord(packetLength);
		os.write(data);
		System.out.println("RECV:");
		System.out.println(util.HexDump.hexDump(baos.toByteArray()));
	}
	
	public BNCSInputStream getData() {
		return new BNCSInputStream(new ByteArrayInputStream(data));
	}
}
