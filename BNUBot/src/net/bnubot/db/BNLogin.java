/**
 * This file is distributed under the GPL
 * $Id: BNLogin.java 1240 2008-04-02 18:12:18Z scotta $
 */

package net.bnubot.db;

import java.util.Date;

import net.bnubot.DatabaseContext;
import net.bnubot.db.auto._BNLogin;
import net.bnubot.util.BNetUser;

import org.apache.cayenne.DataObjectUtils;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SelectQuery;

public class BNLogin extends _BNLogin {
	private static final long serialVersionUID = 8786293271622053325L;

	/**
	 * Get a BNLogin from a BNetUser
	 * @param user The BNetUser
	 * @return The BNLogin, or NULL if the user is not in the database
	 */
	public static BNLogin get(BNetUser user) {
		Expression expr = ExpressionFactory.likeIgnoreCaseExp(BNLogin.LOGIN_PROPERTY, user.getFullAccountName());
		SelectQuery query = new SelectQuery(BNLogin.class, expr);
		return (BNLogin)DataObjectUtils.objectForQuery(DatabaseContext.getContext(), query);
	}

	/**
	 * Get or create a BNLogin from a BNetUser
	 * @param user The BNetUser
	 * @return The BNLogin
	 * @throws Exception If a commit error occurs
	 */
	public static BNLogin getCreate(BNetUser user) throws Exception {
		BNLogin bnl = get(user);
		if(bnl != null)
			return bnl;
		
		Date now = new Date(System.currentTimeMillis());
		
		bnl = DatabaseContext.getContext().newObject(BNLogin.class);
		bnl.setCreated(now);
		bnl.setLastSeen(now);
		bnl.setLogin(user.getFullAccountName());
		bnl.updateRow();
		return bnl;
	}
}
