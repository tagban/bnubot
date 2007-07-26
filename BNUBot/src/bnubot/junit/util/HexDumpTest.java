/**
 * This file is distributed under the GPL 
 * $Id$
 */

package bnubot.junit.util;

import bnubot.core.bncs.ProductIDs;
import bnubot.util.HexDump;
import junit.framework.TestCase;

public class HexDumpTest extends TestCase {

	public void testDWordToPretty() {
		assertEquals("STAR", HexDump.DWordToPretty(ProductIDs.PRODUCT_STAR));
		assertEquals("W2BN", HexDump.DWordToPretty(ProductIDs.PRODUCT_W2BN));
		assertEquals("W3XP", HexDump.DWordToPretty(ProductIDs.PRODUCT_W3XP));
	}
	
	public void testPrettyToDWord() {
		assertEquals(ProductIDs.PRODUCT_STAR, HexDump.PrettyToDWord("STAR"));
		assertEquals(ProductIDs.PRODUCT_W2BN, HexDump.PrettyToDWord("W2BN"));
		assertEquals(ProductIDs.PRODUCT_W3XP, HexDump.PrettyToDWord("W3XP"));
	}
}
