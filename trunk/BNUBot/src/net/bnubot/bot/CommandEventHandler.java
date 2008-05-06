/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import net.bnubot.core.Connection;
import net.bnubot.core.EventHandler;
import net.bnubot.core.Profile;
import net.bnubot.core.commands.AccountDoesNotExistException;
import net.bnubot.core.commands.CommandRunnable;
import net.bnubot.core.commands.InsufficientAccessException;
import net.bnubot.core.commands.InvalidUseException;
import net.bnubot.db.Account;
import net.bnubot.db.BNLogin;
import net.bnubot.db.Command;
import net.bnubot.db.Mail;
import net.bnubot.db.Rank;
import net.bnubot.db.conf.DatabaseContext;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetUser;
import net.bnubot.util.CookieUtility;
import net.bnubot.util.OperatingSystem;
import net.bnubot.util.Out;
import net.bnubot.util.TimeFormatter;
import net.bnubot.vercheck.CurrentVersion;

import org.apache.cayenne.ObjectContext;

public class CommandEventHandler extends EventHandler {
	private static final Hashtable<Connection, Boolean> sweepBanInProgress = new Hashtable<Connection, Boolean>();
	private static final Hashtable<Connection, Integer> sweepBannedUsers = new Hashtable<Connection, Integer>();

	private static final Hashtable<Connection, Vote> votes = new Hashtable<Connection, Vote>();
	private static class Vote extends Thread {
		private long startTime;
		private Connection connection;
		private BNetUser subject;
		private boolean isBan;
		private Hashtable<String, Boolean> votes = new Hashtable<String, Boolean>();

		private boolean voteCancelled = false;

		public Vote(Connection connection, BNetUser subject, boolean isBan) {
			startTime = System.currentTimeMillis();
			this.connection = connection;
			this.subject = subject;
			this.isBan = isBan;
			start();
		}

		public BNetUser getSubject() {
			return subject;
		}

		public void cancel() {
			voteCancelled = true;
			send("Vote cancelled.");
		}

		public void castVote(Account user, boolean vote) {
			votes.put(user.getName(), new Boolean(vote));
		}

		private void send(String text) {
			connection.sendChat(text, false);
		}

		@Override
		public void run() {
			send("A vote to " + (isBan ? "ban " : "kick ") + subject.toString() + " has started. Type \"%trigger%vote yes\" or \"%trigger%vote no\" to vote. Vote lasts 30 seconds.");

			// Wait 30 seconds for voters to vote
			while(!voteCancelled) {
				if(System.currentTimeMillis() - startTime > 30000)
					break;

				yield();
				try {
					sleep(1000);
				} catch (InterruptedException e) {}
			}

			if(!voteCancelled) {
				// Tally up the votes
				int yay = 0, nay = 0;
				for(String voter : votes.keySet()) {
					if(votes.get(voter).booleanValue())
						yay++;
					else
						nay++;
				}

				if(yay + nay >= 5) {
					float ratio = ((float)yay) / (yay + nay);
					// Check for 2/3 ratio
					if(ratio * 3 >= 2)
						send((isBan ? "/ban " : "/kick ") + subject.getFullLogonName() + " " + yay + " to " + nay);
					else
						send("Vote failed, " + yay + " to " + nay + ", needed 2/3 ratio.");
				} else {
					send("Not enough votes: " + yay + " to " + nay + ", needed 5 votes.");
				}
			}

			CommandEventHandler.votes.remove(connection);
		}
	}


	private long lastCommandTime = 0;
	private BNetUser lastCommandUser = null;
	private boolean lastCommandWhisperBack = true;

	public CommandEventHandler() {
		if(DatabaseContext.getContext() == null)
			throw new IllegalStateException("Can not enable commands without a database!");
		initializeCommands();
	}

	public void touchUser(Connection source, BNetUser user, String action) {
		try {
			BNLogin rsUser = BNLogin.getCreate(user);
			if(rsUser != null) {
				rsUser.setLastSeen(new Timestamp(System.currentTimeMillis()));
				if(action != null)
					rsUser.setLastAction(action);
				rsUser.updateRow();
			}
		} catch(Exception e) {
			Out.exception(e);
		}
	}

