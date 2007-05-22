package core.bncs;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BNCSInputStream extends DataInputStream {

	public BNCSInputStream(InputStream in) {
		super(in);
	}

	public int readWord() throws IOException {
        return	((readByte() << 0) & 0x00FF) |
				((readByte() << 8) & 0xFF00);
	}

	public int readDWord() throws IOException {
        return	((readByte() << 0) & 0x000000FF) |
				((readByte() << 8) & 0x0000FF00) |
				((readByte() << 16) & 0x00FF0000) |
				((readByte() << 24) & 0xFF000000);
	}
	
	public String readNTString() throws IOException {
		String out = new String();
		byte b;
		while(true) {
			b = readByte();
			if(b == 0)
				return out;
			out += new Character((char)b);
		}
	}
}
