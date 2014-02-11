/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.bnubot.bot.CommandEventHandler;
import net.bnubot.bot.GameTracker;
import net.bnubot.bot.HTMLOutputEventHandler;
import net.bnubot.bot.LockdownEventHandler;
import net.bnubot.bot.MusicEventHandler;
import net.bnubot.bot.TelnetEventHandler;
import net.bnubot.bot.console.ConsoleEventHandler;
import net.bnubot.bot.gui.GuiEventHandler;
import net.bnubot.bot.swt.SWTEventHandler;
import net.bnubot.bot.trivia.TriviaEventHandler;
import net.bnubot.logging.Out;
import net.bnubot.settings.Settings;

/**
 * @author scotta
 */
public class PluginManager {
	private static final String HEADER = "plugin";
	private static final List<Class<? extends EventHandler>> plugins = new ArrayList<Class<? extends EventHandler>>();
	private static final List<Class<? extends EventHandler>> enabledPlugins = new ArrayList<Class<? extends EventHandler>>();
	private static boolean sortEnabledPluginsNeeded = true;
	private static final Comparator<Class<? extends EventHandler>> enabledPluginsSorter = new Comparator<Class<? extends EventHandler>>() {
		@Override
		public int compare(Class<? extends EventHandler> o1, Class<? extends EventHandler> o2) {
			Integer i0 = Integer.valueOf(plugins.indexOf(o1));
			Integer i1 = Integer.valueOf(plugins.indexOf(o2));
			return i0.compareTo(i1);
		}};

	static {
		// These have always been in PluginManager
		register(HTMLOutputEventHandler.class);
		register(TriviaEventHandler.class);
		register(TelnetEventHandler.class);
		register(LockdownEventHandler.class);
		register(GameTracker.class);

		// Newly imported
		register(ConsoleEventHandler.class);
		//register(SWTEventHandler.class);
		register(GuiEventHandler.class, true);
		register(CommandEventHandler.class, true);
		register(MusicEventHandler.class, true);
	}

	public static void register(Class<? extends EventHandler> plugin) {
		register(plugin, false);
	}

	private static void register(Class<? extends EventHandler> plugin, boolean defaultEnabledValue) {
		boolean enable = Settings.getSection(HEADER).read(plugin.getName(), defaultEnabledValue);
		Out.debug(PluginManager.class, "Registering " + plugin.getName());
		plugins.add(plugin);

		if(enable)
			enabledPlugins.add(plugin);
	}

	public static boolean isEnabled(Class<? extends EventHandler> plugin) {
		return enabledPlugins.contains(plugin);
	}

	public static void setEnabled(Class<? extends EventHandler> plugin, boolean enable) {
		if(isEnabled(plugin) == enable)
			return;

		if(enable) {
			enabledPlugins.add(plugin);
			sortEnabledPluginsNeeded = true;
		} else {
			enabledPlugins.remove(plugin);
		}

		try {
			for(Profile p : Profile.getProfiles()) {
				if(enable) {
					EventHandler eh = p.constructPlugin(plugin);
					for(Connection c : p.getConnections())
						c.addEventHandler(eh);
				} else {
					for(Connection c : p.getConnections()) {
						boolean found = false;
						for(EventHandler e : c.getEventHandlers()) {
							if(e.getClass().equals(plugin)) {
								c.removeEventHandler(e);
								found = true;
								break;
							}
						}
						if(!found)
							throw new IllegalStateException("Couldn't find " + plugin.getSimpleName() + " in " + c.getName());
					}
				}
			}
		} catch(Exception e) {
			// This state is not recoverable; we don't know how many enabled the plugin
			Out.fatalException(e);
		}

		Settings.getSection(HEADER).write(plugin.getName(), enable);
	}

	public static List<Class<? extends EventHandler>> getPlugins() {
		return plugins;
	}

	public static List<Class<? extends EventHandler>> getEnabledPlugins() {
		if(sortEnabledPluginsNeeded) {
			Collections.sort(enabledPlugins, enabledPluginsSorter);
			sortEnabledPluginsNeeded = false;
		}
		return enabledPlugins;
	}

	public static boolean getEnableGui() {
		return isEnabled(GuiEventHandler.class);
	}

	public static void setEnableGui(boolean enable) {
		setEnabled(GuiEventHandler.class, enable);
	}

	public static boolean getEnableCli() {
		return isEnabled(ConsoleEventHandler.class);
	}

	public static void setEnableCli(boolean enable) {
		setEnabled(ConsoleEventHandler.class, enable);
	}

	public static boolean getEnableSwt() {
		return isEnabled(SWTEventHandler.class);
	}

	public static void setEnableSwt(boolean enable) {
		setEnabled(SWTEventHandler.class, enable);
	}

	public static boolean getEnableCommands() {
		return isEnabled(CommandEventHandler.class);
	}
}
