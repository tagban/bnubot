/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * @author scotta
 */
public class BNetInputStream extends DataInputStream {

	public BNetInputStream(InputStream in) {
		super(in);
	}

	public byte[] readFully() throws IOException {
		byte[] ret = new byte[available()];
		readFully(ret);
        return ret;
	}

	/**
     * Bytes for this operation are read from the contained input stream.
     *
     * @return     the next word of this input stream as a signed 16-bit
     *             <code>short</code>.
     * @exception  IOException   the stream has been closed and the contained
     * 		   input stream does not support reading after close, or
     * 		   another I/O error occurs.
	 */
	public short readWord() throws IOException {
        return (short)
        		(((readByte() << 0) & 0x00FF) |
				((readByte() << 8) & 0xFF00));
	}

	/**
     * Bytes for this operation are read from the contained input stream.
     *
     * @return     the next double-word of this input stream as a signed 32-bit
     *             <code>int</code>.
     * @exception  IOException   the stream has been closed and the contained
     * 		   input stream does not support reading after close, or
     * 		   another I/O error occurs.
	 */
	public int readDWord() throws IOException {
        return	((readByte() << 0) & 0x000000FF) |
				((readByte() << 8) & 0x0000FF00) |
				((readByte() << 16) & 0x00FF0000) |
				((readByte() << 24) & 0xFF000000);
	}

	/**
     * Bytes for this operation are read from the parameter <code>data[off..off+3]</code>.
     *
     * @param data the byte array to read the double-word from.
     * @param off the index to start reading from <code>data</code>.
     * @return     the next double-word of this input stream as a signed 32-bit
     *             <code>int</code>.
     * @exception  IOException   the stream has been closed and the contained
     * 		   input stream does not support reading after close, or
     * 		   another I/O error occurs.
	 */
	public static int readDWord(byte[] data, int off) {
        return	((data[0 + off] << 0) & 0x000000FF) |
				((data[1 + off] << 8) & 0x0000FF00) |
				((data[2 + off] << 16) & 0x00FF0000) |
				((data[3 + off] << 24) & 0xFF000000);
	}

	/**
     * Bytes for this operation are read from the contained input stream.
     *
     * @return     the next quad-word of this input stream as a signed 64-bit
     *             <code>long</code>.
     * @exception  IOException   the stream has been closed and the contained
     * 		   input stream does not support reading after close, or
     * 		   another I/O error occurs.
	 */
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
		return new String(readNTBytes());
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
