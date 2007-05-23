package util;

public class HexDump {
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
