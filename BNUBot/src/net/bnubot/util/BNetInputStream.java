/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class BNetInputStream extends DataInputStream {

	public BNetInputStream(InputStream in) {
		super(in);
	}

	public int readWord() throws IOException {
        return	((readByte() << 0) & 0x00FF) |
				((readByte() << 8) & 0xFF00);
	}

	public int readDWord() throws IOException {
        return	((readByte() << 0) & 0x000000FF) |
				((readByte() << 8) & 0x0000FF00) |
				((readByte() << 16) & 0x00FF0000) |
				((readByte() << 24) & 0xFF000000);
	}

	public long readQWord() throws IOException {
		long qw = readDWord() & 0xFFFFFFFFl;
		long qw2 = readDWord() & 0xFFFFFFFFl;
		qw |= (qw2 << 32l);
		return qw;
	}
	
	public String readNTString() throws IOException {
		return readNTString(null);
	}
	
	public String readNTStringUTF8() throws IOException {
		return readNTString("UTF-8");
	}
	
	public String readNTString(String encoding) throws IOException {
		int length = 64;
		int pos = 0;
		ByteBuffer bb = ByteBuffer.allocate(length);
		while(true) {
			byte b = readByte();
			if(b == 0) {
				if(encoding == null)
					return new String(bb.array(), 0, pos);
				return new String(bb.array(), 0, pos, encoding);
			}
			
			pos++;
			if(pos > length) {
				length += length;
				ByteBuffer bb2 = ByteBuffer.allocate(length);
				bb2.put(bb.array());
				bb = bb2;
			}
			bb.put(b);
		}
	}
	
	public String readFixedLengthString(int length) throws IOException {
		byte[] out = new byte[length];
		read(out, 0, length);
		return new String(out);
	}
}
