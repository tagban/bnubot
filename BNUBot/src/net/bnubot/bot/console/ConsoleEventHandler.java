/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.console;

import java.util.HashMap;

import net.bnubot.core.Connection;
import net.bnubot.core.EventHandlerImpl;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetUser;

public class ConsoleEventHandler extends EventHandlerImpl {
	private static final HashMap<Connection, CLIThread> threads = new HashMap<Connection, CLIThread>();
	
	public synchronized void initialize(Connection source) {
		CLIThread thread = new CLIThread(source);
		threads.put(source, thread);
		thread.start();
	}
	
	public void disable(Connection source) {
		CLIThread thread = threads.get(source);
		if(thread != null)
			thread.disable();
	}

	public void joinedChannel(Connection source, String channel) {
		System.out.println("Joining channel " + channel);
	}

	public void channelUser(Connection source, BNetUser user) {
		if(GlobalSettings.displayChannelUsers)
			System.out.println(user.toStringEx() + user.getStatString().toString());
	}
	
	public void channelJoin(Connection source, BNetUser user) {
		if(GlobalSettings.displayJoinParts)
			System.out.println(user.toStringEx() + " has joined the channel" + user.getStatString().toString() + ".");
	}
	public void channelLeave(Connection source, BNetUser user) {
		if(GlobalSettings.displayJoinParts)
			System.out.println(user.toStringEx() + " has left the channel.");
	}

	public void recieveChat(Connection source, BNetUser user, String text) {
		System.out.println("<" + user.toString() + "> " + text);
	}

	public void recieveEmote(Connection source, BNetUser user, String text) {
		System.out.println("<" + user.toString() + " " + text + ">");
	}

	public void whisperRecieved(Connection source, BNetUser user, String text) {
		System.out.println("<From: " + user.toString() + "> " + text);
	}

	public void whisperSent(Connection source, BNetUser user, String text) {
		System.out.println("<To: " + user.toString() + "> " + text);
	}
	
	public void recieveDebug(Connection source, String text) {
		System.out.println("(DEBUG) " + text);
	}

	public void recieveInfo(Connection source, String text) {
		System.out.println("(INFO) " + text);
	}

	public void recieveError(Connection source, String text) {
		System.out.println("(ERROR) " + text);
	}
}
