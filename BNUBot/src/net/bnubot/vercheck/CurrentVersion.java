/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.vercheck;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Properties;

import net.bnubot.logging.Out;
import net.bnubot.util.SortedProperties;

/**
 * @author scotta
 */
public final class CurrentVersion {
	protected static ReleaseType RELEASE_TYPE = null;
	protected static Integer VER_MAJOR = null;
	protected static Integer VER_MINOR = null;
	protected static Integer VER_REVISION = null;
	protected static Integer VER_RELEASE = null;
	private static boolean VER_SVN_SET = false;
	protected static Integer VER_SVN_REVISION = null;
	private static VersionNumber VER = null;
	private static Date BUILD_DATE = null;
	private static boolean fromJar = false;

	private static final String sReleaseType = "RELEASE_TYPE";
	private static final String sVerMajor = "VER_MAJOR";
	private static final String sVerMinor = "VER_MINOR";
	private static final String sVerRevision = "VER_REVISION";
	private static final String sVerRelease = "VER_RELEASE";
	private static final String sVerSVNRevision = "VER_SVN_REVISION";
	private static final String sBuildDate = "BUILD_DATE";

	private static final Integer revision(File f) {
		if(VER_SVN_SET)
			return VER_SVN_REVISION;

		if(!f.exists())
			return null;

		Integer r = null;
		for(File sf : f.listFiles()) {
			if(sf.isDirectory()) {
				Integer r2 = revision(sf);
				if((r2 != null) && ((r == null) || (r2 > r)))
					r = r2;
				continue;
			}

			int ext = sf.getName().indexOf(".java");
			if(ext == -1)
				continue;
			if(ext != sf.getName().length() - 5)
				continue;

			try (FileReader fr = new FileReader(sf)) {
				boolean revisionFound = false;
				boolean authorFound = false;
				do {
					int i = fr.read();
					if(i == -1) { // <EOF>
						if(!revisionFound)
							Out.error(CurrentVersion.class, "Couldn't find Id: tag in " + sf.getPath());
						if(!authorFound)
							Out.error(CurrentVersion.class, "Couldn't find @author tag in " + sf.getPath());
						break;
					}

					// Search for "$Id: "
					if((i == '$')
					&& (fr.read() == 'I')
					&& (fr.read() == 'd')
					&& (fr.read() == ':')
					&& (fr.read() == ' ')) {
						// Skip over the filename
						String fileName = new String();
						do {
							int c = fr.read();
							if(c == ' ')
								break;
							fileName += (char)c;
						} while(true);

						if(!sf.getName().equals(fileName)) {
							Out.error(CurrentVersion.class, "File name in Id: tag doesn't match actual file name: " + sf.getPath());
							break;
						}

						// Read in the revision as a String
						String rev = "";
						do {
							int c = fr.read();
							if(c < '0')
								break;
							if(c > '9')
								break;

							rev += (char) c;
						} while(true);

						if(rev.length() == 0)
							continue;

						// Parse the long
						int r2 = Integer.parseInt(rev);
						if((r == null) || (r2 > r))
							r = r2;

						revisionFound = true;
					}

					// Search for "@author "
					if((i == '@')
					&& (fr.read() == 'a')
					&& (fr.read() == 'u')
					&& (fr.read() == 't')
					&& (fr.read() == 'h')
					&& (fr.read() == 'o')
					&& (fr.read() == 'r')
					&& (fr.read() == ' ')) {
						authorFound = true;
					}

					if(authorFound && revisionFound)
						break;
				} while(true);
			} catch(Exception e) {
				Out.exception(e);
			}
		}
		return r;
	}

	private static final Integer revision() {
		if(!VER_SVN_SET) {
			VER_SVN_REVISION = revision(new File("src"));
			VER_SVN_SET = true;
		}

		return VER_SVN_REVISION;
	}

	public static boolean fromJar() {
		version();
		return fromJar;
	}

