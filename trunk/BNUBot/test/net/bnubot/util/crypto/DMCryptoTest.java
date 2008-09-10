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
public class DMCryptoTest extends TestCase {
	public void testComplete() {
		ByteArray in = new ByteArray("testing 1 2 3");
		ByteArray out = DMCrypto.decode(DMCrypto.encode(in));

		assertEquals(in, out);
	}
}
