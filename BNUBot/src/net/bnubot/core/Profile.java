/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import net.bnubot.bot.CommandEventHandler;
import net.bnubot.bot.console.ConsoleEventHandler;
import net.bnubot.bot.database.CommandResultSet;
import net.bnubot.bot.database.Database;
import net.bnubot.bot.gui.GuiEventHandler;
import net.bnubot.bot.swt.SWTDesktop;
import net.bnubot.core.commands.CommandRunnable;
import net.bnubot.settings.ConnectionSettings;
import net.bnubot.settings.DatabaseSettings;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.Out;

public class Profile {
	private static final List<Profile> profiles = new ArrayList<Profile>();
	private static final Dictionary<String, CommandRunnable> commands = new Hashtable<String, CommandRunnable>();
	
	public static void registerCommand(String name, CommandRunnable action) {
		if(commands.get(name) != null)
			throw new IllegalArgumentException("The command " + name + " is already registered");
		
		Database db = Database.getInstance();
		if(db == null)
			throw new IllegalStateException("No database has been initialized");
		
		try {
			boolean exists;
			CommandResultSet crs = db.getCommand(name);
			exists = crs.next();
			db.close(crs);
			if(!exists)
				db.createCommand(name);
		} catch (SQLException e) {
			throw new IllegalStateException(e);
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
		return findCreateProfile(cs.profile).insertConnection(cs);
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
		
		chatQueue = new ChatQueue();
		chatQueue.start();
	}

	private boolean insertConnection(ConnectionSettings cs) throws Exception {
		Connection con = ConnectionFactory.createConnection(cs, chatQueue, this);
		Out.setThreadOutputConnectionIfNone(con);
		synchronized(cons) {
			if(cons.size() > 0) {
				Connection primary = cons.get(0);

				// Add secondary EHs
				for(EventHandler eh : primary.getEventHandlers())
					con.addSecondaryEventHandler(eh);

				primary.addSlave(con);
			} else {
				// Start the database
				if(GlobalSettings.enableCommands) {
					if(Database.getInstance() == null) {
						DatabaseSettings ds = new DatabaseSettings();
						ds.load();

						if((ds.driver == null) || (ds.url == null)) {
							String msg = "Database is not configured; disabling commands.";
							Out.info(Profile.class, msg);
						} else {
							try {
								new Database(ds);
								ds.save();
							} catch(Exception e) {
								Out.exception(e);
								String msg = "Failed to initialize the database; commands disabled.\n" + e.getMessage();
								Out.error(Profile.class, msg);
							}
						}
					}
				}
				
				// Plugins
				for(Class<? extends EventHandler> plugin : PluginManager.getEnabledPlugins())
					con.addEventHandler(plugin.newInstance());

				// CLI
				if(GlobalSettings.enableCLI)
					con.addEventHandler(new ConsoleEventHandler());

				// GUI
				if(GlobalSettings.enableGUI)
					con.addEventHandler(new GuiEventHandler(con));

				// SWT GUI
				if(GlobalSettings.enableSWT)
					try {
						con.addEventHandler(SWTDesktop.createSWTEventHandler());
					} catch(NoClassDefFoundError e) {
						// Failed to create SWT GUI; revert to Swing
						GlobalSettings.enableSWT = false;
						GlobalSettings.enableGUI = true;
					}

				// Commands
				if(GlobalSettings.enableCommands) {
					if(Database.getInstance() != null)
						con.addEventHandler(new CommandEventHandler());
				}
			}
			
			// Start the Connection thread
			con.start();
			
			// Wait for the Connection thread to initialize
			while(!con.isInitialized()) {
				Thread.sleep(10);
				Thread.yield();
			}
			
			// Add it to the list of connections
			return cons.add(con);
		}
	}

	public String getName() {
		return name;
	}

	public void dispose() {
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
}
