package bnubot.bot;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

import bnubot.Version;
import bnubot.bot.database.*;
import bnubot.core.*;
import bnubot.core.clan.ClanMember;
import bnubot.core.friend.FriendEntry;
import bnubot.util.TimeFormatter;

public class CommandEventHandler implements EventHandler {
	Connection c = null;
	Database d = null;
	Boolean sweepBanInProgress = false;
	int sweepBannedUsers;
	
	long	lastCommandTime = 0;
	BNetUser lastCommandUser = null;

	private class InvalidUseException extends Exception {
		private static final long serialVersionUID = 3993849990858233332L;
	}
	private class InsufficientAccessException extends Exception {
		private static final long serialVersionUID = -1954683087381833989L;
		public InsufficientAccessException(String string) {
			super(string);
		}
	}
	
	public CommandEventHandler(Database d) {
		this.d = d;
	}
	
	public void initialize(Connection c) {
		this.c = c;
	}
	
	public void touchUser(BNetUser user, String action) {
		try {
			ResultSet rsUser = d.getCreateUser(user);
			if(rsUser.next()) {
				rsUser.updateTimestamp("lastSeen", new Timestamp(new Date().getTime()));
				rsUser.updateString("lastAction", action);
				rsUser.updateRow();
			}
		} catch(SQLException e) {
			c.recieveError(e.getClass().getName() + ": " + e.getMessage());
		}
	}
	
