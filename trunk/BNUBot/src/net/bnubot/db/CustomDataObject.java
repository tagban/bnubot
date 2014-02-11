/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.db;

import org.apache.cayenne.CayenneDataObject;
import org.apache.cayenne.DataObjectUtils;

/**
 * @author scotta
 */
@SuppressWarnings("serial")
public abstract class CustomDataObject extends CayenneDataObject implements Comparable<CustomDataObject> {
	/**
	 * Try to save changes to this object
	 * @throws Exception If a commit error occurs
	 */
	public void updateRow() throws Exception {
		try {
			getObjectContext().commitChanges();
		} catch(Exception e) {
			getObjectContext().rollbackChanges();
			throw e;
		}
	}

	public Comparable toSortField() {
		return Integer.valueOf(DataObjectUtils.intPKForObject(this));
	}

	public final String toDisplayString() {
		return toSortField().toString();
	}

	@Override
	public String toString() {
		return toDisplayString();
	}

	@Override
	@SuppressWarnings("unchecked")
	public final int compareTo(CustomDataObject o) {
		return toSortField().compareTo(o.toSortField());
	}
}
