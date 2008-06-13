/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot;

import junit.framework.Test;
import junit.framework.TestSuite;

public class BNUBotTestSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite("BNUBot");

		suite.addTestSuite(net.bnubot.util.BNetUserTest.class);
		suite.addTestSuite(net.bnubot.util.crypto.Base64Test.class);
		suite.addTestSuite(net.bnubot.util.crypto.DMCryptoTest.class);
		suite.addTestSuite(net.bnubot.util.CookieUtilityTest.class);
		suite.addTestSuite(net.bnubot.util.crypto.HexDumpTest.class);
		suite.addTestSuite(net.bnubot.util.StatStringTest.class);
		suite.addTestSuite(net.bnubot.util.TimeFormatterTest.class);
		suite.addTestSuite(net.bnubot.util.WildcardTest.class);
		suite.addTestSuite(net.bnubot.util.task.TaskTest.class);
		suite.addTestSuite(net.bnubot.vercheck.VersionNumberTest.class);

		return suite;
	}
}
