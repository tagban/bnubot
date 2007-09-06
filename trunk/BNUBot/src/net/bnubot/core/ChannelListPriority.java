/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.core;

public enum ChannelListPriority {
	PRIORITY_BLIZZARD_REP (5, 0x01),
	PRIORITY_BNET_REP (4, 0x08),
	PRIORITY_OPERATOR (3, 0x02),
	PRIORITY_SPEAKER (2, 0x04),
	PRIORITY_BLIZZARD_GUEST (1, 0x40),
	PRIORITY_NORMAL (0, 0);
	
	int priority;
	int flags;
	ChannelListPriority(int priority, int flags) {
		this.priority = priority;
		this.flags = flags;
	}
	
	public static final int getPrioByFlags(int flags) {
		for(ChannelListPriority p : values())
			if((p.flags & flags) != 0)
				return p.priority;
		return PRIORITY_NORMAL.priority;
	}
};
