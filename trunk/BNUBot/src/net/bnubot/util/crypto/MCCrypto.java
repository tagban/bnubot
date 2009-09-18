/**
 * This file is distributed under the GPL
 * $Id: MCCrypto.java 1438 2008-05-08 05:36:02Z scotta $
 */

package net.bnubot.util.crypto;

import net.bnubot.util.ByteArray;

/**
 * MirageChat
 * @author scotta
 */
public class MCCrypto {
	public static ByteArray decode(ByteArray data) {
		return encode(data);
	}

	public static ByteArray encode(ByteArray data) {
		byte[] out = new byte[data.length()];
		for(int i = 0; i < out.length; i++) {
			int b = data.byteAt(i) & 0xFF;

			// Swap 0-9
			if((b >= '0') && (b <= '9'))
				b += 0xC2;
			else if((b >= '0' + 0xC2) && (b <= '9' + 0xC2))
				b -= 0xC2;

			// Swap a-z
			else if((b >= 'a') && (b <= 'z'))
				b += 0x77;
			else if((b >= 'a' + 0x77) && (b <= 'z' + 0x77))
				b -= 0x77;

			// Swap A-Z
			else if((b >= 'A') && (b <= 'Z'))
				b += 0x7D;
			else if((b >= 'A' + 0x7D) && (b <= 'Z' + 0x7D))
				b -= 0x7D;

			// Write the new byte
			out[i] = (byte)b;
		}
		return new ByteArray(out);
	}
}
