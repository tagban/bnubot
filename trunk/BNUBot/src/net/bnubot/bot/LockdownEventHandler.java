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
import net.bnubot.db.BNLogin;
import net.bnubot.db.Rank;
import net.bnubot.util.BNetUser;
import net.bnubot.util.Out;

/**
 * @author sanderson
 *
 */
public class LockdownEventHandler extends EventHandler {
	private static final String CHANNEL_CLOSED = "The clan channel is now private and only clan members may enter.";
	private static final String CHANNEL_OPEN = "The clan channel is now public and anyone can enter.";

	static {
		Profile.registerCommand("lockdown", new CommandRunnable() {
			@Override
			public void run(Connection source, BNetUser user, String param, String[] params, boolean whisperBack, Account commanderAccount, boolean superUser)
			throws Exception {
				LockdownEventHandler leh = null;
				for(EventHandler eh : source.getEventHandlers())
					if(eh instanceof LockdownEventHandler) {
						leh = (LockdownEventHandler)eh;
						break;
					}
				if(leh == null) {
					user.sendChat("Failed to locate lockdown module", whisperBack);
					return;
				}
				if(leh.lockdownEnabled)
					leh.endLockdown(source);
				else
					leh.startLockdown(source);
			}
			});
	}

	public static final long LOCKDOWN_DURATION = 30 * 1000; // 30 seconds

	private BNetUser flooder = null;
	private long floodStartTime = 0;
	private long floodActions = 0;

	private boolean lockdownEnabled = false;
	private Connection lockdownThreadSource = null;
	private final Thread lockdownThread = new Thread() {
		@Override
		public void run() {
			long lockdownDisableTime = System.currentTimeMillis() + LOCKDOWN_DURATION;
			while(System.currentTimeMillis() < lockdownDisableTime) {
				try { sleep(3000); } catch (Exception e) {}
				yield();
			}
			// Timeout period is over; turn off
			endLockdown(lockdownThreadSource);
		}
	};

	private void floodEvent(Connection source, BNetUser user) {
		long now = System.currentTimeMillis();

		// Expire the flood timer after 500ms
		if(now - floodStartTime > 500) {
			flooder = user;
			floodActions = 1;
			floodStartTime = now;
		} else if(user.equals(flooder)) {
			floodActions++;
			if(floodActions >= 3)
				floodDetected(source, user);
		}
	}

	private void floodDetected(Connection source, BNetUser user) {
		startLockdown(source);
		source.sendChat("/f m Flood detected from " + user.getFullLogonName(), false);

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
		if(lockdownEnabled)
			return;
		source.sendChat("/c priv", false);
	}

	private void endLockdown(final Connection source) {
		if(!lockdownEnabled)
			return;
		source.sendChat("/c pub", false);
	}

	@Override
	public void recieveServerInfo(Connection source, String text) {
		if(lockdownEnabled && CHANNEL_OPEN.equals(text)) {
			lockdownEnabled = false;
			source.sendChat("Lockdown disabled!", false);
			return;
		}

		if(!lockdownEnabled && CHANNEL_CLOSED.equals(text)) {
			lockdownEnabled = true;
			source.sendChat("Lockdown enabled!", false);
			lockdownThreadSource = source;
			lockdownThread.start();
			return;
		}
	}

	@Override
	public void bnetConnected(Connection source) {
		source.sendChat("/c pub", false);
		source.sendChat("/o unigpub", false);
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
