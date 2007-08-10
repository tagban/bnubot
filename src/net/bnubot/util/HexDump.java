/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.util;

public class HexDump {
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
        return	((bytes[3] << 0) & 0x000000FF) |
				((bytes[2] << 8) & 0x0000FF00) |
				((bytes[1] << 16) & 0x00FF0000) |
				((bytes[0] << 24) & 0xFF000000);
	}
	
	public static int StringToDWord(String str) {
		byte bytes[] = str.getBytes();
        return	((bytes[0] << 0) & 0x000000FF) |
				((bytes[1] << 8) & 0x0000FF00) |
				((bytes[2] << 16) & 0x00FF0000) |
				((bytes[3] << 24) & 0xFF000000);
	}
	
	private static String hexChr(int b) {
		return Integer.toHexString(b & 0xF);
	}
	
	private static String toHex(int b) {
		return hexChr((b & 0xF0) >> 4) + hexChr(b & 0x0F);
	}
	
	public static String hexDump(byte data[]) {
		String output = new String();
		for(int offset = 0; offset < data.length; offset += 16) {
			int end = data.length - offset;
			if(end < 1)
				break;
			if(end > 16)
				end = 16;
			for(int i = 0; i < 16; i++) {
				if(i >= end)
					output += "   ";
				else
					output += " " + toHex(data[offset+i]);
			}
			output += "\t";
			for(int i = 0; i < end; i++) {
				byte b = data[offset+i];
				if(b < 0x20) {
					output += ".";
					continue;
				}
				output += new Character((char)b);
			}
			output += "\n";
		}
		return output;
	}
}
