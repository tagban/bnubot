/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author scotta
 */
public class BNUBotTestSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite("BNUBot");
		//$JUnit-BEGIN$

		suite.addTestSuite(net.bnubot.util.BNetInputStreamTest.class);
		suite.addTestSuite(net.bnubot.util.BNetOutputStreamTest.class);
		suite.addTestSuite(net.bnubot.util.BNetUserTest.class);
		suite.addTestSuite(net.bnubot.util.ByteArrayTest.class);
		suite.addTestSuite(net.bnubot.util.crypto.Base64Test.class);
		suite.addTestSuite(net.bnubot.util.crypto.DMCryptoTest.class);
		suite.addTestSuite(net.bnubot.util.CookieUtilityTest.class);
		suite.addTestSuite(net.bnubot.util.crypto.HexDumpTest.class);
		suite.addTestSuite(net.bnubot.util.SHA1SumTest.class);
		suite.addTestSuite(net.bnubot.util.StatStringTest.class);
		suite.addTestSuite(net.bnubot.util.TimeFormatterTest.class);
		suite.addTestSuite(net.bnubot.util.WildcardTest.class);
		suite.addTestSuite(net.bnubot.util.task.TaskTest.class);
		suite.addTestSuite(net.bnubot.vercheck.VersionNumberTest.class);

		//$JUnit-END$
		return suite;
	}
}
