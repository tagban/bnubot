/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util;

import java.util.Properties;

/**
 * @author scotta
 */
public enum OperatingSystem {
	WINDOWS,
	OSX,
	LINUX,
	UNKNOWN;

	private static OperatingSystem initOS() {
		String osName = System.getProperty("os.name");
		if(osName.equals("Mac OS X"))
			return OSX;
		if(osName.startsWith("Windows "))
			return WINDOWS;
		if(osName.startsWith("Linux"))
			return LINUX;
		return UNKNOWN;
	}

	public static final OperatingSystem userOS = initOS();

	/**
	 * @return user-displayable operating system version
	 */
	public static String osVersion() {
		Properties p = System.getProperties();
		String osName = p.getProperty("os.name");
		String osVersion = p.getProperty("os.version");

		if((osVersion != null) && (osVersion.length() != 0))
			osName += " " + osVersion;

		switch(userOS) {
		case OSX:
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
			else if(osVersion.startsWith("10.6"))
				osName += " Snow Leopard";
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
