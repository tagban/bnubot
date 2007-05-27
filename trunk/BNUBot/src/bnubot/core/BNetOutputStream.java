package bnubot.core;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BNetOutputStream extends DataOutputStream {

	public BNetOutputStream(OutputStream out) {
		super(out);
	}

	public void writeWord(int word) throws IOException {
		int w = 0;
		w |= (word & 0xFF00) >> 8;
		w |= (word & 0x00FF) << 8;
		writeChar(w);
	}

	public void writeDWord(int doubleword) throws IOException {
		/*int dw = 0;
		dw |= (doubleword & 0x000000FF) << 24;
		dw |= (doubleword & 0x0000FF00) << 8;
		dw |= (doubleword & 0x00FF0000) >> 8;
		dw |= (doubleword & 0xFF000000) >> 24;
		writeInt(dw);*/
		writeByte((doubleword & 0x000000FF));
		writeByte((doubleword & 0x0000FF00) >> 8);
		writeByte((doubleword & 0x00FF0000) >> 16);
		writeByte((doubleword & 0xFF000000) >> 24);
	}

	public void writeQWord(long quadword) throws IOException {
		int low =	(int)((quadword >> 0l) & 0xFFFFFFFF);
		int high =	(int)((quadword >> 32l) & 0xFFFFFFFF);
		writeDWord(low);
		writeDWord(high);
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
