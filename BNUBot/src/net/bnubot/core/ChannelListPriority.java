/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.bnubot.util.BNetUser;

/**
 * @author scotta
 */
public enum ChannelListPriority {
	PRIORITY_BLIZZARD_REP (5, 0x01),
	PRIORITY_BNET_REP (4, 0x08),
	PRIORITY_OPERATOR (3, 0x02),
	PRIORITY_SPEAKER (2, 0x04),
	PRIORITY_BLIZZARD_GUEST (1, 0x40),
	PRIORITY_NORMAL (0, 0);

	private int priority;
	private int flags;
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

	private static final Comparator<BNetUser> bnetSorter = new Comparator<BNetUser>() {
		@Override
		public int compare(BNetUser arg0, BNetUser arg1) {
			int prio0 = ChannelListPriority.getPrioByFlags(arg0.getFlags());
			int prio1 = ChannelListPriority.getPrioByFlags(arg0.getFlags());
			Integer i0 = Integer.valueOf(prio0);
			Integer i1 = Integer.valueOf(prio1);
			return i0.compareTo(i1);
		}
	};

	public static void sort(List<BNetUser> users) {
		Collections.sort(users, bnetSorter);
	}
};
