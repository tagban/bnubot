/**
 * This file is distributed under the GPL 
 * $Id$
 */

package bnubot.vercheck;

public class VersionNumber {
	private Integer VER_MAJOR = null;
	private Integer VER_MINOR = null;
	private Integer VER_REVISION = null;
	private Integer VER_ALPHA = null;
	private Integer VER_BETA = null;
	private Integer VER_RELEASE_CANDIDATE = null;
	private Integer VER_SVN_REVISION = null;
	private String VER_STRING = null;

	public VersionNumber(Integer major, Integer minor, Integer revision, Integer alpha, Integer beta, Integer rc) {
		VER_MAJOR = major;
		VER_MINOR = minor;
		VER_REVISION = revision;
		VER_ALPHA = alpha;
		VER_BETA = beta;
		VER_RELEASE_CANDIDATE = rc;
	}
	
	public VersionNumber(Integer major, Integer minor, Integer revision, Integer alpha, Integer beta, Integer rc, Integer svn) {
		this(major, minor, revision, alpha, beta, rc);
		VER_SVN_REVISION = svn;
	}
	
	public String toString() {
		if(VER_STRING != null)
			return VER_STRING;
		
		VER_STRING = VER_MAJOR.toString() + '.' + VER_MINOR.toString() + '.' + VER_REVISION.toString();
		if(VER_ALPHA != null)
			VER_STRING += " alpha " + VER_ALPHA.toString();
		else if(VER_BETA != null)
			VER_STRING += " beta " + VER_BETA.toString();
		else if(VER_RELEASE_CANDIDATE != null)
			VER_STRING += " RC " + VER_RELEASE_CANDIDATE.toString();
		
		if(VER_SVN_REVISION != null)
			VER_STRING += " (r" + VER_SVN_REVISION.toString() + ")";
		
		return VER_STRING;
	}
	
	public boolean isNewerThan(VersionNumber vn) {
		if(VER_MAJOR > vn.VER_MAJOR) return true;
		if(VER_MAJOR < vn.VER_MAJOR) return false;
		if(VER_MINOR > vn.VER_MINOR) return true;
		if(VER_MINOR < vn.VER_MINOR) return false;
		if(VER_REVISION > vn.VER_REVISION) return true;
		if(VER_REVISION < vn.VER_REVISION) return false;

		if(VER_RELEASE_CANDIDATE != vn.VER_RELEASE_CANDIDATE) {
			if(VER_RELEASE_CANDIDATE == null)
				return false;
			if(vn.VER_RELEASE_CANDIDATE == null)
				return true;
			if(VER_RELEASE_CANDIDATE > vn.VER_RELEASE_CANDIDATE) return true;
			if(VER_RELEASE_CANDIDATE < vn.VER_RELEASE_CANDIDATE) return false;
		}

		if(VER_BETA != vn.VER_BETA) {
			if(VER_BETA == null)
				return false;
			if(vn.VER_BETA == null)
				return true;
			if(VER_BETA > vn.VER_BETA) return true;
			if(VER_BETA < vn.VER_BETA) return false;
		}

		if(VER_ALPHA != vn.VER_ALPHA) {
			if(VER_ALPHA == null)
				return false;
			if(vn.VER_ALPHA == null)
				return true;
			if(VER_ALPHA > vn.VER_ALPHA) return true;
			if(VER_ALPHA < vn.VER_ALPHA) return false;
		}
		
		return false;
	}
}
