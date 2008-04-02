/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.db;

import java.util.Date;

import net.bnubot.DatabaseContext;
import net.bnubot.db.auto._Mail;

public class Mail extends _Mail {
	private static final long serialVersionUID = 7450788505595409098L;

	public static Mail send(Account from, Account to, String message) throws Exception {
		Mail mail = DatabaseContext.getContext().newObject(Mail.class);
		mail.setSentFrom(from);
		mail.setSentTo(to);
		mail.setMessage(message);
		mail.setIsread(false);
		mail.setSent(new Date());
		mail.updateRow();
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
