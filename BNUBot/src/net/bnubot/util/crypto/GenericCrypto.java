/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util.crypto;

import net.bnubot.util.ByteArray;

/**
 * @author scotta
 */
public class GenericCrypto {
	public static final int CRYPTO_REVERSE = 0x01;
	public static final int CRYPTO_MC = 0x02;
	public static final int CRYPTO_DM = 0x04;
	public static final int CRYPTO_HEX = 0x08;
	public static final int CRYPTO_BASE64 = 0x10;

	public static ByteArray decode(ByteArray text) {
		if(text.length() <= 1)
			return text;

		switch(text.byteAt(0)) {
		case (byte)0xB7: return new ByteArray("{Reverse} ").concat(decode(ReverseCrypto.decode(text.removeFirst())));
		case (byte)0xB8: return new ByteArray("{MC} ").concat(decode(MCCrypto.decode(text.removeFirst())));
		case (byte)0xA4: return new ByteArray("{DM} ").concat(decode(DMCrypto.decode(text.removeFirst())));
		case (byte)0xA3:
			try {
				return new ByteArray("{HEX} ").concat(decode(HexDump.decode(text.removeFirst())));
			} catch(Exception e) {
				return new ByteArray("{INVALID HEX} ").concat(text);
			}
		case (byte)0xE6: return new ByteArray("{B64} ").concat(decode(Base64.decode(text.removeFirst())));
		}
		return text;
	}

	public static ByteArray encode(ByteArray data, int crypto) {
		if((crypto & CRYPTO_REVERSE) != 0)
			data = new ByteArray((byte)0xB7).concat(ReverseCrypto.encode(data));
		if((crypto & CRYPTO_MC) != 0)
			data = new ByteArray((byte)0xB8).concat(MCCrypto.encode(data));
		if((crypto & CRYPTO_DM) != 0)
			data = new ByteArray((byte)0xA4).concat(DMCrypto.encode(data));
		if((crypto & CRYPTO_HEX) != 0)
			data = new ByteArray((byte)0xA3).concat(HexDump.encode(data));
		if((crypto & CRYPTO_BASE64) != 0)
			data = new ByteArray((byte)0xE6).concat(Base64.encode(data));
		return data;
	}
}
