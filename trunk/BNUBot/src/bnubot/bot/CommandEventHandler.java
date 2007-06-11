package bnubot.bot;

import java.util.*;

import javax.resource.spi.IllegalStateException;

import org.hibernate.Session;
import org.hibernate.Transaction;

import bnubot.bot.database.*;
import bnubot.bot.database.pojo.*;
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
	
			User userUser = d.getCreateUser(user);
			Account userAccount = userUser.getAccount();
			
			boolean superUser = false;
			if(c.getMyUser().equals(user) == false) {
				if(userAccount == null)
					return;
				if(userAccount.getAccess() <= 0)
					return;
			} else {
				superUser = true;
				if(userAccount == null) {
					c.recieveError("");
				}
			}
			
			lastCommandUser = user;
			lastCommandTime = new Date().getTime();
		
			switch(command.charAt(0)) {
			case 'a':
				if(command.equals("add")) {
					try {
						if(params.length != 2)
							throw new InvalidUsageException();
						
						Account subjectAccount = d.getAccount(params[0]);
						if(subjectAccount == null) {
							BNetUser target = new BNetUser(params[0], user.getFullAccountName());
							User u = d.getUser(target);
							if(u != null)
								subjectAccount = u.getAccount();
						}
						
						if(subjectAccount == null) {
							c.sendChat(user, "That user does not have an account. See %trigger%createaccount.");
							break;
						}
						
						long access = Long.valueOf(params[1]);
						
						if(!superUser) {
							if(access >= userAccount.getAccess())
								throw new InsufficientAccessException("to add users beyond " + (userAccount.getAccess() - 1));
							if(subjectAccount.equals(userAccount))
								throw new InsufficientAccessException("to modify your self");
						}
						
						Session s = d.openSession();
						Transaction tx = s.beginTransaction();
						subjectAccount.setAccess(access);
						s.saveOrUpdate(subjectAccount);
						tx.commit();
						s.close();
						
						c.sendChat(user, "Added user [" + subjectAccount.getName() + "] sucessfully with access " + access);
					} catch(InvalidUsageException e) {
						c.sendChat(user, "Usage: %trigger%add <user>[@<realm>] <access>");
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
					
					Account subjectAccount = d.getAccount(params[0]);
					if(subjectAccount != null) {
						c.sendChat(user, "The account [" + params[0] + "] already exists");
						break;
					}
					
					subjectAccount = d.getCreateAccount(params[0], new Long(0));
					if(subjectAccount == null)
						throw new IllegalStateException("Failed to create account");
					
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
					
					Account subjectAccount = d.getAccount(params[0]);
					if(subjectAccount == null) {
						c.sendChat(user, "The account [" + params[0] + "] does not exist");
						break;
					}
					
					Date mostRecent = null;
					Iterator<User> it = subjectAccount.getUsers().iterator();
					while(it.hasNext()) {
						User subject = it.next();
						
						Date nt = new Date(Date.parse(subject.getLastSeen()));
						if(mostRecent == null)
							mostRecent = nt;
						else {
							if(nt.compareTo(mostRecent) > 0)
								mostRecent = nt;
						}
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

					BNetUser bnetusr = new BNetUser(params[0], user.getFullAccountName());
					User subject = d.getUser(bnetusr);
					if(subject == null) {
						c.sendChat(user, "The user [" + params[0] + "] does not exist");
						break;
					}
					
					Account subjectAccount = d.getAccount(params[1]);
					if(subjectAccount == null) {
						c.sendChat(user, "The account [" + params[1] + "] does not exist");
						break;
					}
					
					
					Session s = d.openSession();
					Transaction tx = s.beginTransaction();
					
					subject.setAccount(subjectAccount);
					
					tx.commit();
					s.close();
					
					c.sendChat(user, "The account [" + params[1] + "] has been assigned to user [" + params[0] + "]");
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
	
						BNetUser bnetusr = new BNetUser(params[0], user.getFullAccountName());
						User subject = d.getUser(bnetusr);
						Account subjectAccount;
						
						if(subject == null) {
							subjectAccount = d.getAccount(params[0]);
							if(subjectAccount == null) {
								c.sendChat(user, "User [" + params[0] + "] not found in database");
								break;
							}
						} else {
							subjectAccount = subject.getAccount();
						
							if(subjectAccount == null) {
								c.sendChat(user, "User [" + params[0] + "] does not have a bot account");
								break;
							}
						}
						
						String aliases = null;
						Iterator<User> it = subjectAccount.getUsers().iterator();
						while(it.hasNext()) {
							User u = it.next();
							if(aliases == null)
								aliases = u.getLogin();
							else
								aliases += ", " + u.getLogin();
						}
						
						c.sendChat(user, "User [" + subjectAccount.getName() + "] has access " + subjectAccount.getAccess() + " and aliases " + aliases);
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
		d.getCreateUser(user);
	}
	
	public void channelLeave(BNetUser user, int flags, int ping, StatString statstr) {
		d.getCreateUser(user);
	}
	
	public void channelUser(BNetUser user, int flags, int ping, StatString statstr) {
		User u = d.getCreateUser(user);
		Session s = d.openSession();
		u.setLastSeen(new Date().toString());
		s.save(u);
		s.close();
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
	
	private String lastInfo = null;
	private void recieveInfoError(String text) {
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
