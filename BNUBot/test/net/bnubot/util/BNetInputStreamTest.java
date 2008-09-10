/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.TestCase;

/**
 * @author scotta
 */
public class BNetInputStreamTest extends TestCase {
	private BNetInputStream getStream(byte[] data) {
		return new BNetInputStream(new ByteArrayInputStream(data));
	}

	public void testNumbers() throws IOException {
		BNetInputStream is = getStream(new byte[] {
				0x01,
				0x02, 0x03,
				0x04, 0x05, 0x06, 0x07});

		assertEquals((byte)0x01, is.readByte());
		assertEquals((short)0x0302, is.readWord());
		assertEquals(0x07060504, is.readDWord());
	}

	public void testSigned() throws IOException {
		BNetInputStream is = getStream(new byte[] {
				(byte)0xFF,
				0x02, (byte)0xFF,
				0x04, 0x05, 0x06, (byte)0xFF});

		assertEquals((byte)0xFF, is.readByte());
		assertEquals((short)0xFF02, is.readWord());
		assertEquals(0xFF060504, is.readDWord());
	}
}
