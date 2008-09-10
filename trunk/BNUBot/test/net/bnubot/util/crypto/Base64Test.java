/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util.crypto;

import junit.framework.TestCase;
import net.bnubot.util.ByteArray;

/**
 * @author scotta
 */
public class Base64Test extends TestCase {

	public void testEncode() {
		assertEquals(Base64.encode(new ByteArray("asdf")), "YXNkZg==");
	}

	public void testDecode() {
		assertEquals(Base64.decode(new ByteArray("YXNkZg==")), "asdf");
	}
}
