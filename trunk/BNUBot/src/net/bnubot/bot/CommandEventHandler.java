/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import net.bnubot.DatabaseContext;
import net.bnubot.core.Connection;
import net.bnubot.core.EventHandler;
import net.bnubot.core.Profile;
import net.bnubot.core.clan.ClanMember;
import net.bnubot.core.commands.AccountDoesNotExistException;
import net.bnubot.core.commands.CommandRunnable;
import net.bnubot.core.commands.InsufficientAccessException;
import net.bnubot.core.commands.InvalidUseException;
import net.bnubot.core.friend.FriendEntry;
import net.bnubot.db.Account;
import net.bnubot.db.BNLogin;
import net.bnubot.db.Command;
import net.bnubot.db.Mail;
import net.bnubot.db.Rank;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetUser;
import net.bnubot.util.CookieUtility;
import net.bnubot.util.OperatingSystem;
import net.bnubot.util.Out;
import net.bnubot.util.TimeFormatter;
import net.bnubot.vercheck.CurrentVersion;

import org.apache.cayenne.DataObjectUtils;

public class CommandEventHandler implements EventHandler {
	private static final Hashtable<Connection, Boolean> sweepBanInProgress = new Hashtable<Connection, Boolean>();
	private static final Hashtable<Connection, Integer> sweepBannedUsers = new Hashtable<Connection, Integer>();
	
	static {
		initializeCommands();
	}
	
	private long	lastCommandTime = 0;
	private BNetUser lastCommandUser = null;
	
	public CommandEventHandler() {
		if(DatabaseContext.getContext() == null)
			throw new IllegalStateException("Can not enable commands without a database!");
	}

	public void initialize(Connection source) {}
	public void disable(Connection source) {}
	
	public void touchUser(Connection source, BNetUser user, String action) {
		BNLogin rsUser = BNLogin.getCreate(user);
		if((rsUser != null) && (action != null)) {
			rsUser.setLastSeen(new Timestamp(System.currentTimeMillis()));
			rsUser.setLastAction(action);
			try {
				rsUser.getObjectContext().commitChanges();
			} catch(Exception e) {
				Out.exception(e);
				rsUser.getObjectContext().rollbackChanges();
			}
		}
	}
	
