/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot;

import net.bnubot.core.Connection;
import net.bnubot.core.EventHandler;
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
	public static final long LOCKDOWN_DURATION = 30 * 1000; // 30 seconds

	private BNetUser flooder = null;
	private long floodStartTime = 0;
	private long floodActions = 0;

	private boolean lockdownEnabled = false;
	private long lockdownDisableTime = 0;

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

	private void floodDetected(final Connection source, BNetUser user) {
		source.dispatchRecieveDebug("Enabling channel lockdown due to flood detection from " + user.getFullLogonName());
		source.sendChat(null, "/c priv", false, false);
		source.sendChat("/f m Flood detected from " + user.getFullLogonName(), false);

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


		new Thread() {
			@Override
			public void run() {
				lockdownDisableTime = System.currentTimeMillis() + LOCKDOWN_DURATION;
				while(System.currentTimeMillis() < lockdownDisableTime) {
					try { sleep(3000); } catch (Exception e) {}
					yield();
				}
				// Timeout period is over; turn off
				source.sendChat(null, "/c pub", false, false);
			}
		}.start();
	}

	@Override
	public void recieveServerInfo(Connection source, String text) {
		if(lockdownEnabled && "The clan channel is now public and anyone can enter.".equals(text)) {
			lockdownEnabled = false;
			source.sendChat("Lockdown disabled!", false);
			return;
		}

		if(!lockdownEnabled && "The clan channel is now private and only clan members may enter.".equals(text)) {
			lockdownEnabled = true;
			source.sendChat("Lockdown enabled! Auto-disabling in 30 seconds.", false);
			lockdownDisableTime = System.currentTimeMillis() + LOCKDOWN_DURATION;
			return;
		}
	}

	@Override
	public void bnetConnected(Connection source) {
		source.sendChat("/c pub", false);
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
