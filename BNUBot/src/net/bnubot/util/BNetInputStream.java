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

	public byte[] readFully() throws IOException {
		byte[] ret = new byte[available()];
		readFully(ret);
        return ret;
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

	public static int readDWord(byte[] data, int off) {
        return	((data[0 + off] << 0) & 0x000000FF) |
				((data[1 + off] << 8) & 0x0000FF00) |
				((data[2 + off] << 16) & 0x00FF0000) |
				((data[3 + off] << 24) & 0xFF000000);
	}

	public long readQWord() throws IOException {
		long qw = readDWord() & 0xFFFFFFFFl;
		long qw2 = readDWord() & 0xFFFFFFFFl;
		qw |= (qw2 << 32l);
		return qw;
	}

	public StatString readStatString() {
		return new StatString(this);
	}

	public String readCommaTermString() throws IOException {
		String out = new String();
		do {
			int c = readByte();
			if(c == 0)
				throw new IOException("Read a null, looking for comma terminator");
			if(c == ',')
				return out;
			out += (char)c;
		} while(true);
	}

	public String readNTString() throws IOException {
		byte[] data = readNTBytes();

		// MC encryption
		if(data.length > 1 && data[0] == (byte)0xB8) {
			for(int i = 1; i < data.length; i++) {
				//asdf[i] -= asdf[0] + i;
				int b = data[i] & 0xFF;
				if((b >= '0' + 0xC2) && (b <= '9' + 0xC2))
					data[i] = (byte)(b - 0xC2);
				if((b >= 'a' + 0x77) && (b <= 'z' + 0x77))
					data[i] = (byte)(b - 0x77);
				if((b >= 'A' + 0x7D) && (b <= 'Z' + 0x7D))
					data[i] = (byte)(b - 0x7D);
			}
			return "{MC} " + new String(data, 1, data.length-1);
		}
		System.out.println(HexDump.hexDump(data));

		return new String(data);
	}

	public String readNTStringUTF8() throws IOException {
		return readNTString("UTF-8");
	}

	public String readNTString(String encoding) throws IOException {
		return new String(readNTBytes(), encoding);
	}

	public byte[] readNTBytes() throws IOException {
		int length = 64;
		int pos = 0;
		ByteBuffer bb = ByteBuffer.allocate(length);
		while(true) {
			byte b = readByte();
			if(b == 0) {
				byte[] out = new byte[pos];
				for(int i = 0; i < pos; i++)
					out[i] = bb.get(i);
				return out;
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
}
