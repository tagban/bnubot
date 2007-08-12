/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.core;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

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
		String out = new String();
		while(true) {
			char c = (char)readByte();
			if(c == 0)
				return out;
			out += c;
		}
	}
	
	public String readFixedLengthString(int length) throws IOException {
		byte[] out = new byte[length];
		read(out, 0, length);
		return new String(out);
	}
}
