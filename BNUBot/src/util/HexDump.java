package util;

public class HexDump {
	private static String hexChr(int b) {
		switch(b) {
		case 0:	return "0";
		case 1:	return "1";
		case 2:	return "2";
		case 3:	return "3";
		case 4:	return "4";
		case 5:	return "5";
		case 6:	return "6";
		case 7:	return "7";
		case 8:	return "8";
		case 9:	return "9";
		case 10:	return "A";
		case 11:	return "B";
		case 12:	return "C";
		case 13:	return "D";
		case 14:	return "E";
		case 15:	return "F";
		}
		return "?";
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
