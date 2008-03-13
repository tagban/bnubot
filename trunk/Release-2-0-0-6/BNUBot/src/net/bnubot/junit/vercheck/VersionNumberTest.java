/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.junit.vercheck;

import net.bnubot.vercheck.ReleaseType;
import net.bnubot.vercheck.VersionNumber;
import junit.framework.TestCase;

public class VersionNumberTest extends TestCase {
	public void testVersionCompare() {
		VersionNumber dev_1_0_0_10 = new VersionNumber(ReleaseType.Development, 1, 0, 0, null, 10, null);
		VersionNumber stable_1_0_0_10 = new VersionNumber(ReleaseType.Stable, 1, 0, 0, null, 10, null);
		assertEquals(0, dev_1_0_0_10.compareTo(stable_1_0_0_10));
	}
}
