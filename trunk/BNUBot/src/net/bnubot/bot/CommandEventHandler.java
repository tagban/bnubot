/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import net.bnubot.bot.database.AccountResultSet;
import net.bnubot.bot.database.BNLoginResultSet;
import net.bnubot.bot.database.CommandResultSet;
import net.bnubot.bot.database.Database;
import net.bnubot.bot.database.RankResultSet;
import net.bnubot.core.Connection;
import net.bnubot.core.EventHandler;
import net.bnubot.core.Profile;
import net.bnubot.core.bncs.ProductIDs;
import net.bnubot.core.clan.ClanMember;
import net.bnubot.core.commands.AccountDoesNotExistException;
import net.bnubot.core.commands.CommandRunnable;
import net.bnubot.core.commands.InsufficientAccessException;
import net.bnubot.core.commands.InvalidUseException;
import net.bnubot.core.friend.FriendEntry;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetUser;
import net.bnubot.util.CookieUtility;
import net.bnubot.util.Out;
import net.bnubot.util.TimeFormatter;
import net.bnubot.vercheck.CurrentVersion;

public class CommandEventHandler implements EventHandler {
	private static final Database d = Database.getInstance();
	private static final Hashtable<Connection, Boolean> sweepBanInProgress = new Hashtable<Connection, Boolean>();
	private static final Hashtable<Connection, Integer> sweepBannedUsers = new Hashtable<Connection, Integer>();
	
	private long	lastCommandTime = 0;
	private BNetUser lastCommandUser = null;
	
	public CommandEventHandler() {
		if(d == null)
			throw new AssertionError("Can not enable commands without a database!");
		initializeCommands();
	}

	public void initialize(Connection source) {}
	public void disable(Connection source) {}
	
