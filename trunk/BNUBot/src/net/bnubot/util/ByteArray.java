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
		this(new byte[] {b});
	}

	public ByteArray(byte[] data) {
		this.data = data;
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
		int pos = 0;
		for(int i = beginIndex; i < endIndex; i++)
			this.data[pos++] = data[i];
	}

	public final byte[] getBytes() {
		return data;
	}

	public ByteArray concat(byte[] str) {
		byte[] out = new byte[data.length + str.length];
		int i = 0;
		for(byte b : data)
			out[i++] = b;
		for(byte b : str)
			out[i++] = b;
		return new ByteArray(out);
	}

	public ByteArray concat(ByteArray str) {
		return concat(str.data);
	}

	public ByteArray removeFirst() {
		byte[] out = new byte[data.length - 1];
		for(int i = 1; i < data.length; i++)
			out[i-1] = data[i];
		return new ByteArray(out);
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
