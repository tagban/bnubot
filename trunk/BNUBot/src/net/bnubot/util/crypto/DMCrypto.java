/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util.crypto;


/**
 * @author scotta
 *
 */
public class DMCrypto {
	public static byte[] decode(byte[] data) {
		int lenbyte = data[0] - 64;
		byte[] out = new byte[lenbyte];

		int pos = 1;
		for(int i = 0; i < lenbyte; i++) {
			int oper = data[pos++] - 100;
			int a = data[pos++] - 63;
			out[i] = (byte)(a ^ oper);
		}
		return out;
	}

	public static byte[] encode(byte[] data) {
		return encode(data, 196); //221);
	}

	public static byte[] encode(byte[] data, int shit) {
		if(data.length > 110)
			// TODO: truncate
			throw new IllegalArgumentException("DMCrypto.encode() Can only take 110 bytes");

		byte[] out;
		{
			int outlen = data.length * 2;
			if(shit > outlen)
				outlen = shit;
			out = new byte[outlen];
		}

		int pos = 0;
		out[pos++] = (byte)(data.length + 64);
		for (byte element : data) {
			int tmp = (int)(20 * Math.random() + 1);
			out[pos++] = (byte)(tmp + 100);
			out[pos++] = (byte)((element ^ tmp) + 63);
		}

		while(pos < out.length)
			out[pos++] = (byte)(180 * Math.random() + 60);

		return out;
	}
}
