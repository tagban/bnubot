/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util;

import junit.framework.TestCase;

/**
 * @author scotta
 */
public class ByteArrayTest extends TestCase {

	public void testRemoveFirst() {
		ByteArray ba = new ByteArray("abcdefgh");
		assertEquals("bcdefgh", ba.removeFirst().toString());
	}

	public void testConcat() {
		ByteArray ba1 = new ByteArray("abcd");
		ByteArray ba2 = new ByteArray("efgh");
		assertEquals("abcdefgh", ba1.concat(ba2).toString());
	}

	public void testSubString() {
		ByteArray ba = new ByteArray("abcdefgh");
		assertEquals("bcdefgh", ba.removeFirst().toString());
	}

}
