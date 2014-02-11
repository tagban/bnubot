/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.db;

import java.util.Date;

import net.bnubot.db.auto._Mail;
import net.bnubot.db.conf.DatabaseContext;

import org.apache.cayenne.DataObjectUtils;

/**
 * @author scotta
 */
public class Mail extends _Mail {
	private static final long serialVersionUID = 7450788505595409098L;

	/**
	 * Send mail to a user
	 * @param from The Account the mail is sent from
	 * @param to The Account the mail is sent to
	 * @param message The message to send
	 * @return The Mail object
	 * @throws Exception If a commit error occurs
	 */
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
	 * Get the number of unread mail messages
	 * @param account The user to check
	 * @return The number of unread mail messages
	 */
	public static int getUnreadCount(Account account) {
		// TODO: Replace this with a more efficient query
		int count = 0;
		for(Mail m : account.getRecievedMail())
			if(!m.isIsread())
				count++;
		return count;
	}

	@Override
	public Integer toSortField() {
		return Integer.valueOf(DataObjectUtils.intPKForObject(this));
	}
}
