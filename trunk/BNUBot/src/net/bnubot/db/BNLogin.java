/**
 * This file is distributed under the GPL
 * $Id: BNLogin.java 1240 2008-04-02 18:12:18Z scotta $
 */

package net.bnubot.db;

import org.apache.cayenne.DataObjectUtils;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SelectQuery;

import net.bnubot.DatabaseContext;
import net.bnubot.db.auto._BNLogin;
import net.bnubot.util.BNetUser;

public class BNLogin extends _BNLogin {
	private static final long serialVersionUID = 8786293271622053325L;

	public static BNLogin get(BNetUser user) {
		Expression expr = ExpressionFactory.likeIgnoreCaseExp(BNLogin.LOGIN_PROPERTY, user.getFullAccountName());
		SelectQuery query = new SelectQuery(BNLogin.class, expr);
		return (BNLogin)DataObjectUtils.objectForQuery(DatabaseContext.getContext(), query);
	}

	public static BNLogin getCreate(BNetUser user) {
		BNLogin bnl = get(user);
		if(bnl != null)
			return bnl;
		
		// TODO: create the user
		
		return bnl;
	}

	/**
	 * Try to save changes to this object
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
