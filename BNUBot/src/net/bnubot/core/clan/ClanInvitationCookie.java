/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.clan;

import net.bnubot.core.AcceptOrDecline;
import net.bnubot.core.bncs.BNCSConnection;
import net.bnubot.util.crypto.HexDump;

/**
 * @author scotta
 */
public class ClanInvitationCookie implements AcceptOrDecline {
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

		source.dispatchRecieveInfo("You were invited to join Clan " + HexDump.DWordToPretty(clanTag) + " (" + clanName + ") by " + inviter);
		source.dispatchRecieveInfo("Type /accept or /decline to respond.");
	}

	@Override
	public void accept() throws Exception {
		source.sendClanInvitationResponse(cookie, clanTag, inviter, 0x06);
		source.dispatchRecieveInfo("You have accepted the invitation to join Clan " + HexDump.DWordToPretty(clanTag));
	}

	@Override
	public void decline() throws Exception {
		source.sendClanInvitationResponse(cookie, clanTag, inviter, 0x04);
		source.dispatchRecieveInfo("You have declined the invitation to join Clan " + HexDump.DWordToPretty(clanTag));
	}
}
