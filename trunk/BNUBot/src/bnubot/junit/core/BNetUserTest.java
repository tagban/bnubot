package bnubot.junit.core;

import bnubot.core.BNetUser;
import junit.framework.TestCase;

public class BNetUserTest extends TestCase {

	public void testSomeBehavior() {
		BNetUser u = new BNetUser("testuser@Azeroth", "Azeroth");
		assertEquals(u.getShortLogonName(), "testuser");
		assertEquals(u.getFullLogonName(), "testuser@Azeroth");
		assertEquals(u.getFullAccountName(), "testuser@Azeroth");
		
		u = new BNetUser("testuser", "Azeroth");
		assertEquals(u.getShortLogonName(), "testuser");
		assertEquals(u.getFullLogonName(), "testuser@Azeroth");
		assertEquals(u.getFullAccountName(), "testuser@Azeroth");
		
		u = new BNetUser("testuser@USEast", "Azeroth");
		assertEquals(u.getShortLogonName(), "testuser@USEast");
		assertEquals(u.getFullLogonName(), "testuser@USEast");
		assertEquals(u.getFullAccountName(), "testuser@USEast");
		
		u = new BNetUser("testuser@Azeroth", "someone@USEast");
		assertEquals(u.getShortLogonName(), "testuser@Azeroth");
		assertEquals(u.getFullLogonName(), "testuser@Azeroth");
		assertEquals(u.getFullAccountName(), "testuser@Azeroth");
		
		u = new BNetUser("testuser", "someone@USEast");
		assertEquals(u.getShortLogonName(), "testuser");
		assertEquals(u.getFullLogonName(), "testuser@USEast");
		assertEquals(u.getFullAccountName(), "testuser@USEast");
		
		u = new BNetUser("testuser@USEast", "someone@USEast");
		assertEquals(u.getShortLogonName(), "testuser");
		assertEquals(u.getFullLogonName(), "testuser@USEast");
		assertEquals(u.getFullAccountName(), "testuser@USEast");
		
		u = new BNetUser("testuser@USEast", "someone@USEast");
		assertEquals(u.getShortLogonName(), "testuser");
		assertEquals(u.getFullLogonName(), "testuser@USEast");
		assertEquals(u.getFullAccountName(), "testuser@USEast");
		
		u = new BNetUser("testuser@USEast#2", "someone@USEast");
		assertEquals(u.getShortLogonName(), "testuser#2");
		assertEquals(u.getFullLogonName(), "testuser@USEast#2");
		assertEquals(u.getFullAccountName(), "testuser@USEast");
		
		u = new BNetUser("testuser#2", "someone@USEast");
		assertEquals(u.getShortLogonName(), "testuser#2");
		assertEquals(u.getFullLogonName(), "testuser@USEast#2");
		assertEquals(u.getFullAccountName(), "testuser@USEast");
		
		u = new BNetUser("testuser@Azeroth#2", "someone@USEast");
		assertEquals(u.getShortLogonName(), "testuser@Azeroth#2");
		assertEquals(u.getFullLogonName(), "testuser@Azeroth#2");
		assertEquals(u.getFullAccountName(), "testuser@Azeroth");
	}
}
