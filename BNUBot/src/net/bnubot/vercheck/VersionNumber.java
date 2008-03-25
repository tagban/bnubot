/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.vercheck;

import java.util.Date;

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

		// Check Stable
		if(vn.RELEASE_TYPE.isStable() ^ !RELEASE_TYPE.isStable())
			return RELEASE_TYPE.isStable() ? 1 : -1;

		// Check RC
		if(vn.RELEASE_TYPE.isReleaseCandidate() ^ !RELEASE_TYPE.isReleaseCandidate())
			return RELEASE_TYPE.isReleaseCandidate() ? 1 : -1;

		// Check Beta
		if(vn.RELEASE_TYPE.isBeta() ^ !RELEASE_TYPE.isBeta())
			return RELEASE_TYPE.isBeta() ? 1 : -1;

		// Check Alpha
		if(vn.RELEASE_TYPE.isAlpha() ^ !RELEASE_TYPE.isAlpha())
			return RELEASE_TYPE.isAlpha() ? 1 : -1;

		// Check Development
		if(vn.RELEASE_TYPE.isDevelopment() ^ !RELEASE_TYPE.isDevelopment())
			return RELEASE_TYPE.isDevelopment() ? 1 : -1;

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

	public Integer revision() {
		return VER_SVN_REVISION;
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
}