	public static final VersionNumber version() {
		if(VER != null)
			return VER;

		try {
			String vpPath = "/net/bnubot/version.properties";
			// Eclipse likes to copy version.properties to bin; if it's there, delete it
			File f = new File("bin" + vpPath);
			if(f.exists())
				f.delete();
			f = null;
			// Try to load the file out of the JAR
			URL vp = VersionNumber.class.getResource(vpPath);
			InputStream is = null;
			try {
				is = vp.openStream();
				fromJar = true;
			} catch(NullPointerException e) {
				// Either the JAR is messed up, or we're running in the ide - look for the file in the working directory
				f = new File("src" + vpPath);
				if(f.exists())
					is = new FileInputStream(f);
			}
			if(is == null) {
				// Failed to determine the bot version
				Out.fatalException(new FileNotFoundException(vpPath));
			}

			Properties versionprops = new SortedProperties();
			versionprops.load(is);
			is.close();

			Integer VER_SVN_REVISION_FILE = null;
			if(versionprops.containsKey(sReleaseType))
				RELEASE_TYPE = ReleaseType.valueOf((String)versionprops.get(sReleaseType));
			if(versionprops.containsKey(sVerMajor))
				VER_MAJOR = Integer.parseInt((String)versionprops.get(sVerMajor));
			if(versionprops.containsKey(sVerMinor))
				VER_MINOR = Integer.parseInt((String)versionprops.get(sVerMinor));
			if(versionprops.containsKey(sVerRevision))
				VER_REVISION = Integer.parseInt((String)versionprops.get(sVerRevision));
			if(versionprops.containsKey(sVerRelease))
				VER_RELEASE = Integer.parseInt((String)versionprops.get(sVerRelease));
			if(versionprops.containsKey(sVerSVNRevision))
				VER_SVN_REVISION_FILE = Integer.parseInt((String)versionprops.get(sVerSVNRevision));

			// From a JAR has no src folder; from Eclipse has no JAR
			if(fromJar != (revision() == null))
				throw new IllegalStateException();

			if(fromJar) {
				BUILD_DATE = new Date(Long.parseLong(versionprops.getProperty(sBuildDate)));
				VER_SVN_REVISION = VER_SVN_REVISION_FILE;
			} else {
				BUILD_DATE = new Date();
				versionprops.setProperty(sBuildDate, Long.toString(BUILD_DATE.getTime()));

				if((VER_SVN_REVISION_FILE == null) || (VER_SVN_REVISION > VER_SVN_REVISION_FILE)) {
					Out.info(CurrentVersion.class, "File version (" + VER_SVN_REVISION_FILE + ") does not match calculated (" + VER_SVN_REVISION + ").");

					RELEASE_TYPE = ReleaseType.Development;
					versionprops.setProperty(sReleaseType, RELEASE_TYPE.name());
					versionprops.setProperty(sVerSVNRevision, Integer.toString(VER_SVN_REVISION));
				}
			}

			VER = new VersionNumber(RELEASE_TYPE, VER_MAJOR, VER_MINOR, VER_REVISION, VER_RELEASE, VER_SVN_REVISION, BUILD_DATE);
			if(!fromJar)
				versionprops.store(new FileOutputStream(f), VER.toString());
			return VER;
		} catch(Exception e) {
			Out.exception(e);
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	public static void setVersion(VersionNumber vnCurrent) {
		try {
			Properties versionprops = new SortedProperties();

			versionprops.setProperty(sReleaseType, vnCurrent.getReleaseType().name());
			versionprops.setProperty(sVerMajor, vnCurrent.getMajor().toString());
			versionprops.setProperty(sVerMinor, vnCurrent.getMinor().toString());
			versionprops.setProperty(sVerRevision, vnCurrent.getRevision().toString());
			versionprops.setProperty(sVerRelease, vnCurrent.getRelease().toString());
			versionprops.setProperty(sVerSVNRevision, vnCurrent.getSvnRevision().toString());
			versionprops.setProperty(sBuildDate, Long.toString(vnCurrent.getBuildDate().getTime()));

			File file = new File("src/net/bnubot/version.properties");
			versionprops.store(new FileOutputStream(file), VER.toString());
		} catch (Exception e) {
			Out.exception(e);
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
}
