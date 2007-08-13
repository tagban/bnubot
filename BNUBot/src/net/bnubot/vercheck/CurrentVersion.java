/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.vercheck;

import java.io.*;
import java.net.URL;
import java.util.Date;
import java.util.Properties;

import net.bnubot.util.Out;

public final class CurrentVersion {
	protected static Integer VER_MAJOR = null;
	protected static Integer VER_MINOR = null;
	protected static Integer VER_REVISION = null;
	protected static Integer VER_RELEASE_CANDIDATE = null;
	protected static Integer VER_ALPHA = null;
	protected static Integer VER_BETA = null;
	private static boolean VER_SVN_SET = false;
	protected static Integer VER_SVN_REVISION = null;
	private static VersionNumber VER = null;
	private static String BUILD_DATE = null;
	
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
			
			try {
				FileReader fr = new FileReader(sf);
				do {
					int i = fr.read();
					if(i == -1) { // <EOF>
						Out.error(CurrentVersion.class, "Couldn't find Id: tag in " + sf.getPath());
						break;
					}
					
					// Search for "$Id: " 
					if(i != '$')
						continue;
					if(fr.read() != 'I')
						continue;
					if(fr.read() != 'd')
						continue;
					if(fr.read() != ':')
						continue;
					if(fr.read() != ' ')
						continue;

					// Skip over the filename
					while(fr.read() != ' ');
					
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
					
					break;
				} while(true);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return r;
	}
	
	public static final Integer revision() {
		if(!VER_SVN_SET) {
			VER_SVN_REVISION = revision(new File("src"));
			VER_SVN_SET = true;
		}
		
		return VER_SVN_REVISION;
	}
	
	public static final VersionNumber version() {
		if(VER != null)
			return VER;
		
		try {
			String vpPath = "/net/bnubot/version.properties";
			// Try to load the file out of the JAR
			URL vp = VersionNumber.class.getResource(vpPath);
			File f = null;
			InputStream is = null;
			try {
				is = vp.openStream();
			} catch(NullPointerException e) {
				// Either the JAR is messed up, or we're running in the ide - look for the file in the working directory
				f = new File("." + vpPath);
				if(f.exists())
					is = new FileInputStream(f);
			}
			if(is == null) {
				// Failed to determine the bot version
				new FileNotFoundException(vpPath).printStackTrace();
				System.exit(1);
			}
			
			Properties versionprops = new Properties();
			versionprops.load(is);
			is.close();
			
			Integer VER_SVN_REVISION_FILE = null;
			if(versionprops.containsKey("VER_MAJOR"))
				VER_MAJOR = Integer.parseInt((String)versionprops.get("VER_MAJOR"));
			if(versionprops.containsKey("VER_MINOR"))
				VER_MINOR = Integer.parseInt((String)versionprops.get("VER_MINOR"));
			if(versionprops.containsKey("VER_REVISION"))
				VER_REVISION = Integer.parseInt((String)versionprops.get("VER_REVISION"));
			if(versionprops.containsKey("VER_RELEASE_CANDIDATE"))
				VER_RELEASE_CANDIDATE = Integer.parseInt((String)versionprops.get("VER_RELEASE_CANDIDATE"));
			if(versionprops.containsKey("VER_ALPHA"))
				VER_ALPHA = Integer.parseInt((String)versionprops.get("VER_ALPHA"));
			if(versionprops.containsKey("VER_BETA"))
				VER_BETA = Integer.parseInt((String)versionprops.get("VER_BETA"));
			if(versionprops.containsKey("VER_SVN_REVISION"))
				VER_SVN_REVISION_FILE = Integer.parseInt((String)versionprops.get("VER_SVN_REVISION"));
			
			if(f == null)
				BUILD_DATE = versionprops.getProperty("BUILD_DATE");
			else {
				BUILD_DATE = new Date().toString();
				versionprops.setProperty("BUILD_DATE", BUILD_DATE);
			}
			
			if(revision() == null) {
				VER_SVN_REVISION = VER_SVN_REVISION_FILE;
			} else {
				if((VER_SVN_REVISION_FILE == null) || (VER_SVN_REVISION > VER_SVN_REVISION_FILE)) {
					Out.info(CurrentVersion.class, "File version is " + VER_SVN_REVISION_FILE);
					Out.info(CurrentVersion.class, "Calculated version is " + VER_SVN_REVISION);
					
					if((f != null) && (f.exists())) {
						versionprops.setProperty("VER_SVN_REVISION", Integer.toString(VER_SVN_REVISION));
						versionprops.store(new FileOutputStream(f), null);
					}
				}
			}
			
			VER = new VersionNumber(VER_MAJOR, VER_MINOR, VER_REVISION, VER_ALPHA, VER_BETA, VER_RELEASE_CANDIDATE, revision(), BUILD_DATE);
			return VER;
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		throw new NullPointerException();
	}
}
