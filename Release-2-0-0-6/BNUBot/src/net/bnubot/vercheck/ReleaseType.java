/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.vercheck;

public enum ReleaseType {
	Stable,
	ReleaseCandidate,
	Beta,
	Alpha, 
	Development;
	
	public boolean isDevelopment() {
		return (this == Development);
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
