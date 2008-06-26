/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util;

import junit.framework.TestCase;

/**
 * @author scotta
 */
public class TimeFormatterTest extends TestCase {
	public void testParseDuration() {
		assertEquals(TimeFormatter.SECOND, TimeFormatter.parseDuration("1s"));
		assertEquals(2 * TimeFormatter.SECOND, TimeFormatter.parseDuration("2s"));
		assertEquals(TimeFormatter.MINUTE, TimeFormatter.parseDuration("1m"));
		assertEquals(TimeFormatter.HOUR, TimeFormatter.parseDuration("1h"));
		assertEquals(TimeFormatter.DAY, TimeFormatter.parseDuration("1d"));
		assertEquals(TimeFormatter.WEEK, TimeFormatter.parseDuration("1w"));
		assertEquals(TimeFormatter.WEEK
				+ TimeFormatter.DAY
				+ TimeFormatter.HOUR
				+ TimeFormatter.MINUTE
				+ TimeFormatter.SECOND,
				TimeFormatter.parseDuration("1w 1d 1h 1m 1s"));

		// And for the grand finale, test some poorly formed input
		assertEquals(2 * TimeFormatter.SECOND, TimeFormatter.parseDuration(" 1s  1s "));
	}
}
