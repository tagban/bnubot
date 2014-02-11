/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.db;

import java.util.Date;
import java.util.List;

import net.bnubot.db.auto._Account;
import net.bnubot.db.conf.DatabaseContext;
import net.bnubot.logging.Out;
import net.bnubot.util.BNetUser;

import org.apache.cayenne.DataObjectUtils;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SelectQuery;

/**
 * @author scotta
 */
public class Account extends _Account {
	private static final long serialVersionUID = -8256413482643908852L;

	/**
	 * Get an Account from a BNetUser
	 * @param user The BNetUser
	 * @return The Account, or NULL if the user has no account
	 */
	public static Account get(BNetUser user) {
		BNLogin bnl = BNLogin.get(user);
		if(bnl == null)
			return null;
		return bnl.getAccount();
	}

	/**
	 * Get an Account by name
	 * @param name The name
	 * @return The Account or NULL
	 */
	public static Account get(String name) {
		try {
			Expression expression = ExpressionFactory.likeIgnoreCaseExp(Account.NAME_PROPERTY, name);
			SelectQuery query = new SelectQuery(Account.class, expression);
			return (Account)DataObjectUtils.objectForQuery(DatabaseContext.getContext(), query);
		} catch(Exception e) {
			Out.exception(e);
			return null;
		}
	}

	/**
	 * @param rank The minimum access
	 * @return A List&lt;Account> of users with at minimum access
	 */
	@SuppressWarnings("unchecked")
	public static List<Account> getRanked(int rank) {
		Expression expression = ExpressionFactory.greaterOrEqualExp(Account.RANK_PROPERTY, rank);
		SelectQuery query = new SelectQuery(Account.class, expression);
		return DatabaseContext.getContext().performQuery(query);
	}

	/**
	 * Create an Account
	 * @param name The Account's name
	 * @param access The Account's Rank
	 * @param recruiter The Account's recuter's Account
	 * @return The created Account
	 * @throws Exception If a commit error occurs
	 */
	public static Account create(String name, Rank access, Account recruiter) throws Exception {
		Date now = new Date(System.currentTimeMillis());

		ObjectContext context = DatabaseContext.getContext();
		Account account = context.newObject(Account.class);
		account.setCreated(now);
		account.setLastRankChange(now);
		account.setName(name);
		account.setRank(access);
		account.setRecruiter(recruiter);
		account.setTriviaCorrect(0);
		account.setTriviaWin(0);
		account.setFlagSpoof(0);
		account.updateRow();
		return account;
	}

	/**
	 * Get the Trivia leaderboard
	 * @return A List&lt;Account> of all users with trivia_correct>0, descending
	 */
	@SuppressWarnings("unchecked")
	public static List<Account> getTriviaLeaders() {
		Expression expression = ExpressionFactory.greaterExp(Account.TRIVIA_CORRECT_PROPERTY, Integer.valueOf(0));
		SelectQuery query = new SelectQuery(Account.class, expression);
		query.addOrdering(Account.TRIVIA_CORRECT_PROPERTY, false);
		return DatabaseContext.getContext().performQuery(query);
	}

	/**
	 * Equivalent to getRank().getAccess(), with null checking
	 * @return The access level of the Account's Rank
	 */
	public int getAccess() {
		Rank rank = getRank();
		if(rank == null)
			return 0;
		return rank.getAccess();
	}

	/**
	 * Calculates "Recruit Score" by giving 1 point for every promotion the Account's recruits have above recruitAccess
	 * @param recruitAccess the value of the lowest member in the clan
	 * @return the calculated recruit score
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

	/**
	 * Get the list of wins/levels for a user's account
	 * @param recruitTagPrefix (optional) The required BNetUser logon prefix
	 * @param recruitTagSuffix (optional) The required BNetUser logon suffix
	 * @return long[3] {wins, d2level, w3level}
	 */
	public long[] getWinsLevels(String recruitTagPrefix, String recruitTagSuffix) {
		long wins = 0;
		long d2level = 0;
		long w3level = 0;
		recruitTagPrefix = recruitTagPrefix.toLowerCase();
		recruitTagSuffix = recruitTagSuffix.toLowerCase();
		for(BNLogin login : getBnLogins()) {
			String bnlogin = login.getLogin().toLowerCase();

			// Check prefix
			if((recruitTagPrefix != null) && !bnlogin.startsWith(recruitTagPrefix))
				continue;

			// Check suffix
			if((recruitTagSuffix != null) && !bnlogin.endsWith(recruitTagSuffix))
				continue;

			// Calculate wins
			Integer star = login.getWinsSTAR();
			Integer sexp = login.getWinsSEXP();
			Integer w2bn = login.getWinsW2BN();
			if(star != null)
				wins += star.intValue();
			if(sexp != null)
				wins += sexp.intValue();
			if(w2bn != null)
				wins += w2bn.intValue();

			// Calculate D2 level
			Integer d2 = login.getLevelD2();
			if(d2 != null)
				d2level = Math.max(d2level, d2.intValue());

			// Calculate W3 level
			Integer w3 = login.getLevelW3();
			if(w3 != null)
				w3level += Math.max(w3level, w3.intValue());
		}
		return new long[] {wins, d2level, w3level};
	}

	@Override
	public String toSortField() {
		return getName();
	}
}
