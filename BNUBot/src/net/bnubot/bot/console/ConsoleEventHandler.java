/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.console;

import java.util.HashMap;

import net.bnubot.bot.console.ColorConsole.ColorConstant;
import net.bnubot.core.Connection;
import net.bnubot.core.EventHandler;
import net.bnubot.core.Profile;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetUser;

/**
 * @author scotta
 */
public class ConsoleEventHandler extends EventHandler {
	private static final HashMap<Connection, CLIThread> threads = new HashMap<Connection, CLIThread>();

	private static ColorConsole con = new ColorConsole(System.out);

	public ConsoleEventHandler(Profile profile) {
		super(profile);
	}

	@Override
	public synchronized void initialize(Connection source) {
		CLIThread thread = new CLIThread(source);
		threads.put(source, thread);
		thread.start();
	}

	@Override
	public void disable(Connection source) {
		CLIThread thread = threads.get(source);
		if(thread != null)
			thread.disable();
	}

	@Override
	public void joinedChannel(Connection source, String channel) {
		con.begin()
		.color(ColorConstant.FG_GREEN)
		.string("Joining channel ")
		.string(channel)
		.end();
	}

	@Override
	public void channelUser(Connection source, BNetUser user) {
		if(GlobalSettings.displayChannelUsers)
			con.begin()
			.color(ColorConstant.FG_GREEN)
			.string(user.toStringEx())
			.string(user.getStatString().toString())
			.end();
	}

	@Override
	public void channelJoin(Connection source, BNetUser user) {
		if(GlobalSettings.getDisplayJoinParts())
			con.begin()
			.color(ColorConstant.FG_GREEN)
			.string(user.toStringEx())
			.string(" has joined the channel")
			.string(user.getStatString().toString())
			.string(".")
			.end();
	}
	@Override
	public void channelLeave(Connection source, BNetUser user) {
		if(GlobalSettings.getDisplayJoinParts())
			con.begin()
			.color(ColorConstant.FG_GREEN)
			.string(user.toStringEx())
			.string(" has left the channel.")
			.end();
	}

	@Override
	public void recieveChat(Connection source, BNetUser user, String text) {
		con.begin()
		.string("<")
		.string(user.toString())
		.string("> ")
		.string(text)
		.end();
	}

	@Override
	public void recieveBroadcast(Connection source, String username, int flags, String text) {
		con.begin()
		.string("<")
		.string(username)
		.string("> ")
		.string(text)
		.end();
	}

	@Override
	public void recieveEmote(Connection source, BNetUser user, String text) {
		con.begin()
		.string("<")
		.string(user.toString())
		.string(" ")
		.string(text)
		.string(">")
		.end();
	}

	@Override
	public void whisperRecieved(Connection source, BNetUser user, String text) {
		con.begin()
		.string("<From: ")
		.string(user.toString())
		.string("> ")
		.string(text)
		.end();
	}

	@Override
	public void whisperSent(Connection source, BNetUser user, String text) {
		con.begin()
		.string("<To: ")
		.string(user.toString())
		.string("> ")
		.string(text)
		.end();
	}

	@Override
	public void recieveDebug(Connection source, String text) {
		con.begin()
		.color(ColorConstant.FG_YELLOW)
		.string(text)
		.end();
	}

	@Override
	public void recieveInfo(Connection source, String text) {
		con.begin()
		.color(ColorConstant.FG_BLUE)
		.string(text)
		.end();
	}

	@Override
	public void recieveError(Connection source, String text) {
		con.begin()
		.color(ColorConstant.FG_RED)
		.string(text)
		.end();
	}

	@Override
	public void recieveServerInfo(Connection source, String text) {
		con.begin()
		.color(ColorConstant.FG_MAGENTA)
		.string("(")
		.string(source.getServerType())
		.string(") ")
		.color(ColorConstant.FG_BLUE)
		.string(text)
		.end();
	}

	@Override
	public void recieveServerError(Connection source, String text) {
		con.begin()
		.color(ColorConstant.FG_MAGENTA)
		.string("(")
		.string(source.getServerType())
		.string(") ")
		.color(ColorConstant.FG_RED)
		.string(text)
		.end();
	}
}
