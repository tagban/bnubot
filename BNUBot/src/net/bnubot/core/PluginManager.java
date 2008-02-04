/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core;

import java.util.ArrayList;
import java.util.List;

import net.bnubot.bot.CommandEventHandler;
import net.bnubot.bot.HTMLOutputEventHandler;
import net.bnubot.bot.console.ConsoleEventHandler;
import net.bnubot.bot.gui.GuiEventHandler;
import net.bnubot.bot.swt.SWTEventHandler;
import net.bnubot.bot.trivia.TriviaEventHandler;
import net.bnubot.util.Out;

public class PluginManager {
	private static final List<Class<? extends EventHandler>> plugins = new ArrayList<Class<? extends EventHandler>>();
	static {
		register(CommandEventHandler.class);
		register(ConsoleEventHandler.class);
		register(GuiEventHandler.class);
		register(HTMLOutputEventHandler.class);
		register(SWTEventHandler.class);
		register(TriviaEventHandler.class);
	}
	
	public static void register(Class<? extends EventHandler> eh) {
		Out.info(PluginManager.class, "Registering " + eh.getName());
		plugins.add(eh);
		// TODO 
	}
}
