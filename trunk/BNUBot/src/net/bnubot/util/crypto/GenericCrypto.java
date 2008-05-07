/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util.crypto;

/**
 * @author sanderson
 *
 */
public class GenericCrypto {
	public static final int CRYPTO_HEX = 0x01;
	public static final int CRYPTO_BASE64 = 0x02;
	public static final int CRYPTO_DM = 0x04;
	public static final int CRYPTO_MC = 0x08;

	private static byte[] concat(byte b0, byte[] b1) {
		byte[] out = new byte[1 + b1.length];
		int i = 0;
		out[i++] = b0;
		for(byte b : b1)
			out[i++] = b;
		return out;
	}

	public static String decode(byte[] data) {
		if(data.length > 1 && data[0] == (byte)0xB8)
			return "{MC} " + MCEncryption.decode(data);
		return new String(data);
	}

	public static byte[] encode(String input, int crypto) {
		byte[] data = input.getBytes();
		if((crypto & CRYPTO_MC) != 0)
			data = concat((byte)0xB8, MCEncryption.encode(data));
		if((crypto & CRYPTO_HEX) != 0)
			data = concat((byte)0x00, HexDump.encode(data).getBytes());
		return data;
	}
}