	public void parseCommand(BNetUser user, String command, String param) {
		try {
			String[] params = null;
			if(param != null)
				params = param.split(" ");
			
			Long commanderAccess = null; 
			String commanderAccount = null;
			Long commanderAccountID = null; 
	
			//Don't ask questions if they are a super-user
			boolean superUser = user.equals(c.getMyUser());
			try {
				ResultSet rsAccount = d.getAccount(user);
				if((rsAccount == null) || !rsAccount.next())
					if(superUser)
						throw new Exception();
					else
						return;
					
				commanderAccess = rsAccount.getLong("access");
				commanderAccount = rsAccount.getString("name");
				commanderAccountID = rsAccount.getLong("id");
				if(commanderAccess <= 0)
					return;
			} catch(Exception e) {
				d.getCreateUser(user);
			}
			

			
			ResultSet rsCommand = d.getCommand(command);
			if(!rsCommand.next()) {
				c.recieveError("Command not found in database");
				return;
			}
			command = rsCommand.getString("name");
			if(!superUser) {
				long requiredAccess = rsCommand.getLong("access");
				if(commanderAccess < requiredAccess) {
					c.recieveError("Insufficient access");
					return;
				}
			}
			
			lastCommandUser = user;
			lastCommandTime = new Date().getTime();
		
			COMMAND: switch(command.charAt(0)) {
			case 'a':
				if(command.equals("add")) {
					try {
						if(params.length != 2)
							throw new InvalidUseException();
						
						ResultSet rsSubjectAccount = d.getAccount(params[0]);
						if(!rsSubjectAccount.next()) {
							c.sendChat(user, "That user does not have an account. See %trigger%createaccount and %trigger%setaccount.");
						}

						long targetAccess = Long.parseLong(params[1]);
						String subjectAccount = rsSubjectAccount.getString("name");
						
						if(!superUser) {
							if(subjectAccount.equals(commanderAccount))
								throw new InsufficientAccessException("to modify your self");
							if(targetAccess >= commanderAccess)
								throw new InsufficientAccessException("to add users beyond " + (commanderAccess - 1));
						}
						
						rsSubjectAccount.updateLong("access", targetAccess);
						rsSubjectAccount.updateRow();
						c.sendChat(user, "Added user [" + subjectAccount + "] successfully with access " + targetAccess);
					} catch(InvalidUseException e) {
						c.sendChat(user, "Use: %trigger%add <account> <access>");
						break;
					}
					break;
				}
				break;
			case 'b':
				if(command.equals("ban")) {
					if(param.length() == 0) {
						c.sendChat(user, "Use: %trigger%ban <user>[@<realm>] [reason]");
						break;
					}
					c.sendChat("/ban " + param);
					break;
				}
				break;
			case 'c':
				if(command.equals("createaccount")) {
					if(params.length != 1) {
						c.sendChat(user, "Use: %trigger%createaccount <account>");
						break;
					}
					
					ResultSet rsAccount = d.getAccount(params[0]);
					if(rsAccount.next()) {
						c.sendChat(user, "The account [" + params[0] + "] already exists");
						break;
					}
					
					rsAccount = d.createAccount(params[0], 0L);
					if(!rsAccount.next()) {
						c.sendChat(user, "Failed to create account [" + params[0] + "] for an unknown reason");
						break;
					}
					
					c.sendChat(user, "The account [" + params[0] + "] has been created");
					break;
				}
				break;
			case 'd':
				if(command.equals("disconnect")) {
					c.setConnected(false);
					break;
				}
				break;
			case 'i':
				if(command.equals("info")) {
					Properties p = System.getProperties();
					c.sendChat(user, "BNU-Bot " + Version.version() + " running on " + p.getProperty("os.name") + " (" + p.getProperty("os.arch") + ")");
					break;
				}
				break;
			case 'k':
				if(command.equals("kick")) {
					if(params.length != 1) {
						c.sendChat(user, "Use: %trigger%kick <user>[@<realm>]");
						break;
					}
					c.sendChat("/kick " + params[0]);
					break;
				}
				break;
			case 'm':
				if(command.equals("mail")) {
					if(commanderAccountID == null) {
						c.sendChat(user, "You must have an account to use mail.");
						break;
					}
					
					try {
						if((params == null) || (params.length < 1))
							throw new InvalidUseException();
						if(params[0].equals("send")) {
							//send <account> <message>
							params = param.split(" ", 3);
							if(params.length < 3)
								throw new InvalidUseException();
							
							ResultSet rsTargetAccount = d.getAccount(params[1]);
							if(!rsTargetAccount.next()) {
								c.sendChat(user, "The account [" + params[1] + "] does not exist");
								break;
							}
							params[1] = rsTargetAccount.getString("name");
							Long targetAccountID = rsTargetAccount.getLong("id");
							
							d.sendMail(commanderAccountID, targetAccountID, params[2]);
							c.sendChat(user, "Mail queued for delivery to " +  params[1]);
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
									boolean read = rsMail.getBoolean("read");
									if(read)
										continue;

									String message = "";
									if(true)
										message = "#" + rsMail.getRow() + " of ?: ";
									
									message += "From ";
									message += rsMail.getString("name");
									message += " [";
									message += rsMail.getString("sent");
									message += "]: ";
									message += rsMail.getString("message");
									
									d.setMailRead(rsMail.getLong("id"));
									rsMail.close();
									
									c.sendChat(user, message);
									break COMMAND;
								}
								
								String message = "You have no unread mail!";
								long mailCount = d.getMailCount(commanderAccountID);
								if(mailCount > 0)
									message += " To read your " + mailCount + " messages, type [ %trigger%mail read <number> ]";
								c.sendChat(user, message);
							} else {
								long mailNumber = 0;
								while(rsMail.next()) {
									mailNumber++;
									if(mailNumber != id)
										continue;

									String message = "";
									if(true)
										message = "#" + rsMail.getRow() + " of ?: ";
									
									message += "From ";
									message += rsMail.getString("name");
									message += " [";
									message += rsMail.getString("sent");
									message += "]: ";
									message += rsMail.getString("message");
									
									d.setMailRead(rsMail.getLong("id"));
									rsMail.close();
									
									c.sendChat(user, message);
									break COMMAND;
								}
								
								c.sendChat(user, "You only have " + mailNumber + " messages!");
							}
							rsMail.close();
							break;
						} else if(params[0].equals("empty")
								||params[0].equals("delete")
								||params[0].equals("clear")) {
							//empty
							if(params.length != 1)
								throw new InvalidUseException();
							
							if(d.unreadMail(commanderAccountID)) {
								c.sendChat(user, "You have unread mail!");
								break;
							}
							
							d.clearMail(commanderAccountID);
							c.sendChat(user, "Mailbox cleaned!");
						}
					} catch(InvalidUseException e) {
						c.sendChat(user, "Use: %trigger%mail (read [number] | empty | send <account> <message>)");
					}
					break;
				}
				break;
			case 'q':
				if(command.equals("quit")) {
					System.exit(0);
				}
				break;
			case 'r':
				if(command.equals("reconnect")) {
					c.reconnect();
					break;
				}
				break;
			case 's':
				if(command.equals("say")) {
					c.sendChat(param);
					break;
				}
				if(command.equals("seen")) {
					if(params.length != 1) {
						c.sendChat(user, "Use: %trigger%seen <account>");
					}
					
					Timestamp mostRecent = null;
					
					ResultSet rsSubjectUsers = d.getAccountUsers(params[0]);
					if((rsSubjectUsers == null) | (!rsSubjectUsers.next())) {
						//They don't have an account by that name, check if it's a user
						BNetUser bnSubject = BNetUser.getBNetUser(params[0], user);
						ResultSet rsSubject = d.getUser(bnSubject);
						if(!rsSubject.next()) {
							c.sendChat(user, "I have never seen [" + bnSubject.getFullAccountName() + "]");
							break;
						} else {
							mostRecent = rsSubject.getTimestamp("lastSeen");
						}
					} else {
						//Check the user's accounts						
						do {
							Timestamp nt = rsSubjectUsers.getTimestamp("lastSeen");
							if(mostRecent == null)
								mostRecent = nt;
							else {
								if((nt != null) && (nt.compareTo(mostRecent) > 0))
									mostRecent = nt;
							}
						} while(rsSubjectUsers.next());
					}
					
					if(mostRecent == null) {
						c.sendChat(user, "I have never seen [" + params[0] + "]");
						break;
					}
					
					String diff = TimeFormatter.formatTime(new Date().getTime() - mostRecent.getTime());
					c.sendChat(user, "User [" + params[0] + "] was last seen " + diff + " ago");
					break;
				}
				if(command.equals("setaccount")) {
					if((params.length < 1) || (params.length > 2)) {
						c.sendChat(user, "Use: %trigger%setaccount <user>[@<realm>] [<account>]");
						break;
					}

					BNetUser bnSubject = BNetUser.getBNetUser(params[0], user.getFullAccountName());
					ResultSet rsSubject = d.getUser(bnSubject);
					if(!rsSubject.next()) {
						c.sendChat(user, "The user [" + bnSubject.getFullAccountName() + "] does not exist");
						break;
					}
					String subject = rsSubject.getString("login");
					
					String newAccount = null;
					if(params.length == 2) {
						ResultSet rsSubjectAccount = d.getAccount(params[1]);
						if(!rsSubjectAccount.next()) {
							c.sendChat(user, "The account [" + params[1] + "] does not exist");
							break;
						}
						newAccount = rsSubjectAccount.getString("name");
					}
					
					rsSubject.updateString("account", newAccount);
					rsSubject.updateRow();
					bnSubject.resetPrettyName();
					c.sendChat(user, "User [" + subject + "] was added to account [" + newAccount + "] successfully.");
					break;
				}
				if(command.equals("setrank")) {
					int newRank;
					try {
						if(params.length != 2)
							throw new InvalidUseException();
						newRank = Integer.valueOf(params[1]);
						if((newRank < 1) || (newRank > 3))
							throw new InvalidUseException();
					} catch(InvalidUseException e) {
						c.sendChat(user, "Use: %trigger%setrank <user> <rank:1-3>");
						break;
					}
					
					// TODO: validate that params[0] is in the clan
					c.sendClanRankChange(params[0], newRank);
					c.sendChat(user, "Success");
					break;
				}
				if(command.equals("sweepban")) {
					if(params.length < 1) {
						c.sendChat(user, "Use: %trigger%sweepban <channel>");
						break;
					}
					sweepBanInProgress = true;
					sweepBannedUsers = 0;
					c.sendChat("/who " + param);
					break;
				}
				break;
			case 't':
				if(command.equals("trigger")) {
					char trigger = c.getConnectionSettings().trigger.charAt(0);
					String output = "0000" + Integer.toString(trigger);
					output = output.substring(output.length() - 4);
					output = "Current trigger: " + trigger + " (alt+" + output + ")";
					c.sendChat(user, output);
					break;
				}
				break;
			case 'u':
				if(command.equals("unban")) {
					if(params.length != 1) {
						c.sendChat(user, "Use: %trigger%unban <user>[@<realm>]");
						break;
					}
					c.sendChat("/unban " + params[0]);
					break;
				}
				break;
			case 'w':
				if(command.equals("whoami")) {
					parseCommand(user, "whois", user.getShortLogonName());
					break;
				}
				if(command.equals("whois")) {
					try {
						if(params.length != 1)
							throw new InvalidUseException();
						
						BNetUser bnSubject = BNetUser.getBNetUser(params[0], user);
						ResultSet rsSubject = d.getUser(bnSubject);
						ResultSet rsSubjectAccount = null;
						if(!rsSubject.next()) {
							rsSubject.close();
							rsSubject = null;
							
							rsSubjectAccount = d.getAccount(params[0]);
							
							if(!rsSubjectAccount.next()) {
								c.sendChat(user, "User [" + params[0] + "] not found in database");
								break;
							}
						} else {
							BNetUser subject = BNetUser.getBNetUser(rsSubject.getString("login"));
							rsSubjectAccount = d.getAccount(subject);
							
							if((rsSubjectAccount == null) || (!rsSubjectAccount.next())) {
								c.sendChat(user, "User [" + params[0] + "] has no account");
								break;
							}
						}
						
						String result = bnSubject.toString();

						String subjectAccount = rsSubjectAccount.getString("name");
						long subjectAccess = rsSubjectAccount.getLong("access");
						ResultSet rsSubjectRank = d.getRank(subjectAccess);
						if(rsSubjectRank.next()) {
							result += " " + rsSubjectRank.getString("verbstr");
							result += " (" + subjectAccess + ")";
						} else {
							result += " has access " + subjectAccess;
						}
						
						// Append aliases
						ArrayList<String> aliases = new ArrayList<String>();
						Timestamp lastSeen = null;
						rsSubject = d.getAccountUsers(subjectAccount);
						while(rsSubject.next()) {
							if(lastSeen == null)
								lastSeen = rsSubject.getTimestamp("lastSeen");
							else {
								Timestamp nt = rsSubject.getTimestamp("lastSeen");
								if((nt != null) && (nt.compareTo(lastSeen) > 0))
									lastSeen = nt;
							}
							aliases.add(rsSubject.getString("login"));
						}

						if(lastSeen != null) {
							result += " who was last seen [ ";
							result += TimeFormatter.formatTime(new Date().getTime() - lastSeen.getTime());
							result += " ] ago";
						}
						
						boolean andHasAliases = false;
						for(int i = 0; i < aliases.size(); i++) {
							String l = aliases.get(i);
							
							if(bnSubject.equals(l))
								continue;
							
							if(!andHasAliases) {
								andHasAliases = true;
								result += " and has aliases ";
							} else {
								result += ", ";
							}
							
							result += l;
						}
						
						c.sendChat(user, result);
					} catch(InvalidUseException e) {
						c.sendChat(user, "Use: %trigger%whois <user>[@realm]");
						break;
					}
					break;
				}
				break;
			}
		
		} catch(InsufficientAccessException e) {
			c.sendChat(user, "You have insufficient access " + e.getMessage());
		} catch(Exception e) {
			e.printStackTrace();
			c.sendChat(user, e.getClass().getName() + ": " + e.getMessage());
		}
	}

	public void channelJoin(BNetUser user, int flags, int ping, StatString statstr) {
		touchUser(user, "Joining channel");
		
		try {
			ResultSet rsAccount = d.getAccount(user);
			if((rsAccount != null) && rsAccount.next()) {
				long rank = rsAccount.getLong("access");
				ResultSet rsRank = d.getRank(rank);
				if(rsRank.next()) {
					String greeting = rsRank.getString("greeting");
					if(greeting != null) {
						greeting = String.format(greeting, user.toString(), ping);
						c.sendChat(greeting);
					}
				}
				rsRank.close();
			}
			if(rsAccount != null)
				rsAccount.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void channelLeave(BNetUser user, int flags, int ping, StatString statstr) {
		touchUser(user, "Leaving channel");
	}
	
	public void channelUser(BNetUser user, int flags, int ping, StatString statstr) {
		touchUser(user, "In channel");
	}
	
	public void joinedChannel(String channel) {}

	public void recieveChat(BNetUser user, int flags, int ping, String text) {
		if(text == null)
			return;
		if(text.length() == 0)
			return;
		
		touchUser(user, "In channel");
		
		char trigger = c.getConnectionSettings().trigger.charAt(0);
		
		if(text.equals("?trigger"))
			parseCommand(user, "trigger", null); //c.sendChat(user, "The bot's trigger is: " + trigger);
		else
			if(text.charAt(0) == trigger) {
				String[] command = text.substring(1).split(" ", 2);
				String params = null;
				if(command.length > 1)
					params = command[1];
			
				parseCommand(user, command[0], params);
			}
	}

	public void recieveEmote(BNetUser user, int flags, int ping, String text) {}
	
	private boolean enableSendInfoErrorBack = false;
	private String lastInfo = null;
	private void recieveInfoError(String text) {
		if(!enableSendInfoErrorBack)
			return;
		
		long timeElapsed = new Date().getTime() - lastCommandTime;
		// 200ms
		if(timeElapsed < 200) {
			if(!text.equals(lastInfo)) {
				lastInfo = text;
				c.sendChat(lastCommandUser, text);
			}
		}
	}
	
	public void recieveError(String text) {
		recieveInfoError(text);
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
	
	public void recieveInfo(String text) {
		if(sweepBanInProgress) {
			boolean turnItOff = true;
			
			if(text.length() > 17) {
				if(text.substring(0, 17).equals("Users in channel ")) {
					if(sweepBannedUsers == 0) {
						turnItOff = false;
						c.sendChat("Sweepbanning channel " + text.substring(17, text.length() - 1));
					}
				}
			}
			
			String users[] = text.split(", ");
			if(users.length == 2) {
				if(users[0].indexOf(' ') == -1) {
					if(users[1].indexOf(' ') == -1) {
						c.sendChat("/ban " + removeOpUserBrackets(users[0]));
						c.sendChat("/ban " + removeOpUserBrackets(users[1]));
						sweepBannedUsers += 2;
						turnItOff = false;
					}
				}
			} else {
				if(text.indexOf(' ') == -1) {
					c.sendChat("/ban " + removeOpUserBrackets(text));
					sweepBannedUsers++;
					turnItOff = true;
				}
			}
			
			if(turnItOff)
				sweepBanInProgress = false;
		}
		
		if(sweepBanInProgress)
			return;
		
		recieveInfoError(text);
	}

	public void bnetConnected() {}
	public void bnetDisconnected() {}
	public void titleChanged() {}

	public void whisperRecieved(BNetUser user, int flags, int ping, String text) {
		if(text == null)
			return;
		if(text.length() == 0)
			return;
		
		char trigger = c.getConnectionSettings().trigger.charAt(0);
		if(text.charAt(0) == trigger)
			text = text.substring(1);
		
		int i = text.indexOf(' ');
		if(i == -1) {
			parseCommand(user, text, null);
		} else {
			String command = text.substring(0, i);
			String paramString = text.substring(i + 1);
			
			parseCommand(user, command, paramString);
		}
	}

	public void whisperSent(BNetUser user, int flags, int ping, String text) {}

	public void friendsList(FriendEntry[] entries) {}
	public void friendsUpdate(byte entry, byte location, byte status, int product, String locationName) {}
	public void clanMemberRankChange(byte oldRank, byte newRank, String user) {}
	public void clanMemberList(ClanMember[] members) {}
	public void clanMOTD(Object cookie, String text) {}
	public void queryRealms2(String[] realms) {}
	public void logonRealmEx(int[] MCPChunk1, int ip, int port, int[] MCPChunk2, String uniqueName) {}
}
