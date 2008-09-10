/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util.crypto;

import junit.framework.TestCase;
import net.bnubot.core.bncs.ProductIDs;
import net.bnubot.util.ByteArray;

/**
 * @author scotta
 */
public class HexDumpTest extends TestCase {

	public void testDWordToPretty() {
		assertEquals("STAR", HexDump.DWordToPretty(ProductIDs.STAR.getDword()));
		assertEquals("W2BN", HexDump.DWordToPretty(ProductIDs.W2BN.getDword()));
		assertEquals("W3XP", HexDump.DWordToPretty(ProductIDs.W3XP.getDword()));
	}

	public void testPrettyToDWord() {
		assertEquals(ProductIDs.STAR.getDword(), HexDump.PrettyToDWord("STAR"));
		assertEquals(ProductIDs.W2BN.getDword(), HexDump.PrettyToDWord("W2BN"));
		assertEquals(ProductIDs.W3XP.getDword(), HexDump.PrettyToDWord("W3XP"));
	}

	public void testHexDump() throws Exception {
		String str = "00017f80ff";
		assertEquals(HexDump.encode(new ByteArray(new byte[] {0x00, 0x01, 0x7F, (byte)0x80, (byte)0xFF})), str);
		assertEquals(HexDump.encode(HexDump.decode(new ByteArray(str))), str);
	}
}
