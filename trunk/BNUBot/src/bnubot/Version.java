package bnubot;

public final class Version {
	public static final Integer VER_MAJOR = 2;
	public static final Integer VER_MINOR = 0;
	public static final Integer VER_REVISION = 0;
	public static final Integer VER_RELEASE_CANDIDATE = null;
	public static final Integer VER_ALPHA = null;
	public static final Integer VER_BETA = 3;
	private static String VER_STRING = null;
	
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
	
		VER_STRING += "$Revision$";
		VER_STRING += "Version: $Id$";
		
		return VER_STRING;
	}
}
