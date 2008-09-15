/**
 * This file is distributed under the GPL
 * $Id$
 */
package org.jbls.util;

import junit.framework.TestCase;
import net.bnubot.util.ByteArray;

/**
 * @author scotta
 */
public class ByteFromIntArrayTest extends TestCase {
	public void test() {
		int []test = { 0x01234567, 0x89abcdef };
		ByteFromIntArray bfia = new ByteFromIntArray(false);
		assertEquals(new ByteArray(
				new byte[] { 0x01, 0x23, 0x45, 0x67, (byte)0x89, (byte)0xab, (byte)0xcd, (byte)0xef }),
				bfia.getByteArray(test));
	}
}
