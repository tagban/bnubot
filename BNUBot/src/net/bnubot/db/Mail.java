/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.db;

import java.util.Date;

import net.bnubot.DatabaseContext;
import net.bnubot.db.auto._Mail;

import org.apache.cayenne.ObjectContext;

public class Mail extends _Mail {
	private static final long serialVersionUID = 7450788505595409098L;

	public static Mail send(Account from, Account to, String message) {
		ObjectContext context = DatabaseContext.getContext();
		Mail mail = context.newObject(Mail.class);
		mail.setSentFrom(from);
		mail.setSentTo(to);
		mail.setMessage(message);
		mail.setIsread(false);
		mail.setSent(new Date());
		context.commitChanges();
		return mail;
	}

	/**
	 * @param commanderAccount
	 * @return
	 */
	public static int getUnreadCount(Account commanderAccount) {
		// TODO Auto-generated method stub
		return 0;
	}
}
