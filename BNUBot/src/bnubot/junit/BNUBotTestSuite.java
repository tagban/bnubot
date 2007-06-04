package bnubot.junit;

import junit.framework.*;

public class BNUBotTestSuite {
	
	public static Test suite() {
		TestSuite suite = new TestSuite("BNUBot");
		
		suite.addTestSuite(bnubot.junit.core.BNetUserTest.class);
		
		return suite;
	}
}
