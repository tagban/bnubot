/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.db;

import org.apache.cayenne.CayenneDataObject;

public class CustomDataObject extends CayenneDataObject {
	private static final long serialVersionUID = -4559567938379477963L;

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
}
