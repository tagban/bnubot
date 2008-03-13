/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util;

import java.util.Properties;

public enum OperatingSystem {
	WINDOWS,
	OSX,
	UNKNOWN;
	
	public static OperatingSystem getOS() {
		String osName = System.getProperty("os.name");
		if(osName.equals("Mac OS X"))
			return OSX;
		if(osName.startsWith("Windows "))
			return WINDOWS;
		return UNKNOWN;
	}

	/**
	 * Get a displayable operating system version
	 */
	public static String osVersion() {
		Properties p = System.getProperties();
		String osName = p.getProperty("os.name");
		String osVersion = p.getProperty("os.version");
		
		switch(getOS()) {
		case OSX:
			osName += " " + osVersion;
			if(osVersion.startsWith("10.0"))
				osName += " Cheetah";
			else if(osVersion.startsWith("10.1"))
				osName += " Puma";
			else if(osVersion.startsWith("10.2"))
				osName += " Jaguar";
			else if(osVersion.startsWith("10.3"))
				osName += " Panther";
			else if(osVersion.startsWith("10.4"))
				osName += " Tiger";
			else if(osVersion.startsWith("10.5"))
				osName += " Leopard";
			break;
		case WINDOWS:
			osName += " " + p.getProperty("sun.os.patch.level");
			break;
		}
		
		osName += " (" + p.getProperty("os.arch") + ")";
		return osName;
	}
	
	public static String javaVersion() {
		return "Java " + System.getProperties().getProperty("java.version");
	}
}
