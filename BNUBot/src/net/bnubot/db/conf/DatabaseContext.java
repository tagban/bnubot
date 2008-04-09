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
import net.bnubot.util.Out;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.access.DataContext;
import org.apache.cayenne.conf.Configuration;
import org.apache.cayenne.query.SelectQuery;

public class DatabaseContext {
	private static ThreadLocal<ObjectContext> contexts = new ThreadLocal<ObjectContext>();
	private static long lastFlush = 0;
	
	static {
		try {
			Configuration.initializeSharedConfiguration();
		} catch(Exception e) {
			contexts = null;
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
		for(BNLogin login : (List<BNLogin>)context.performQuery(new SelectQuery(BNLogin.class))) {
			long age = login.getLastSeen().getTime();
			age = System.currentTimeMillis() - age;
			age /= 86400000l; // convert to days
			
			long maxAge = 90;
			Account account = login.getAccount();
			if(account != null) {
				Rank rank = account.getRank();
				if(rank != null)
					maxAge = rank.getExpireDays();
			}
			
			if(age <= maxAge)
				continue;
			
			if(account != null)
				try {
					Mail.send(account, account, "Your login [ " + login.getLogin() + " ] has been removed due to inactivity (" + age + " days)");
				} catch (Exception e) {
					Out.exception(e);
				}
				
			Out.error(DatabaseContext.class, "Removing " + login.getLogin() + " due to inactivity (" + age + " days)");
			context.deleteObject(login);
		}
		
		try {
			context.commitChanges();
		} catch(Exception e) {
			context.rollbackChanges();
			Out.exception(e);
		}
		
		for(Account account : (List<Account>)context.performQuery(new SelectQuery(Account.class))) {
			if(account.getBnLogins().size() != 0)
				continue;
			if(account.getRecruits().size() != 0) {
				// Give them 0 access
				account.setRank(Rank.get(0));
				continue;
			}
			
			Account recruiter = account.getRecruiter();
			if(recruiter != null)
				try {
					Mail.send(recruiter, recruiter, "Your recruit [ " + account.getName() + " ] has been removed due to inactivity");
				} catch (Exception e) {
					Out.exception(e);
				}
			
			Out.error(DatabaseContext.class, "Removing " + account.getName() + " which has no active BNLogins");
			context.deleteObject(account);
		}
		
		try {
			context.commitChanges();
		} catch(Exception e) {
			context.rollbackChanges();
			Out.exception(e);
		}
	}
}
