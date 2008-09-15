/**
 * This file is distributed under the GPL
 * $Id$
 */
package org.jbls.util;

import junit.framework.TestCase;

/**
 * @author scotta
 */
public class IntFromByteArrayTest extends TestCase {
	public void test() {
		byte[] test = { 1, 2, 3, 4, 5, 6, 7, 8 };
		IntFromByteArray ifba = new IntFromByteArray(true);
		int[] newArray = ifba.getIntArray(test);

		assertEquals(2, newArray.length);
		assertEquals(0x04030201, newArray[0]);
		assertEquals(0x08070605, newArray[1]);
	}
}
