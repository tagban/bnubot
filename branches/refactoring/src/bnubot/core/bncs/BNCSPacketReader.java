/**
 * This file is distributed under the GPL 
 * $Id$
 */

package bnubot.core.bncs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import bnubot.core.BNetInputStream;
import bnubot.core.BNetOutputStream;
import bnubot.util.HexDump;

public class BNCSPacketReader {
	int packetId;
	int packetLength;
	byte data[];
	
	public BNCSPacketReader(InputStream rawis, boolean packetLog) throws IOException {
		BNetInputStream is = new BNetInputStream(rawis);
		
		byte magic;
		do {
			magic = is.readByte();
		} while(magic != (byte)0xFF);
		
		packetId = is.readByte() & 0x000000FF;
		packetLength = is.readWord() & 0x0000FFFF;
		assert(packetLength >= 4);
		
		data = new byte[packetLength-4];
		for(int i = 0; i < packetLength-4; i++) {
			data[i] = is.readByte();
		}
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BNetOutputStream os = new BNetOutputStream(baos);
		os.writeByte(0xFF);
		os.writeByte(packetId);
		os.writeWord(packetLength);
		os.write(data);
		
		if(packetLog)
			System.out.println("RECV\n" + HexDump.hexDump(baos.toByteArray()));
	}
	
	public BNetInputStream getData() {
		return new BNetInputStream(new ByteArrayInputStream(data));
	}
}
