/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.junit;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.bnubot.junit.util.BNetUserTest;
import net.bnubot.junit.util.Base64Test;
import net.bnubot.junit.util.CookieUtilityTest;
import net.bnubot.junit.util.HexDumpTest;
import net.bnubot.junit.util.task.TaskTest;
import net.bnubot.junit.vercheck.VersionNumberTest;

public class BNUBotTestSuite {
	
	public static Test suite() {
		TestSuite suite = new TestSuite("BNUBot");
		
		suite.addTestSuite(BNetUserTest.class);
		suite.addTestSuite(Base64Test.class);
		suite.addTestSuite(CookieUtilityTest.class);
		suite.addTestSuite(HexDumpTest.class);
		suite.addTestSuite(TaskTest.class);
		suite.addTestSuite(VersionNumberTest.class);
		
		return suite;
	}
}
