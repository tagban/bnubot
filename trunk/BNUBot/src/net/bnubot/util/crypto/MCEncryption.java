/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util.crypto;


/**
 * @author sanderson
 *
 */
public class MCEncryption {
	public static byte[] decode(byte[] data) {
		for(int i = 0; i < data.length; i++) {
			int b = data[i] & 0xFF;
			if((b >= '0' + 0xC2) && (b <= '9' + 0xC2))
				data[i] = (byte)(b - 0xC2);
			if((b >= 'a' + 0x77) && (b <= 'z' + 0x77))
				data[i] = (byte)(b - 0x77);
			if((b >= 'A' + 0x7D) && (b <= 'Z' + 0x7D))
				data[i] = (byte)(b - 0x7D);
		}
		return data;
	}

	public static byte[] encode(byte[] data) {
		byte[] out = new byte[data.length];
		for(int i = 0; i < data.length; i++) {
			int b = data[i] & 0xFF;
			if((b >= '0') && (b <= '9'))
				b += 0xC2;
			else if((b >= 'a') && (b <= 'z'))
				b += 0x77;
			else if((b >= 'A') && (b <= 'Z'))
				b += 0x7D;
			out[i] = (byte)b;
		}
		return out;
	}
}
