/**
 * This file is distributed under the GPL 
 * $Id$
 */

package bnubot.junit;

import junit.framework.*;

public class BNUBotTestSuite {
	
	public static Test suite() {
		TestSuite suite = new TestSuite("BNUBot");
		
		suite.addTestSuite(bnubot.junit.core.BNetUserTest.class);
		suite.addTestSuite(bnubot.junit.core.CookieUtilityTest.class);

		suite.addTestSuite(bnubot.junit.util.HexDumpTest.class);
		
		return suite;
	}
}
