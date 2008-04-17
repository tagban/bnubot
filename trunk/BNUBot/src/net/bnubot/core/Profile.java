/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import net.bnubot.bot.CommandEventHandler;
import net.bnubot.bot.console.ConsoleEventHandler;
import net.bnubot.bot.gui.GuiEventHandler;
import net.bnubot.bot.swt.SWTDesktop;
import net.bnubot.core.commands.CommandRunnable;
import net.bnubot.db.Account;
import net.bnubot.db.Command;
import net.bnubot.db.Mail;
import net.bnubot.db.Rank;
import net.bnubot.db.conf.DatabaseContext;
import net.bnubot.settings.ConnectionSettings;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.Out;

public class Profile {
	private static final List<Profile> profiles = new ArrayList<Profile>();
	private static final Dictionary<String, CommandRunnable> commands = new Hashtable<String, CommandRunnable>();
	
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
					Mail.send(a, a, message);
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
		}
		
		commands.put(name, action);
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
		p.insertConnection(con);
		
		// Add it to the list of connections
		return p.cons.add(con);
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

	public void insertConnection(Connection con) throws Exception {
		Out.setThreadOutputConnectionIfNone(con);
		synchronized(cons) {
			if(cons.size() > 0) {
				Connection primary = cons.get(0);

				// Add EHs
				for(EventHandler eh : primary.getEventHandlers())
					con.addEventHandler(eh);
			} else {
				// Plugins
				for(Class<? extends EventHandler> plugin : PluginManager.getEnabledPlugins())
					con.addEventHandler(plugin.newInstance());

				// CLI
				if(GlobalSettings.enableCLI)
					con.addEventHandler(new ConsoleEventHandler());

				// SWT GUI
				if(GlobalSettings.enableSWT)
					try {
						con.addEventHandler(SWTDesktop.createSWTEventHandler());
					} catch(NoClassDefFoundError e) {
						// Failed to create SWT GUI; revert to Swing
						GlobalSettings.enableSWT = false;
						GlobalSettings.enableGUI = true;
					}

				// GUI
				if(GlobalSettings.enableGUI)
					con.addEventHandler(new GuiEventHandler(con));

				// Commands
				if(GlobalSettings.enableCommands)
					try {
						con.addEventHandler(new CommandEventHandler());
					} catch(Exception e) {
						Out.exception(e);
					}
			}
			
			// Start the Connection thread
			con.start();
			
			// Wait for the Connection thread to initialize
			while(!con.isInitialized()) {
				Thread.sleep(10);
				Thread.yield();
			}
		}
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
		for(Enumeration<String> en = commands.keys(); en.hasMoreElements();) {
			String command = en.nextElement();
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
			if(cons.size() == 0)
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
}
