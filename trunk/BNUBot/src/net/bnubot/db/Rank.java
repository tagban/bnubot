/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.db;

import net.bnubot.db.auto._Rank;
import net.bnubot.db.conf.DatabaseContext;

import org.apache.cayenne.DataObjectUtils;
import org.apache.cayenne.query.SQLTemplate;

/**
 * @author scotta
 */
public class Rank extends _Rank {
	private static final long serialVersionUID = 6998327103095647711L;
	private static SQLTemplate maxRank = new SQLTemplate(Rank.class, "SELECT " +
			"#result('rank.ID' 'int' 'id') " +
			"FROM rank " +
			"ORDER BY id DESC");
	private static SQLTemplate minRank = new SQLTemplate(Rank.class, "SELECT " +
			"#result('rank.ID' 'int' 'id') " +
			"FROM rank " +
			"ORDER BY id ASC");
	static {
		maxRank.setFetchLimit(1);
		minRank.setFetchLimit(1);
	}

	/**
	 * Get the maximum access rank in the database
	 * @return A Rank with the highest access
	 */
	public static Rank getMax() {
		return (Rank)DatabaseContext.getContext().performQuery(maxRank).get(0);
	}

	public static Rank getMin() {
		return (Rank)DatabaseContext.getContext().performQuery(minRank).get(0);
	}

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

	@Override
	public Integer toSortField() {
		return Integer.valueOf(DataObjectUtils.intPKForObject(this));
	}
}
