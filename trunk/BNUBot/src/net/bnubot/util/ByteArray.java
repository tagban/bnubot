/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util;

import java.io.UnsupportedEncodingException;

/**
 * @author scotta
 */
public final class ByteArray {
	private final byte[] data;

	public ByteArray(byte b) {
		this.data = new byte[] {b};
	}

	public ByteArray(byte[] data) {
		this.data = data.clone();
	}

	public ByteArray(String text) {
		byte[] data;
		try {
			data = text.getBytes("UTF-8");
		} catch(UnsupportedEncodingException e) {
			data = text.getBytes();
		}
		this.data = data;
	}

	public ByteArray(byte[] data, int beginIndex, int endIndex) {
		this.data = new byte[endIndex - beginIndex];
		System.arraycopy(data, beginIndex, this.data, 0, this.data.length);
	}

	public final byte[] getBytes() {
		return data;
	}

	public ByteArray concat(byte[] str) {
		byte[] out = new byte[data.length + str.length];
		System.arraycopy(data, 0, out, 0, data.length);
		System.arraycopy(str, 0, out, data.length, str.length);
		return new ByteArray(out);
	}

	public ByteArray concat(ByteArray str) {
		return concat(str.data);
	}

	public ByteArray removeFirst() {
		return substring(1);
	}

	@Override
	public String toString() {
		try {
			return new String(data, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return new String(data);
		}
	}

	public int length() {
		return data.length;
	}

	public byte byteAt(int i) {
		return data[i];
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof byte[]) {
			byte[] x = (byte[])obj;
			if(x.length != data.length)
				return false;
			for(int i = 0; i < x.length; i++) {
				if(x[i] != data[i])
					return false;
			}
			return true;
		}
		if(obj instanceof ByteArray)
			return equals(((ByteArray)obj).data);
		if(obj instanceof String)
			return toString().equals(obj);
		return false;
	}

	public ByteArray substring(int beginIndex) {
		return substring(beginIndex, data.length);
	}

	public ByteArray substring(int beginIndex, int endIndex) {
		return new ByteArray(data, beginIndex, endIndex);
	}
}
