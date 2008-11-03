/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.vercheck;

import java.util.Date;

import junit.framework.TestCase;

/**
 * @author scotta
 */
public class VersionNumberTest extends TestCase {
	public void testGetters() {
		VersionNumber vn = new VersionNumber(ReleaseType.Stable, 0, 1, 2, 3, 4, new Date(5));
		assertEquals(ReleaseType.Stable, vn.getReleaseType());
		assertEquals(0, vn.getMajor().intValue());
		assertEquals(1, vn.getMinor().intValue());
		assertEquals(2, vn.getRevision().intValue());
		assertEquals(3, vn.getRelease().intValue());
		assertEquals(4, vn.getSvnRevision().intValue());
		assertEquals(new Date(5), vn.getBuildDate());
	}

	public void testCompareMajor() {
		VersionNumber a = new VersionNumber(ReleaseType.Stable, 0, 0, 0, 0);
		VersionNumber b = new VersionNumber(ReleaseType.Stable, 1, 0, 0, 0);
		assertEquals(-1, a.compareTo(b));
		assertEquals(1, b.compareTo(a));
	}

	public void testCompareMinor() {
		VersionNumber a = new VersionNumber(ReleaseType.Stable, 0, 0, 0, 0);
		VersionNumber b = new VersionNumber(ReleaseType.Stable, 0, 1, 0, 0);
		assertEquals(-1, a.compareTo(b));
		assertEquals(1, b.compareTo(a));
	}

	public void testCompareRevision() {
		VersionNumber a = new VersionNumber(ReleaseType.Stable, 0, 0, 0, 0);
		VersionNumber b = new VersionNumber(ReleaseType.Stable, 0, 0, 1, 0);
		assertEquals(-1, a.compareTo(b));
		assertEquals(1, b.compareTo(a));
	}

	public void testCompareRelease() {
		VersionNumber a = new VersionNumber(ReleaseType.Stable, 0, 0, 0, 0);
		VersionNumber b = new VersionNumber(ReleaseType.Stable, 0, 0, 0, 1);
		assertEquals(-1, a.compareTo(b));
		assertEquals(1, b.compareTo(a));
	}

	public void testCompareSVNRevision() {
		VersionNumber a = new VersionNumber(ReleaseType.Stable, 0, 0, 0, 0, 0, null);
		VersionNumber b = new VersionNumber(ReleaseType.Stable, 0, 0, 0, 0, 1, null);
		assertEquals(-1, a.compareTo(b));
		assertEquals(1, b.compareTo(a));
	}

	public void testCompareBuildDate() {
		VersionNumber a = new VersionNumber(ReleaseType.Stable, 0, 0, 0, 0, null, new Date(0));
		VersionNumber b = new VersionNumber(ReleaseType.Stable, 0, 0, 0, 0, null, new Date(1));
		assertEquals(-1, a.compareTo(b));
		assertEquals(1, b.compareTo(a));
	}

	public void testCompareReleaseType() {
		VersionNumber dev = new VersionNumber(ReleaseType.Development, 1, 0, 0, 10);
		VersionNumber stable = new VersionNumber(ReleaseType.Stable, 1, 0, 0, 10);
		assertEquals(0, dev.compareTo(stable));
	}

}
