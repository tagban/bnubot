/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot;

import net.bnubot.core.Connection;
import net.bnubot.core.EventHandler;
import net.bnubot.core.Profile;
import net.bnubot.core.bncs.BNCSConnection;
import net.bnubot.core.commands.CommandRunnable;
import net.bnubot.core.friend.FriendEntry;
import net.bnubot.db.Account;
import net.bnubot.util.BNetUser;

/**
 * @author scotta
 */
public class GameTracker extends EventHandler {
	public GameTracker() {
		initializeCommands();
	}

	private static boolean commandsInitialized = false;
	private static void initializeCommands() {
		if(commandsInitialized)
			return;
		commandsInitialized = true;

		Profile.registerCommand("login", new CommandRunnable() {
		@Override
		public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
		throws Exception {
			findThis(source, GameTracker.class);
			source.sendFriendsList();
			source.sendChat("/f a " + user.getFullAccountName());
		}});
		/*Profile.registerCommand("", new CommandRunnable() {
		public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
		throws Exception {
			// ...
		}});*/
	}

	@Override
	public void recieveInfo(Connection source, String text) {
		if(text.startsWith("Added ") && text.endsWith(" to your friends list.")) {
			String user = text.substring(7, text.length() - 29);
			System.err.println("\"" + user + "\"");
		} else if(text.startsWith("Removed ") && text.endsWith(" from your friends list.")) {
			String user = text.substring(9, text.length() - 33);
			System.err.println("\"" + user + "\"");
		}
	}

	@Override
	public void friendsAdd(BNCSConnection source, FriendEntry friend) {
		source.sendChat("/w " + friend.getAccount() + " You are logged in. You must add me to your friends list.");
	}
}
