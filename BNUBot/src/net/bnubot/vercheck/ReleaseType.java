/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.vercheck;

/**
 * @author scotta
 */
public enum ReleaseType {
	Stable,
	ReleaseCandidate,
	Beta,
	Alpha,
	Nightly,
	Development;

	public boolean isDevelopment() {
		return (this == Development);
	}

	public boolean isNightly() {
		return (this == Nightly);
	}

	public boolean isAlpha() {
		return (this == Alpha);
	}

	public boolean isBeta() {
		return (this == Beta);
	}

	public boolean isReleaseCandidate() {
		return (this == ReleaseCandidate);
	}

	public boolean isStable() {
		return (this == Stable);
	}
}
