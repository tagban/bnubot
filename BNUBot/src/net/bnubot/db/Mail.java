/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.db;

import java.util.Date;

import net.bnubot.db.auto._Mail;

import org.apache.cayenne.access.DataContext;

public class Mail extends _Mail {
	private static final long serialVersionUID = 7450788505595409098L;

	public static Mail send(DataContext context, Account from, Account to, String message) {
		Mail mail = context.newObject(Mail.class);
		mail.setSentFrom(from);
		mail.setSentTo(to);
		mail.setMessage(message);
		mail.setIsread(false);
		mail.setSent(new Date());
		context.commitChanges();
		return mail;
	}
}
