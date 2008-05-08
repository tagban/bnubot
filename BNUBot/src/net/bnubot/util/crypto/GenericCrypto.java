/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util.crypto;

public class GenericCrypto {
	public static final int CRYPTO_REVERSE = 0x01;
	public static final int CRYPTO_MC = 0x02;
	public static final int CRYPTO_DM = 0x04;
	public static final int CRYPTO_HEX = 0x08;
	public static final int CRYPTO_BASE64 = 0x10;

	private static byte[] concat(byte b0, byte[] b1) {
		byte[] out = new byte[1 + b1.length];
		int i = 0;
		out[i++] = b0;
		for(byte b : b1)
			out[i++] = b;
		return out;
	}

	private static byte[] removeFirst(byte[] data) {
		byte[] out = new byte[data.length - 1];
		for(int i = 1; i < data.length; i++)
			out[i-1] = data[i];
		return out;
	}

	public static String decode(byte[] data) {
		//System.out.println(HexDump.hexDump(data));
		if(data.length <= 1)
			return new String(data);
		switch(data[0]) {
		case (byte)0xB7: return "{REVERSE} " + decode(ReverseCrypto.decode(removeFirst(data)));
		case (byte)0xB8: return "{MC} " + decode(MCCrypto.decode(removeFirst(data)));
		case (byte)0xA4: return "{DM} " + decode(DMCrypto.decode(removeFirst(data)));
		case (byte)0xA3: return "{HEX} " + decode(HexDump.decode(data, 1, data.length));
		case (byte)0xE6: return "{B64} " + decode(Base64.decode(removeFirst(data)));
		}
		return new String(data);
	}

	public static byte[] encode(String input, int crypto) {
		byte[] data = input.getBytes();
		if((crypto & CRYPTO_REVERSE) != 0)
			data = concat((byte)0xB7, ReverseCrypto.encode(data));
		if((crypto & CRYPTO_MC) != 0)
			data = concat((byte)0xB8, MCCrypto.encode(data));
		if((crypto & CRYPTO_DM) != 0)
			data = concat((byte)0xA4, DMCrypto.encode(data));
		if((crypto & CRYPTO_HEX) != 0)
			data = concat((byte)0xA3, HexDump.encode(data).getBytes());
		if((crypto & CRYPTO_BASE64) != 0)
			data = concat((byte)0xE6, Base64.encode(data));
		return data;
	}
}
