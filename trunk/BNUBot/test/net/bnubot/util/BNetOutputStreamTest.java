/**
 *
 */
package net.bnubot.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

/**
 * @author scotta
 */
public class BNetOutputStreamTest extends TestCase {

	public void testNumbers() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		byte b = 0x01;
		short word = 0x0203;
		int dword = 0x03040506;
		long qword = 0x0708090a0b0c0d0eL;

		try (BNetOutputStream os = new BNetOutputStream(baos)) {
			os.writeByte(b);
			os.writeWord(word);
			os.writeDWord(dword);
			os.writeQWord(qword);
		}

		assertEquals(new ByteArray(baos.toByteArray()), new byte[] {
			0x01,
			0x03, 0x02,
			0x06, 0x05, 0x04, 0x03,
			0x0e, 0x0d, 0x0c, 0x0b, 0x0a, 0x09, 0x08, 0x07
		});
	}

	public void testSigned() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		byte b = (byte)0xFF;
		short word = (short)0xFF03;
		int dword = 0xFF040506;
		long qword = 0xFF08090a0b0c0d0eL;

		try (BNetOutputStream os = new BNetOutputStream(baos)) {
			os.writeByte(b);
			os.writeWord(word);
			os.writeDWord(dword);
			os.writeQWord(qword);
		}

		assertEquals(new ByteArray(baos.toByteArray()), new byte[] {
			(byte)0xFF,
			0x03, (byte)0xFF,
			0x06, 0x05, 0x04, (byte)0xFF,
			0x0e, 0x0d, 0x0c, 0x0b, 0x0a, 0x09, 0x08, (byte)0xFF
		});
	}
}
