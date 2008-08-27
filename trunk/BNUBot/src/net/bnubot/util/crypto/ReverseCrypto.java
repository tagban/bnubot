/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util.crypto;

import net.bnubot.util.ByteArray;

/**
 * @author scotta
 */
public class ReverseCrypto {
	private static boolean isSpecial(byte b) {
		if((b >= 'a') && (b <= 'z'))
			return false;
		if((b >= 'A') && (b <= 'Z'))
			return false;
		return true;
	}

	public static ByteArray decode(ByteArray data) {
		byte[] nonSpecial = new byte[data.length()];
		int nsPos = 0;
		for(byte b : data.getBytes())
			if(!isSpecial(b))
				nonSpecial[nsPos++] = b;

		byte[] dataOut = new byte[data.length()];
		int pos = 0;
		for(byte b : data.getBytes()) {
			if(isSpecial(b))
				dataOut[pos++] = b;
			else
				dataOut[pos++] = nonSpecial[--nsPos];
		}

		return new ByteArray(dataOut);
	}

	public static ByteArray encode(ByteArray data) {
		return decode(data);
	}
}
