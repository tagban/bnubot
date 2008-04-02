/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.db;

import java.util.List;

import net.bnubot.DatabaseContext;
import net.bnubot.db.auto._Account;
import net.bnubot.util.BNetUser;

import org.apache.cayenne.DataObjectUtils;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SelectQuery;

public class Account extends _Account {
	private static final long serialVersionUID = -8256413482643908852L;

	public static Account get(BNetUser user) {
		BNLogin bnl = BNLogin.get(user);
		if(bnl == null)
			return null;
		return bnl.getAccount();
	}

	public static Account get(String name) {
		Expression expression = ExpressionFactory.likeIgnoreCaseExp(Account.NAME_PROPERTY, name);
		SelectQuery query = new SelectQuery(Account.class, expression);
		return (Account)DataObjectUtils.objectForQuery(DatabaseContext.getContext(), query);
	}
	
	public int getAccess() {
		Rank rank = getRank();
		if(rank == null)
			return 0;
		return DataObjectUtils.intPKForObject(rank);
	}

	/**
	 * @param rank
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Account> getRanked(int rank) {
		Expression expression = ExpressionFactory.greaterOrEqualDbExp(Account.RANK_PROPERTY, rank);
		SelectQuery query = new SelectQuery(Account.class, expression);
		return DatabaseContext.getContext().performQuery(query);
	}

	/**
	 * @param recruitAccess
	 * @return
	 */
	public long getRecruitScore(long recruitAccess) {
		long rs = 0;
		for(Account recruit : getRecruits()) {
			int access = recruit.getAccess();
			if(access > recruitAccess)
				rs += access - recruitAccess;
		}
		return rs;
	}

	public static Account create(String name, Rank access, Account recruiter) {
		ObjectContext context = DatabaseContext.getContext();
		Account account = context.newObject(Account.class);
		account.setName(name);
		account.setRank(access);
		account.setRecruiter(recruiter);
		try {
			context.commitChanges();
			return account;
		} catch(Exception e) {
			context.rollbackChanges();
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static List<Account> getTriviaLeaders() {
		Expression expression = ExpressionFactory.greaterExp(Account.TRIVIA_CORRECT_PROPERTY, new Integer(0));
		SelectQuery query = new SelectQuery(Account.class, expression);
		query.addOrdering(Account.TRIVIA_CORRECT_PROPERTY, false);
		return DatabaseContext.getContext().performQuery(query);
	}

	/**
	 * @param recruitTagPrefix
	 * @param recruitTagSuffix
	 * @return
	 */
	public long[] getWinsLevels(String recruitTagPrefix, String recruitTagSuffix) {
		// TODO Auto-generated method stub
		return null;
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
