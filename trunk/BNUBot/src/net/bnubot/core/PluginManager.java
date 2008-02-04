/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core;

import java.util.ArrayList;
import java.util.List;

import net.bnubot.bot.HTMLOutputEventHandler;
import net.bnubot.bot.trivia.TriviaEventHandler;
import net.bnubot.settings.Settings;
import net.bnubot.util.Out;

public class PluginManager {
	private static final List<Class<? extends EventHandler>> plugins = new ArrayList<Class<? extends EventHandler>>();
	private static final List<Class<? extends EventHandler>> enabledPlugins = new ArrayList<Class<? extends EventHandler>>();
	
	static {
		register(HTMLOutputEventHandler.class);
		register(TriviaEventHandler.class);
	}
	
	public static void register(Class<? extends EventHandler> plugin) {
		Out.info(PluginManager.class, "Registering " + plugin.getName());
		plugins.add(plugin);
		
		if(Settings.readBoolean(null, plugin.getName(), false))
			enabledPlugins.add(plugin);
	}

	public static boolean isEnabled(Class<? extends EventHandler> plugin) {
		return enabledPlugins.contains(plugin);
	}

	public static void setEnabled(Class<? extends EventHandler> plugin, boolean enable) {
		if(enable) {
			if(enabledPlugins.contains(plugin))
				enabledPlugins.add(plugin);
			// TODO add to running connections
		} else {
			enabledPlugins.remove(plugin);
			// TODO remove from running connections
		}
		Settings.writeBoolean(null, plugin.getName(), enable);
	}

	public static List<Class<? extends EventHandler>> getPlugins() {
		return plugins;
	}
	
	public static List<Class<? extends EventHandler>> getEnabledPlugins() {
		return enabledPlugins;
	}
}
