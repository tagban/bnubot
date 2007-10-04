/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.junit;

import junit.framework.Test;
import junit.framework.TestSuite;

public class BNUBotTestSuite {
	
	public static Test suite() {
		TestSuite suite = new TestSuite("BNUBot");
		
		suite.addTestSuite(net.bnubot.junit.core.BNetUserTest.class);
		suite.addTestSuite(net.bnubot.junit.core.CookieUtilityTest.class);

		suite.addTestSuite(net.bnubot.junit.util.Base64Test.class);
		suite.addTestSuite(net.bnubot.junit.util.HexDumpTest.class);
		
		return suite;
	}
}