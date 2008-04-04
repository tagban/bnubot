/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.db;

import java.util.Date;
import java.util.List;

import net.bnubot.db.auto._Account;
import net.bnubot.db.conf.DatabaseContext;
import net.bnubot.util.BNetUser;

import org.apache.cayenne.DataObjectUtils;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SelectQuery;

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
	 * @return The Account
	 */
	public static Account get(String name) {
		Expression expression = ExpressionFactory.likeIgnoreCaseExp(Account.NAME_PROPERTY, name);
		SelectQuery query = new SelectQuery(Account.class, expression);
		return (Account)DataObjectUtils.objectForQuery(DatabaseContext.getContext(), query);
	}

	/**
	 * Get the access level of an Account
	 * @return The access level of the user's Rank
	 */
	public int getAccess() {
		Rank rank = getRank();
		if(rank == null)
			return 0;
		return DataObjectUtils.intPKForObject(rank);
	}

	/**
	 * Get a List&lt;Account> of users with at minimum access 
	 * @param rank The minimum access
	 * @return A List&lt;Account> of the applicable users
	 */
	@SuppressWarnings("unchecked")
	public static List<Account> getRanked(int rank) {
		Expression expression = ExpressionFactory.greaterOrEqualDbExp(Account.RANK_PROPERTY, rank);
		SelectQuery query = new SelectQuery(Account.class, expression);
		return DatabaseContext.getContext().performQuery(query);
	}

	/**
	 * 
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
		account.updateRow();
		return account;
	}
	
	/**
	 * Get the Trivia leaderboard
	 * @return A List&lt;Account> of all users with trivia_correct>0, descending
	 */
	@SuppressWarnings("unchecked")
	public static List<Account> getTriviaLeaders() {
		Expression expression = ExpressionFactory.greaterExp(Account.TRIVIA_CORRECT_PROPERTY, new Integer(0));
		SelectQuery query = new SelectQuery(Account.class, expression);
		query.addOrdering(Account.TRIVIA_CORRECT_PROPERTY, false);
		return DatabaseContext.getContext().performQuery(query);
	}

	/**
	 * Get the list of wins/levels for a user's account
	 * @param recruitTagPrefix (optional) The required BNetUser logon prefix
	 * @param recruitTagSuffix (optional) The required BNetUser logon suffix
	 * @return TODO
	 */
	public long[] getWinsLevels(String recruitTagPrefix, String recruitTagSuffix) {
		long wins = 0;
		long d2level = 0;
		long w3level = 0;
		for(BNLogin login : getBnLogins()) {
			// Check prefix
			if((recruitTagPrefix != null) && !login.getLogin().startsWith(recruitTagPrefix))
				continue;
			
			// Check suffix
			if((recruitTagSuffix != null) && !login.getLogin().endsWith(recruitTagSuffix))
				continue;
			
			// Calculate wins
			if(login.getWinsSTAR() != null)
				wins += login.getWinsSTAR().intValue();
			if(login.getWinsSEXP() != null)
				wins += login.getWinsSEXP().intValue();
			if(login.getWinsW2BN() != null)
				wins += login.getWinsW2BN().intValue();
			
			// Calculate D2 level
			if(login.getLevelD2() != null)
				d2level = Math.max(d2level, login.getLevelD2().intValue());
			
			// Calculate W3 level
			if(login.getLevelW3() != null)
				w3level += Math.max(w3level, login.getLevelW3().intValue());
		}
		return new long[] {wins, d2level, w3level};
	}
}
