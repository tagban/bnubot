/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util.crypto;

import junit.framework.TestCase;
import net.bnubot.util.crypto.Base64;

/**
 * @author scotta
 */
public class Base64Test extends TestCase {

	public void testEncode() {
		assertEquals("YXNkZg==", Base64.encode("asdf"));
	}

	public void testDecode() {
		assertEquals("asdf", Base64.decode("YXNkZg=="));
	}
}