	public void touchUser(Connection source, BNetUser user, String action) {
		try {
			BNLoginResultSet rsUser = d.getCreateUser(user);
			if(rsUser.next() && (action != null)) {
				rsUser.setLastSeen(new Timestamp(System.currentTimeMillis()));
				rsUser.setLastAction(action);
				rsUser.updateRow();
			}
			d.close(rsUser);
		} catch(Exception e) {
			source.recieveError(e.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}
	
	public static void initializeCommands() {
		Profile.registerCommand("access", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					long commanderAccess, String commanderAccount, Long commanderAccountID, boolean superUser)
			throws Exception {
				try {
					if(params == null)
						throw new InvalidUseException();
					if(params.length != 1)
						throw new InvalidUseException();

					CommandResultSet rsSubjectCategory;
					if(params[0].equals("all"))
						rsSubjectCategory = d.getCommands(commanderAccess);
					else
						rsSubjectCategory = d.getCommandCategory(params[0], commanderAccess);
					
					if((rsSubjectCategory == null) || !rsSubjectCategory.next()) {
						source.sendChat(user, "The category [" + params[0] + "] does not exist!", whisperBack);
						if(rsSubjectCategory != null)
							d.close(rsSubjectCategory);
						return;
					}
					
					String result = "Available commands for rank " + commanderAccess + " in cagegory " + params[0] + ": ";
					result += rsSubjectCategory.getName() + " (" + rsSubjectCategory.getAccess() + ")";
					while(rsSubjectCategory.next())
						result += ", " + rsSubjectCategory.getName() + " (" + rsSubjectCategory.getAccess() + ")";
					
					d.close(rsSubjectCategory);
					source.sendChat(user, result, whisperBack);
				} catch(InvalidUseException e) {
					String use = "Use: %trigger%access <category> -- Available categories for rank " + commanderAccess + ": all";
					CommandResultSet rsCategories = d.getCommandCategories(commanderAccess);
					while(rsCategories.next())
						use += ", " + rsCategories.getCmdGroup();
					d.close(rsCategories);
					source.sendChat(user, use, whisperBack);
				}
			}});
		Profile.registerCommand("add", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					long commanderAccess, String commanderAccount, Long commanderAccountID, boolean superUser)
			throws Exception {
				try {
					if(params == null)
						throw new InvalidUseException();
					if(params.length != 2)
						throw new InvalidUseException();
					
					AccountResultSet rsSubjectAccount = d.getAccount(params[0]);
					if(rsSubjectAccount.next()) {
						// They are doing add on an account name
						rsSubjectAccount.saveCursor();
					} else {
						// They don't have an account by that name, check if it's a user
						BNetUser bnSubject = source.getBNetUser(params[0], user);
						
						rsSubjectAccount = d.getAccount(bnSubject);
						if((rsSubjectAccount != null) && rsSubjectAccount.next()) {
							// The account exists; fall through
						} else {
							// The account does not exist
							BNLoginResultSet rsSubject = d.getUser(bnSubject);
							if(!rsSubject.next()) {
								d.close(rsSubject);
								source.sendChat(user, "I have never seen [" + bnSubject.getFullAccountName() + "]", whisperBack);
								return;
							}
							rsSubject.saveCursor();
							
							try {
								createAccount(params[0], commanderAccountID, rsSubject);
							} catch(AccountDoesNotExistException e) {
								source.sendChat(user, e.getMessage(), whisperBack);
								return;
							}
							
							// Re-load the account
							rsSubjectAccount = d.getAccount(bnSubject);
							if((rsSubjectAccount == null) || !rsSubjectAccount.next()) {
								source.sendChat(user, "Failed to create account for an unknown reason", whisperBack);
								if(rsSubjectAccount != null)
									d.close(rsSubjectAccount);
								return;
							}
						}
					}

					long targetAccess = Long.parseLong(params[1]);
					String subjectAccount = rsSubjectAccount.getName();
					
					if(!superUser) {
						if(subjectAccount.equals(commanderAccount))
							throw new InsufficientAccessException("to modify your self");
						if(targetAccess >= commanderAccess)
							throw new InsufficientAccessException("to add users beyond " + (commanderAccess - 1));
					}

					rsSubjectAccount.setAccess(targetAccess);
					rsSubjectAccount.setLastRankChange(new Timestamp(System.currentTimeMillis()));
					rsSubjectAccount.updateRow();
					d.close(rsSubjectAccount);
					source.sendChat(user, "Added user [" + subjectAccount + "] successfully with access " + targetAccess, whisperBack);
				} catch(InvalidUseException e) {
					source.sendChat(user, "Use: %trigger%add <account> <access>", whisperBack);
				}
			}});
		Profile.registerCommand("auth", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					long commanderAccess, String commanderAccount, Long commanderAccountID, boolean superUser)
			throws Exception {
				try {
					if(params == null)
						throw new InvalidUseException();
					if(params.length != 1)
						throw new InvalidUseException();
					
					CommandResultSet rsSubectCommand = d.getCommand(params[0]);
					if((rsSubectCommand == null) || !rsSubectCommand.next()) {
						source.sendChat(user, "The command [" + params[0] + "] does not exist!", whisperBack);
						return;
					}
					
					params[0] = rsSubectCommand.getName();
					long access = rsSubectCommand.getAccess();
					
					source.sendChat(user, "Authorization required for " + params[0] + " is " + access, whisperBack);
				} catch(InvalidUseException e) {
					source.sendChat(user, "Use: %trigger%auth <command>", whisperBack);
				}
			}});
		Profile.registerCommand("autopromotion", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					long commanderAccess, String commanderAccount, Long commanderAccountID, boolean superUser)
			throws Exception {
				try {
					if((params != null) && (params.length != 1))
						throw new InvalidUseException();
					
					Long subjectAccountId = null;
					Long subjectRank = null;
					if(params == null) {
						subjectAccountId = commanderAccountID;
						subjectRank = commanderAccess;
					} else {
						AccountResultSet rsSubjectAccount = d.getAccount(params[0]);
						if(rsSubjectAccount.next()) {
							subjectAccountId = rsSubjectAccount.getId();
							subjectRank = rsSubjectAccount.getAccess();
						}
						d.close(rsSubjectAccount);
					}
					
					if((subjectAccountId == null) || (subjectRank == null))
						throw new AccountDoesNotExistException(params[0]);
					
					AccountResultSet rsSubjectAccount = d.getAccount(subjectAccountId);
					if(!rsSubjectAccount.next()) {
						d.close(rsSubjectAccount);
						//This isn't actually invalid use, but it's a state we should never encounter
						throw new InvalidUseException();
					}
					
					long wins[] = d.getAccountWinsLevels(subjectAccountId, GlobalSettings.recruitTagPrefix, GlobalSettings.recruitTagSuffix);
					long recruitScore = d.getAccountRecruitScore(subjectAccountId, GlobalSettings.recruitAccess);
					Timestamp ts = rsSubjectAccount.getLastRankChange();
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
					
					RankResultSet rsRank = d.getRank(subjectRank);
					if(rsRank.next()) {
						Long apDays = rsRank.getApDays();
						Long apWins = rsRank.getApWins();
						Long apD2Level = rsRank.getApD2Level();
						Long apW3Level = rsRank.getApW3Level();
						Long apRecruitScore = rsRank.getApRecruitScore();

						boolean condition = false;
						condition |= (apDays == null);
						condition |= (apWins == null);
						condition |= (apD2Level == null);
						condition |= (apW3Level == null);
						if(condition == false)
							condition = (apDays == 0) && (apWins == 0) && (apD2Level == 0) && (apW3Level == 0);
						if(condition) {
							String result = "Autopromotions are not enabled for rank " + subjectRank + ". ";
							result += rsSubjectAccount.getName() + "'s current status is: ";
							result += "Days: " + timeElapsed;
							result += ", Wins: " + wins[0];
							result += ", D2 Level: " + wins[1];
							result += ", W3 level: " + wins[2];
							if(recruitScore > 0)
								result += ", Recruit Score: " + recruitScore;
							
							source.sendChat(user, result, whisperBack);
						} else {
							String result = "AutoPromotion Info for [" + rsSubjectAccount.getName() + "]: ";
							result += "Days: " + timeElapsed + "/" + apDays;
							result += ", Wins: " + wins[0] + "/" + apWins;
							result += ", D2 Level: " + wins[1] + "/" + apD2Level;
							result += ", W3 level: " + wins[2] + "/" + apW3Level;
							if(((apRecruitScore != null) && (apRecruitScore > 0)) || (recruitScore > 0)) {
								result += ", Recruit Score: " + recruitScore;
								if(apRecruitScore != null)
									result += "/" + apRecruitScore;
							}
							
							source.sendChat(user, result, whisperBack);
						}
					} else {
						String result = "Rank " + subjectRank + " was not found in the database; please contact the bot master and report this error. ";
						result += rsSubjectAccount.getName() + "'s current status is: ";
						result += timeElapsed + " days, ";
						result += wins[0] + " wins, ";
						result += wins[1] + " D2 level, ";
						result += wins[2] + " W3 level";
						
						source.sendChat(user, result, whisperBack);
					}
					d.close(rsRank);
					d.close(rsSubjectAccount);
				} catch(InvalidUseException e) {
					source.sendChat(user, "Use: %trigger%automaticpromotion [account]", whisperBack);
				}
			}});
		Profile.registerCommand("ban", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					long commanderAccess, String commanderAccount, Long commanderAccountID, boolean superUser)
			throws Exception {
				doKickBan(source, user, param, true, whisperBack);
			}});
		Profile.registerCommand("createaccount", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					long commanderAccess, String commanderAccount, Long commanderAccountID, boolean superUser)
			throws Exception {
				if((params == null) || (params.length != 1)) {
					source.sendChat(user, "Use: %trigger%createaccount <account>", whisperBack);
					return;
				}
				
				AccountResultSet rsAccount = d.getAccount(params[0]);
				if(rsAccount.next()) {
					d.close(rsAccount);
					source.sendChat(user, "The account [" + params[0] + "] already exists", whisperBack);
					return;
				}
				d.close(rsAccount);
				
				rsAccount = d.createAccount(params[0], 0L, commanderAccountID);
				if(!rsAccount.next()) {
					d.close(rsAccount);
					source.sendChat(user, "Failed to create account [" + params[0] + "] for an unknown reason", whisperBack);
					return;
				}
				d.close(rsAccount);
				
				source.sendChat(user, "The account [" + params[0] + "] has been created", whisperBack);
			}});
		Profile.registerCommand("disconnect", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					long commanderAccess, String commanderAccount, Long commanderAccountID, boolean superUser)
			throws Exception {
				source.setConnected(false);
			}});
		Profile.registerCommand("home", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					long commanderAccess, String commanderAccount, Long commanderAccountID, boolean superUser)
			throws Exception {
				source.sendJoinChannel(source.getConnectionSettings().channel);
			}});
		Profile.registerCommand("info", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					long commanderAccess, String commanderAccount, Long commanderAccountID, boolean superUser)
			throws Exception {
				source.sendChat(user, "BNU-Bot " + CurrentVersion.version() + " running on " + osVersion() + " with " + javaVersion(), whisperBack);
			}});
		Profile.registerCommand("invite", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					long commanderAccess, String commanderAccount, Long commanderAccountID, boolean superUser)
			throws Exception {
				if((params == null) || (params.length != 1))
					source.sendChat(user, "Use: %trigger%invite <user>", whisperBack);
				else
					source.sendClanInvitation(new CommandResponseCookie(user, whisperBack), params[0]);
			}});
		Profile.registerCommand("kick", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					long commanderAccess, String commanderAccount, Long commanderAccountID, boolean superUser)
			throws Exception {
				doKickBan(source, user, param, false, whisperBack);
			}});
		Profile.registerCommand("mail", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					long commanderAccess, String commanderAccount, Long commanderAccountID, boolean superUser)
			throws Exception {
				if(commanderAccountID == null) {
					source.sendChat(user, "You must have an account to use mail.", whisperBack);
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
						
						AccountResultSet rsTargetAccount = d.getAccount(params[1]);
						if(!rsTargetAccount.next()) {
							d.close(rsTargetAccount);
							throw new AccountDoesNotExistException(params[1]);
						}
						params[1] = rsTargetAccount.getName();
						Long targetAccountID = rsTargetAccount.getId();
						d.close(rsTargetAccount);
						
						d.sendMail(commanderAccountID, targetAccountID, params[2]);
						source.sendChat(user, "Mail queued for delivery to " +  params[1], whisperBack);
					} else if(params[0].equals("read")
							||params[0].equals("get")) {
						//read [number]
						if((params.length < 1) || (params.length > 2))
							throw new InvalidUseException();
						
						Long id = null;
						if(params.length == 2) {
							try {
								id = Long.parseLong(params[1]);
							} catch(Exception e) {
								throw new InvalidUseException();
							}
						}
						
						ResultSet rsMail = d.getMail(commanderAccountID);
						if(id == null) {
							while(rsMail.next()) {
								boolean read = rsMail.getBoolean("isread");
								if(read)
									continue;

								String message = "#";
								message += rsMail.getRow();
								message += " of ";
								message += d.getMailCount(commanderAccountID);
								message += ": From ";
								message += rsMail.getString("name");
								message += " [";
								message += TimeFormatter.formatTime(System.currentTimeMillis() - rsMail.getTimestamp("sent").getTime());
								message += " ago]: ";
								message += rsMail.getString("message");
								
								d.setMailRead(rsMail.getLong("id"));
								d.close(rsMail);
								
								source.sendChat(user, message, true);
								return;
							}
							
							String message = "You have no unread mail!";
							long mailCount = d.getMailCount(commanderAccountID);
							if(mailCount > 0)
								message += " To read your " + mailCount + " messages, type [ %trigger%mail read <number> ]";
							source.sendChat(user, message, whisperBack);
						} else {
							long mailNumber = 0;
							while(rsMail.next()) {
								mailNumber++;
								if(mailNumber != id)
									continue;

								String message = "#";
								message += rsMail.getRow();
								message += " of ";
								message += d.getMailCount(commanderAccountID);
								message += ": From ";
								message += rsMail.getString("name");
								message += " [";
								message += TimeFormatter.formatTime(System.currentTimeMillis() - rsMail.getTimestamp("sent").getTime());
								message += " ago]: ";
								message += rsMail.getString("message");
								
								d.setMailRead(rsMail.getLong("id"));
								d.close(rsMail);
								
								source.sendChat(user, message, true);
								return;
							}
							
							source.sendChat(user, "You only have " + mailNumber + " messages!", whisperBack);
						}
						d.close(rsMail);
						return;
					} else if(params[0].equals("empty")
							||params[0].equals("delete")
							||params[0].equals("clear")) {
						//empty
						if(params.length != 1)
							throw new InvalidUseException();
						
						if(d.getUnreadMailCount(commanderAccountID) > 0) {
							source.sendChat(user, "You have unread mail!", whisperBack);
							return;
						}
						
						d.clearMail(commanderAccountID);
						source.sendChat(user, "Mailbox cleaned!", whisperBack);
					} else
						throw new InvalidUseException();
				} catch(InvalidUseException e) {
					source.sendChat(user, "Use: %trigger%mail (read [number] | empty | send <account> <message>)", whisperBack);
				}
			}});
		Profile.registerCommand("mailall", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					long commanderAccess, String commanderAccount, Long commanderAccountID, boolean superUser)
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
					AccountResultSet rsAccounts = d.getRankedAccounts(rank);
					while(rsAccounts.next()) {
						long targetAccountID = rsAccounts.getId();
						d.sendMail(commanderAccountID, targetAccountID, message);
						numAccounts++;
					}
					source.sendChat(user, "Mail queued for delivery to " + numAccounts + " accounts", whisperBack);
				} catch(InvalidUseException e) {
					source.sendChat(user, "Use: %trigger%mailall <minimum rank> <message>", whisperBack);
				}
			}});
		Profile.registerCommand("ping", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					long commanderAccess, String commanderAccount, Long commanderAccountID, boolean superUser)
			throws Exception {
				if((params == null) || (params.length != 1)) {
					source.sendChat(user, "Use: %trigger%ping <user>[@<realm>]", whisperBack);
					return;
				}
				
				BNetUser bnSubject = source.getBNetUser(params[0], user);
				Integer ping = bnSubject.getPing();
				if(ping == null)
					source.sendChat(user, "I do not know the ping for " + bnSubject.getFullLogonName(), whisperBack);
				else
					source.sendChat(user, "Ping for " + bnSubject.getFullLogonName() + ": " + ping, whisperBack);
			}});
		Profile.registerCommand("pingme", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					long commanderAccess, String commanderAccount, Long commanderAccountID, boolean superUser)
			throws Exception {
				Integer ping = user.getPing();
				if(ping == null)
					source.sendChat(user, "I do not know the ping for " + user.getFullLogonName(), whisperBack);
				else
					source.sendChat(user, "Your ping is: " + ping, whisperBack);
			}});
		Profile.registerCommand("quit", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					long commanderAccess, String commanderAccount, Long commanderAccountID, boolean superUser)
			throws Exception {
				System.exit(0);
			}});
		Profile.registerCommand("reconnect", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					long commanderAccess, String commanderAccount, Long commanderAccountID, boolean superUser)
			throws Exception {
				source.reconnect();
			}});
		Profile.registerCommand("recruit", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					long commanderAccess, String commanderAccount, Long commanderAccountID, boolean superUser)
			throws Exception {
				if((params == null) || (params.length != 2)) {
					source.sendChat(user, "Use: %trigger%recruit <user>[@<realm>] <account>", whisperBack);
					return;
				}
				
				BNetUser bnSubject = source.getBNetUser(params[0], user);
				BNLoginResultSet rsSubject = d.getUser(bnSubject);
				if(!rsSubject.next()) {
					d.close(rsSubject);
					source.sendChat(user, "I have never seen [" + bnSubject.getFullLogonName() + "] in the channel", whisperBack);
					return;
				}
				
				rsSubject.saveCursor();
				Long subjectAccountId = rsSubject.getAccount();
				if(subjectAccountId != null) {
					d.close(rsSubject);
					source.sendChat(user, "That user already has an account!", whisperBack);
					return;
				}

				String requiredTagPrefix = GlobalSettings.recruitTagPrefix;
				String requiredTagSuffix = GlobalSettings.recruitTagSuffix;
				
				if(requiredTagPrefix != null) {
					if(bnSubject.getFullAccountName().substring(0, requiredTagPrefix.length()).compareToIgnoreCase(requiredTagPrefix) != 0) {
						d.close(rsSubject);
						source.sendChat(user, "That user must have the " + requiredTagPrefix + " tag!", whisperBack);
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
						source.sendChat(user, "That user must have the " + requiredTagSuffix + " tag!", whisperBack);
						return;
					}
				}
				
				if(commanderAccountID == null) {
					d.close(rsSubject);
					source.sendChat(user, "You must have an account to use recruit.", whisperBack);
					return;
				}
				
				try {
					createAccount(params[1], commanderAccountID, rsSubject);
				} catch(AccountDoesNotExistException e) {
					source.sendChat(user, e.getMessage(), whisperBack);
					return;
				}

				bnSubject.resetPrettyName();
				source.queueChatHelper("Welcome to the clan, " + bnSubject.toString() + "!", false);
			}});
		Profile.registerCommand("recruits", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					long commanderAccess, String commanderAccount, Long commanderAccountID, boolean superUser)
			throws Exception {
				try {
					if((params != null) && (params.length != 1))
						throw new InvalidUseException();
					
					
					Long subjectAccountId = null;
					String output = null;
					if(params == null) {
						subjectAccountId = commanderAccountID;
						output = "You have recruited: ";
					} else {
						AccountResultSet rsSubject = d.getAccount(params[0]);
						if(!rsSubject.next()) {
							d.close(rsSubject);
							throw new AccountDoesNotExistException(params[0]);
						}
						subjectAccountId = rsSubject.getId();
						output = rsSubject.getName();
						output += " has recruited: ";
						d.close(rsSubject);
					}
					
					AccountResultSet rsRecruits = d.getAccountRecruits(subjectAccountId, GlobalSettings.recruitAccess);
					if(rsRecruits.next()) {
						do {
							output += rsRecruits.getName() + "(" + rsRecruits.getAccess() + ") ";
						} while(rsRecruits.next());
					} else {
						output += "no one";
					}
					d.close(rsRecruits);
					
					output = output.trim();
					source.sendChat(user, output, whisperBack);
				} catch(InvalidUseException e) {
					source.sendChat(user, "Use: %trigger%recruits [account]", whisperBack);
				}
				return;
			}});
		Profile.registerCommand("renameaccount", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					long commanderAccess, String commanderAccount, Long commanderAccountID, boolean superUser)
			throws Exception {
				if((params == null) || (params.length != 2)) {
					source.sendChat(user, "Use: %trigger%renameaccount <old account> <new account>", whisperBack);
					return;
				}
				
				AccountResultSet rsSubjectAccount = d.getAccount(params[0]);
				if((rsSubjectAccount == null) || !rsSubjectAccount.next())
					throw new AccountDoesNotExistException(params[0]);
				
				try {
					rsSubjectAccount.setName(params[1]);
					rsSubjectAccount.updateRow();
				} catch(SQLException e) {
					//TODO: Verify that the exception was actually caused by the UNIQUE restriction
					source.sendChat(user, "The account [" + params[1] + "] already exists!", whisperBack);
					return;
				}
				
				source.sendChat(user, "The account [" + params[0] + "] was successfully renamed to [" + params[1] + "]", whisperBack);
			}});
		Profile.registerCommand("say", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					long commanderAccess, String commanderAccount, Long commanderAccountID, boolean superUser)
			throws Exception {
				source.queueChatHelper(param, false);
			}});
		Profile.registerCommand("seen", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					long commanderAccess, String commanderAccount, Long commanderAccountID, boolean superUser)
			throws Exception {
				if((params == null) || (params.length != 1)) {
					source.sendChat(user, "Use: %trigger%seen <account>", whisperBack);
					return;
				}
				
				Timestamp mostRecent = null;
				String mostRecentAction = null;
				
				AccountResultSet rsSubjectAccount = d.getAccount(params[0]);
				if(!rsSubjectAccount.next()) {
					d.close(rsSubjectAccount);
					
					//They don't have an account by that name, check if it's a user
					BNetUser bnSubject = source.getBNetUser(params[0], user);
					BNLoginResultSet rsSubject = d.getUser(bnSubject);
					if(!rsSubject.next()) {
						d.close(rsSubject);
						source.sendChat(user, "I have never seen [" + bnSubject.getFullAccountName() + "]", whisperBack);
						return;
					}
					
					mostRecent = rsSubject.getLastSeen();
					mostRecentAction = rsSubject.getLastAction();
					params[0] = rsSubject.getLogin();
					d.close(rsSubject);
				} else {
					BNLoginResultSet rsSubjectUsers = d.getAccountUsers(rsSubjectAccount.getId());
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
					source.sendChat(user, "I have never seen [" + params[0] + "]", whisperBack);
					return;
				}
				
				String diff = TimeFormatter.formatTime(System.currentTimeMillis() - mostRecent.getTime());
				diff = "User [" + params[0] + "] was last seen " + diff + " ago";
				if(mostRecentAction != null)
					diff += " " + mostRecentAction;
				source.sendChat(user, diff, whisperBack);
			}});
		Profile.registerCommand("setaccount", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					long commanderAccess, String commanderAccount, Long commanderAccountID, boolean superUser)
			throws Exception {
				if((params == null) || (params.length < 1) || (params.length > 2)) {
					source.sendChat(user, "Use: %trigger%setaccount <user>[@<realm>] [<account>]", whisperBack);
					return;
				}

				BNetUser bnSubject = source.getBNetUser(params[0], user);
				BNLoginResultSet rsSubject = d.getUser(bnSubject);
				if(!rsSubject.next()) {
					d.close(rsSubject);
					source.sendChat(user, "I have never seen [" + bnSubject.getFullAccountName() + "] in the channel", whisperBack);
					return;
				}
				rsSubject.saveCursor();
				String subject = rsSubject.getLogin();
				
				Long newAccount = null;
				if(params.length == 2) {
					AccountResultSet rsSubjectAccount = d.getAccount(params[1]);
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
					AccountResultSet rsSubjectAccount = d.getAccount(newAccount);
					if(rsSubjectAccount.next())
						params[1] = rsSubjectAccount.getName();
					d.close(rsSubjectAccount);
				}
				
				bnSubject.resetPrettyName();
				source.sendChat(user, "User [" + subject + "] was added to account [" + params[1] + "] successfully.", whisperBack);
			}});
		Profile.registerCommand("setbirthday", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					long commanderAccess, String commanderAccount, Long commanderAccountID, boolean superUser)
			throws Exception {
				if(commanderAccountID == null) {
					source.sendChat(user, "You must have an account to use setbirthday.", whisperBack);
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
					
					AccountResultSet rsAccount = d.getAccount(commanderAccountID);
					if(!rsAccount.next())
						throw new SQLException();
					rsAccount.setBirthday(new java.sql.Date(bd.getTime()));
					rsAccount.updateRow();
					d.close(rsAccount);
					
					source.sendChat(user, "Your birthday has been set to [ " + new SimpleDateFormat("M/d/y").format(bd) + " ]", whisperBack);
				} catch(InvalidUseException e) {
					source.sendChat(user, "Use: %trigger%setbirthday <date:MM/DD/YY>", whisperBack);
				}
			}});
		Profile.registerCommand("setrank", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					long commanderAccess, String commanderAccount, Long commanderAccountID, boolean superUser)
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
					source.sendChat(user, "Use: %trigger%setrank <user> <rank(peon|grunt|shaman|1-3)>", whisperBack);
				}
			}});
		Profile.registerCommand("setrecruiter", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					long commanderAccess, String commanderAccount, Long commanderAccountID, boolean superUser)
			throws Exception {
				if((params == null) || (params.length != 2)) {
					source.sendChat(user, "Use: %trigger%setrecruiter <account> <account>", whisperBack);
					return;
				}
				
				AccountResultSet rsSubject = d.getAccount(params[0]);
				if(!rsSubject.next()) {
					d.close(rsSubject);
					throw new AccountDoesNotExistException(params[0]);
				}
				rsSubject.saveCursor();
				params[0] = rsSubject.getName();
				
				AccountResultSet rsTarget = d.getAccount(params[1]);
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
						source.sendChat(user, "Recursion detected: " + recursive, whisperBack);
						break;
					}

					if(cb != null) {
						d.close(rsTarget);
						rsTarget = d.getAccount(cb);
						if(!rsTarget.next())
							cb = null;
					}
					
					if(cb == null) {
						rsSubject.refreshCursor();
						rsSubject.setCreatedBy(targetID);
						rsSubject.updateRow();
						source.sendChat(user, "Successfully updated recruiter for [ " + params[0] + " ] to [ " + params[1] + " ]" , whisperBack);
						break;
					}
					
				} while(true);
				
				recursive = null;
				d.close(rsTarget);
				d.close(rsSubject);
			}});
		Profile.registerCommand("sweepban", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					long commanderAccess, String commanderAccount, Long commanderAccountID, boolean superUser)
			throws Exception {
				if((params == null) || (params.length < 1)) {
					source.sendChat(user, "Use: %trigger%sweepban <channel>", whisperBack);
					return;
				}
				sweepBanInProgress.put(source, true);
				sweepBannedUsers.put(source, 0);
				source.queueChatHelper("/who " + param, false);
			}});
		Profile.registerCommand("trigger", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					long commanderAccess, String commanderAccount, Long commanderAccountID, boolean superUser)
			throws Exception {
				char trigger = getTrigger(source);
				String output = "0000" + Integer.toString(trigger);
				output = output.substring(output.length() - 4);
				output = "Current trigger: " + trigger + " (alt+" + output + ")";
				source.sendChat(user, output, whisperBack);
			}});
		Profile.registerCommand("unban", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					long commanderAccess, String commanderAccount, Long commanderAccountID, boolean superUser)
			throws Exception {
				// TODO: Wildcard unbanning (requires keeping track of banned users)
				if((params == null) || (params.length != 1))
					source.sendChat(user, "Use: %trigger%unban <user>[@<realm>]", whisperBack);
				else
					source.queueChatHelper("/unban " + params[0], false);
			}});
		Profile.registerCommand("whoami", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					long commanderAccess, String commanderAccount, Long commanderAccountID, boolean superUser)
			throws Exception {
				source.parseCommand(user, "whois", user.getShortLogonName(), whisperBack);
			}});
		Profile.registerCommand("whois", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					long commanderAccess, String commanderAccount, Long commanderAccountID, boolean superUser)
			throws Exception {
				try {
					if((params == null) || (params.length != 1))
						throw new InvalidUseException();
					

					BNetUser bnSubject = null;
					AccountResultSet rsSubjectAccount = d.getAccount(params[0]);
					String result = null;
					if(rsSubjectAccount.next()) {
						result = rsSubjectAccount.getName();
					} else {
						bnSubject = source.getBNetUser(params[0], user);
						BNLoginResultSet rsSubject = d.getUser(bnSubject);
						
						if((rsSubject == null) || (!rsSubject.next())) {
							source.sendChat(user, "I have never seen [" + bnSubject.getFullLogonName() + "] in the channel", whisperBack);
							return;
						}
						
						bnSubject = source.getBNetUser(rsSubject.getLogin());
						d.close(rsSubject);
						d.close(rsSubjectAccount);
						rsSubjectAccount = d.getAccount(bnSubject);
						
						if((rsSubjectAccount == null) || (!rsSubjectAccount.next())) {
							source.sendChat(user, "User [" + params[0] + "] has no account", whisperBack);
							if(rsSubjectAccount != null)
								d.close(rsSubjectAccount);
							return;
						}
						
						result = bnSubject.toString();
					}

					long subjectAccountID = rsSubjectAccount.getId();
					long subjectAccess = rsSubjectAccount.getAccess();
					RankResultSet rsSubjectRank = d.getRank(subjectAccess);
					
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
					BNLoginResultSet rsSubject = d.getAccountUsers(subjectAccountID);
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
						AccountResultSet rsCreatorAccount = d.getAccount(cb);
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
					source.sendChat(user, result, whisperBack);
				} catch(InvalidUseException e) {
					source.sendChat(user, "Use: %trigger%whois <user>[@realm]", whisperBack);
				}
			}});
		/*Profile.registerCommand("", new CommandRunnable() {
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack,
					long commanderAccess, String commanderAccount, Long commanderAccountID, boolean superUser)
			throws Exception {
				// ...
			}});*/
	}
	
	public boolean parseCommand(Connection source, BNetUser user, String command, String param, boolean whisperBack) {
		try {
			Long commanderAccess = null;
			String commanderAccount = null;
			Long commanderAccountID = null;
	
			//Don't ask questions if they are a super-user
			boolean superUser = user.equals(source.getMyUser());
			try {
				AccountResultSet rsAccount = d.getAccount(user);
				if((rsAccount == null) || !rsAccount.next()) {
					if(rsAccount != null)
						d.close(rsAccount);
					if(superUser)
						throw new InvalidUseException();
					return false;
				}
					
				commanderAccess = rsAccount.getAccess();
				commanderAccount = rsAccount.getName();
				commanderAccountID = rsAccount.getId();
				d.close(rsAccount);
				if(commanderAccess <= 0)
					return false;
			} catch(InvalidUseException e) {
				d.close(d.getCreateUser(user));
			}
			

			// Closed-scope for rsCommand
			{
				CommandResultSet rsCommand = d.getCommand(command);
				if(!rsCommand.next()) {
					if(!whisperBack)
						source.recieveError("Command " + command + " not found in database");
					d.close(rsCommand);
					return false;
				}
				command = rsCommand.getName();
				if(!superUser) {
					long requiredAccess = rsCommand.getAccess();
					if(commanderAccess < requiredAccess) {
						source.recieveError("Insufficient access (" + commanderAccess + "/" + requiredAccess + ")");
						d.close(rsCommand);
						return false;
					}
				}
				d.close(rsCommand);
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
					lastCommandTime,
					commanderAccount,
					commanderAccountID,
					superUser);
		} catch(AccountDoesNotExistException e) {
			source.sendChat(user, "The account [" + e.getMessage() + "] does not exist!", whisperBack);
		} catch(InsufficientAccessException e) {
			source.sendChat(user, "You have insufficient access " + e.getMessage(), whisperBack);
		} catch(Exception e) {
			Out.exception(e);
			source.sendChat(user, e.getClass().getSimpleName() + ": " + e.getMessage(), whisperBack);
		}
		return true;
	}

	private static void createAccount(String accountName, Long commanderAccountID, BNLoginResultSet rsSubject)
	throws SQLException, AccountDoesNotExistException {
		AccountResultSet rsSubjectAccount = d.getAccount(accountName);
		if(rsSubjectAccount.next()) {
			d.close(rsSubjectAccount);
			d.close(rsSubject);
			throw new AccountDoesNotExistException("That account already exists!");
		}
		d.close(rsSubjectAccount);
		
		rsSubjectAccount = d.createAccount(accountName, 0, commanderAccountID);
		if(!rsSubjectAccount.next()) {
			d.close(rsSubjectAccount);
			d.close(rsSubject);
			throw new AccountDoesNotExistException("Failed to create account [" + accountName + "] for an unknown reason");
		}
		rsSubjectAccount.saveCursor();
		
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
				source.sendChat(user, "Use: %trigger%ban ( <user>[@<realm>] | pattern ) [reason]", whisperBack);
			else
				source.sendChat(user, "Use: %trigger%kick ( <user>[@<realm>] | pattern ) [reason]", whisperBack);
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
				source.sendChat(user, "That pattern did not match any users.", whisperBack);
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
				source.sendChat(user, "Skipped " + numSkipped + " users with operator status.", whisperBack);
		}
	}

	/**
	 * Get a displayable operating system version
	 */
	public static String osVersion() {
		Properties p = System.getProperties();
		String osName = p.getProperty("os.name");
		String osVersion = p.getProperty("os.version");
		
		if(osName.equals("Mac OS X")) {
			osName += " " + osVersion;
			if(osVersion.startsWith("10.0"))
				osName += " Cheetah";
			else if(osVersion.startsWith("10.1"))
				osName += " Puma";
			else if(osVersion.startsWith("10.2"))
				osName += " Jaguar";
			else if(osVersion.startsWith("10.3"))
				osName += " Panther";
			else if(osVersion.startsWith("10.4"))
				osName += " Tiger";
			else if(osVersion.startsWith("10.5"))
				osName += " Leopard";
		} else if(osName.startsWith("Windows ")) {
			osName += " " + p.getProperty("sun.os.patch.level");
		}
		osName += " (" + p.getProperty("os.arch") + ")";
		return osName;
	}
	
	public static String javaVersion() {
		return "Java " + System.getProperties().getProperty("java.version");
	}

	public void channelJoin(Connection source, BNetUser user) {
		if(!source.getConnectionSettings().enableGreetings)
			return;
		
		touchUser(source, user, "joining the channel");
		
		try {
			BNLoginResultSet rsUser = d.getUser(user);
			if(!rsUser.next()) {
				d.close(rsUser);
				return;
			}
			
			switch(user.getStatString().getProduct()) {
			case ProductIDs.PRODUCT_STAR: {
				Integer newWins = user.getStatString().getWins();
				if(newWins != null) {
					Long oldWins = rsUser.getWinsSTAR();
					if((oldWins == null) || (newWins > oldWins)) {
						rsUser.setWinsSTAR((long)newWins);
						rsUser.updateRow();
					}
				}
				break;
			}
			
			case ProductIDs.PRODUCT_SEXP: {
				Integer newWins = user.getStatString().getWins();
				if(newWins != null) {
					Long oldWins = rsUser.getWinsSEXP();
					if((oldWins == null) || (newWins > oldWins)) {
						rsUser.setWinsSEXP((long)newWins);
						rsUser.updateRow();
					}
				}
				break;
			}
			
			case ProductIDs.PRODUCT_W2BN: {
				Integer newWins = user.getStatString().getWins();
				if(newWins != null) {
					Long oldWins = rsUser.getWinsW2BN();
					if((oldWins == null) || (newWins > oldWins)) {
						rsUser.setWinsW2BN((long)newWins);
						rsUser.updateRow();
					}
				}
				break;
			}
				
			case ProductIDs.PRODUCT_D2DV:
			case ProductIDs.PRODUCT_D2XP: {
				Integer newLevel = user.getStatString().getCharLevel();
				if(newLevel != null) {
					Long oldLevel = rsUser.getLevelD2();
					if((oldLevel == null) || (newLevel > oldLevel)) {
						rsUser.setLevelD2((long)newLevel);
						rsUser.updateRow();
					}
				}
				break;
			}

			case ProductIDs.PRODUCT_WAR3:
			case ProductIDs.PRODUCT_W3XP: {
				Integer newLevel = user.getStatString().getLevel();
				if(newLevel != null) {
					Long oldLevel = rsUser.getLevelW3();
					if((oldLevel == null) || (newLevel > oldLevel)) {
						rsUser.setLevelW3((long)newLevel);
						rsUser.updateRow();
					}
				}
				break;
			}
			}
			d.close(rsUser);
			
			AccountResultSet rsAccount = d.getAccount(user);
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
					source.queueChatHelper("Happy birthday, " + user.getShortPrettyName() + "! Today, you are " + age + " years old!", false);
				}
			}

			//check for autopromotions
			long rank = rsAccount.getAccess();
			long id = rsAccount.getId();
			RankResultSet rsRank = d.getRank(rank);
			if(rsRank.next()) {
				String greeting = rsRank.getGreeting();
				if(greeting != null) {
					greeting = String.format(greeting, user.getShortPrettyName(), user.getPing(), user.getFullAccountName());
					source.queueChatHelper(greeting, false);
				}

				//Autopromotions:
				Long apDays = rsRank.getApDays();
				Timestamp ts = rsAccount.getLastRankChange();
				//Check that they meet the days requirement
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
					long wins[] = d.getAccountWinsLevels(id, GlobalSettings.recruitTagPrefix, GlobalSettings.recruitTagSuffix);
					
					boolean condition = false;
					condition |= ((apWins > 0) && (wins[0] >= apWins));
					condition |= ((apD2Level > 0) && (wins[1] >= apD2Level));
					condition |= ((apW3Level > 0) && (wins[2] >= apW3Level));
					condition |= ((apWins == 0) && (apD2Level == 0) && (apW3Level == 0));
					
					if(condition) {
						// Check RS
						long rs = d.getAccountRecruitScore(id, GlobalSettings.recruitAccess);
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
							source.sendChat(user, "You need " + Long.toString(apRS - rs) + " more recruitment points to recieve a promotion!", false);
						}
					} else {
						String msg = "You need ";
						switch(user.getStatString().getProduct()) {
						case ProductIDs.PRODUCT_STAR:
						case ProductIDs.PRODUCT_SEXP:
						case ProductIDs.PRODUCT_W2BN:
							msg += Long.toString(apWins - wins[0]) + " more win";
							if(apWins - wins[0] > 1)
								msg += "s";
							break;
						case ProductIDs.PRODUCT_D2DV:
						case ProductIDs.PRODUCT_D2XP:
							msg += "to reach Diablo 2 level " + apD2Level;
							break;
						case ProductIDs.PRODUCT_WAR3:
						case ProductIDs.PRODUCT_W3XP:
							msg += "to reach Warcraft 3 level " + apW3Level;
							break;
						default:
							break apBlock;
						}
						msg += " to recieve a promotion!";
						source.sendChat(user, msg, false);
					}
				}
			}
			d.close(rsRank);
			d.close(rsAccount);

			//Mail
			long umc = d.getUnreadMailCount(id);
			if(umc > 0)
				source.sendChat(user, "You have " + umc + " unread messages; type [ %trigger%mail read ] to retrieve them", false);
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
			parseCommand(source, user, "trigger", null, GlobalSettings.whisperBack);
		else
			if(text.charAt(0) == getTrigger(source)) {
				String[] command = text.substring(1).split(" ", 2);
				String params = null;
				if(command.length > 1)
					params = command[1];
			
				parseCommand(source, user, command[0], params, GlobalSettings.whisperBack);
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
				source.sendChat(lastCommandUser, text, false);
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
		
		int i = text.indexOf(' ');
		if(i == -1) {
			parseCommand(source, user, text, null, true);
		} else {
			String command = text.substring(0, i);
			String paramString = text.substring(i + 1);
			
			parseCommand(source, user, command, paramString, true);
		}
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
