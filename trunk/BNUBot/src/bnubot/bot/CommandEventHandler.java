package bnubot.bot;

import bnubot.bot.database.*;
import bnubot.core.Connection;

public class CommandEventHandler implements EventHandler {
	Connection c = null;
	Database d = null;
	Boolean sweepBanInProgress = false;
	
	public CommandEventHandler(Database d) {
		this.d = d;
	}
	
	public void initialize(Connection c) {
		this.c = c;
	}

	public void channelJoin(String user, int flags, int ping, String statstr) {
		if((user.compareToIgnoreCase("bnu-camel@azeroth") == 0)
		|| (user.compareToIgnoreCase("bnu-camel@useast") == 0)
		|| (user.compareToIgnoreCase("bnu-camel") == 0)) {
			c.sendChat("/me hails Camel");
		}
	}

	public void channelLeave(String user, int flags, int ping, String statstr) {
	}

	public void channelUser(String user, int flags, int ping, String statstr) {
	}

	public void joinedChannel(String channel) {
		c.sendChat("/me is a BNU-Bot");
	}

	public void recieveChat(String user, String text) {
		User u = d.getUserDatabase().getUser(user);
		if(u == null)
			return;
		if(u.getAccess() <= 0)
			return;
		
		if(text.charAt(0) == '~') {
			String[] command = text.substring(1).split(" ");
			String paramString = text.substring(text.indexOf(' ') + 1);
		
			switch(command[0].charAt(0)) {
			case 'a':
				if(command[0].compareToIgnoreCase("add") == 0) {
					try {
						if(command.length != 3)
							throw new Exception();
						int access = Integer.valueOf(command[2]);
						d.getUserDatabase().addUser(command[1], new User(access));
						
						c.sendChat("Added user [" + command[1] + "] sucessfully with access " + access);
					} catch(Exception e) {
						c.sendChat("Usage: ~add <user>[@<realm>] <access>");
						break;
					}
					break;
				}
				
			case 'b':
				if(command[0].compareToIgnoreCase("ban") == 0) {
					if(command.length == 1) {
						c.sendChat("Usage: ~ban <user>[@<realm>] [reason]");
						break;
					}
					c.sendChat("/" + text.substring(1));
					break;
				}
			case 'k':
				if(command[0].compareToIgnoreCase("kick") == 0) {
					if(command.length <= 1) {
						c.sendChat("Usage: ~kick <user>[@<realm>] [reason]");
						break;
					}
					break;
				}
			case 's':
				if(command[0].compareToIgnoreCase("sweepban") == 0) {
					if(command.length < 2) {
						c.sendChat("Usage: ~sweepban <channel>");
						break;
					}
					sweepBanInProgress = true;
					c.sendChat("/who " + paramString);
					break;
				}
			case 'u':
				if(command[0].compareToIgnoreCase("unban") == 0) {
					if(command.length != 2) {
						c.sendChat("Usage: ~unban <user>[@<realm>]");
						break;
					}
					c.sendChat("/unban " + command[1]);
					break;
				}
			case 'w':
				if(command[0].compareToIgnoreCase("whois") == 0) {
					if(command.length != 2) {
						c.sendChat("Usage: ~whois <user>[@realm]");
						break;
					}
					
					User usr = d.getUserDatabase().getUser(command[1]);
					
					if(usr == null) {
						c.sendChat("User [" + command[1] + "] not found in database");
						break;
					}
					
					c.sendChat("User [" + command[1] + "] has access " + u.getAccess());
					break;
				}
			default:
				c.sendChat("Command not recognized.");
			}
		}
	}

	public void recieveEmote(String user, String text) {
		// TODO Auto-generated method stub
		
	}

	public void recieveError(String text) {
		// TODO Auto-generated method stub
		
	}

	public void recieveInfo(String text) {
		// TODO Auto-generated method stub
		if(sweepBanInProgress) {
			boolean turnItOff = true;
			
			if(text.substring(0, 17).compareTo("Users in channel ") == 0)
				turnItOff = false;
			
			String users[] = text.split(", ");
			if(users.length == 2) {
				if(users[0].indexOf(' ') == -1) {
					if(users[1].indexOf(' ') == -1) {
						c.sendChat("| /ban " + users[0]);
						c.sendChat("| /ban " + users[1]);
						turnItOff = false;
						

						c.sendChat("[temporary fix]...turning sweepban mode off to prevent flooding");
						turnItOff = true;
					}
				}
			}
			
			if(turnItOff)
				sweepBanInProgress = false;
		}
		
	}

	public void bnetConnected() {
		// TODO Auto-generated method stub
		
	}

	public void bnetDisconnected() {
		// TODO Auto-generated method stub
		
	}

	public void whisperRecieved(String user, String text) {
		// TODO Auto-generated method stub
		
	}

	public void whisperSent(String user, String text) {
		// TODO Auto-generated method stub
		
	}

}
