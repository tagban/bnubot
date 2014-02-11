/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot;

import java.util.LinkedList;
import java.util.List;

import net.bnubot.core.Connection;
import net.bnubot.core.EventHandler;
import net.bnubot.core.Profile;
import net.bnubot.core.commands.CommandRunnable;
import net.bnubot.db.Account;
import net.bnubot.db.BNLogin;
import net.bnubot.db.Rank;
import net.bnubot.db.conf.DatabaseContext;
import net.bnubot.logging.Out;
import net.bnubot.util.BNetUser;
import net.bnubot.util.UnloggedException;

/**
 * @author scotta
 */
public class LockdownEventHandler extends EventHandler {
	public static final long LOCKDOWN_DURATION = 5 * 60 * 1000; // 5 minutes

	public LockdownEventHandler(Profile profile) {
		super(profile);
		if(DatabaseContext.getContext() == null)
			throw new UnloggedException("Can not enable lockdown without a database!");
		initializeCommands();
	}

	private static boolean commandsInitialized = false;
	public static void initializeCommands() {
		if(commandsInitialized)
			return;
		commandsInitialized = true;

		Profile.registerCommand("lockdown", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				LockdownEventHandler leh = EventHandler.findThis(source, LockdownEventHandler.class);
				if(leh == null) {
					user.sendChat("Failed to locate lockdown module", whisperBack);
					return;
				}
				if(leh.lockdownEnabled) {
					// Lockdown is on; turn it off
					leh.endLockdown(source);
				} else {
					if(leh.lockdownPossible) {
						// Lockdown is off; turn it on
						leh.startLockdown(source);
					} else {
						// Lockdown is not possible
						user.sendChat("Lockdown is not possible; I must be an operator of a Clan channel", whisperBack);
					}
				}
			} });
	}

	// Algorithm 1: single-user floods
	private BNetUser flooder = null;
	private long floodStartTime = 0;
	private long floodActions = 0;
	private static final long SINGLEUSER_ACTIONS = 4;
	private static final long SINGLEUSER_TIME = 500;

	// Algorithm 2: multi-user floods
	private List<Long> multiUserActions = new LinkedList<Long>();
	private static final long MULTIUSER_SIZE_LIMIT = 20;
	private static final long MULTIUSER_TIME_LIMIT = 5000;

	private boolean lockdownPossible = false;
	private boolean lockdownEnabled = false;
	private Connection lockdownThreadSource = null;
	private final Runnable lockdownThread = new Runnable() {
		@Override
		public void run() {
			long lockdownDisableTime = System.currentTimeMillis() + LOCKDOWN_DURATION;
			while(System.currentTimeMillis() < lockdownDisableTime) {
				try { Thread.sleep(3000); } catch (Exception e) {}
				Thread.yield();
			}
			// Timeout period is over; turn off
			endLockdown(lockdownThreadSource);
		}
	};

	private void floodEvent(Connection source, BNetUser user) {
		if(!lockdownPossible)
			return;

		long now = System.currentTimeMillis();

		// Detect multi-user floods
		multiUserActions.add(new Long(now));
		while(multiUserActions.size() > MULTIUSER_SIZE_LIMIT)
			multiUserActions.remove(0);
		long earlyThreshold = now - MULTIUSER_TIME_LIMIT;
		while(multiUserActions.get(0) < earlyThreshold)
			multiUserActions.remove(0);
		if(multiUserActions.size() == MULTIUSER_SIZE_LIMIT) {
			floodDetected(source, user);
			return;
		}

		// Detect single-user floods
		if(now - floodStartTime > SINGLEUSER_TIME) {
			flooder = user;
			floodActions = 1;
			floodStartTime = now;
		} else if(user.equals(flooder)) {
			floodActions++;
			if(floodActions >= SINGLEUSER_ACTIONS)
				floodDetected(source, user);
		}
	}

	private void floodDetected(Connection source, BNetUser user) {
		if(lockdownEnabled)
			return;
		startLockdown(source);
		source.sendChat("/f m Flood detected from " + user.getFullLogonName());

		// Shitlist the user
		try {
			BNLogin bnlUser = BNLogin.get(user);
			if((bnlUser != null) && (bnlUser.getAccount() == null)) {
				// The user has no account; shitlist him!
				Account account = Account.create(user.getFullAccountName(), Rank.getMin(), null);
				bnlUser.setAccount(account);
				bnlUser.updateRow();
			}
		} catch(Exception e) {
			Out.exception(e);
		}
	}

	private void startLockdown(Connection source) {
		if(lockdownEnabled || !lockdownPossible)
			return;
		source.sendChat("/c priv", Integer.MAX_VALUE);
		source.sendChat("Lockdown enabled.");
		lockdownEnabled = true;
		lockdownThreadSource = source;
		new Thread(lockdownThread).start();
	}

	private void endLockdown(final Connection source) {
		if(!lockdownEnabled)
			return;
		source.sendChat("/c pub", Integer.MAX_VALUE);
		source.sendChat("Lockdown disabled.");
		lockdownEnabled = false;
	}

	@Override
	public void joinedChannel(Connection source, String channel) {
		lockdownPossible = false;
		if(lockdownEnabled) {
			source.dispatchRecieveError("Warning, you've left a channel in lockdown!");
			lockdownEnabled = false;
		}
	}

	@Override
	public void channelUser(Connection source, BNetUser user) {
		// Check if lockdown has become possible
		if(!lockdownPossible
		&& source.getProfile().isOneOfMyUsers(user)
		&& ((user.getFlags() & 2) != 0)
		&& (source.getChannel() != null)
		&& source.getChannel().startsWith("Clan ")) {
			lockdownPossible = true;
			source.sendChat("/c pub");
			source.sendChat("/o unigpub");
		}
	}

	@Override
	public void channelJoin(Connection source, BNetUser user) {
		floodEvent(source, user);
	}

	@Override
	public void channelLeave(Connection source, BNetUser user) {
		floodEvent(source, user);
	}

	@Override
	public void recieveChat(Connection source, BNetUser user, String text) {
		floodEvent(source, user);
	}

	@Override
	public void recieveEmote(Connection source, BNetUser user, String text) {
		floodEvent(source, user);
	}
}
