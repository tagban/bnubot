/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.db;

import java.util.List;

import net.bnubot.DatabaseContext;
import net.bnubot.db.auto._Account;
import net.bnubot.util.BNetUser;

import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SelectQuery;

public class Account extends _Account {
	private static final long serialVersionUID = -8256413482643908852L;

	public static Account get(BNetUser user) {
		Bnlogin bnl = Bnlogin.get(user);
		if(bnl == null)
			return null;
		return bnl.getAccount();
	}
	
	@SuppressWarnings("unchecked")
	public static List<Account> getTriviaLeaders() {
		Expression expression = ExpressionFactory.greaterExp(Account.TRIVIA_CORRECT_PROPERTY, new Integer(0));
		SelectQuery query = new SelectQuery(Account.class, expression);
		query.addOrdering(Account.TRIVIA_CORRECT_PROPERTY, false);
		return DatabaseContext.getContext().performQuery(query);
	}
}
