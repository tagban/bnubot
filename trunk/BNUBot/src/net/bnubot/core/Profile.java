/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bnubot.core.commands.AccountDoesNotExistException;
import net.bnubot.core.commands.CommandDoesNotExistException;
import net.bnubot.core.commands.CommandFailedWithDetailsException;
import net.bnubot.core.commands.CommandRunnable;
import net.bnubot.core.commands.InsufficientAccessException;
import net.bnubot.db.Account;
import net.bnubot.db.Command;
import net.bnubot.db.Mail;
import net.bnubot.db.Rank;
import net.bnubot.db.conf.DatabaseContext;
import net.bnubot.logging.Out;
import net.bnubot.settings.ConnectionSettings;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetUser;
import net.bnubot.util.task.Task;
import net.bnubot.util.task.TaskManager;

/**
 * @author scotta
 */
public class Profile {
	private static final List<Profile> profiles = new ArrayList<Profile>();
	private static final Map<String, CommandRunnable> commands = new HashMap<String, CommandRunnable>();

	protected static List<Profile> getProfiles() {
		return profiles;
	}

	public static void registerCommand(String name, CommandRunnable action) {
		if(commands.get(name) != null)
			throw new IllegalArgumentException("The command " + name + " is already registered");

		if(Command.get(name) == null) {
			Rank max = Rank.getMax();

			Command c = DatabaseContext.getContext().newObject(Command.class);
			c.setRank(max);
			c.setCmdgroup(null);
			c.setDescription(null);
			c.setName(name);
			try {
				c.updateRow();
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}

			String message = "Created command " + name + " with access " + max.getAccess() + "; to change, use %trigger%setauth " + name + " <access>";
			for(Account a : max.getAccountArray())
				try {
					Mail.send(null, a, message);
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
		}

		commands.put(name, action);
	}

	/**
	 * Command initiated by the user via sendChat()
	 */
	protected static boolean internalParseCommand(Connection source, String command, boolean whisperBack) throws InternalError {
		try {
			BNetUser myUser = source.getMyUser();
			if(myUser == null)
				myUser = new BNetUser(source, source.cs.username, source.cs.getMyRealm());
			workCommand(source, myUser, command, whisperBack, true);
		} catch(CommandDoesNotExistException e) {
			return false;
		} catch(AccountDoesNotExistException e) {
			source.dispatchRecieveError("The account [" + e.getMessage() + "] does not exist!");
		} catch(CommandFailedWithDetailsException e) {
			source.dispatchRecieveError(e.getMessage());
		} catch(InsufficientAccessException e) {
			source.dispatchRecieveError("You have insufficient access " + e.getMessage());
		} catch(Exception e) {
			Out.exception(e);
		}
		return true;
	}

	/**
	 * Command initiated with a trigger
	 */
	protected static boolean parseCommand(Connection source, BNetUser user, String command, boolean whisperBack) {
		if(Out.isDebug(Profile.class))
			Out.debugAlways(Profile.class, user.toString() + ": " + command + " [" + whisperBack + "]");

		//Don't ask questions if they are a super-user
		final boolean superUser = user.equals(source.getMyUser());

		try {
			workCommand(source, user, command, whisperBack, superUser);
		} catch(CommandDoesNotExistException e) {
			return false;
		} catch(AccountDoesNotExistException e) {
			user.sendChat("The account [" + e.getMessage() + "] does not exist!", whisperBack);
		} catch(CommandFailedWithDetailsException e) {
			user.sendChat(e.getMessage(), whisperBack);
		} catch(InsufficientAccessException e) {
			if(e.canContactUser())
				user.sendChat("You have insufficient access " + e.getMessage() , whisperBack);
			else
				source.dispatchRecieveError(user.getFullAccountName() + " does not have access " + e.getMessage());
		} catch(Exception e) {
			Out.exception(e);
			user.sendChat(e.getClass().getSimpleName() + ": " + e.getMessage(), whisperBack);
		}
		return true;
	}

	private static void workCommand(Connection source, BNetUser user, String command, boolean whisperBack, final boolean superUser) throws Exception {
		// Grab the part of the command string after the space
		String param = null;
		{
			String[] paramHelper = command.split(" ", 2);
			command = paramHelper[0];
			if(paramHelper.length > 1)
				param = paramHelper[1];
		}

		// Check if the command exists
		Command rsCommand = Command.get(command);
		if(rsCommand == null)
			throw new CommandDoesNotExistException(command);

		//Reset the command to the case in the database
		command = rsCommand.getName();

		Account commanderAccount = Account.get(user);
		if(!superUser) {
			int commanderAccess = 0;
			if(commanderAccount != null)
				commanderAccess = commanderAccount.getAccess();

			int requiredAccess = rsCommand.getAccess();
			if(commanderAccess < requiredAccess)
				throw new InsufficientAccessException("(" + commanderAccess + "/" + requiredAccess + ")", commanderAccess > 0);
		}

		CommandRunnable cr = Profile.getCommand(command);
		if(cr == null) {
			source.dispatchRecieveError("Command " + command + " has no associated runnable");
			throw new CommandDoesNotExistException(command);
		}

		String[] params = null;
		if(param != null)
			params = param.split(" ");

		cr.run(source,
				user,
				param,
				params,
				whisperBack,
				commanderAccount,
				superUser);
	}

	public static CommandRunnable getCommand(String name) {
		return commands.get(name);
	}

	private static Profile findCreateProfile(String name) {
		synchronized(profiles) {
			for(Profile p : profiles)
				if(p.getName().equals(name))
					return p;
		}
		return new Profile(name);
	}

	private static boolean add(ConnectionSettings cs) throws Exception {
		Profile p = findCreateProfile(cs.profile);
		Connection con = ConnectionFactory.createConnection(cs, p.chatQueue, p);
		return p.insertConnection(con);
	}

	private final List<Connection> cons = new ArrayList<Connection>();
	private final ChatQueue chatQueue;
	private final String name;
	public long lastAntiIdle;

	private Profile(String name) {
		this.name = name;

		synchronized(profiles) {
			profiles.add(this);
		}

		chatQueue = new ChatQueue(name);
		chatQueue.start();
	}

	private boolean insertConnection(Connection con) throws Exception {
		Out.setThreadOutputHandler(con);

		synchronized(cons) {
			// Add it to the list of connections
			if(!cons.add(con))
				return false;

			if(cons.size() > 1) {
				Connection primary = cons.get(0);

				// Add EHs
				Collection<EventHandler> eventHandlers = primary.getEventHandlers();
				synchronized(eventHandlers) {
					for(EventHandler eh : eventHandlers)
						con.addEventHandler(eh);
				}
			} else {
				// Plugins
				for(Class<? extends EventHandler> plugin : PluginManager.getEnabledPlugins())
					try {
						con.addEventHandler(constructPlugin(plugin));
					} catch(IllegalStateException e) {
						Out.error(getClass(), "Failed to construct plugin " + plugin.getSimpleName() + ": " + e.getMessage());
					} catch(InvocationTargetException e) {
						Out.exception(e.getCause());
					} catch(Exception e) {
						Out.exception(e);
					}
			}
		}

		// Start the Connection thread
		con.start();

		// Wait for the Connection thread to initialize
		Task t = TaskManager.createTask("Initializing " + con.toShortString());
		while(!con.isInitialized()) {
			Thread.sleep(10);
			Thread.yield();
		}
		t.complete();

		Out.setThreadOutputHandler(null);
		return true;
	}

	protected EventHandler constructPlugin(Class<? extends EventHandler> plugin) throws Exception {
		return plugin.getConstructor(Profile.class).newInstance(this);
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		switch(cons.size()) {
		case 0: return name;
		case 1: return cons.get(0).toString();
		}
		return name + ": " + getPrimaryConnection().toString();
	}

	public void dispose() {
		chatQueue.dispose();

		synchronized(cons) {
			for(Connection con : cons)
				con.dispose();
		}

		synchronized(profiles) {
			// Remove this profile from the list
			profiles.remove(this);

			// Get the highest botnum
			int max = 0;
			for(Profile p : profiles)
				for(Connection con : p.cons)
					max = Math.max(max, con.getConnectionSettings().botNum);
			GlobalSettings.numBots = max;
		}
		GlobalSettings.save();
	}

	public static void newConnection(int newConnectionId) {
		ConnectionSettings cs = new ConnectionSettings(newConnectionId);
		try {
			add(cs);
		} catch (Exception e) {
			Out.exception(e);
		}
	}

	public static void newConnection() {
		// Build a list of pre-existing connection ids
		List<Integer> connectionIds;
		synchronized(profiles) {
			connectionIds = new ArrayList<Integer>(profiles.size());
			for(Profile p : profiles) {
				synchronized(p.cons) {
					for(Connection con : p.cons)
						connectionIds.add(con.getConnectionSettings().botNum);
				}
			}
		}

		for(int i = 1; i <= GlobalSettings.numBots; i++)
			if(!connectionIds.contains(i)) {
				newConnection(i);
				return;
			}

		newConnection(++GlobalSettings.numBots);
		GlobalSettings.save();
	}

	public List<Connection> getConnections() {
		return cons;
	}

	public static List<String> findCommandsForTabComplete(String containing) {
		containing = containing.toLowerCase();

		List<String> ret = new ArrayList<String>();
		for(String command : commands.keySet()) {
			if(command.toLowerCase().startsWith(containing))
				ret.add(command);
		}
		return ret;
	}

	public ChatQueue getChatQueue() {
		return chatQueue;
	}

	/**
	 * Gets the Connection which is to be considered the primary
	 * @return The first connected connection; if all disconnected, the first one; if none, null
	 */
	public Connection getPrimaryConnection() {
		synchronized(cons) {
			if(cons.size() == 1)
				return cons.get(0);

			// Return the first connected connection
			for(Connection c : cons)
				if(c.isConnected())
					return c;

			// All are disconnected; return the first one
			if(cons.size() > 0)
				return cons.get(0);
		}

		// There aren't any connections at all!
		return null;
	}

	/**
	 * @param user the BNetUser to look for
	 * @return true if the user is myUser in any of the profile's connections
	 */
	public boolean isOneOfMyUsers(BNetUser user) {
		if(user == null)
			return false;
		for(Connection con : cons)
			if(user.equals(con.getMyUser()))
				return true;
		return false;
	}
}
