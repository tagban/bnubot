/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util.crypto;

public class ReverseCrypto {
	public static void main(String[] args) {
		 //{REVERSE} here i'll da oi tgian: aho wbout this?
		 //siht t'uo ba wohniag: ati odlli ereh?
		System.out.println(new String(
				decode("siht t\'uo ba wohniag: ati odlli ereh?".getBytes()
						)));
	}
	private static boolean isSpecial(byte b) {
		if((b >= 'a') && (b <= 'z'))
			return false;
		if((b >= 'A') && (b <= 'Z'))
			return false;
		return true;
	}

	public static byte[] decode(byte[] data) {
		byte[] nonSpecial = new byte[data.length];
		int nsPos = 0;
		for(byte b : data)
			if(!isSpecial(b))
				nonSpecial[nsPos++] = b;

		byte[] dataOut = new byte[data.length];
		for(int i = 0; i < data.length; i++) {
			byte b = data[i];
			if(isSpecial(b))
				dataOut[i] = data[i];
			else
				dataOut[i] = nonSpecial[--nsPos];
		}

		return dataOut;
	}

	public static byte[] encode(byte[] data) {
		return decode(data);
	}
}
