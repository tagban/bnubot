/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util;

import junit.framework.TestCase;

/**
 * @author scotta
 */
public class SHA1SumTest extends TestCase {

	public void testBadInput() {
		try {
			new SHA1Sum("badinput");
			fail();
		} catch(Exception e) {}
		try {
			new SHA1Sum("bad input");
			fail();
		} catch(Exception e) {}
	}

	public void testGoodInput() {
		try {
			new SHA1Sum("0123456789012345678901234567890123456789");
			new SHA1Sum("aaaaaaaaaaBBBBBBBBBBccccccccccDDDDDDDDDD");
		} catch(Exception e) {
			fail();
		}
	}

	public void testEqual() throws Exception {
		SHA1Sum a = new SHA1Sum("0000000000000000000000000000000000000000");
		SHA1Sum b = new SHA1Sum("0000000000000000000000000000000000000000");
		SHA1Sum c = new SHA1Sum("0000000000000000000000000000000000000001");
		assertTrue(a.equals(b));
		assertFalse(a.equals(c));
		assertFalse(a.equals(new Object()));
	}

	public void testHash() throws Exception {
		assertEquals(new SHA1Sum("test".getBytes()).toString(),
				"a94a8fe5ccb19ba61c4c0873d391e987982fbbd3");
	}

}
