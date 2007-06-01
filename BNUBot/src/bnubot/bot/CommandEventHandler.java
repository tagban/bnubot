package bnubot.bot;

import java.util.Properties;

import bnubot.bot.database.*;
import bnubot.core.Connection;

public class CommandEventHandler implements EventHandler {
	Connection c = null;
	Database d = null;
	Boolean sweepBanInProgress = false;
	int sweepBannedUsers;
	
	public CommandEventHandler(Database d) {
		this.d = d;
	}
	
	public void initialize(Connection c) {
		this.c = c;
	}
	
	private void parseCommand(String user, String command, String param) {
		String[] params = param.split(" ");
		
		switch(command.charAt(0)) {
		case 'a':
			if(command.compareToIgnoreCase("add") == 0) {
				try {
					if(params.length != 2)
						throw new Exception();
					int access = Integer.valueOf(params[1]);
					d.getUserDatabase().addUser(params[0], new User(access));
					
					c.sendChat("Added user [" + params[0] + "] sucessfully with access " + access);
				} catch(Exception e) {
					c.sendChat("Usage: ~add <user>[@<realm>] <access>");
					break;
				}
				break;
			}
			
		case 'b':
			if(command.compareToIgnoreCase("ban") == 0) {
				if(param.length() == 0) {
					c.sendChat("Usage: ~ban <user>[@<realm>] [reason]");
					break;
				}
				c.sendChat("/ban " + param);
				break;
			}
		
		case 'i':
			if(command.compareToIgnoreCase("info") == 0) {
				Properties p = System.getProperties();
				c.sendChat("BNU-Bot v2.0 beta running on " + p.getProperty("os.name") + " (" + p.getProperty("os.arch") + ")");
				break;
			}
			
		case 'k':
			if(command.compareToIgnoreCase("kick") == 0) {
				if(params.length != 1) {
					c.sendChat("Usage: ~kick <user>[@<realm>]");
					break;
				}
				c.sendChat("/kick " + params[0]);
				break;
			}
		case 's':
			if(command.compareToIgnoreCase("setrank") == 0) {
				int newRank;
				try {
					if(params.length != 2)
						throw new Exception();
					newRank = Integer.valueOf(params[1]);
					if((newRank < 1) || (newRank > 3))
						throw new Exception();
				} catch(Exception e) {
					c.sendChat("Usage: ~setrank <user> <rank:1-3>");
					break;
				}
				
				// TODO: validate that params[0] is in the clan
				try {
					c.setClanRank(params[0], newRank);
					c.sendChat("Success");
				} catch(Exception e) {
					c.sendChat("Failure");
				}
				break;
			}
			if(command.compareToIgnoreCase("sweepban") == 0) {
				if(params.length < 1) {
					c.sendChat("Usage: ~sweepban <channel>");
					break;
				}
				sweepBanInProgress = true;
				sweepBannedUsers = 0;
				c.sendChat("/who " + param);
				break;
			}
		case 'u':
			if(command.compareToIgnoreCase("unban") == 0) {
				if(params.length != 1) {
					c.sendChat("Usage: ~unban <user>[@<realm>]");
					break;
				}
				c.sendChat("/unban " + params[0]);
				break;
			}
		case 'w':
			if(command.compareToIgnoreCase("whois") == 0) {
				if(params.length != 1) {
					c.sendChat("Usage: ~whois <user>[@realm]");
					break;
				}
				
				User usr = d.getUserDatabase().getUser(params[0]);
				
				if(usr == null) {
					c.sendChat("User [" + params[0] + "] not found in database");
					break;
				}
				
				c.sendChat("User [" + params[0] + "] has access " + usr.getAccess());
				break;
			}
		default:
			c.sendChat("Command not recognized.");
		}
	}
	

	public void channelJoin(String user, int flags, int ping, String statstr) {
		if((user.compareToIgnoreCase("bnu-camel@azeroth") == 0)
		|| (user.compareToIgnoreCase("bnu-camel@useast") == 0)
		|| (user.compareToIgnoreCase("bnu-camel") == 0)) {
			c.sendChat("/me hails Camel");
		}
	}

	public void channelLeave(String user, int flags, int ping, String statstr) {}
	public void channelUser(String user, int flags, int ping, String statstr) {}
	public void joinedChannel(String channel) {}

	public void recieveChat(String user, String text) {
		User u = d.getUserDatabase().getUser(user);
		if(u == null)
			return;
		if(u.getAccess() <= 0)
			return;
		if(text == null)
			return;
		if(text.length() == 0)
			return;
		
		char trigger = c.getConnectionSettings().trigger.charAt(0);
		
		if(text.compareToIgnoreCase("?trigger") == 0)
			c.sendChat("The bot's trigger is: " + trigger);
		
		if(text.charAt(0) == trigger) {
			String[] command = text.substring(1).split(" ");
			String paramString = text.substring(text.indexOf(' ') + 1);
		
			parseCommand(user, command[0], paramString);
		}
	}

	public void recieveEmote(String user, String text) {}
	public void recieveError(String text) {}

	// If the name is "[NAME]", return "NAME" otherwise pass name through
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
				if(text.substring(0, 17).compareTo("Users in channel ") == 0) {
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
		
	}

	public void bnetConnected() {}
	public void bnetDisconnected() {}

	public void whisperRecieved(String user, String text) {
		User u = d.getUserDatabase().getUser(user);
		if(u == null)
			return;
		if(u.getAccess() <= 0)
			return;
		if(text == null)
			return;
		if(text.length() == 0)
			return;
		
		String[] command = text.substring(1).split(" ");
		String paramString = text.substring(text.indexOf(' ') + 1);
		
		parseCommand(user, command[0], paramString);
	}

	public void whisperSent(String user, String text) {}

}
