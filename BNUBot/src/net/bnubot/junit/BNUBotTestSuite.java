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

		suite.addTestSuite(net.bnubot.junit.util.BNetUserTest.class);
		suite.addTestSuite(net.bnubot.junit.util.crypto.Base64Test.class);
		suite.addTestSuite(net.bnubot.junit.util.CookieUtilityTest.class);
		suite.addTestSuite(net.bnubot.junit.util.crypto.HexDumpTest.class);
		suite.addTestSuite(net.bnubot.junit.util.StatStringTest.class);
		suite.addTestSuite(net.bnubot.junit.util.WildcardTest.class);
		suite.addTestSuite(net.bnubot.junit.util.task.TaskTest.class);
		suite.addTestSuite(net.bnubot.junit.vercheck.VersionNumberTest.class);

		return suite;
	}
}