	public static void initializeCommands() {
		Profile.registerCommand("access", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					int commanderAccess, Account commanderAccount, boolean superUser)
			throws Exception {
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
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					int commanderAccess, Account commanderAccount, boolean superUser)
			throws Exception {
				try {
					if(params == null)
						throw new InvalidUseException();
					if(params.length != 2)
						throw new InvalidUseException();
					
					Account rsSubjectAccount = Account.get(params[0]);
					if(rsSubjectAccount == null) {
						// They don't have an account by that name, check if it's a user
						BNetUser bnSubject = source.getBNetUser(params[0], user);
						
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
					String subjectAccount = rsSubjectAccount.getName();
					
					if(!superUser) {
						if(subjectAccount.equals(commanderAccount))
							throw new InsufficientAccessException("to modify your self");
						if(targetAccess >= commanderAccess)
							throw new InsufficientAccessException("to add users beyond " + (commanderAccess - 1));
					}

					rsSubjectAccount.setRank(targetRank);
					rsSubjectAccount.setLastRankChange(new Timestamp(System.currentTimeMillis()));
					try {
						rsSubjectAccount.getObjectContext().commitChanges();
						user.sendChat("Added user [" + subjectAccount + "] successfully with access " + targetAccess, whisperBack);
					} catch(Exception e) {
						Out.exception(e);
						rsSubjectAccount.getObjectContext().rollbackChanges();
						user.sendChat("Failed: " + e.getMessage(), whisperBack);
					}
				} catch(InvalidUseException e) {
					user.sendChat("Use: %trigger%add <account> <access>", whisperBack);
				}
			}});
		Profile.registerCommand("auth", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					int commanderAccess, Account commanderAccount, boolean superUser)
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
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					int commanderAccess, Account commanderAccount, boolean superUser)
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
						String result = "Rank " + subjectRank + " does not exist! ";
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
						String result = "Autopromotions are not enabled for rank " + subjectRank + ". ";
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
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					int commanderAccess, Account commanderAccount, boolean superUser)
			throws Exception {
				doKickBan(source, user, param, true, whisperBack);
			}});
		Profile.registerCommand("createaccount", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					int commanderAccess, Account commanderAccount, boolean superUser)
			throws Exception {
				if((params == null) || (params.length != 1)) {
					user.sendChat("Use: %trigger%createaccount <account>", whisperBack);
					return;
				}
				
				Account rsAccount = Account.get(params[0]);
				if(rsAccount != null) {
					user.sendChat("The account [" + params[0] + "] already exists", whisperBack);
					return;
				}
				
				rsAccount = Account.create(params[0], Rank.get(0), commanderAccount);
				if(rsAccount == null)
					user.sendChat("Failed to create account [" + params[0] + "] for an unknown reason", whisperBack);
				else
					user.sendChat("The account [" + params[0] + "] has been created", whisperBack);
			}});
		Profile.registerCommand("disconnect", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					int commanderAccess, Account commanderAccount, boolean superUser)
			throws Exception {
				for(Connection con : source.getProfile().getConnections())
					con.disconnect(false);
			}});
		Profile.registerCommand("home", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					int commanderAccess, Account commanderAccount, boolean superUser)
			throws Exception {
				source.sendJoinChannel(source.getConnectionSettings().channel);
			}});
		Profile.registerCommand("info", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					int commanderAccess, Account commanderAccount, boolean superUser)
			throws Exception {
				user.sendChat("BNU-Bot " + CurrentVersion.version() + " running on " + OperatingSystem.osVersion() + " with " + OperatingSystem.javaVersion(), whisperBack);
			}});
		Profile.registerCommand("invite", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					int commanderAccess, Account commanderAccount, boolean superUser)
			throws Exception {
				if((params == null) || (params.length != 1))
					user.sendChat("Use: %trigger%invite <user>", whisperBack);
				else
					source.sendClanInvitation(new CommandResponseCookie(user, whisperBack), params[0]);
			}});
		Profile.registerCommand("kick", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					int commanderAccess, Account commanderAccount, boolean superUser)
			throws Exception {
				doKickBan(source, user, param, false, whisperBack);
			}});
		Profile.registerCommand("mail", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					int commanderAccess, Account commanderAccount, boolean superUser)
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
						user.sendChat("Mail queued for delivery to " +  params[1], whisperBack);
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
						
						if(d.getUnreadMailCount(commanderAccount) > 0) {
							user.sendChat("You have unread mail!", whisperBack);
							return;
						}
						
						d.clearMail(commanderAccount);
						user.sendChat("Mailbox cleaned!", whisperBack);
					} else
						throw new InvalidUseException();
				} catch(InvalidUseException e) {
					user.sendChat("Use: %trigger%mail (read [number] | empty | send <account> <message>)", whisperBack);
				}
			}});
		Profile.registerCommand("mailall", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					int commanderAccess, Account commanderAccount, boolean superUser)
			throws Exception {
				try {
					//<rank> <message>
					if(param == null)
						throw new InvalidUseException();
					params = param.split(" ", 2);
					if((params.length < 2) || (params[1].length() == 0))
						throw new InvalidUseException();
					
					long rank = 0;
					try {
						rank = Long.parseLong(params[0]);
					} catch(Exception e) {
						throw new InvalidUseException();
					}
					
					String message = "[Sent to ranks " + rank + "+] " + params[1];
					
					int numAccounts = 0;
					Account rsAccounts = d.getRankedAccounts(rank);
					while(rsAccounts.next()) {
						long targetAccountID = rsAccounts.getId();
						d.sendMail(commanderAccount, targetAccountID, message);
						numAccounts++;
					}
					user.sendChat("Mail queued for delivery to " + numAccounts + " accounts", whisperBack);
				} catch(InvalidUseException e) {
					user.sendChat("Use: %trigger%mailall <minimum rank> <message>", whisperBack);
				}
			}});
		Profile.registerCommand("ping", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					int commanderAccess, Account commanderAccount, boolean superUser)
			throws Exception {
				if((params == null) || (params.length != 1)) {
					user.sendChat("Use: %trigger%ping <user>[@<realm>]", whisperBack);
					return;
				}
				
				BNetUser bnSubject = source.getBNetUser(params[0], user);
				Integer ping = bnSubject.getPing();
				if(ping == null)
					user.sendChat("I do not know the ping for " + bnSubject.getFullLogonName(), whisperBack);
				else
					user.sendChat("Ping for " + bnSubject.getFullLogonName() + ": " + ping, whisperBack);
			}});
		Profile.registerCommand("pingme", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					int commanderAccess, Account commanderAccount, boolean superUser)
			throws Exception {
				Integer ping = user.getPing();
				if(ping == null)
					user.sendChat("I do not know the ping for " + user.getFullLogonName(), whisperBack);
				else
					user.sendChat("Your ping is: " + ping, whisperBack);
			}});
		Profile.registerCommand("quit", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					int commanderAccess, Account commanderAccount, boolean superUser)
			throws Exception {
				System.exit(0);
			}});
		Profile.registerCommand("reconnect", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					int commanderAccess, Account commanderAccount, boolean superUser)
			throws Exception {
				for(Connection con : source.getProfile().getConnections())
					con.reconnect();
			}});
		Profile.registerCommand("recruit", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					int commanderAccess, Account commanderAccount, boolean superUser)
			throws Exception {
				if((params == null) || (params.length != 2)) {
					user.sendChat("Use: %trigger%recruit <user>[@<realm>] <account>", whisperBack);
					return;
				}
				
				BNetUser bnSubject = source.getBNetUser(params[0], user);
				BNLogin rsSubject = BNLogin.get(bnSubject);
				if(!rsSubject.next()) {
					d.close(rsSubject);
					user.sendChat("I have never seen [" + bnSubject.getFullLogonName() + "] in the channel", whisperBack);
					return;
				}
				
				rsSubject.saveCursor();
				Long subjectAccountId = rsSubject.getAccount();
				if(subjectAccountId != null) {
					d.close(rsSubject);
					user.sendChat("That user already has an account!", whisperBack);
					return;
				}

				String requiredTagPrefix = GlobalSettings.recruitTagPrefix;
				String requiredTagSuffix = GlobalSettings.recruitTagSuffix;
				
				if(requiredTagPrefix != null) {
					if(bnSubject.getFullAccountName().substring(0, requiredTagPrefix.length()).compareToIgnoreCase(requiredTagPrefix) != 0) {
						d.close(rsSubject);
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
						d.close(rsSubject);
						user.sendChat("That user must have the " + requiredTagSuffix + " tag!", whisperBack);
						return;
					}
				}
				
				if(commanderAccount == null) {
					d.close(rsSubject);
					user.sendChat("You must have an account to use recruit.", whisperBack);
					return;
				}
				
				try {
					createAccount(params[1], commanderAccount, rsSubject);
				} catch(AccountDoesNotExistException e) {
					user.sendChat(e.getMessage(), whisperBack);
					return;
				}

				bnSubject.resetPrettyName();
				source.queueChatHelper("Welcome to the clan, " + bnSubject.toString() + "!", false);
			}});
		Profile.registerCommand("recruits", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					int commanderAccess, Account commanderAccount, boolean superUser)
			throws Exception {
				try {
					if((params != null) && (params.length != 1))
						throw new InvalidUseException();
					
					
					Long subjectAccountId = null;
					String output = null;
					if(params == null) {
						subjectAccountId = commanderAccount;
						output = "You have recruited: ";
					} else {
						Account rsSubject = Account.get(params[0]);
						if(!rsSubject.next()) {
							d.close(rsSubject);
							throw new AccountDoesNotExistException(params[0]);
						}
						subjectAccountId = rsSubject.getId();
						output = rsSubject.getName();
						output += " has recruited: ";
						d.close(rsSubject);
					}
					
					Account rsRecruits = Account.getRecruits(subjectAccountId, GlobalSettings.recruitAccess);
					if(rsRecruits.next()) {
						do {
							output += rsRecruits.getName() + "(" + rsRecruits.getAccess() + ") ";
						} while(rsRecruits.next());
					} else {
						output += "no one";
					}
					d.close(rsRecruits);
					
					output = output.trim();
					user.sendChat(output, whisperBack);
				} catch(InvalidUseException e) {
					user.sendChat("Use: %trigger%recruits [account]", whisperBack);
				}
				return;
			}});
		Profile.registerCommand("rejoin", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					int commanderAccess, Account commanderAccount, boolean superUser)
			throws Exception {
				String channel = source.getChannel();
				source.sendLeaveChat();
				source.sendJoinChannel(channel);
			}});
		Profile.registerCommand("renameaccount", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					int commanderAccess, Account commanderAccount, boolean superUser)
			throws Exception {
				if((params == null) || (params.length != 2)) {
					user.sendChat("Use: %trigger%renameaccount <old account> <new account>", whisperBack);
					return;
				}
				
				Account rsSubjectAccount = Account.get(params[0]);
				if((rsSubjectAccount == null) || !rsSubjectAccount.next())
					throw new AccountDoesNotExistException(params[0]);
				
				try {
					rsSubjectAccount.setName(params[1]);
					rsSubjectAccount.updateRow();
				} catch(SQLException e) {
					//TODO: Verify that the exception was actually caused by the UNIQUE restriction
					user.sendChat("The account [" + params[1] + "] already exists!", whisperBack);
					return;
				}
				
				user.sendChat("The account [" + params[0] + "] was successfully renamed to [" + params[1] + "]", whisperBack);
			}});
		Profile.registerCommand("say", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					int commanderAccess, Account commanderAccount, boolean superUser)
			throws Exception {
				source.queueChatHelper(param, false);
			}});
		Profile.registerCommand("seen", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					int commanderAccess, Account commanderAccount, boolean superUser)
			throws Exception {
				if((params == null) || (params.length != 1)) {
					user.sendChat("Use: %trigger%seen <account>", whisperBack);
					return;
				}
				
				Timestamp mostRecent = null;
				String mostRecentAction = null;
				
				Account rsSubjectAccount = Account.get(params[0]);
				if(!rsSubjectAccount.next()) {
					d.close(rsSubjectAccount);
					
					//They don't have an account by that name, check if it's a user
					BNetUser bnSubject = source.getBNetUser(params[0], user);
					BNLogin rsSubject = BNLogin.get(bnSubject);
					if(!rsSubject.next()) {
						d.close(rsSubject);
						user.sendChat("I have never seen [" + bnSubject.getFullAccountName() + "]", whisperBack);
						return;
					}
					
					mostRecent = rsSubject.getLastSeen();
					mostRecentAction = rsSubject.getLastAction();
					params[0] = rsSubject.getLogin();
					d.close(rsSubject);
				} else {
					BNLogin rsSubjectUsers = Account.getUsers(rsSubjectAccount.getId());
					params[0] = rsSubjectAccount.getName();
					d.close(rsSubjectAccount);
					if(!rsSubjectUsers.next()) {
					} else {
						//Check the user's accounts
						do {
							Timestamp nt = rsSubjectUsers.getLastSeen();
							if(mostRecent == null) {
								mostRecent = nt;
								mostRecentAction = rsSubjectUsers.getLastAction();
							} else {
								if((nt != null) && (nt.compareTo(mostRecent) > 0)) {
									mostRecent = nt;
									mostRecentAction = rsSubjectUsers.getLastAction();
								}
							}
						} while(rsSubjectUsers.next());
					}
					d.close(rsSubjectUsers);
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
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					int commanderAccess, Account commanderAccount, boolean superUser)
			throws Exception {
				if((params == null) || (params.length < 1) || (params.length > 2)) {
					user.sendChat("Use: %trigger%setaccount <user>[@<realm>] [<account>]", whisperBack);
					return;
				}

				BNetUser bnSubject = source.getBNetUser(params[0], user);
				BNLogin rsSubject = BNLogin.get(bnSubject);
				if(!rsSubject.next()) {
					d.close(rsSubject);
					user.sendChat("I have never seen [" + bnSubject.getFullAccountName() + "] in the channel", whisperBack);
					return;
				}
				rsSubject.saveCursor();
				String subject = rsSubject.getLogin();
				
				Long newAccount = null;
				if(params.length == 2) {
					Account rsSubjectAccount = Account.get(params[1]);
					if(!rsSubjectAccount.next()) {
						d.close(rsSubjectAccount);
						d.close(rsSubject);
						throw new AccountDoesNotExistException(params[1]);
					}
					newAccount = rsSubjectAccount.getId();
					d.close(rsSubjectAccount);
				}
				
				rsSubject.refreshCursor();
				rsSubject.setAccount(newAccount);
				rsSubject.updateRow();
				d.close(rsSubject);
				
				// Set params[1] to what the account looks like in the database
				if(newAccount == null) {
					params = new String[] { params[0], "NULL" };
				} else {
					Account rsSubjectAccount = Account.get(newAccount);
					if(rsSubjectAccount.next())
						params[1] = rsSubjectAccount.getName();
					d.close(rsSubjectAccount);
				}
				
				bnSubject.resetPrettyName();
				user.sendChat("User [" + subject + "] was added to account [" + params[1] + "] successfully.", whisperBack);
			}});
		Profile.registerCommand("setauth", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					int commanderAccess, Account commanderAccount, boolean superUser)
			throws Exception {
				try {
					if((params == null) || (params.length != 2))
						throw new InvalidUseException();
					
					Command rsCommand = Command.get(params[0]);
					if(!rsCommand.next()) {
						d.close(rsCommand);
						user.sendChat("That command does not exist!", whisperBack);
						return;
					}
					
					try {
						Long access = new Long(params[1]);
						rsCommand.setAccess(access);
						rsCommand.updateRow();
						d.close(rsCommand);
						
						user.sendChat("Successfully changed the authorization required for command [" + params[0] + "] to " + access, whisperBack);
					} catch(NumberFormatException e) {
						d.close(rsCommand);
						throw new InvalidUseException();
					}
				} catch(InvalidUseException e) {
					user.sendChat("Use: %trigger%setauth <command> <access>", whisperBack);
				}
			}});
		Profile.registerCommand("setbirthday", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					int commanderAccess, Account commanderAccount, boolean superUser)
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
					
					if(commanderAccount == null)
						throw new SQLException();
					commanderAccount.setBirthday(new java.sql.Date(bd.getTime()));
					commanderAccount.updateRow();
					
					user.sendChat("Your birthday has been set to [ " + new SimpleDateFormat("M/d/y").format(bd) + " ]", whisperBack);
				} catch(InvalidUseException e) {
					user.sendChat("Use: %trigger%setbirthday <date:MM/DD/YY>", whisperBack);
				}
			}});
		Profile.registerCommand("setrank", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					int commanderAccess, Account commanderAccount, boolean superUser)
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
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					int commanderAccess, Account commanderAccount, boolean superUser)
			throws Exception {
				if((params == null) || (params.length != 2)) {
					user.sendChat("Use: %trigger%setrecruiter <account> <account>", whisperBack);
					return;
				}
				
				Account rsSubject = Account.get(params[0]);
				if(!rsSubject.next()) {
					d.close(rsSubject);
					throw new AccountDoesNotExistException(params[0]);
				}
				rsSubject.saveCursor();
				params[0] = rsSubject.getName();
				
				Account rsTarget = Account.get(params[1]);
				if(!rsTarget.next()) {
					d.close(rsSubject);
					d.close(rsTarget);
					throw new AccountDoesNotExistException(params[1]);
				}
				params[1] = rsTarget.getName();

				
				long subjectID = rsSubject.getId();
				long targetID = rsTarget.getId();
				
				String recursive = params[0];
				
				do {
					long id = rsTarget.getId();
					Long cb = rsTarget.getCreatedBy();
					
					recursive += " -> " + rsTarget.getName();
					if(id == subjectID) {
						user.sendChat("Recursion detected: " + recursive, whisperBack);
						break;
					}

					if(cb != null) {
						d.close(rsTarget);
						rsTarget = Account.get(cb);
						if(!rsTarget.next())
							cb = null;
					}
					
					if(cb == null) {
						rsSubject.refreshCursor();
						rsSubject.setCreatedBy(targetID);
						rsSubject.updateRow();
						user.sendChat("Successfully updated recruiter for [ " + params[0] + " ] to [ " + params[1] + " ]" , whisperBack);
						break;
					}
					
				} while(true);
				
				recursive = null;
				d.close(rsTarget);
				d.close(rsSubject);
			}});
		Profile.registerCommand("sweepban", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					int commanderAccess, Account commanderAccount, boolean superUser)
			throws Exception {
				if((params == null) || (params.length < 1)) {
					user.sendChat("Use: %trigger%sweepban <channel>", whisperBack);
					return;
				}
				sweepBanInProgress.put(source, true);
				sweepBannedUsers.put(source, 0);
				source.queueChatHelper("/who " + param, false);
			}});
		Profile.registerCommand("trigger", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					int commanderAccess, Account commanderAccount, boolean superUser)
			throws Exception {
				char trigger = getTrigger(source);
				String output = "0000" + Integer.toString(trigger);
				output = output.substring(output.length() - 4);
				output = "Current trigger: " + trigger + " (alt+" + output + ")";
				user.sendChat(output, whisperBack);
			}});
		Profile.registerCommand("unban", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					int commanderAccess, Account commanderAccount, boolean superUser)
			throws Exception {
				// TODO: Wildcard unbanning (requires keeping track of banned users)
				if((params == null) || (params.length != 1))
					user.sendChat("Use: %trigger%unban <user>[@<realm>]", whisperBack);
				else
					source.queueChatHelper("/unban " + params[0], false);
			}});
		Profile.registerCommand("whoami", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					int commanderAccess, Account commanderAccount, boolean superUser)
			throws Exception {
				source.parseCommand(user, "whois " + user.getShortLogonName(), whisperBack);
			}});
		Profile.registerCommand("whois", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					int commanderAccess, Account commanderAccount, boolean superUser)
			throws Exception {
				try {
					if((params == null) || (params.length != 1))
						throw new InvalidUseException();
					

					BNetUser bnSubject = null;
					Account rsSubjectAccount = Account.get(params[0]);
					String result = null;
					if(rsSubjectAccount.next()) {
						result = rsSubjectAccount.getName();
					} else {
						bnSubject = source.getBNetUser(params[0], user);
						BNLogin rsSubject = BNLogin.get(bnSubject);
						
						if((rsSubject == null) || (!rsSubject.next())) {
							user.sendChat("I have never seen [" + bnSubject.getFullLogonName() + "] in the channel", whisperBack);
							return;
						}
						
						bnSubject = source.getBNetUser(rsSubject.getLogin(), user);
						d.close(rsSubject);
						d.close(rsSubjectAccount);
						rsSubjectAccount = Account.get(bnSubject);
						
						if((rsSubjectAccount == null) || (!rsSubjectAccount.next())) {
							user.sendChat("User [" + params[0] + "] has no account", whisperBack);
							if(rsSubjectAccount != null)
								d.close(rsSubjectAccount);
							return;
						}
						
						result = bnSubject.toString();
					}

					long subjectAccountID = rsSubjectAccount.getId();
					long subjectAccess = rsSubjectAccount.getAccess();
					Rank rsSubjectRank = d.getRank(subjectAccess);
					
					Date subjectBirthday = rsSubjectAccount.getBirthday();
					
					if(rsSubjectRank.next()) {
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
						
						result += " " + rsSubjectRank.getVerbStr();
						result += " (" + subjectAccess + ")";
					} else {
						result += " has access " + subjectAccess;
					}
					d.close(rsSubjectRank);
					
					if(subjectBirthday != null) {
						double age = System.currentTimeMillis() - subjectBirthday.getTime();
						age /= 1000 * 60 * 60 * 24 * 365.24;
						age = Math.floor(age * 100) / 100;
						result += ", is " + Double.toString(age) + " years old";
					}
					
					// Append aliases
					ArrayList<String> aliases = new ArrayList<String>();
					Timestamp lastSeen = null;
					BNLogin rsSubject = Account.getUsers(subjectAccountID);
					while(rsSubject.next()) {
						if(lastSeen == null)
							lastSeen = rsSubject.getLastSeen();
						else {
							Timestamp nt = rsSubject.getLastSeen();
							if((nt != null) && (nt.compareTo(lastSeen) > 0))
								lastSeen = nt;
						}
						aliases.add(rsSubject.getLogin());
					}
					d.close(rsSubject);

					if(lastSeen != null) {
						result += ", was last seen [ ";
						result += TimeFormatter.formatTime(System.currentTimeMillis() - lastSeen.getTime());
						result += " ] ago";
					}
					
					Long cb = rsSubjectAccount.getCreatedBy();
					if(cb != null) {
						Account rsCreatorAccount = Account.get(cb);
						if(rsCreatorAccount.next()) {
							result += ", was recruited by ";
							result += rsCreatorAccount.getName();
						}
						d.close(rsCreatorAccount);
					}
					
					boolean andHasAliases = false;
					for(String l : aliases) {
						
						if((bnSubject != null) && (bnSubject.equals(l)))
							continue;
						
						if(!andHasAliases) {
							andHasAliases = true;
							result += ", and has aliases ";
						} else {
							result += ", ";
						}
						
						result += l;
					}
					
					d.close(rsSubjectAccount);
					user.sendChat(result, whisperBack);
				} catch(InvalidUseException e) {
					user.sendChat("Use: %trigger%whois <user>[@realm]", whisperBack);
				}
			}});
		/*Profile.registerCommand("", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					int commanderAccess, Account commanderAccount, boolean superUser)
			throws Exception {
				// ...
			}});*/
	}
	
	private static void sendMail(BNetUser user, boolean whisperBack, int id, int size, Mail m) {
		StringBuilder message = new StringBuilder("#");
		message.append(id);
		message.append(" of ");
		message.append(size);
		message.append(": From ");
		message.append(m.getSentFrom().getName());
		message.append(" [");
		message.append(TimeFormatter.formatTime(System.currentTimeMillis() - m.getSent().getTime()));
		message.append(" ago]: ");
		message.append(m.getMessage());
		
		m.setIsread(true);
		try {
			m.getObjectContext().commitChanges();
		} catch(Exception e) {
			Out.exception(e);
			m.getObjectContext().rollbackChanges();
			user.sendChat("Failed to set mail read", whisperBack);
		}
		
		user.sendChat(message.toString(), true);
	}
	
	public boolean parseCommand(Connection source, BNetUser user, String command, boolean whisperBack) {
		Out.debug(getClass(), user.toString() + ": " + command + " [" + whisperBack + "]");
		
		try {
			int commanderAccess = 0;
			Account commanderAccount = null;
			
			//Don't ask questions if they are a super-user
			boolean superUser = user.equals(source.getMyUser());
			try {
				commanderAccount = Account.get(user);
				if(commanderAccount == null) {
					if(superUser)
						throw new InvalidUseException();
					return false;
				}
				
				Rank commanderRank = commanderAccount.getRank();
				if(commanderRank != null)
					commanderAccess = DataObjectUtils.intPKForObject(commanderRank);
				if(commanderAccess <= 0)
					return false;
			} catch(InvalidUseException e) {
				BNLogin.getCreate(user);
			}
			
			// Grab the obsolete 'param' string
			String param = null;
			{
				String[] paramHelper = command.split(" ", 2);
				command = paramHelper[0];
				if(paramHelper.length > 1)
					param = paramHelper[1];
			}
			
			// Closed-scope for rsCommand
			{
				Command rsCommand = Command.get(command);
				if(rsCommand == null) {
					if(!whisperBack)
						source.recieveError("Command " + command + " not found in database");
					return false;
				}
				command = rsCommand.getName();
				if(!superUser) {
					int requiredAccess = rsCommand.getAccess();
					if(commanderAccess < requiredAccess) {
						source.recieveError("Insufficient access (" + commanderAccess + "/" + requiredAccess + ")");
						return false;
					}
				}
			}
			
			lastCommandUser = user;
			lastCommandTime = System.currentTimeMillis();
		
			CommandRunnable cr = Profile.getCommand(command);
			if(cr == null) {
				source.recieveError("Command " + command + " has no associated runnable");
				return false;
			}
			
			String[] params = null;
			if(param != null)
				params = param.split(" ");
			
			cr.run(source,
					user,
					param,
					params,
					whisperBack,
					commanderAccess,
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
	throws SQLException, AccountDoesNotExistException {
		Account rsSubjectAccount = Account.get(accountName);
		if(rsSubjectAccount != null)
			throw new AccountDoesNotExistException("That account already exists!");
		
		rsSubjectAccount = Account.create(accountName, 0, recruiter);
		if(rsSubjectAccount == null)
			throw new AccountDoesNotExistException("Failed to create account [" + accountName + "] for an unknown reason");
		
		long subjectAccountId = rsSubjectAccount.getId();
		rsSubject.refreshCursor();
		rsSubject.setAccount(subjectAccountId);
		rsSubject.updateRow();
		d.close(rsSubject);
		rsSubjectAccount.refreshCursor();
		rsSubjectAccount.setAccess(GlobalSettings.recruitAccess);
		rsSubjectAccount.updateRow();
		d.close(rsSubjectAccount);
	}

	private static char getTrigger(Connection source) {
		return source.getConnectionSettings().trigger.charAt(0);
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
			source.queueChatHelper(out, false);
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
				source.queueChatHelper(out, false);
			}
			
			if(numSkipped > 0)
				user.sendChat("Skipped " + numSkipped + " users with operator status.", whisperBack);
		}
	}

	public void channelJoin(Connection source, BNetUser user) {
		if(!source.getConnectionSettings().enableGreetings)
			return;
		
		touchUser(source, user, "joining the channel");
		
		try {
			BNLogin rsUser = BNLogin.getCreate(user);
			if(rsUser == null)
				return;
			
			switch(user.getStatString().getProduct()) {
			case STAR: {
				Integer newWins = user.getStatString().getWins();
				if(newWins != null) {
					Integer oldWins = rsUser.getWinsSTAR();
					if((oldWins == null) || (newWins > oldWins)) {
						rsUser.setWinsSTAR(newWins);
						rsUser.updateRow();
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
						rsUser.updateRow();
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
						rsUser.updateRow();
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
						rsUser.updateRow();
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
						rsUser.updateRow();
					}
				}
				break;
			}
			}
			d.close(rsUser);
			
			Account rsAccount = Account.get(user);
			if(rsAccount == null)
				return;
			if(!rsAccount.next()) {
				d.close(rsAccount);
				return;
			}
			rsAccount.saveCursor();
			
			//check for birthdays
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
					source.queueChatHelper("Happy birthday, " + user.toString() + "! Today, you are " + age + " years old!", false);
				}
			}

			long rank = rsAccount.getAccess();
			long id = rsAccount.getId();
			Rank rsRank = d.getRank(rank);
			if(rsRank.next()) {
				// Greetings
				String greeting = rsRank.getGreeting();
				if(greeting != null) {
					greeting = String.format(greeting, user.toString(), user.getPing(), user.getFullAccountName());
					source.queueChatHelper(greeting, false);
				}

				// Autopromotions
				Long apDays = rsRank.getApDays();
				Timestamp ts = rsAccount.getLastRankChange();
				// Check that they meet the days requirement
				apBlock: if((apDays != null) && (apDays != 0)) {
					if(ts != null) {
						double timeElapsed = System.currentTimeMillis() - ts.getTime();
						timeElapsed /= 1000 * 60 * 60 * 24;
						
						if(timeElapsed < apDays)
							break apBlock;
					} else
						break apBlock;
					
					Long apWins = rsRank.getApWins();
					Long apD2Level = rsRank.getApD2Level();
					Long apW3Level = rsRank.getApW3Level();
					if((apWins == null)
					|| (apD2Level == null)
					|| (apW3Level == null))
						break apBlock;
					long wins[] = Account.getWinsLevels(id, GlobalSettings.recruitTagPrefix, GlobalSettings.recruitTagSuffix);
					
					boolean condition = false;
					condition |= ((apWins > 0) && (wins[0] >= apWins));
					condition |= ((apD2Level > 0) && (wins[1] >= apD2Level));
					condition |= ((apW3Level > 0) && (wins[2] >= apW3Level));
					condition |= ((apWins == 0) && (apD2Level == 0) && (apW3Level == 0));
					
					if(condition) {
						// Check RS
						long rs = Account.getRecruitScore(id, GlobalSettings.recruitAccess);
						Long apRS = rsRank.getApRecruitScore();
						if((apRS == null) || (apRS == 0) || (rs >= apRS)) {
							// Give them a promotion
							rank++;
							rsAccount.refreshCursor();
							rsAccount.setAccess(rank);
							rsAccount.setLastRankChange(new Timestamp(System.currentTimeMillis()));
							rsAccount.updateRow();
							user.resetPrettyName();	//Reset the presentable name
							source.queueChatHelper("Congratulations " + user.toString() + ", you just recieved a promotion! Your rank is now " + rank + ".", false);
							String apMail = rsRank.getApMail();
							if((apMail != null) && (apMail.length() > 0))
								d.sendMail(id, id, apMail);
						} else {
							user.sendChat("You need " + Long.toString(apRS - rs) + " more recruitment points to recieve a promotion!", false);
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
						user.sendChat(msg, false);
					}
				}
			}
			d.close(rsRank);
			d.close(rsAccount);

			//Mail
			long umc = d.getUnreadMailCount(id);
			if(umc > 0)
				user.sendChat("You have " + umc + " unread messages; type [ %trigger%mail read ] to retrieve them", false);
		} catch (Exception e) {
			Out.exception(e);
		}
	}
	
	public void channelLeave(Connection source, BNetUser user) {
		touchUser(source, user, "leaving the channel");
	}
	
	public void channelUser(Connection source, BNetUser user) {
		touchUser(source, user, null);
	}
	
	public void joinedChannel(Connection source, String channel) {}

	public void recieveChat(Connection source, BNetUser user, String text) {
		if(text == null)
			return;
		if(text.length() == 0)
			return;
		
		touchUser(source, user, "chatting in the channel");
		
		if(text.equals("?trigger"))
			parseCommand(source, user, "trigger", GlobalSettings.whisperBack);
		else
			if(text.charAt(0) == getTrigger(source)) {
				parseCommand(source, user, text.substring(1), GlobalSettings.whisperBack);
			}
	}

	public void recieveEmote(Connection source, BNetUser user, String text) {}
	
	private boolean enableSendInfoErrorBack = false;
	private String lastInfo = null;
	private void recieveInfoError(Connection source, String text) {
		if(!enableSendInfoErrorBack)
			return;
		
		long timeElapsed = System.currentTimeMillis() - lastCommandTime;
		// 200ms
		if(timeElapsed < 200) {
			if(!text.equals(lastInfo)) {
				lastInfo = text;
				lastCommandUser.sendChat(text, false);
			}
		}
	}
	
	public void recieveError(Connection source, String text) {
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
	
	public void recieveInfo(Connection source, String text) {
		if(sweepBanInProgress.get(source) == Boolean.TRUE) {
			boolean turnItOff = true;
			
			if(text.length() > 17) {
				if(text.substring(0, 17).equals("Users in channel ")) {
					if(sweepBannedUsers.get(source) == 0) {
						turnItOff = false;
						source.queueChatHelper("Sweepbanning channel " + text.substring(17, text.length() - 1), false);
					}
				}
			}
			
			String users[] = text.split(", ");
			if(users.length == 2) {
				if(users[0].indexOf(' ') == -1) {
					if(users[1].indexOf(' ') == -1) {
						source.queueChatHelper("/ban " + removeOpUserBrackets(users[0]), false);
						source.queueChatHelper("/ban " + removeOpUserBrackets(users[1]), false);
						sweepBannedUsers.put(source, sweepBannedUsers.get(source) + 2);
						turnItOff = false;
					}
				}
			} else {
				if(text.indexOf(' ') == -1) {
					source.queueChatHelper("/ban " + removeOpUserBrackets(text), false);
					sweepBannedUsers.put(source, sweepBannedUsers.get(source) + 1);
					turnItOff = true;
				}
			}
			
			if(turnItOff)
				sweepBanInProgress.put(source, false);
		}
		
		if(sweepBanInProgress.get(source) == Boolean.TRUE)
			return;
		
		recieveInfoError(source, text);
	}
	
	public void recieveDebug(Connection source, String text) {}

	public void bnetConnected(Connection source) {}
	public void bnetDisconnected(Connection source) {}
	public void titleChanged(Connection source) {}

	public void whisperRecieved(Connection source, BNetUser user, String text) {
		if(text == null)
			return;
		if(text.length() == 0)
			return;
		
		if(text.charAt(0) == getTrigger(source))
			text = text.substring(1);
		
		parseCommand(source, user, text, true);
	}

	public void whisperSent(Connection source, BNetUser user, String text) {}

	public void friendsList(Connection source, FriendEntry[] entries) {}
	public void friendsUpdate(Connection source, FriendEntry friend) {}
	public void friendsAdd(Connection source, FriendEntry friend) {}
	public void friendsPosition(Connection source, byte oldPosition, byte newPosition) {}
	public void friendsRemove(Connection source, byte entry) {}
	
	public void queryRealms2(Connection source, String[] realms) {}
	public void logonRealmEx(Connection source, int[] MCPChunk1, int ip, int port, int[] MCPChunk2, String uniqueName) {}

	public void clanMOTD(Connection source, Object cookie, String text) {}
	public void clanMemberList(Connection source, ClanMember[] members) {}
	public void clanMemberRemoved(Connection source, String username) {}
	public void clanMemberStatusChange(Connection source, ClanMember member) {}
	public void clanMemberRankChange(Connection source, byte oldRank, byte newRank, String user) {}
}
