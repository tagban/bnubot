/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.db;

import org.apache.cayenne.DataObjectUtils;

import net.bnubot.DatabaseContext;
import net.bnubot.db.auto._Rank;

public class Rank extends _Rank {
	private static final long serialVersionUID = 6998327103095647711L;

	/**
	 * @param targetAccess
	 * @return
	 */
	public static Rank get(int access) {
		return DataObjectUtils.objectForPK(DatabaseContext.getContext(), Rank.class, access);
	}

}
