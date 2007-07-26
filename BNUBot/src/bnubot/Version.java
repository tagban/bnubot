/**
 * This file is distributed under the GPL 
 * $Id$
 */

package bnubot;

import java.io.*;

public final class Version {
	public static final Integer VER_MAJOR = 2;
	public static final Integer VER_MINOR = 0;
	public static final Integer VER_REVISION = 0;
	public static final Integer VER_RELEASE_CANDIDATE = null;
	public static final Integer VER_ALPHA = null;
	public static final Integer VER_BETA = 3;
	private static Integer VER_SVN_REVISION = null;
	private static String VER_STRING = null;
	
	private static final Integer revision(File f) {
		Integer r = null;
		for(File sf : f.listFiles()) {
			if(sf.isDirectory()) {
				Integer r2 = revision(sf);
				if((r2 != null) && ((r == null) || (r2 > r)))
					r = r2;
				continue;
			}
			
			int ext = sf.getName().indexOf(".java");
			if(ext != sf.getName().length() - 5)
				continue;
			
			try {
				FileReader fr = new FileReader(sf);
				do {
					int i = fr.read();
					if(i == -1) { // <EOF>
						System.err.println("Couldn't find Id: tag in " + sf.getPath());
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
		if(VER_SVN_REVISION == null)
			VER_SVN_REVISION = revision(new File("."));
		
		return VER_SVN_REVISION;
	}
	
	public static final String version() {
		if(VER_STRING != null)
			return VER_STRING;
		
		VER_STRING = VER_MAJOR.toString() + '.' + VER_MINOR.toString() + '.' + VER_REVISION.toString();
		if(VER_ALPHA != null)
			VER_STRING += " alpha " + VER_ALPHA.toString();
		else if(VER_BETA != null)
			VER_STRING += " beta " + VER_BETA.toString();
		else if(VER_RELEASE_CANDIDATE != null)
			VER_STRING += " RC " + VER_RELEASE_CANDIDATE.toString();
		
		if(revision() != null)
			VER_STRING += " (r" + VER_SVN_REVISION.toString() + ")";
		
		return VER_STRING;
	}
}