	private static boolean commandsInitialized = false;
	public static void initializeCommands() {
		if(commandsInitialized)
			return;
		commandsInitialized = true;

		Profile.registerCommand("access", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				int commanderAccess = 0;
				if(commanderAccount != null)
					commanderAccess = commanderAccount.getAccess();

				try {
					if(params == null)
						throw new InvalidUseException();
					if(params.length != 1)
						throw new InvalidUseException();

					List<Command> commands;
					if(params[0].equals("all"))
						commands = Command.getCommands(commanderAccess);
					else
						commands = Command.getCommands(params[0], commanderAccess);

					if((commands == null) || (commands.size() == 0)) {
						user.sendChat("The category [" + params[0] + "] does not exist!", whisperBack);
						return;
					}

					StringBuilder result = new StringBuilder("Available commands for rank ");
					result.append(commanderAccess).append(" in cagegory ");
					result.append(params[0]).append(": ");
					boolean first = true;
					for(Command c : commands) {
						if(first)
							first = false;
						else
							result.append(", ");
						result.append(c.getName()).append(" (");
						result.append(c.getAccess()).append(")");
					}

					user.sendChat(result.toString(), whisperBack);
				} catch(InvalidUseException e) {
					StringBuilder use = new StringBuilder("Use: %trigger%access <category> -- Available categories for rank ");
					use.append(commanderAccess).append(": all");
					boolean first = true;
					for(Command c : Command.getGroups()) {
						if(first)
							first = false;
						else
							use.append(", ");
						use.append(c.getCmdgroup());
					}
					user.sendChat(use.toString(), whisperBack);
				}
			}});
		Profile.registerCommand("add", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				try {
					if(params == null)
						throw new InvalidUseException();
					if(params.length != 2)
						throw new InvalidUseException();

					Account rsSubjectAccount = Account.get(params[0]);
					if(rsSubjectAccount == null) {
						// They don't have an account by that name, check if it's a user
						BNetUser bnSubject = source.getCreateBNetUser(params[0], user);

						rsSubjectAccount = Account.get(bnSubject);
						if(rsSubjectAccount == null) {
							// The account does not exist
							BNLogin rsSubject = BNLogin.get(bnSubject);
							if(rsSubject == null) {
								user.sendChat("I have never seen [" + bnSubject.getFullAccountName() + "]", whisperBack);
								return;
							}

							try {
								createAccount(params[0], commanderAccount, rsSubject);
							} catch(AccountDoesNotExistException e) {
								user.sendChat(e.getMessage(), whisperBack);
								return;
							}

							// Re-load the account
							rsSubjectAccount = Account.get(bnSubject);
							if(rsSubjectAccount == null) {
								user.sendChat("Failed to create account for an unknown reason", whisperBack);
								return;
							}
						}
					}

					int targetAccess = Integer.parseInt(params[1]);
					Rank targetRank = Rank.get(targetAccess);
					if(targetRank == null) {
						user.sendChat("Invalid rank: " + targetAccess, whisperBack);
						return;
					}

					if(!superUser) {
						if(rsSubjectAccount.equals(commanderAccount))
							throw new InsufficientAccessException("to modify your self");

						int commanderAccess = 0;
						if(commanderAccount != null)
							commanderAccess = commanderAccount.getAccess();
						if(targetAccess >= commanderAccess)
							throw new InsufficientAccessException("to add users beyond " + (commanderAccess - 1));
					}

					rsSubjectAccount.setRank(targetRank);
					rsSubjectAccount.setLastRankChange(new Timestamp(System.currentTimeMillis()));
					try {
						rsSubjectAccount.updateRow();
						user.sendChat("Added user [" + rsSubjectAccount.getName() + "] successfully with access " + targetAccess, whisperBack);
					} catch(Exception e) {
						Out.exception(e);
						user.sendChat("Failed: " + e.getMessage(), whisperBack);
					}
				} catch(InvalidUseException e) {
					user.sendChat("Use: %trigger%add <account> <access>", whisperBack);
				}
			}});
		Profile.registerCommand("allseen", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				String response = "Last 10 users seen: ";
				boolean first = true;
				for(BNLogin login : BNLogin.getLastSeen(10)) {
					if(!first)
						response += ", ";
					first = false;

					response += login.getLogin();
					response += " [";
					long time = System.currentTimeMillis() - login.getLastSeen().getTime();
					response += TimeFormatter.formatTime(time, false);
					response += "]";
				}
				user.sendChat(response, whisperBack);
			}});
		Profile.registerCommand("auth", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				try {
					if(params == null)
						throw new InvalidUseException();
					if(params.length != 1)
						throw new InvalidUseException();

					Command rsSubjectCommand = Command.get(params[0]);
					if(rsSubjectCommand == null) {
						user.sendChat("The command [" + params[0] + "] does not exist!", whisperBack);
						return;
					}

					params[0] = rsSubjectCommand.getName();
					int access = rsSubjectCommand.getAccess();

					user.sendChat("Authorization required for " + params[0] + " is " + access, whisperBack);
				} catch(InvalidUseException e) {
					user.sendChat("Use: %trigger%auth <command>", whisperBack);
				}
			}});
		Profile.registerCommand("autopromotion", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				try {
					if((params != null) && (params.length != 1))
						throw new InvalidUseException();

					Account subject;
					if(params == null)
						subject = Account.get(user);
					else
						subject = Account.get(params[0]);

					if(subject == null)
						throw new AccountDoesNotExistException(params[0]);

					long wins[] = subject.getWinsLevels(GlobalSettings.recruitTagPrefix, GlobalSettings.recruitTagSuffix);
					long recruitScore = subject.getRecruitScore(GlobalSettings.recruitAccess);
					Date ts = subject.getLastRankChange();
					String timeElapsed;
					if(ts != null) {
						double te = System.currentTimeMillis() - ts.getTime();
						te /= 1000 * 60 * 60 * 24;
						//Round to 2 decimal places
						timeElapsed = ("00" + ((long)Math.floor(te * 100) % 100));
						timeElapsed = timeElapsed.substring(timeElapsed.length()-2);
						timeElapsed = (long)Math.floor(te) + "." + timeElapsed;
					} else {
						timeElapsed = "?";
					}

					Rank rsRank = subject.getRank();
					if(rsRank == null) {
						String result = "Rank does not exist! ";
						result += subject.getName() + "'s current status is: ";
						result += timeElapsed + " days, ";
						result += wins[0] + " wins, ";
						result += wins[1] + " D2 level, ";
						result += wins[2] + " W3 level";

						user.sendChat(result, whisperBack);
						return;
					}

					Integer apDays = rsRank.getApDays();
					Integer apWins = rsRank.getApWins();
					Integer apD2Level = rsRank.getApD2Level();
					Integer apW3Level = rsRank.getApW3Level();
					Integer apRecruitScore = rsRank.getApRecruitScore();

					// Check if any fields are null
					boolean condition = false;
					condition |= (apDays == null);
					condition |= (apWins == null);
					condition |= (apD2Level == null);
					condition |= (apW3Level == null);
					if(condition == false)
						// No nulls; check if all zeroes
						condition = (apDays == 0) && (apWins == 0) && (apD2Level == 0) && (apW3Level == 0);
					if(condition) {
						String result = "AutoPromotions are not enabled for rank " + subject.getAccess() + ". ";
						result += subject.getName() + "'s current status is: ";
						result += "Days: " + timeElapsed;
						result += ", Wins: " + wins[0];
						result += ", D2 Level: " + wins[1];
						result += ", W3 level: " + wins[2];
						if(recruitScore > 0)
							result += ", Recruit Score: " + recruitScore;

						user.sendChat(result, whisperBack);
					} else {
						String result = "AutoPromotion Info for [" + subject.getName() + "]: ";
						result += "Days: " + timeElapsed + "/" + apDays;
						result += ", Wins: " + wins[0] + "/" + apWins;
						result += ", D2 Level: " + wins[1] + "/" + apD2Level;
						result += ", W3 level: " + wins[2] + "/" + apW3Level;
						if(((apRecruitScore != null) && (apRecruitScore > 0)) || (recruitScore > 0)) {
							result += ", Recruit Score: " + recruitScore;
							if(apRecruitScore != null)
								result += "/" + apRecruitScore;
						}

						user.sendChat(result, whisperBack);
					}
				} catch(InvalidUseException e) {
					user.sendChat("Use: %trigger%automaticpromotion [account]", whisperBack);
				}
			}});
		Profile.registerCommand("ban", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				doKickBan(source, user, param, true, whisperBack);
			}});
		Profile.registerCommand("createaccount", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				if((params == null) || (params.length != 1)) {
					user.sendChat("Use: %trigger%createaccount <account>", whisperBack);
					return;
				}

				Account rsAccount = Account.get(params[0]);
				if(rsAccount != null) {
					user.sendChat("The account [" + rsAccount.getName() + "] already exists", whisperBack);
					return;
				}

				rsAccount = Account.create(params[0], Rank.get(0), commanderAccount);
				if(rsAccount == null)
					user.sendChat("Failed to create account [" + params[0] + "] for an unknown reason", whisperBack);
				else
					user.sendChat("The account [" + params[0] + "] has been created", whisperBack);
			}});
		Profile.registerCommand("disconnect", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				for(Connection con : source.getProfile().getConnections())
					con.disconnect(false);
			}});
		Profile.registerCommand("home", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				source.sendJoinChannel(source.getConnectionSettings().channel);
			}});
		Profile.registerCommand("info", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				user.sendChat("BNU-Bot " + CurrentVersion.version() + " running on " + OperatingSystem.osVersion() + " with " + OperatingSystem.javaVersion(), whisperBack);
			}});
		Profile.registerCommand("invite", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				if((params == null) || (params.length != 1))
					user.sendChat("Use: %trigger%invite <user>", whisperBack);
				else
					source.sendClanInvitation(new CommandResponseCookie(user, whisperBack), params[0]);
			}});
		Profile.registerCommand("kick", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				doKickBan(source, user, param, false, whisperBack);
			}});
		Profile.registerCommand("mail", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				if(commanderAccount == null) {
					user.sendChat("You must have an account to use mail.", whisperBack);
					return;
				}

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
						if(params.length == 2) {
							try {
								id = Integer.parseInt(params[1]);
							} catch(Exception e) {
								throw new InvalidUseException();
							}
						}

						List<Mail> rsMail = commanderAccount.getRecievedMail();
						// Sort the mail by sent date
						Collections.sort(rsMail, new Comparator<Mail>() {
							public int compare(Mail arg0, Mail arg1) {
								return arg0.getSent().compareTo(arg1.getSent());
							}});
						if(id == 0) {
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
							user.sendChat(message, whisperBack);
						} else {
							if((rsMail.size() >= id) && (id >= 1))
								sendMail(user, whisperBack, id, rsMail.size(), rsMail.get(id-1));
							else
								user.sendChat("You only have " + rsMail.size() + " messages!", whisperBack);
						}
						return;
					} else if(params[0].equals("empty")
							||params[0].equals("delete")
							||params[0].equals("clear")) {
						//empty
						if(params.length != 1)
							throw new InvalidUseException();

						if(Mail.getUnreadCount(commanderAccount) > 0) {
							user.sendChat("You have unread mail!", whisperBack);
							return;
						}

						try {
							ObjectContext context = commanderAccount.getObjectContext();
							for(Mail m : commanderAccount.getRecievedMail())
								context.deleteObject(m);
							commanderAccount.updateRow();
							user.sendChat("Mailbox cleaned!", whisperBack);
						} catch(Exception e) {
							Out.exception(e);
							user.sendChat("Failed to delete mail: " + e.getMessage(), whisperBack);
						}
					} else
						throw new InvalidUseException();
				} catch(InvalidUseException e) {
					user.sendChat("Use: %trigger%mail (read [number] | empty | send <account> <message>)", whisperBack);
				}
			}});
		Profile.registerCommand("mailall", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				try {
					//<rank> <message>
					if(param == null)
						throw new InvalidUseException();
					params = param.split(" ", 2);
					if((params.length < 2) || (params[1].length() == 0))
						throw new InvalidUseException();

					int rank = 0;
					try {
						rank = Integer.parseInt(params[0]);
					} catch(Exception e) {
						throw new InvalidUseException();
					}

					String message = "[Sent to ranks " + rank + "+] " + params[1];

					List<Account> rsAccounts = Account.getRanked(rank);
					for(Account a : rsAccounts)
						Mail.send(commanderAccount, a, message);
					user.sendChat("Mail queued for delivery to " + rsAccounts.size() + " accounts", whisperBack);
				} catch(InvalidUseException e) {
					user.sendChat("Use: %trigger%mailall <minimum rank> <message>", whisperBack);
				}
			}});
		Profile.registerCommand("ping", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				if((params == null) || (params.length != 1)) {
					user.sendChat("Use: %trigger%ping <user>[@<realm>]", whisperBack);
					return;
				}

				BNetUser bnSubject = source.getCreateBNetUser(params[0], user);
				Integer ping = bnSubject.getPing();
				if(ping == null)
					user.sendChat("I do not know the ping for " + bnSubject.getFullLogonName(), whisperBack);
				else
					user.sendChat("Ping for " + bnSubject.getFullLogonName() + ": " + ping, whisperBack);
			}});
		Profile.registerCommand("pingme", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				user.sendChat("Your ping is: " + user.getPing(), whisperBack);
			}});
		Profile.registerCommand("quit", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				System.exit(0);
			}});
		Profile.registerCommand("reconnect", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				for(Connection con : source.getProfile().getConnections())
					con.reconnect();
			}});
		Profile.registerCommand("recruit", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				if((params == null) || (params.length != 2)) {
					user.sendChat("Use: %trigger%recruit <user>[@<realm>] <account>", whisperBack);
					return;
				}

				if(commanderAccount == null) {
					user.sendChat("You must have an account to use recruit.", whisperBack);
					return;
				}

				BNetUser bnSubject = source.getCreateBNetUser(params[0], user);
				BNLogin rsSubject = BNLogin.get(bnSubject);
				if(rsSubject == null) {
					user.sendChat("I have never seen [" + bnSubject.getFullLogonName() + "] in the channel", whisperBack);
					return;
				}

				if(rsSubject.getAccount() != null) {
					user.sendChat("That user already has an account!", whisperBack);
					return;
				}

				String requiredTagPrefix = GlobalSettings.recruitTagPrefix;
				String requiredTagSuffix = GlobalSettings.recruitTagSuffix;

				if(requiredTagPrefix != null) {
					if(bnSubject.getFullAccountName().substring(0, requiredTagPrefix.length()).compareToIgnoreCase(requiredTagPrefix) != 0) {
						user.sendChat("That user must have the " + requiredTagPrefix + " tag!", whisperBack);
						return;
					}
				}

				if(requiredTagSuffix != null) {
					String s = bnSubject.getFullAccountName();
					int i = s.indexOf("@");
					if(i != -1)
						s = s.substring(0, i);
					s = s.substring(s.length() - requiredTagSuffix.length());
					if(s.compareToIgnoreCase(requiredTagSuffix) != 0) {
						user.sendChat("That user must have the " + requiredTagSuffix + " tag!", whisperBack);
						return;
					}
				}

				try {
					createAccount(params[1], commanderAccount, rsSubject);
				} catch(AccountDoesNotExistException e) {
					user.sendChat(e.getMessage(), whisperBack);
					return;
				}

				bnSubject.resetPrettyName();
				source.sendChat("Welcome to the clan, " + bnSubject.toString() + "!", false);
			}});
		Profile.registerCommand("recruits", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				try {
					if((params != null) && (params.length != 1))
						throw new InvalidUseException();


					Account subjectAccount = null;
					String output = null;
					if(params == null) {
						subjectAccount = commanderAccount;
						output = "You have recruited: ";
					} else {
						subjectAccount = Account.get(params[0]);
						if(subjectAccount == null)
							throw new AccountDoesNotExistException(params[0]);
						output = subjectAccount.getName();
						output += " has recruited: ";
					}

					List<Account> rsRecruits = subjectAccount.getRecruits();
					// Remove accounts below the threshold
					for(Account recruit : rsRecruits) {
						if(recruit.getAccess() < GlobalSettings.recruitAccess)
							rsRecruits.remove(recruit);
					}

					if(rsRecruits.size() > 0) {
						for(Account recruit : rsRecruits)
							output += recruit.getName() + "(" + recruit.getAccess() + ") ";
					} else {
						output += "no one";
					}

					output = output.trim();
					user.sendChat(output, whisperBack);
				} catch(InvalidUseException e) {
					user.sendChat("Use: %trigger%recruits [account]", whisperBack);
				}
				return;
			}});
		Profile.registerCommand("rejoin", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				String channel = source.getChannel();
				source.sendLeaveChat();
				source.sendJoinChannel(channel);
			}});
		Profile.registerCommand("renameaccount", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				if((params == null) || (params.length != 2)) {
					user.sendChat("Use: %trigger%renameaccount <old account> <new account>", whisperBack);
					return;
				}

				Account rsSubjectAccount = Account.get(params[0]);
				Account targetAccount = Account.get(params[1]);

				if((targetAccount != null) && !targetAccount.equals(rsSubjectAccount)) {
					user.sendChat("The Account [" + targetAccount.getName() + "] already exists!", whisperBack);
					return;
				}

				if(rsSubjectAccount == null)
					throw new AccountDoesNotExistException(params[0]);

				params[0] = rsSubjectAccount.getName();

				try {
					rsSubjectAccount.setName(params[1]);
					rsSubjectAccount.updateRow();
				} catch(Exception e) {
					Out.exception(e);
					user.sendChat("Rename failed for an unknown reason.", whisperBack);
					return;
				}

				user.sendChat("The account [" + params[0] + "] was successfully renamed to [" + params[1] + "]", whisperBack);
			}});
		Profile.registerCommand("say", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				source.sendChat(param, false);
			}});
		Profile.registerCommand("search", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				if((params == null) || (params.length != 1)) {
					user.sendChat("Use: %trigger%search <pattern>", whisperBack);
					return;
				}

				String out = "Users found: ";
				int num = 0;
				for(BNLogin login : BNLogin.search(params[0])) {
					if(num >= 10) {
						out += ", <more>";
						break;
					}

					if(num++ > 0)
						out += ", ";
					out += login.getLogin();
				}
				if(num == 0)
					out = "No users found!";
				user.sendChat(out, whisperBack);
			}});
		Profile.registerCommand("searchrank", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				try {
					if((params == null) || (params.length < 1) || (params.length > 2))
						throw new InvalidUseException();

					int access;
					try {
						access = Integer.parseInt(params[0]);
					} catch(NumberFormatException e) {
						throw new InvalidUseException();
					}

					int accessUpper = access;
					if(params.length > 1)
						try {
							accessUpper = Integer.parseInt(params[1]);
						} catch(NumberFormatException e) {
							throw new InvalidUseException();
						}

					String out = "Accounts found: ";
					boolean first = true;
					for(int i = access; i <= accessUpper; i++) {
						Rank rank = Rank.get(i);
						if(rank == null)
							continue;
						for(Account account : rank.getAccountArray()) {
							if(!first)
								out += ", ";
							first = false;
							out += account.getName();
							out += " {" + i + "}";
						}
					}
					if(first)
						out = "No users found!";

					user.sendChat(out, whisperBack);
				} catch(InvalidUseException e) {
					user.sendChat("Use: %trigger%searchrank <rank_lowest> [rank_highest]", whisperBack);
				}
			}});
		Profile.registerCommand("seen", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				if((params == null) || (params.length != 1)) {
					user.sendChat("Use: %trigger%seen <account>", whisperBack);
					return;
				}

				Date mostRecent = null;
				String mostRecentAction = null;

				Account rsSubjectAccount = Account.get(params[0]);
				if(rsSubjectAccount == null) {
					//They don't have an account by that name, check if it's a user
					BNetUser bnSubject = source.getCreateBNetUser(params[0], user);
					BNLogin rsSubject = BNLogin.get(bnSubject);
					if(rsSubject == null) {
						user.sendChat("I have never seen [" + bnSubject.getFullAccountName() + "]", whisperBack);
						return;
					}

					mostRecent = rsSubject.getLastSeen();
					mostRecentAction = rsSubject.getLastAction();
					params[0] = rsSubject.getLogin();
				} else {
					params[0] = rsSubjectAccount.getName();
					for(BNLogin rsSubjectUsers : rsSubjectAccount.getBnLogins()) {
						Date nt = rsSubjectUsers.getLastSeen();
						if(mostRecent == null) {
							mostRecent = nt;
							mostRecentAction = rsSubjectUsers.getLastAction();
						} else {
							if((nt != null) && (nt.compareTo(mostRecent) > 0)) {
								mostRecent = nt;
								mostRecentAction = rsSubjectUsers.getLastAction();
							}
						}
					}
				}

				if(mostRecent == null) {
					user.sendChat("I have never seen [" + params[0] + "]", whisperBack);
					return;
				}

				String diff = TimeFormatter.formatTime(System.currentTimeMillis() - mostRecent.getTime());
				diff = "User [" + params[0] + "] was last seen " + diff + " ago";
				if(mostRecentAction != null)
					diff += " " + mostRecentAction;
				user.sendChat(diff, whisperBack);
			}});
		Profile.registerCommand("setaccount", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				if((params == null) || (params.length < 1) || (params.length > 2)) {
					user.sendChat("Use: %trigger%setaccount <user>[@<realm>] [<account>]", whisperBack);
					return;
				}

				BNetUser bnSubject = source.getCreateBNetUser(params[0], user);
				BNLogin rsSubject = BNLogin.get(bnSubject);
				if(rsSubject == null) {
					user.sendChat("I have never seen [" + bnSubject.getFullAccountName() + "] in the channel", whisperBack);
					return;
				}
				String subject = rsSubject.getLogin();

				Account newAccount = null;
				if(params.length == 2) {
					newAccount = Account.get(params[1]);
					if(newAccount == null)
						throw new AccountDoesNotExistException(params[1]);
				}

				rsSubject.setAccount(newAccount);
				rsSubject.updateRow();

				// Set params[1] to what the account looks like in the database
				if(newAccount == null)
					params = new String[] { params[0], "NULL" };
				else
					params[1] = newAccount.getName();

				bnSubject.resetPrettyName();
				user.sendChat("User [" + subject + "] was added to account [" + params[1] + "] successfully.", whisperBack);
			}});
		Profile.registerCommand("setauth", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				try {
					if((params == null) || (params.length != 2))
						throw new InvalidUseException();

					Command rsCommand = Command.get(params[0]);
					if(rsCommand == null) {
						user.sendChat("That command does not exist!", whisperBack);
						return;
					}

					try {
						int access = Integer.parseInt(params[1]);
						Rank rank = Rank.get(access);
						if(rank == null) {
							user.sendChat("That access level does not exist!", whisperBack);
							return;
						}
						rsCommand.setRank(rank);
						rsCommand.updateRow();

						user.sendChat("Successfully changed the authorization required for command [" + rsCommand.getName() + "] to " + access, whisperBack);
					} catch(NumberFormatException e) {
						throw new InvalidUseException();
					}
				} catch(InvalidUseException e) {
					user.sendChat("Use: %trigger%setauth <command> <access>", whisperBack);
				}
			}});
		Profile.registerCommand("setbirthday", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				if(commanderAccount == null) {
					user.sendChat("You must have an account to use setbirthday.", whisperBack);
					return;
				}

				try {
					if(params == null)
						throw new InvalidUseException();

					Date bd = null;
					try {
						SimpleDateFormat sdf = new SimpleDateFormat("M/d/y");
						bd = sdf.parse(param);
					} catch(Exception e) {
						Out.exception(e);
					}
					if(bd == null)
						throw new InvalidUseException();

					commanderAccount.setBirthday(new java.sql.Date(bd.getTime()));
					commanderAccount.updateRow();

					user.sendChat("Your birthday has been set to [ " + new SimpleDateFormat("M/d/y").format(bd) + " ]", whisperBack);
				} catch(InvalidUseException e) {
					user.sendChat("Use: %trigger%setbirthday <date:MM/DD/YY>", whisperBack);
				}
			}});
		Profile.registerCommand("setrank", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				try {
					if(params == null)
						throw new InvalidUseException();
					if(params.length != 2)
						throw new InvalidUseException();

					int newRank = 0;
					if(params[1].compareToIgnoreCase("peon") == 0)
						newRank = 1;
					else if(params[1].compareToIgnoreCase("grunt") == 0)
						newRank = 2;
					else if(params[1].compareToIgnoreCase("shaman") == 0)
						newRank = 3;
					else
						try {
							newRank = Integer.valueOf(params[1]);
						} catch(Exception e) {}

					if((newRank < 1) || (newRank > 3))
						throw new InvalidUseException();

					// TODO: validate that params[0] is in the clan
					source.sendClanRankChange(
							CookieUtility.createCookie(new CommandResponseCookie(user, whisperBack)),
							params[0],
							newRank);
				} catch(InvalidUseException e) {
					user.sendChat("Use: %trigger%setrank <user> <rank(peon|grunt|shaman|1-3)>", whisperBack);
				}
			}});
		Profile.registerCommand("setrecruiter", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				if((params == null) || (params.length != 2)) {
					user.sendChat("Use: %trigger%setrecruiter <account> <account>", whisperBack);
					return;
				}

				Account rsSubject = Account.get(params[0]);
				if(rsSubject == null)
					throw new AccountDoesNotExistException(params[0]);
				params[0] = rsSubject.getName();

				Account rsTarget = Account.get(params[1]);
				if(rsTarget == null)
					throw new AccountDoesNotExistException(params[1]);
				params[1] = rsTarget.getName();

				String recursive = params[0];

				Account cb = rsTarget;
				do {
					recursive += " -> " + cb.getName();
					if(cb == rsSubject) {
						user.sendChat("Recursion detected: " + recursive, whisperBack);
						break;
					}

					cb = cb.getRecruiter();

					if(cb == null) {
						rsSubject.setRecruiter(rsTarget);
						rsSubject.updateRow();
						user.sendChat("Successfully updated recruiter for [ " + params[0] + " ] to [ " + params[1] + " ]" , whisperBack);
						break;
					}
				} while(true);
			}});
		Profile.registerCommand("sweepban", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				if((params == null) || (params.length < 1)) {
					user.sendChat("Use: %trigger%sweepban <channel>", whisperBack);
					return;
				}
				sweepBanInProgress.put(source, true);
				sweepBannedUsers.put(source, 0);
				source.sendChat("/who " + param, false);
			}});
		Profile.registerCommand("trigger", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				char trigger = source.getTrigger();
				String output = "0000" + Integer.toString(trigger);
				output = output.substring(output.length() - 4);
				output = "Current trigger: " + trigger + " (alt+" + output + ")";
				user.sendChat(output, whisperBack);
			}});
		Profile.registerCommand("unban", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				// TODO: Wildcard unbanning (requires keeping track of banned users)
				if((params == null) || (params.length != 1)) {
					user.sendChat("Use: %trigger%unban <user>[@<realm>]", whisperBack);
					return;
				}

				BNetUser target = new BNetUser(source, params[0], user.getFullAccountName());
				source.sendChat("/unban " + target.getFullLogonName(), false);
			}});
		Profile.registerCommand("vote", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				Vote vote = votes.get(source);
				if(vote == null)
					user.sendChat("There is no vote in progress", whisperBack);
				else if(commanderAccount == null)
					user.sendChat("You must have an account to use vote.", whisperBack);
				else if(param.equals("yes"))
					vote.castVote(commanderAccount, true);
				else if(param.equals("no"))
					vote.castVote(commanderAccount, false);
				else
					user.sendChat("Use: %trigger%vote (yes | no)", whisperBack);
			}});
		Profile.registerCommand("voteban", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				if((param == null) || (params.length != 1)) {
					user.sendChat("Use: %trigger%voteban <user>[@<realm>]", whisperBack);
					return;
				}

				startVote(source, user, param, whisperBack, Boolean.TRUE);
			}});
		Profile.registerCommand("votecancel", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				Vote vote = votes.get(source);
				if(vote == null)
					user.sendChat("There is no vote in progress", whisperBack);
				else
					vote.cancel();
			}});
		Profile.registerCommand("votekick", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				if((param == null) || (params.length != 1)) {
					user.sendChat("Use: %trigger%votekick <user>[@<realm>]", whisperBack);
					return;
				}

				startVote(source, user, param, whisperBack, Boolean.FALSE);
			}});
		Profile.registerCommand("whisperback", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				boolean wb = !GlobalSettings.whisperBack;
				GlobalSettings.whisperBack = wb;
				user.sendChat("WhisperBack is now " + (wb ? "on" : "off"), wb);
			}});
		Profile.registerCommand("whoami", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				source.dispatchParseCommand(user, "whois " + user.getShortLogonName(), whisperBack);
			}});
		Profile.registerCommand("whois", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				try {
					if((params == null) || (params.length != 1))
						throw new InvalidUseException();

					BNetUser bnSubject = null;
					Account rsSubjectAccount = Account.get(params[0]);
					String result = null;
					if(rsSubjectAccount != null) {
						result = rsSubjectAccount.getName();
					} else {
						bnSubject = source.getCreateBNetUser(params[0], user);

						BNLogin rsSubject = BNLogin.get(bnSubject);
						if(rsSubject == null) {
							user.sendChat("I have never seen [" + bnSubject.getFullLogonName() + "] in the channel", whisperBack);
							return;
						}

						rsSubjectAccount = rsSubject.getAccount();
						if(rsSubjectAccount == null) {
							user.sendChat("User [" + params[0] + "] has no account", whisperBack);
							return;
						}

						result = bnSubject.toString();
					}

					// Access
					Rank rsSubjectRank = rsSubjectAccount.getRank();
					if(rsSubjectRank != null) {
						if(bnSubject == null) {
							String prefix = rsSubjectRank.getShortPrefix();
							if(prefix == null)
								prefix = rsSubjectRank.getPrefix();

							if(prefix == null)
								prefix = "";
							else
								prefix += " ";

							result = prefix + rsSubjectAccount.getName();
						}

						result += " " + rsSubjectRank.getVerbstr();
						result += " (" + rsSubjectAccount.getAccess() + ")";
					} else {
						result += " has access " + rsSubjectAccount.getAccess();
					}

					// Birthday
					Date subjectBirthday = rsSubjectAccount.getBirthday();
					if(subjectBirthday != null) {
						double age = System.currentTimeMillis() - subjectBirthday.getTime();
						age /= 1000 * 60 * 60 * 24 * 365.24;
						age = Math.floor(age * 100) / 100;
						result += ", is " + Double.toString(age) + " years old";
					}

					// Last seen
					Date lastSeen = null;
					for(BNLogin rsSubject : rsSubjectAccount.getBnLogins()) {
						if(lastSeen == null)
							lastSeen = rsSubject.getLastSeen();
						else {
							Date nt = rsSubject.getLastSeen();
							if((nt != null) && (nt.compareTo(lastSeen) > 0))
								lastSeen = nt;
						}
					}

					if(lastSeen != null) {
						result += ", was last seen [ ";
						result += TimeFormatter.formatTime(System.currentTimeMillis() - lastSeen.getTime());
						result += " ] ago";
					}

					// Recruiter
					Account rsCreatorAccount = rsSubjectAccount.getRecruiter();
					if(rsCreatorAccount != null) {
						result += ", was recruited by ";
						result += rsCreatorAccount.getName();
					}

					boolean andHasAliases = false;
					for(BNLogin alias : rsSubjectAccount.getBnLogins()) {
						if((bnSubject != null) && bnSubject.equals(alias.getLogin()))
							continue;

						result += ", ";
						if(!andHasAliases) {
							andHasAliases = true;
							result += "and has aliases ";
						}

						result += alias.getLogin();
					}

					user.sendChat(result, whisperBack);
				} catch(InvalidUseException e) {
					user.sendChat("Use: %trigger%whois <user>[@realm]", whisperBack);
				}
			}});
		/*Profile.registerCommand("", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				// ...
			}});*/
	}

	private static void sendMail(BNetUser user, boolean whisperBack, int id, int size, Mail m) {
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

		m.setIsread(true);
		try {
			m.updateRow();
		} catch(Exception e) {
			Out.exception(e);
			user.sendChat("Failed to set mail read", whisperBack);
		}

		user.sendChat(message.toString(), true);
	}

	@Override
	public boolean parseCommand(Connection source, BNetUser user, String command, boolean whisperBack) {
		Out.debug(getClass(), user.toString() + ": " + command + " [" + whisperBack + "]");

		try {
			// Grab the obsolete 'param' string
			String param = null;
			{
				String[] paramHelper = command.split(" ", 2);
				command = paramHelper[0];
				if(paramHelper.length > 1)
					param = paramHelper[1];
			}

			Command rsCommand = Command.get(command);

			if(rsCommand == null) {
				if(!whisperBack)
					source.dispatchRecieveError("Command " + command + " not found in database");
				return false;
			}
			command = rsCommand.getName();

			//Don't ask questions if they are a super-user
			boolean superUser = user.equals(source.getMyUser());
			Account commanderAccount = Account.get(user);
			if(!superUser) {
				if(commanderAccount == null)
					return false;

				int commanderAccess = commanderAccount.getAccess();
				if(commanderAccess <= 0)
					return false;

				int requiredAccess = rsCommand.getAccess();
				if(commanderAccess < requiredAccess) {
					source.dispatchRecieveError("Insufficient access (" + commanderAccess + "/" + requiredAccess + ")");
					return false;
				}
			}

			CommandRunnable cr = Profile.getCommand(command);
			if(cr == null) {
				source.dispatchRecieveError("Command " + command + " has no associated runnable");
				return false;
			}

			String[] params = null;
			if(param != null)
				params = param.split(" ");

			if(!user.equals(source.getMyUser())) {
				lastCommandUser = user;
				lastCommandTime = System.currentTimeMillis();
				lastCommandWhisperBack = whisperBack;
			} else {
				lastCommandUser = null;
			}

			cr.run(source,
					user,
					param,
					params,
					whisperBack,
					commanderAccount,
					superUser);
		} catch(AccountDoesNotExistException e) {
			user.sendChat("The account [" + e.getMessage() + "] does not exist!", whisperBack);
		} catch(InsufficientAccessException e) {
			user.sendChat("You have insufficient access " + e.getMessage(), whisperBack);
		} catch(Exception e) {
			Out.exception(e);
			user.sendChat(e.getClass().getSimpleName() + ": " + e.getMessage(), whisperBack);
		}
		return true;
	}

	private static void createAccount(String accountName, Account recruiter, BNLogin rsSubject)
	throws AccountDoesNotExistException {
		Account rsSubjectAccount = Account.get(accountName);
		if(rsSubjectAccount != null)
			throw new AccountDoesNotExistException("That account already exists!");

		try {
			rsSubjectAccount = Account.create(accountName, Rank.get(0), recruiter);
		} catch(Exception e) {}

		if(rsSubjectAccount == null)
			throw new AccountDoesNotExistException("Failed to create account [" + accountName + "] for an unknown reason");

		rsSubject.setAccount(rsSubjectAccount);
		rsSubjectAccount.setRank(Rank.get(GlobalSettings.recruitAccess));

		try {
			rsSubjectAccount.updateRow();
		} catch(Exception e) {
			throw new AccountDoesNotExistException(e.getMessage());
		}
	}

	private static void doKickBan(Connection source, BNetUser user, String param, boolean isBan, boolean whisperBack) {
		if((param == null) || (param.length() == 0)) {
			if(isBan)
				user.sendChat("Use: %trigger%ban ( <user>[@<realm>] | pattern ) [reason]", whisperBack);
			else
				user.sendChat("Use: %trigger%kick ( <user>[@<realm>] | pattern ) [reason]", whisperBack);
			return;
		}

		// Extract the reason
		String[] params = param.split(" ", 2);
		String reason = params[0];
		if(params.length > 1)
			reason = params[1];

		if(isBan && (params[0].indexOf('*') == -1)) {
			// Regular ban
			String out = (isBan) ? "/ban " : "/kick ";
			BNetUser bnSubject = source.findUser(params[0], user);
			if(bnSubject != null)
				out += bnSubject.getFullLogonName();
			else
				out += params[0];
			out += " " + reason;

			// Send the command
			source.sendChat(out, false);
		} else {
			// Wildcard kick/ban
			List<BNetUser> users = source.findUsersWildcard(params[0], user);
			if(users.size() == 0) {
				user.sendChat("That pattern did not match any users.", whisperBack);
				return;
			}

			int numSkipped = 0;
			for(BNetUser u : users) {
				if((u.getFlags() & 0x02) != 0) {
					numSkipped++;
					continue;
				}

				// Build the command
				String out = (isBan) ? "/ban " : "/kick ";
				out += u.getFullLogonName() + " " + reason;

				// Send the command
				source.sendChat(out, false);
			}

			if(numSkipped > 0)
				user.sendChat("Skipped " + numSkipped + " users with operator status.", whisperBack);
		}
	}

	/**
	 * Start a vote
	 * @param source The connection to vote on
	 * @param user The user who started the vote
	 * @param target The user to vote against
	 * @param whisperBack
	 * @param isBan Whether to ban if vote succeeds
	 */
	private static void startVote(Connection source, BNetUser user, String target, boolean whisperBack, boolean isBan) {
		if(votes.get(source) != null) {
			user.sendChat("There is already a vote in progress!", whisperBack);
			return;
		}

		BNetUser bnSubject = source.findUser(target, user);
		if(bnSubject == null) {
			user.sendChat("User not found", whisperBack);
			return;
		}

		Vote v = new Vote(source, bnSubject, isBan);
		votes.put(source, v);
	}

	@Override
	public void channelJoin(Connection source, BNetUser user) {
		if(!source.getConnectionSettings().enableGreetings)
			return;

		touchUser(source, user, "joining the channel");

		try {
			BNLogin rsUser = BNLogin.getCreate(user);
			if(rsUser == null)
				return;

			// Fix the case of the user's login if it has changed
			rsUser.setLogin(user.getFullAccountName());

			switch(user.getStatString().getProduct()) {
			case STAR: {
				Integer newWins = user.getStatString().getWins();
				if(newWins != null) {
					Integer oldWins = rsUser.getWinsSTAR();
					if((oldWins == null) || (newWins > oldWins)) {
						rsUser.setWinsSTAR(newWins);
					}
				}
				break;
			}

			case SEXP: {
				Integer newWins = user.getStatString().getWins();
				if(newWins != null) {
					Integer oldWins = rsUser.getWinsSEXP();
					if((oldWins == null) || (newWins > oldWins)) {
						rsUser.setWinsSEXP(newWins);
					}
				}
				break;
			}

			case W2BN: {
				Integer newWins = user.getStatString().getWins();
				if(newWins != null) {
					Integer oldWins = rsUser.getWinsW2BN();
					if((oldWins == null) || (newWins > oldWins)) {
						rsUser.setWinsW2BN(newWins);
					}
				}
				break;
			}

			case D2DV:
			case D2XP: {
				Integer newLevel = user.getStatString().getCharLevel();
				if(newLevel != null) {
					Integer oldLevel = rsUser.getLevelD2();
					if((oldLevel == null) || (newLevel > oldLevel)) {
						rsUser.setLevelD2(newLevel);
					}
				}
				break;
			}

			case WAR3:
			case W3XP: {
				Integer newLevel = user.getStatString().getLevel();
				if(newLevel != null) {
					Integer oldLevel = rsUser.getLevelW3();
					if((oldLevel == null) || (newLevel > oldLevel)) {
						rsUser.setLevelW3(newLevel);
					}
				}
				break;
			}
			}
			try {
				rsUser.updateRow();
			} catch(Exception e) {
				Out.exception(e);
			}

			Account rsAccount = Account.get(user);
			Rank rsRank = (rsAccount == null) ? Rank.get(0) : rsAccount.getRank();

			if(rsRank != null) {
				// Greetings
				String greeting = rsRank.getGreeting();
				if(greeting != null) {
					try {
						greeting = String.format(greeting, user.toString(), user.getPing(), user.getFullAccountName());
						source.sendChat(greeting, false);
					} catch(NoSuchMethodError e) {}
				}
			}

			if(rsAccount == null)
				return;

			//Birthdays
			Date birthday = rsAccount.getBirthday();
			if(birthday != null) {
				SimpleDateFormat sdf = new SimpleDateFormat("M-d");

				Calendar cal = Calendar.getInstance();
				Date today = cal.getTime();
				String s1 = sdf.format(today);
				String s2 = sdf.format(birthday);

				if(s1.equals(s2)) {
					cal.setTime(birthday);
					int age = cal.get(Calendar.YEAR);
					cal.setTime(today);
					age = cal.get(Calendar.YEAR) - age;
					source.sendChat("Happy birthday, " + user.toString() + "! Today, you are " + age + " years old!", false);
				}
			}

			//Mail
			int umc = Mail.getUnreadCount(rsAccount);
			if(umc > 0)
				user.sendChat("You have " + umc + " unread messages; type [ %trigger%mail read ] to retrieve them", true);

			if(rsRank != null) {
				// Autopromotions
				Integer apDays = rsRank.getApDays();
				// Check that they meet the days requirement
				apBlock: if((apDays != null) && (apDays != 0)) {
					if(rsAccount.getLastRankChange() != null) {
						double timeElapsed = System.currentTimeMillis() - rsAccount.getLastRankChange().getTime();
						timeElapsed /= 1000 * 60 * 60 * 24;

						if(timeElapsed < apDays)
							break apBlock;
					} else
						break apBlock;

					Integer apWins = rsRank.getApWins();
					Integer apD2Level = rsRank.getApD2Level();
					Integer apW3Level = rsRank.getApW3Level();
					if((apWins == null)
					|| (apD2Level == null)
					|| (apW3Level == null))
						break apBlock;
					long wins[] = rsAccount.getWinsLevels(GlobalSettings.recruitTagPrefix, GlobalSettings.recruitTagSuffix);

					boolean condition = false;
					condition |= ((apWins > 0) && (wins[0] >= apWins));
					condition |= ((apD2Level > 0) && (wins[1] >= apD2Level));
					condition |= ((apW3Level > 0) && (wins[2] >= apW3Level));
					condition |= ((apWins == 0) && (apD2Level == 0) && (apW3Level == 0));

					if(condition) {
						// Check RS
						long rs = rsAccount.getRecruitScore(GlobalSettings.recruitAccess);
						Integer apRS = rsRank.getApRecruitScore();
						if((apRS == null) || (apRS == 0) || (rs >= apRS)) {
							int rank = rsAccount.getAccess();
							// Give them a promotion
							rank++;
							rsAccount.setRank(Rank.get(rank));
							rsAccount.setLastRankChange(new Date(System.currentTimeMillis()));
							try {
								rsAccount.updateRow();
							} catch(Exception e) {
								Out.exception(e);
								break apBlock;
							}
							user.resetPrettyName();	//Reset the presentable name
							source.sendChat("Congratulations " + user.toString() + ", you just recieved a promotion! Your rank is now " + rank + ".", false);
							String apMail = rsRank.getApMail();
							if((apMail != null) && (apMail.length() > 0))
								Mail.send(null, rsAccount, apMail);
						} else {
							user.sendChat("You need " + Long.toString(apRS - rs) + " more recruitment points to recieve a promotion!", true);
						}
					} else {
						String msg = "You need ";
						switch(user.getStatString().getProduct()) {
						case STAR:
						case SEXP:
						case W2BN:
							msg += Long.toString(apWins - wins[0]) + " more win";
							if(apWins - wins[0] > 1)
								msg += "s";
							break;
						case D2DV:
						case D2XP:
							msg += "to reach Diablo 2 level " + apD2Level;
							break;
						case WAR3:
						case W3XP:
							msg += "to reach Warcraft 3 level " + apW3Level;
							break;
						default:
							break apBlock;
						}
						msg += " to recieve a promotion!";
						user.sendChat(msg, true);
					}
				}
			}
		} catch (Exception e) {
			Out.exception(e);
		}
	}

	@Override
	public void channelLeave(Connection source, BNetUser user) {
		touchUser(source, user, "leaving the channel");

		Vote vote = votes.get(source);
		if((vote != null) && vote.getSubject().equals(user))
			vote.cancel();
	}

	@Override
	public void channelUser(Connection source, BNetUser user) {
		try {
			BNLogin.getCreate(user);
		} catch (Exception e) {
			Out.exception(e);
		}
	}

	@Override
	public void recieveChat(Connection source, BNetUser user, String text) {
		touchUser(source, user, "chatting in the channel");
	}

	@Override
	public void recieveEmote(Connection source, BNetUser user, String text) {
		touchUser(source, user, "chatting in the channel");
	}

	private String lastInfo = null;
	private void recieveInfoError(Connection source, String text) {
		if(lastCommandUser == null)
			return;
		if(sweepBanInProgress.get(source) == Boolean.TRUE)
			return;

		long timeElapsed = System.currentTimeMillis() - lastCommandTime;
		// 500ms
		if((timeElapsed < 500)
		&& (!text.equals(lastInfo))) {
			lastInfo = text;
			lastCommandUser.sendChat(text, lastCommandWhisperBack);
		}
	}

	@Override
	public void recieveServerError(Connection source, String text) {
		recieveInfoError(source, text);
	}

	/**
	 * If the name is "[NAME]", return "NAME" otherwise pass name through
	 * @param name	The name from the /who response
	 * @return		Name with [] removed
	 */
	private String removeOpUserBrackets(String name) {
		if(name.charAt(0) == '[') {
			if(name.charAt(name.length() - 1) == ']') {
				return name.substring(1, name.length() - 1);
			}
		}
		return name;
	}

	@Override
	public void recieveServerInfo(Connection source, String text) {
		if(sweepBanInProgress.get(source) == Boolean.TRUE) {
			boolean turnItOff = true;

			if(text.length() > 17) {
				if(text.substring(0, 17).equals("Users in channel ")) {
					if(sweepBannedUsers.get(source) == 0) {
						turnItOff = false;
						source.sendChat("Sweepbanning channel " + text.substring(17, text.length() - 1), false);
					}
				}
			}

			String users[] = text.split(", ");
			if(users.length == 2) {
				if(users[0].indexOf(' ') == -1) {
					if(users[1].indexOf(' ') == -1) {
						source.sendChat("/ban " + removeOpUserBrackets(users[0]), false);
						source.sendChat("/ban " + removeOpUserBrackets(users[1]), false);
						sweepBannedUsers.put(source, sweepBannedUsers.get(source) + 2);
						turnItOff = false;
					}
				}
			} else {
				if(text.indexOf(' ') == -1) {
					source.sendChat("/ban " + removeOpUserBrackets(text), false);
					sweepBannedUsers.put(source, sweepBannedUsers.get(source) + 1);
					turnItOff = true;
				}
			}

			if(turnItOff)
				sweepBanInProgress.put(source, false);
		}

		if(sweepBanInProgress.get(source) == Boolean.TRUE)
			return;

		if(text.contains(" was banned by ")
		|| text.contains(" was unbanned by ")
		|| text.contains(" was kicked out of the channel by "))
			return;

		recieveInfoError(source, text);
	}
}
