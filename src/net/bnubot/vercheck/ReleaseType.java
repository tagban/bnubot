/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.vercheck;

public enum ReleaseType {
	Stable(0),
	ReleaseCandidate(1),
	Beta(2),
	Alpha(3), 
	Development(4);
	
	private int t;
	private ReleaseType(int t) {
		this.t = t;
	}
	
	public boolean isDevelopment() {
		return (t >= Development.t);
	}
	
	public boolean isAlpha() {
		return (t >= Alpha.t);
	}
	
	public boolean isBeta() {
		return (t >= Beta.t);
	}
	
	public boolean isReleaseCandidate() {
		return (t >= ReleaseCandidate.t);
	}
}
