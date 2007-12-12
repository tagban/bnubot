/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.junit.util;

import net.bnubot.util.BNetUser;
import junit.framework.TestCase;

public class BNetUserTest extends TestCase {
	
	public void testSimpleConstructor() {
		BNetUser u = null;
		try {
			u = new BNetUser("test");
			fail("This should throw an IllegalStateException");
		} catch(IllegalStateException e) {}
		assertEquals(null, u);
		
		u = new BNetUser("testuser@Azeroth");
		assertEquals(u.getShortLogonName(), "testuser@Azeroth");
		assertEquals(u.getFullLogonName(), "testuser@Azeroth");
		assertEquals(u.getFullAccountName(), "testuser@Azeroth");
	}

	public void testSameRealm() {
		BNetUser u = new BNetUser("testuser@Azeroth", "Azeroth");
		assertEquals(u.getShortLogonName(), "testuser");
		assertEquals(u.getFullLogonName(), "testuser@Azeroth");
		assertEquals(u.getFullAccountName(), "testuser@Azeroth");
	}

	public void testNoRealm() {
		BNetUser u = new BNetUser("testuser", "Azeroth");
		assertEquals(u.getShortLogonName(), "testuser");
		assertEquals(u.getFullLogonName(), "testuser@Azeroth");
		assertEquals(u.getFullAccountName(), "testuser@Azeroth");
	}

	public void testDifferentRealm() {
		BNetUser u = new BNetUser("testuser@USEast", "Azeroth");
		assertEquals(u.getShortLogonName(), "testuser@USEast");
		assertEquals(u.getFullLogonName(), "testuser@USEast");
		assertEquals(u.getFullAccountName(), "testuser@USEast");
	}

	public void testDifferentRealm2() {
		BNetUser u = new BNetUser("testuser@Azeroth", "someone@USEast");
		assertEquals(u.getShortLogonName(), "testuser@Azeroth");
		assertEquals(u.getFullLogonName(), "testuser@Azeroth");
		assertEquals(u.getFullAccountName(), "testuser@Azeroth");
	}

	public void testNoRealm2() {
		BNetUser u = new BNetUser("testuser", "someone@USEast");
		assertEquals(u.getShortLogonName(), "testuser");
		assertEquals(u.getFullLogonName(), "testuser@USEast");
		assertEquals(u.getFullAccountName(), "testuser@USEast");
	}

	public void testSameRealm2() {
		BNetUser u = new BNetUser("testuser@USEast", "someone@USEast");
		assertEquals(u.getShortLogonName(), "testuser");
		assertEquals(u.getFullLogonName(), "testuser@USEast");
		assertEquals(u.getFullAccountName(), "testuser@USEast");
	}

	public void testSameRealmWithNumber() {
		BNetUser u = new BNetUser("testuser#2@USEast", "someone@USEast");
		assertEquals(u.getShortLogonName(), "testuser#2");
		assertEquals(u.getFullLogonName(), "testuser#2@USEast");
		assertEquals(u.getFullAccountName(), "testuser@USEast");
	}

	public void testNoRealmWithNumber() {
		BNetUser u = new BNetUser("testuser#2", "someone@USEast");
		assertEquals(u.getShortLogonName(), "testuser#2");
		assertEquals(u.getFullLogonName(), "testuser#2@USEast");
		assertEquals(u.getFullAccountName(), "testuser@USEast");
	}

	public void testDifferentRealmWithNumber() {
		BNetUser u = new BNetUser("testuser@Azeroth#2", "someone@USEast");
		assertEquals(u.getShortLogonName(), "testuser#2@Azeroth");
		assertEquals(u.getFullLogonName(), "testuser#2@Azeroth");
		assertEquals(u.getFullAccountName(), "testuser@Azeroth");
	}

	public void testDifferentRealmWithBothNumbers() {
		BNetUser u = new BNetUser("testuser#2@Azeroth", "someone#2@USEast");
		assertEquals(u.getShortLogonName(), "testuser#2@Azeroth");
		assertEquals(u.getFullLogonName(), "testuser#2@Azeroth");
		assertEquals(u.getFullAccountName(), "testuser@Azeroth");
	}

	public void testSameRealmWithBothNumbers() {
		BNetUser u = new BNetUser("testuser#2@USEast", "someone#2@USEast");
		assertEquals(u.getShortLogonName(), "testuser#2");
		assertEquals(u.getFullLogonName(), "testuser#2@USEast");
		assertEquals(u.getFullAccountName(), "testuser@USEast");
	}

	public void testSameRealmWithNumber2() {
		BNetUser u = new BNetUser("testuser@USEast", "someone#2@USEast");
		assertEquals(u.getShortLogonName(), "testuser");
		assertEquals(u.getFullLogonName(), "testuser@USEast");
		assertEquals(u.getFullAccountName(), "testuser@USEast");
	}
	
	public void testPerspective() {
		BNetUser u = new BNetUser("testuser@USEast");
		assertEquals(u.getShortLogonName(), "testuser@USEast");
		assertEquals(u.getFullLogonName(), "testuser@USEast");
		assertEquals(u.getFullAccountName(), "testuser@USEast");
		
		BNetUser u2 = u.toPerspective(new BNetUser("otherguy@Azeroth"));
		assertEquals(u2.getShortLogonName(), "testuser@USEast");
		assertEquals(u2.getFullLogonName(), "testuser@USEast");
		assertEquals(u2.getFullAccountName(), "testuser@USEast");
		
		BNetUser u3 = u.toPerspective(new BNetUser("otherguy2#4@USEast"));
		assertEquals(u3.getShortLogonName(), "testuser");
		assertEquals(u3.getFullLogonName(), "testuser@USEast");
		assertEquals(u3.getFullAccountName(), "testuser@USEast");
	}
}
