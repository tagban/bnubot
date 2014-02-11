/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.commands;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.bnubot.core.Connection;
import net.bnubot.core.commands.AccountDoesNotExistException;
import net.bnubot.core.commands.CommandFailedWithDetailsException;
import net.bnubot.core.commands.CommandRunnable;
import net.bnubot.core.commands.InvalidUseException;
import net.bnubot.db.Account;
import net.bnubot.db.Mail;
import net.bnubot.util.BNetUser;
import net.bnubot.util.TimeFormatter;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.RefreshQuery;

/**
 * @author scotta
 */
public final class CommandMail implements CommandRunnable {
	@Override
	public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
	throws Exception {
		if(commanderAccount == null)
			throw new CommandFailedWithDetailsException("You must have an account to use mail.");

		try {
			if((params == null) || (params.length < 1))
				throw new InvalidUseException();
			if(params[0].equals("send")) {
				//send <account> <message>
				params = param.split(" ", 3);
				if(params.length < 3)
					throw new InvalidUseException();

				Account rsTargetAccount = Account.get(params[1]);
				if(rsTargetAccount == null)
					throw new AccountDoesNotExistException(params[1]);

				params[1] = rsTargetAccount.getName();
				Mail.send(commanderAccount, rsTargetAccount, params[2]);
				user.sendChat("Mail queued for delivery to " + rsTargetAccount.getName(), whisperBack);
			} else if(params[0].equals("read")
					||params[0].equals("get")) {
				//read [number]
				if((params.length < 1) || (params.length > 2))
					throw new InvalidUseException();

				int id = 0;
				boolean all = false;
				if(params.length == 2) {
					try {
						id = Integer.parseInt(params[1]);
					} catch(Exception e) {
						if(!"all".equals(params[1]))
							throw new InvalidUseException();
						all = true;
					}
				}

				List<Mail> rsMail = getMail(commanderAccount);
				if(all) {
					for(Mail m : getMail(commanderAccount))
						sendMail(user, whisperBack, ++id, rsMail.size(), m);
				} else if(id == 0) {
					for(Mail m : rsMail) {
						id++;
						if(m.isIsread())
							continue;

						sendMail(user, whisperBack, id, rsMail.size(), m);
						return;
					}

					String message = "You have no unread mail!";
					if(rsMail.size() > 0)
						message += " To read your " + rsMail.size() + " messages, type [ %trigger%mail read <number> ]";
					throw new CommandFailedWithDetailsException(message);
				} else {
					if((rsMail.size() >= id) && (id >= 1))
						sendMail(user, whisperBack, id, rsMail.size(), rsMail.get(id-1));
					else
						throw new CommandFailedWithDetailsException("You only have " + rsMail.size() + " messages!");
				}
				return;
			} else if(params[0].equals("delete")) {
				//delete <number>
				if(params.length != 2)
					throw new InvalidUseException();

				int id;
				try {
					id = Integer.parseInt(params[1]);
				} catch(Exception e) {
					throw new InvalidUseException();
				}

				List<Mail> rsMail = getMail(commanderAccount);

				if((rsMail.size() >= id) && (id >= 1)) {
					Mail m = rsMail.get(id-1);
					String response = "Deleted " + constructMessage(id, rsMail.size(), m);

					try {
						ObjectContext context = commanderAccount.getObjectContext();
						context.deleteObject(m);
						commanderAccount.updateRow();
						user.sendChat(response, true);

						context.performQuery(new RefreshQuery(commanderAccount));
					} catch(Exception e) {
						throw new CommandFailedWithDetailsException("Failed to delete mail", e);
					}
				} else {
					throw new CommandFailedWithDetailsException("You only have " + rsMail.size() + " messages!");
				}

			} else if(params[0].equals("empty")
					||params[0].equals("clear")) {
				//empty
				if(params.length != 1)
					throw new InvalidUseException();

				if(Mail.getUnreadCount(commanderAccount) > 0)
					throw new CommandFailedWithDetailsException("You have unread mail!");

				try {
					ObjectContext context = commanderAccount.getObjectContext();
					for(Mail m : commanderAccount.getRecievedMail())
						context.deleteObject(m);
					commanderAccount.updateRow();
					user.sendChat("Mailbox cleaned!", whisperBack);
				} catch(Exception e) {
					throw new CommandFailedWithDetailsException("Failed to delete mail", e);
				}
			} else
				throw new InvalidUseException();
		} catch(InvalidUseException e) {
			user.sendChat("Use: " +
					"%trigger%mail read [number | all], " +
					"%trigger%mail delete <number>, " +
					"%trigger%mail empty, " +
					"%trigger%mail send <account> <message>", whisperBack);
		}
	}

	private List<Mail> getMail(Account account) {
		List<Mail> rsMail = account.getRecievedMail();
		// Sort the mail by sent date
		Collections.sort(rsMail, new Comparator<Mail>() {
			@Override
			public int compare(Mail arg0, Mail arg1) {
				return arg0.getSent().compareTo(arg1.getSent());
			}});
		return rsMail;
	}

	private String constructMessage(int id, int size, Mail m) {
		StringBuilder message = new StringBuilder("#");
		message.append(id);
		message.append(" of ");
		message.append(size);
		if(m.getSentFrom() != null) {
			message.append(" from ");
			message.append(m.getSentFrom().getName());
		}
		message.append(" [");
		message.append(TimeFormatter.formatTime(System.currentTimeMillis() - m.getSent().getTime()));
		message.append(" ago]: ");
		message.append(m.getMessage());
		return message.toString();
	}

	private void sendMail(BNetUser user, boolean whisperBack, int id, int size, Mail m)
	throws CommandFailedWithDetailsException {
		user.sendChat(constructMessage(id, size, m), true);

		m.setIsread(true);
		try {
			m.updateRow();
		} catch(Exception e) {
			throw new CommandFailedWithDetailsException("Failed to set mail read", e);
		}
	}
}