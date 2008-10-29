/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.vercheck;

import java.util.Date;

/**
 * @author scotta
 */
public class VersionNumber {
	private ReleaseType RELEASE_TYPE = null;
	private Integer VER_MAJOR = null;
	private Integer VER_MINOR = null;
	private Integer VER_REVISION = null;
	private Integer VER_RELEASE = null;
	private Integer VER_SVN_REVISION = null;
	private String VER_STRING = null;
	private Date BUILD_DATE = null;

	public VersionNumber(ReleaseType rt, Integer major, Integer minor, Integer revision, Integer release) {
		RELEASE_TYPE = rt;
		VER_MAJOR = major;
		VER_MINOR = minor;
		VER_REVISION = revision;
		VER_RELEASE = release;
	}

	public VersionNumber(ReleaseType rt, Integer major, Integer minor, Integer revision, Integer release, Integer svn, Date builddate) {
		this(rt, major, minor, revision, release);
		VER_SVN_REVISION = svn;
		BUILD_DATE = builddate;
	}

	@Override
	public String toString() {
		if(VER_STRING != null)
			return VER_STRING;

		VER_STRING = VER_MAJOR.toString() + '.' + VER_MINOR.toString() + '.' + VER_REVISION.toString();
		if((VER_RELEASE != null) && (VER_RELEASE != 0))
			VER_STRING += " Release " + VER_RELEASE.toString();
		if(RELEASE_TYPE.isDevelopment())
			VER_STRING += " Development";
		else if(RELEASE_TYPE.isNightly())
			VER_STRING += " Nightly Build";
		else if(RELEASE_TYPE.isAlpha())
			VER_STRING += " Alpha";
		else if(RELEASE_TYPE.isBeta())
			VER_STRING += " Beta";
		else if(RELEASE_TYPE.isReleaseCandidate())
			VER_STRING += " RC";

		if(VER_SVN_REVISION != null)
			VER_STRING += " (r" + VER_SVN_REVISION.toString() + ")";

		return VER_STRING;
	}

	public String toFileName() {
		StringBuilder sb = new StringBuilder("BNUBot");
		sb.append('-').append(VER_MAJOR);
		sb.append('-').append(VER_MINOR);
		sb.append('-').append(VER_REVISION);
		sb.append('-').append(VER_RELEASE);
		sb.append('-').append(RELEASE_TYPE);
		sb.append("-r").append(VER_SVN_REVISION);
		sb.append(".jar");
		return sb.toString();
	}

	public Date getBuildDate() {
		return BUILD_DATE;
	}

	public void setBuildDate(Date buildDate) {
		BUILD_DATE = buildDate;
	}

	public boolean isNewerThan(VersionNumber vn) {
		return compareTo(vn) > 0;
	}

	public int compareTo(VersionNumber vn) {
		// Check SVN revision
		if((VER_SVN_REVISION != null) && (vn.VER_SVN_REVISION != null))
			return VER_SVN_REVISION.compareTo(vn.VER_SVN_REVISION);

		// Check Major
		if(VER_MAJOR > vn.VER_MAJOR) return 1;
		if(VER_MAJOR < vn.VER_MAJOR) return -1;

		// Check Minor
		if(VER_MINOR > vn.VER_MINOR) return 1;
		if(VER_MINOR < vn.VER_MINOR) return -1;

		// Check Revision
		if(VER_REVISION > vn.VER_REVISION) return 1;
		if(VER_REVISION < vn.VER_REVISION) return -1;

		// Check Release
		if(VER_RELEASE > vn.VER_RELEASE) return 1;
		if(VER_RELEASE < vn.VER_RELEASE) return -1;

		// Check Build Date
		if((BUILD_DATE != null) && (vn.BUILD_DATE != null))
			return BUILD_DATE.compareTo(vn.BUILD_DATE);

		// They are the same!
		return 0;
	}

	public ReleaseType getReleaseType() {
		return RELEASE_TYPE;
	}

	public void setReleaseType(ReleaseType releaseType) {
		RELEASE_TYPE = releaseType;
		VER_STRING = null;
	}

	public Integer getMajor() {
		return VER_MAJOR;
	}

	public void setMajor(Integer major) {
		VER_MAJOR = major;
		VER_STRING = null;
	}

	public Integer getMinor() {
		return VER_MINOR;
	}

	public void setMinor(Integer minor) {
		VER_MINOR = minor;
		VER_STRING = null;
	}

	public Integer getRevision() {
		return VER_REVISION;
	}

	public void setRevision(Integer revision) {
		VER_REVISION = revision;
		VER_STRING = null;
	}

	public Integer getRelease() {
		return VER_RELEASE;
	}

	public void setRelease(Integer release) {
		VER_RELEASE = release;
		VER_STRING = null;
	}

	public Integer getSvnRevision() {
		return VER_SVN_REVISION;
	}
}
