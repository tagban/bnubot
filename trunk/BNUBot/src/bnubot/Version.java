package bnubot;

public final class Version {
	public static final Integer VER_MAJOR = 2;
	public static final Integer VER_MINOR = 0;
	public static final Integer VER_REVISION = 0;
	public static final Integer VER_RELEASE_CANDIDATE = null;
	public static final Integer VER_ALPHA = null;
	public static final Integer VER_BETA = 3;
	
	public static final String version() {
		String out = VER_MAJOR.toString() + '.' + VER_MINOR + '.' + VER_REVISION;
		if(VER_ALPHA != null)
			out += " alpha " + VER_ALPHA;
		else if(VER_BETA != null)
			out += " beta " + VER_BETA;
		else if(VER_RELEASE_CANDIDATE != null)
			out += " RC " + VER_RELEASE_CANDIDATE;
		
		return out;
	}
}
