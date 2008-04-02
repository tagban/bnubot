/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.db;

import org.apache.cayenne.DataObjectUtils;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SelectQuery;

import net.bnubot.DatabaseContext;
import net.bnubot.db.auto._Bnlogin;
import net.bnubot.util.BNetUser;

public class Bnlogin extends _Bnlogin {
	private static final long serialVersionUID = 8786293271622053325L;

	public static Bnlogin get(BNetUser user) {
		Expression expr = ExpressionFactory.likeIgnoreCaseExp(Bnlogin.LOGIN_PROPERTY, user.getFullAccountName());
		SelectQuery query = new SelectQuery(Bnlogin.class, expr);
		return (Bnlogin)DataObjectUtils.objectForQuery(DatabaseContext.getContext(), query);
	}
}
