/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util.crypto;

import java.security.InvalidParameterException;

import net.bnubot.util.ByteArray;

/**
 * @author scotta
 */
public class HexDump {
	/**
	 * Turn a DWord in to an IP address
	 * @param dword The 32-bit IP address
	 * @return "xxx.xxx.xxx.xxx"
	 */
	public static String DWordToIP(int dword) {
		int bytes[] = new int[4];
		bytes[0] = 0xFF & ((dword & 0x000000FF) >> 0);
		bytes[1] = 0xFF & ((dword & 0x0000FF00) >> 8);
		bytes[2] = 0xFF & ((dword & 0x00FF0000) >> 16);
		bytes[3] = 0xFF & ((dword & 0xFF000000) >> 24);

		return	Integer.toString(bytes[0]) + "." +
				Integer.toString(bytes[1]) + "." +
				Integer.toString(bytes[2]) + "." +
				Integer.toString(bytes[3]);
	}

	public static String DWordToPretty(int dword) {
		byte bytes[] = new byte[4];
		bytes[3] = (byte)((dword & 0x000000FF) >> 0);
		bytes[2] = (byte)((dword & 0x0000FF00) >> 8);
		bytes[1] = (byte)((dword & 0x00FF0000) >> 16);
		bytes[0] = (byte)((dword & 0xFF000000) >> 24);
		return new String(bytes).replaceAll("\0", "");
	}

	public static int PrettyToDWord(String pretty) {
		byte bytes[] = pretty.getBytes();

		// If the string was between 1 and 3 chars long
		if((bytes.length > 0) && (bytes.length < 4)) {
			bytes = new byte[] {0, 0, 0, 0};
			int i = 0;
			for(byte b : pretty.getBytes())
				bytes[i++] = b;
		}

		if(bytes.length != 4)
			throw new InvalidParameterException("bytes.length != 4");
        return	((bytes[3] << 0) & 0x000000FF) |
				((bytes[2] << 8) & 0x0000FF00) |
				((bytes[1] << 16) & 0x00FF0000) |
				((bytes[0] << 24) & 0xFF000000);
	}

	public static int StringToDWord(String str) {
		byte bytes[] = str.getBytes();
		if(bytes.length != 4)
			throw new InvalidParameterException("bytes.length != 4");
        return	((bytes[0] << 0) & 0x000000FF) |
				((bytes[1] << 8) & 0x0000FF00) |
				((bytes[2] << 16) & 0x00FF0000) |
				((bytes[3] << 24) & 0xFF000000);
	}

	public static String getAlphaNumerics(String in) {
		String out = new String();
		for(int i = 0; i < in.length(); i++) {
			char c = in.charAt(i);
			if( ((c >= 'a') && (c <= 'z'))
			 || ((c >= 'A') && (c <= 'Z'))
			 || ((c >= '0') && (c <= '9')))
				out += c;
		}
		return out;
	}

	private static String hexChr(int b) {
		return Integer.toHexString(b & 0xF);
	}

	private static String toHex(int b) {
		return hexChr((b & 0xF0) >> 4) + hexChr(b & 0x0F);
	}

	public static String toHexWord(int b) {
		return hexChr((b & 0xF000) >> 12) + hexChr((b & 0x0F00) >> 8) + hexChr((b & 0x00F0) >> 4) + hexChr(b & 0x000F);
	}

	public static ByteArray encode(ByteArray data) {
		String output = new String();
		for(byte element : data.getBytes())
			output += toHex(element);
		return new ByteArray(output.getBytes());
	}

	private static byte decode(byte c1, byte c2) throws Exception {
		byte output;
		if((c1 >= '0') && (c1 <= '9'))
			output = (byte)(c1 - '0');
		else if((c1 >= 'A') && (c1 <= 'F'))
			output = (byte)((c1 - 'A') + 10);
		else if((c1 >= 'a') && (c1 <= 'f'))
			output = (byte)((c1 - 'a') + 10);
		else
			throw new IllegalArgumentException("Invalid hex string");
		output <<= 4;
		if((c2 >= '0') && (c2 <= '9'))
			output += (byte)(c2 - '0');
		else if((c2 >= 'A') && (c2 <= 'F'))
			output += (byte)((c2 - 'A') + 10);
		else if((c2 >= 'a') && (c2 <= 'f'))
			output += (byte)((c2 - 'a') + 10);
		else
			throw new IllegalArgumentException("Invalid hex string");
		return output;
	}

	public static byte[] decode(String data) throws Exception {
		return decode(data.getBytes(), 0, data.length());
	}

	public static byte[] decode(byte[] data, int o, int l) throws Exception {
		int len = l >> 1;
		byte[] output = new byte[len];
		int pos = o;
		for(int offset = 0; offset < len; offset++)
			output[offset] = decode(data[pos++], data[pos++]);
		return output;
	}

	public static ByteArray decode(ByteArray data) throws Exception {
		return new ByteArray(decode(data.getBytes(), 0, data.length()));
	}

	public static String hexDump(byte data[]) {
		String output = new String();
		for(int offset = 0; offset < data.length; offset += 16) {
			int end = data.length - offset;
			if(end < 1)
				break;
			if(end > 16)
				end = 16;

			if(offset != 0)
				output += "\n";
			output += toHexWord(offset) + "  ";

			for(int i = 0; i < 16; i++) {
				if(i >= end)
					output += "   ";
				else {
					if((i == 4) || (i == 8) || (i == 12))
						output += "-";
					else
						output += " ";
					output += toHex(data[offset+i]);
				}
			}
			output += "   ";
			for(int i = 0; i < end; i++) {
				byte b = data[offset+i];
				if((b < 0x20) || (b >= 0x7F)) {
					output += ".";
					continue;
				}
				output += new Character((char)b);
			}
		}
		return output;
	}
}
