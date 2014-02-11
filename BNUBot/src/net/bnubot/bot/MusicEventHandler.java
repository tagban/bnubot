/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot;

import net.bnubot.core.Connection;
import net.bnubot.core.EventHandler;
import net.bnubot.core.Profile;
import net.bnubot.core.commands.CommandRunnable;
import net.bnubot.db.Account;
import net.bnubot.db.conf.DatabaseContext;
import net.bnubot.util.BNetUser;
import net.bnubot.util.UnloggedException;
import net.bnubot.util.music.MusicController;
import net.bnubot.util.music.MusicControllerFactory;

/**
 * @author scotta
 */
public class MusicEventHandler extends EventHandler {

	public MusicEventHandler(Profile profile) {
		super(profile);
		if(DatabaseContext.getContext() == null)
			throw new UnloggedException("Can not enable commands without a database!");
		initializeCommands();
	}

	private static boolean commandsInitialized = false;
	private static void initializeCommands() {
		if(commandsInitialized)
			return;
		commandsInitialized = true;

		Profile.registerCommand("music", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				MusicController mc = MusicControllerFactory.getMusicController();
				user.sendChat(mc.getCurrentlyPlaying(), whisperBack);
			}});
		Profile.registerCommand("pause", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				MusicController mc = MusicControllerFactory.getMusicController();
				mc.pause();
			}});
		Profile.registerCommand("play", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				MusicController mc = MusicControllerFactory.getMusicController();
				mc.play();
			}});
	}
}
