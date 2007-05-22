package core.bncs;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BNCSOutputStream extends DataOutputStream {

	public BNCSOutputStream(OutputStream out) {
		super(out);
	}

	public void writeWord(int word) throws IOException {
		int w = 0;
		w |= (word & 0xFF00) >> 8;
		w |= (word & 0x00FF) << 8;
		writeChar(w);
	}

	public void writeDWord(int doubleword) throws IOException {
		int dw = 0;
		dw |= (doubleword & 0xFF000000) >> 24;
		dw |= (doubleword & 0x00FF0000) >> 8;
		dw |= (doubleword & 0x0000FF00) << 8;
		dw |= (doubleword & 0x000000FF) << 24;
		writeInt(dw);
	}
	
	public void writeDWord(String str) throws IOException {
		assert(str.length() == 4);
		writeByte((int)str.charAt(3));
		writeByte((int)str.charAt(2));
		writeByte((int)str.charAt(1));
		writeByte((int)str.charAt(0));
	}
	
	public void writeNTString(String str) throws IOException {
		write(str.getBytes());
		writeByte(0);
	}
}
