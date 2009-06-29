/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util;

import junit.framework.TestCase;

/**
 * @author scotta
 */
public class BNetUserTest extends TestCase {

	public void testSimpleConstructor() {
		BNetUser u = null;
		try {
			u = new BNetUser(null, "test", (String)null);
			fail("This should throw an IllegalStateException");
		} catch(IllegalStateException e) {}
		assertEquals(null, u);
	}

	public void testSameRealm() {
		BNetUser u = new BNetUser(null, "testuser@Azeroth", "Azeroth");
		assertEquals(u.getShortLogonName(), "testuser");
		assertEquals(u.getFullLogonName(), "testuser@Azeroth");
		assertEquals(u.getFullAccountName(), "testuser@Azeroth");
	}

	public void testNoRealm() {
		BNetUser u = new BNetUser(null, "testuser", "Azeroth");
		assertEquals(u.getShortLogonName(), "testuser");
		assertEquals(u.getFullLogonName(), "testuser@Azeroth");
		assertEquals(u.getFullAccountName(), "testuser@Azeroth");
	}

	public void testDifferentRealm() {
		BNetUser u = new BNetUser(null, "testuser@USEast", "Azeroth");
		assertEquals(u.getShortLogonName(), "testuser@USEast");
		assertEquals(u.getFullLogonName(), "testuser@USEast");
		assertEquals(u.getFullAccountName(), "testuser@USEast");
	}

	public void testSameRealmWithNumber() {
		BNetUser u = new BNetUser(null, "testuser#2@USEast", "USEast");
		assertEquals(u.getShortLogonName(), "testuser#2");
		assertEquals(u.getFullLogonName(), "testuser#2@USEast");
		assertEquals(u.getFullAccountName(), "testuser@USEast");
	}

	public void testNoRealmWithNumber() {
		BNetUser u = new BNetUser(null, "testuser#2", "USEast");
		assertEquals(u.getShortLogonName(), "testuser#2");
		assertEquals(u.getFullLogonName(), "testuser#2@USEast");
		assertEquals(u.getFullAccountName(), "testuser@USEast");
	}

	public void testDifferentRealmWithNumber() {
		BNetUser u = new BNetUser(null, "testuser@Azeroth#2", "USEast");
		assertEquals(u.getShortLogonName(), "testuser#2@Azeroth");
		assertEquals(u.getFullLogonName(), "testuser#2@Azeroth");
		assertEquals(u.getFullAccountName(), "testuser@Azeroth");
	}

	public void testIlly() {
		BNetUser viewer = new BNetUser(null, "BNU-Camel", "USEast");
		BNetUser banme = new BNetUser(null, "($@$@$@)", "USEast");
		assertEquals("($@$@$@)", banme.getShortLogonName(viewer));
	}

	public void testIlly2() {
		BNetUser viewer = new BNetUser(null, "BNU-Camel", "Azeroth");
		BNetUser banme = new BNetUser(null, "($@$@$@)@USEast", "Azeroth");
		assertEquals("($@$@$@)@USEast", banme.getShortLogonName(viewer));
	}
}
