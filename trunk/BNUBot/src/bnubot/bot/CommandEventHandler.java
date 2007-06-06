package bnubot.bot;

import java.util.Date;
import java.util.Properties;

import bnubot.bot.database.*;
import bnubot.core.BNetUser;
import bnubot.core.Connection;
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
	
	private void parseCommand(BNetUser user, String command, String param) {
		String[] params = param.split(" ");

		User u = d.getUserDatabase().getUser(user.getFullAccountName());
		boolean superUser = false;
		
		if(c.getMyUser().equals(user) == false) {
			if(u == null)
				return;
			if(u.getAccess() <= 0)
				return;
		} else {
			superUser = true;
			if(u == null) {
				u = new User(0);
				d.getUserDatabase().addUser(user.getFullAccountName(), u);
			}
		}
		
		lastCommandUser = user;
		lastCommandTime = new Date().getTime();
		
		try {
			switch(command.charAt(0)) {
			case 'a':
				if(command.equals("add")) {
					try {
						if(params.length != 2)
							throw new InvalidUsageException();
						
						BNetUser target = new BNetUser(params[0], user.getFullAccountName());
						int access = Integer.valueOf(params[1]);
						
						if(!superUser) {
							if(access > u.getAccess())
								throw new InsufficientAccessException("to add users beyond " + u.getAccess());
							if(target.equals(user))
								throw new InsufficientAccessException("to modify your self");
						}
						
						d.getUserDatabase().addUser(target.getFullAccountName(), new User(access));
						c.sendChat(user, "Added user [" + params[0] + "] sucessfully with access " + access);
					} catch(InvalidUsageException e) {
						c.sendChat(user, "Usage: ~add <user>[@<realm>] <access>");
						break;
					}
					break;
				}
				break;
			case 'b':
				if(command.equals("ban")) {
					if(param.length() == 0) {
						c.sendChat(user, "Usage: ~ban <user>[@<realm>] [reason]");
						break;
					}
					c.sendChat("/ban " + param);
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
						c.sendChat(user, "Usage: ~kick <user>[@<realm>]");
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
				if(command.equals("setrank")) {
					int newRank;
					try {
						if(params.length != 2)
							throw new InvalidUsageException();
						newRank = Integer.valueOf(params[1]);
						if((newRank < 1) || (newRank > 3))
							throw new InvalidUsageException();
					} catch(InvalidUsageException e) {
						c.sendChat(user, "Usage: ~setrank <user> <rank:1-3>");
						break;
					}
					
					// TODO: validate that params[0] is in the clan
					c.sendClanRankChange(params[0], newRank);
					c.sendChat(user, "Success");
					break;
				}
				if(command.equals("sweepban")) {
					if(params.length < 1) {
						c.sendChat(user, "Usage: ~sweepban <channel>");
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
						c.sendChat(user, "Usage: ~unban <user>[@<realm>]");
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
	
						params[0] = new BNetUser(params[0], user.getFullAccountName()).getFullAccountName();
						User usr = d.getUserDatabase().getUser(params[0]);
						
						if(usr == null) {
							c.sendChat(user, "User [" + params[0] + "] not found in database");
							break;
						}
						
						c.sendChat(user, "User [" + params[0] + "] has access " + usr.getAccess());
					} catch(InvalidUsageException e) {
						c.sendChat(user, "Usage: ~whois <user>[@realm]");
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
			c.sendChat(user, "Error: " + e.getMessage());
		}
	}

	public void channelJoin(BNetUser user, int flags, int ping, String statstr) {}
	public void channelLeave(BNetUser user, int flags, int ping, String statstr) {}
	public void channelUser(BNetUser user, int flags, int ping, String statstr) {}
	public void joinedChannel(String channel) {}

	public void recieveChat(BNetUser user, int flags, int ping, String text) {
		if(text == null)
			return;
		if(text.length() == 0)
			return;
		
		User u = d.getUserDatabase().getUser(user.getFullAccountName());
		if(u == null)
			return;
		if(u.getAccess() <= 0)
			return;
		
		char trigger = c.getConnectionSettings().trigger.charAt(0);
		
		if(text.equals("?trigger"))
			c.sendChat(user, "The bot's trigger is: " + trigger);
		
		if(text.charAt(0) == trigger) {
			String[] command = text.substring(1).split(" ");
			String paramString = text.substring(text.indexOf(' ') + 1);
		
			parseCommand(user, command[0], paramString);
		}
	}

	public void recieveEmote(BNetUser user, int flags, int ping, String text) {}
	
	private void recieveInfoError(String text) {
		long timeElapsed = new Date().getTime() - lastCommandTime;
		// 200ms
		if(timeElapsed < 200) {
			c.sendChat(lastCommandUser, text);
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

	public void clanMOTD(Object cookie, String text) {
		// TODO Auto-generated method stub
		
	}
}
