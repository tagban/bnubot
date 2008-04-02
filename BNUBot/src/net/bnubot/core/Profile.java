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

import net.bnubot.DatabaseContext;
import net.bnubot.bot.CommandEventHandler;
import net.bnubot.bot.console.ConsoleEventHandler;
import net.bnubot.bot.gui.GuiEventHandler;
import net.bnubot.bot.swt.SWTDesktop;
import net.bnubot.core.commands.CommandRunnable;
import net.bnubot.db.Command;
import net.bnubot.settings.ConnectionSettings;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.Out;

import org.apache.cayenne.DataObjectUtils;
import org.apache.cayenne.access.DataContext;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SelectQuery;

public class Profile {
	private static final List<Profile> profiles = new ArrayList<Profile>();
	private static final Dictionary<String, CommandRunnable> commands = new Hashtable<String, CommandRunnable>();
	
	public static void registerCommand(String name, CommandRunnable action) {
		if(commands.get(name) != null)
			throw new IllegalArgumentException("The command " + name + " is already registered");
		
		DataContext context = DatabaseContext.getContext();
		Expression expr = ExpressionFactory.matchExp(Command.NAME_PROPERTY, name);
		SelectQuery query = new SelectQuery(Command.class, expr);
		Command cmd = (Command) DataObjectUtils.objectForQuery(context, query);
		if(cmd == null) {
			//TODO
			throw new IllegalStateException("TODO: create the command in the database");
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
					try {
						con.addEventHandler(new CommandEventHandler());
					} catch(Exception e) {
						Out.exception(e);
					}
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
