/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.db.conf;

import java.util.List;

import net.bnubot.db.Account;
import net.bnubot.db.BNLogin;
import net.bnubot.db.Mail;
import net.bnubot.db.Rank;
import net.bnubot.logging.Out;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.access.DataContext;
import org.apache.cayenne.conf.Configuration;
import org.apache.cayenne.query.SelectQuery;

/**
 * @author scotta
 */
public class DatabaseContext {
	private static ThreadLocal<ObjectContext> contexts = new ThreadLocal<ObjectContext>();
	private static long lastFlush = 0;

	static {
		try {
			Configuration.initializeSharedConfiguration();
			if(!SchemaValidator.validate())
				contexts = null;
		} catch(Exception e) {
			contexts = null;
			Out.exception(e);
		}
	}

	public static ObjectContext getContext() {
		if(contexts == null)
			return null;

		ObjectContext oc = contexts.get();
		if(oc == null) {
			oc = DataContext.createDataContext();
			contexts.set(oc);
		}

		// Every 20 minutes...
		long now = System.currentTimeMillis();
		if(now - lastFlush > 20 * 60 * 1000) {
			lastFlush = now;
			flush(oc);
		}
		return oc;
	}

	@SuppressWarnings("unchecked")
	private static void flush(ObjectContext context) {
		long defaultMaxAge = 90;
		{
			Rank rank = Rank.get(0);
			if(rank != null)
				defaultMaxAge = rank.getExpireDays();
		}

		boolean debugEnabled = Out.isDebug(DatabaseContext.class);

		for(BNLogin login : (List<BNLogin>)context.performQuery(new SelectQuery(BNLogin.class))) {
			long age = login.getLastSeen().getTime();
			age = System.currentTimeMillis() - age;
			age /= 86400000l; // convert to days

			long maxAge = defaultMaxAge;
			Account account = login.getAccount();
			if(account != null) {
				Rank rank = account.getRank();
				if(rank != null)
					maxAge = rank.getExpireDays();
			}

			if(maxAge == 0)
				continue;
			if(age <= maxAge)
				continue;

			if(account != null)
				try {
					Mail.send(null, account, "Your login [ " + login.getLogin() + " ] has been removed due to inactivity (" + age + " days)");
				} catch (Exception e) {
					Out.exception(e);
					break;
				}

			if(debugEnabled)
				Out.debugAlways(DatabaseContext.class, "Removing " + login.getLogin() + " due to inactivity (" + age + " days)");
			context.deleteObject(login);
			Thread.yield();
		}

		try {
			context.commitChanges();
		} catch(Exception e) {
			context.rollbackChanges();
			Out.exception(e);
		}

		for(Account account : (List<Account>)context.performQuery(new SelectQuery(Account.class))) {
			// Only consider users with no BnLogins
			if(account.getBnLogins().size() != 0)
				continue;

			// Only consider users with positive access; banlisted users shouldn't expire
			// This also prevents the recruiter from being flooded with mail
			if(account.getRank().getAccess() <= 0)
				continue;

			Account recruiter = account.getRecruiter();
			if(recruiter != null)
				try {
					// Notify the recruiter that the user is inactive
					Mail.send(null, recruiter, "Your recruit [ " + account.getName() + " ] has been removed due to inactivity");
				} catch (Exception e) {
					Out.exception(e);
					break;
				}

			// Don't actually remove them from the DB, just set them to no access
			account.setRank(Rank.get(0));
			Thread.yield();
		}

		try {
			context.commitChanges();
		} catch(Exception e) {
			context.rollbackChanges();
			Out.exception(e);
		}
	}
}
