/**
 * This file is distributed under the GPL
 * $Id$
 */

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author scotta
 */
public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for default package");
		//$JUnit-BEGIN$

		suite.addTest(net.bnubot.BNUBotTestSuite.suite());
		suite.addTest(org.jbls.JBLSTestSuite.suite());

		//$JUnit-END$
		return suite;
	}

}
