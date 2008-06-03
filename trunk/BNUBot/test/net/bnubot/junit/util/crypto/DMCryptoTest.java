/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.junit.util.crypto;

import junit.framework.TestCase;
import net.bnubot.util.crypto.DMCrypto;

public class DMCryptoTest extends TestCase {
	public void testComplete() {
		byte[] in = "testing 1 2 3".getBytes();
		byte[] out = DMCrypto.decode(DMCrypto.encode(in));

		assertEquals(in.length, out.length);
		for(int i = 0; i < in.length; i++)
			assertEquals(in[i], out[i]);
	}
}
