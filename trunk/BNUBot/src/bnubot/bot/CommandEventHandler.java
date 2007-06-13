package bnubot.bot;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.*;

import javax.swing.text.DateFormatter;

import bnubot.bot.database.*;
import bnubot.core.*;
import bnubot.core.clan.ClanMember;
import bnubot.core.friend.FriendEntry;

public class CommandEventHandler implements EventHandler {
	Connection c = null;
	Database d = null;
	Boolean sweepBanInProgress = false;
	int sweepBannedUsers;
	
	long	lastCommandTime = 0;
	BNetUser lastCommandUser = null;

	private class InvalidUsageException extends Exception {
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
	
	@SuppressWarnings("deprecation")
	private void parseCommand(BNetUser user, String command, String param) {
		try {
			String[] params = param.split(" ");
			command = command.toLowerCase();
			
			Long commanderAccess = null; 
			String commanderAccount = null;
	
			//Don't ask questions if they are a super-user
			boolean superUser = user.equals(c.getMyUser());
			if(!superUser) {
				ResultSet rsAccount = d.getAccount(user);
				if(rsAccount == null) return;
				if(!rsAccount.next()) return;
				
				commanderAccess = rsAccount.getLong("access");
				commanderAccount = rsAccount.getString("name");
				if(commanderAccess <= 0)
					return;
			} else {
				d.getCreateUser(user);
			}
			
			lastCommandUser = user;
			lastCommandTime = new Date().getTime();
		
			switch(command.charAt(0)) {
			case 'a':
				if(command.equals("add")) {
					try {
						if(params.length != 2)
							throw new InvalidUsageException();
						
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
						
						d.setAccountAccess(subjectAccount, targetAccess);
						c.sendChat(user, "Added user [" + subjectAccount + "] sucessfully with access " + targetAccess);
					} catch(InvalidUsageException e) {
						c.sendChat(user, "Usage: %trigger%add <account> <access>");
						break;
					}
					break;
				}
				break;
			case 'b':
				if(command.equals("ban")) {
					if(param.length() == 0) {
						c.sendChat(user, "Usage: %trigger%ban <user>[@<realm>] [reason]");
						break;
					}
					c.sendChat("/ban " + param);
					break;
				}
				break;
			case 'c':
				if(command.equals("createaccount")) {
					if(params.length != 1) {
						c.sendChat(user, "Usage: %trigger%createaccount <account>");
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
					c.sendChat(user, "BNU-Bot v2.0 beta running on " + p.getProperty("os.name") + " (" + p.getProperty("os.arch") + ")");
					break;
				}
				break;
			case 'k':
				if(command.equals("kick")) {
					if(params.length != 1) {
						c.sendChat(user, "Usage: %trigger%kick <user>[@<realm>]");
						break;
					}
					c.sendChat("/kick " + params[0]);
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
						c.sendChat(user, "Usage: %trigger%seen <account>");
					}
					
					Timestamp mostRecent = null;
					
					ResultSet rsSubjectUsers = d.getAccountUsers(params[0]);
					if((rsSubjectUsers == null) | (!rsSubjectUsers.next())) {
						//They don't have an account by that name, check if it's a user
						BNetUser bnuser = new BNetUser(params[0], user);
						ResultSet rsSubject = d.getUser(bnuser);
						if((rsSubject == null) | (!rsSubject.next())) {
							c.sendChat(user, "I have never seen [" + params[0] + "]");
							break;
						} else {
							mostRecent = Timestamp.valueOf(rsSubject.getString("lastSeen"));
						}
					} else {
						//Check the user's accounts						
						do {
							Timestamp nt = Timestamp.valueOf(rsSubjectUsers.getString("lastSeen"));
							if(mostRecent == null)
								mostRecent = nt;
							else {
								if(nt.compareTo(mostRecent) > 0)
									mostRecent = nt;
							}
						} while(rsSubjectUsers.next());
					}
					
					if(mostRecent == null) {
						c.sendChat(user, "I have never seen [" + params[0] + "]");
						break;
					}
					
					c.sendChat(user, "User [" + params[0] + "] was last seen " + mostRecent.toString());
					break;
				}
				if(command.equals("setaccount")) {
					if(params.length != 2) {
						c.sendChat(user, "Usage: %trigger%setaccount <user>[@<realm>] <account>");
						break;
					}

					BNetUser subject = new BNetUser(params[0], user.getFullAccountName());
					ResultSet rsSubject = d.getUser(subject);
					if(!rsSubject.next()) {
						c.sendChat(user, "The user [" + subject.getFullAccountName() + "] does not exist");
						break;
					}
					
					ResultSet rsSubjectAccount = d.getAccount(params[1]);
					if(!rsSubjectAccount.next()) {
						c.sendChat(user, "The account [" + params[1] + "] does not exist");
						break;
					}
					
					d.setUserAccount(subject, params[1]);
					c.sendChat(user, "The account [" + params[1] + "] has been assigned to user [" + subject.getFullAccountName() + "]");
					break;
				}
				if(command.equals("setrank")) {
					int newRank;
					try {
						if(params.length != 2)
							throw new InvalidUsageException();
						newRank = Integer.valueOf(params[1]);
						if((newRank < 1) || (newRank > 3))
							throw new InvalidUsageException();
					} catch(InvalidUsageException e) {
						c.sendChat(user, "Usage: %trigger%setrank <user> <rank:1-3>");
						break;
					}
					
					// TODO: validate that params[0] is in the clan
					c.sendClanRankChange(params[0], newRank);
					c.sendChat(user, "Success");
					break;
				}
				if(command.equals("sweepban")) {
					if(params.length < 1) {
						c.sendChat(user, "Usage: %trigger%sweepban <channel>");
						break;
					}
					sweepBanInProgress = true;
					sweepBannedUsers = 0;
					c.sendChat("/who " + param);
					break;
				}
				break;
			case 't':
				if(command.equals("test")) {
					BNetUser to = new BNetUser("bnu-master", "Azeroth");
					
					String longText = "";
					for(int i = 0; i < 300; i++) {
						String line = "-=-=-=-=-" + Integer.toString(i*10);
						line = line.substring(line.length() - 10);
						if(line.length() != 10)
							throw new Exception("oops");
						longText += line;
					}
					
					c.sendChat(to, longText);
					break;
				}
				break;
			case 'u':
				if(command.equals("unban")) {
					if(params.length != 1) {
						c.sendChat(user, "Usage: %trigger%unban <user>[@<realm>]");
						break;
					}
					c.sendChat("/unban " + params[0]);
					break;
				}
				break;
			case 'w':
				if(command.equals("whoami")) {
					parseCommand(user, "whois", user.toString());
					break;
				}
				if(command.equals("whois")) {
					try {
						if(params.length != 1)
							throw new InvalidUsageException();
						
						ResultSet rsSubject = d.getUser(new BNetUser(params[0], user));
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
							BNetUser subject = new BNetUser(rsSubject.getString("login"));
							rsSubjectAccount = d.getAccount(subject);
							
							if((rsSubjectAccount == null) || (!rsSubjectAccount.next())) {
								c.sendChat(user, "User [" + params[0] + "] has no account");
								break;
							}
						}
						
						
						String account = rsSubjectAccount.getString("name");
						Long access =  rsSubjectAccount.getLong("access");
						
						String result = "User [" + account + "] has access " + access;
						
						// Append aliases
						rsSubject = d.getAccountUsers(account);
						if(rsSubject.next()) {
							result += " and aliases ";
							result += rsSubject.getString("login");
						}
						while(rsSubject.next()) {
							result += ", " + rsSubject.getString("login");
						}
						
						c.sendChat(user, result);
					} catch(InvalidUsageException e) {
						c.sendChat(user, "Usage: %trigger%whois <user>[@realm]");
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
		try {
			d.getCreateUser(user);
		} catch(SQLException e) {
			//TODO: Handle this error
			recieveError(e.getClass().getName() + ": " + e.getMessage());
		}
	}
	
	public void channelLeave(BNetUser user, int flags, int ping, StatString statstr) {
		try {
			d.getCreateUser(user);
		} catch(SQLException e) {
			//TODO: Handle this error
			recieveError(e.getClass().getName() + ": " + e.getMessage());
		}
	}
	
	public void channelUser(BNetUser user, int flags, int ping, StatString statstr) {
		try {
			ResultSet rsUser = d.getCreateUser(user);
			//TODO: set last seen
			/*Session s = d.openSession();
			u.setLastSeen(new Date().toString());
			s.save(u);
			s.close();*/
		} catch(SQLException e) {
			//TODO: Handle this error
			recieveError(e.getClass().getName() + ": " + e.getMessage());
		}
	}
	
	public void joinedChannel(String channel) {}

	public void recieveChat(BNetUser user, int flags, int ping, String text) {
		if(text == null)
			return;
		if(text.length() == 0)
			return;
		
		char trigger = c.getConnectionSettings().trigger.charAt(0);
		
		if(text.equals("?trigger"))
			c.sendChat(user, "The bot's trigger is: " + trigger);
		else
			if(text.charAt(0) == trigger) {
				String[] command = text.substring(1).split(" ");
				String paramString = text.substring(text.indexOf(' ') + 1);
			
				parseCommand(user, command[0], paramString);
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
		
		String[] command = text.substring(1).split(" ");
		String paramString = text.substring(text.indexOf(' ') + 1);
		
		parseCommand(user, command[0], paramString);
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
