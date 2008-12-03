/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.core.mcp;

import net.bnubot.util.StatString;
import net.bnubot.util.TimeFormatter;

/**
 * @author scotta
 */
public class MCPCharacter {
	private final long time;
	private final String name;
	private final StatString statstr;

	public MCPCharacter(long time, String name, StatString statstr) {
		this.time = time;
		this.name = name;
		this.statstr = statstr;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		String str = name;
		if(time < 0)
			str += "Expired";
		else
			str += TimeFormatter.formatTime(time);
		str += statstr.toString();
		return str;
	}
}
