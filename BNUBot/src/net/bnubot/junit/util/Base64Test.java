/**
 * 
 */
package net.bnubot.junit.util;

import junit.framework.TestCase;
import net.bnubot.util.Base64;

/**
 * @author sanderson
 *
 */
public class Base64Test extends TestCase {

	public void testEncode() {
		assertEquals("YXNkZg==", Base64.encode("asdf"));
	}
	
	public void testDecode() {
		assertEquals("asdf", Base64.decode("YXNkZg=="));
	}
}
