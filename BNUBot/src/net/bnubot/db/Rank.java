/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.db;

import org.apache.cayenne.DataObjectUtils;

import net.bnubot.db.auto._Rank;
import net.bnubot.db.conf.DatabaseContext;

public class Rank extends _Rank {
	private static final long serialVersionUID = 6998327103095647711L;

	/**
	 * Get a Rank by access level
	 * @param access The access level
	 * @return The Rank
	 */
	public static Rank get(int access) {
		return DataObjectUtils.objectForPK(DatabaseContext.getContext(), Rank.class, access);
	}
	
	public int getAccess() {
		return DataObjectUtils.intPKForObject(this);
	}

	public String toDisplayString() {
		return Integer.toString(getAccess());
	}
}
