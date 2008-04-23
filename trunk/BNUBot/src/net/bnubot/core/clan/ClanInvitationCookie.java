/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.clan;

import net.bnubot.core.bncs.BNCSConnection;

/**
 * @author sanderson
 *
 */
public class ClanInvitationCookie {
	private final BNCSConnection source;
	private final int cookie;
	public final int clanTag;
	public final String clanName;
	public final String inviter;

	public ClanInvitationCookie(BNCSConnection source, int cookie, int clanTag, String clanName, String inviter) {
		this.source = source;
		this.cookie = cookie;
		this.clanTag = clanTag;
		this.clanName = clanName;
		this.inviter = inviter;
	}

	public void accept() throws Exception {
		source.sendClanInvitationResponse(cookie, clanTag, inviter, 0x06);
	}

	public void decline() throws Exception {
		source.sendClanInvitationResponse(cookie, clanTag, inviter, 0x04);
	}
}
