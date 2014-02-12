/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.db;

import org.apache.cayenne.CayenneDataObject;

/**
 * @author scotta
 * @param <S> The type used in {@link #toSortField()}
 */
@SuppressWarnings("serial")
public abstract class CustomDataObject<S extends Comparable<? super S>> extends CayenneDataObject implements Comparable<CustomDataObject<S>> {
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

	public abstract S toSortField();

	@Override
	public String toString() {
		return toSortField().toString();
	}

	@Override
	public final int compareTo(CustomDataObject<S> o) {
		return toSortField().compareTo(o.toSortField());
	}
}
