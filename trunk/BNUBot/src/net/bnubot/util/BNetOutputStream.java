/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import net.bnubot.util.crypto.HexDump;

/**
 * @author scotta
 */
public class BNetOutputStream extends DataOutputStream {

	public BNetOutputStream(OutputStream out) {
		super(out);
	}

	public void writeWord(int word) throws IOException {
		int w = 0;
		w |= (word & 0xFF00) >> 8;
		w |= (word & 0x00FF) << 8;
		writeChar(w);
	}

	public void writeDWord(int doubleword) throws IOException {
		writeByte((doubleword & 0x000000FF));
		writeByte((doubleword & 0x0000FF00) >> 8);
		writeByte((doubleword & 0x00FF0000) >> 16);
		writeByte((doubleword & 0xFF000000) >> 24);
	}

	public void writeQWord(long quadword) throws IOException {
		int low =	(int)((quadword >> 0l) & 0xFFFFFFFF);
		int high =	(int)((quadword >> 32l) & 0xFFFFFFFF);
		writeDWord(low);
		writeDWord(high);
	}

	public void writeDWord(String str) throws IOException {
		if(str.length() != 4)
			throw new IOException("string length was not 4!\n" + str);
		writeByte(str.charAt(3));
		writeByte(str.charAt(2));
		writeByte(str.charAt(1));
		writeByte(str.charAt(0));
	}

	public void writeNTString(byte[] str) throws IOException {
		for(byte b : str) {
			if(b == 0)
				throw new IOException("String contains a null character:\n" + HexDump.hexDump(str));
			writeByte(b);
		}
		writeByte(0);
	}

	public void writeNTString(ByteArray str) throws IOException {
		writeNTString(str.getBytes());
	}

	public void writeNTString(String str) throws IOException {
		writeNTString(str.getBytes());
	}
}
