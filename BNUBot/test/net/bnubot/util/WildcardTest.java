/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util;

import junit.framework.TestCase;

/**
 * @author scotta
 */
public class WildcardTest extends TestCase {

	public void testWildcard() {
		assertEquals(true, Wildcard.matches("", ""));
		assertEquals(true, Wildcard.matches("*", ""));
		assertEquals(true, Wildcard.matches("*", "abcd"));
		assertEquals(true, Wildcard.matches("a*", "abcd"));
		assertEquals(true, Wildcard.matches("a*d", "abcd"));
		assertEquals(true, Wildcard.matches("*d", "abcd"));
		assertEquals(true, Wildcard.matches("*c*", "abcd"));
		assertEquals(true, Wildcard.matches("ab*c*d", "abcd"));

		assertEquals(false, Wildcard.matches("d*", "abcd"));
		assertEquals(false, Wildcard.matches(".", ""));

		// Test the disabled '.' character
		assertEquals(false, Wildcard.matches(".", "a"));
	}
}
