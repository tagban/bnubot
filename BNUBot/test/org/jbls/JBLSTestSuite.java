/**
 * This file is distributed under the GPL
 * $Id$
 */
package org.jbls;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author scotta
 */
public class JBLSTestSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite("JBLS");
		//$JUnit-BEGIN$

		suite.addTestSuite(org.jbls.util.ByteFromIntArrayTest.class);
		suite.addTestSuite(org.jbls.util.IntFromByteArrayTest.class);

		//$JUnit-END$
		return suite;
	}

}
