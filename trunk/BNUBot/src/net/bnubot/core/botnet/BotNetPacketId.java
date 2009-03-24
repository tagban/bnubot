/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.botnet;

import net.bnubot.core._super._PacketId;

/**
 * @author scotta
 */
public enum BotNetPacketId implements _PacketId<BotNetConnection> {
	PACKET_IDLE,
	PACKET_LOGON,
	PACKET_STATSUPDATE,
	PACKET_DATABASE,
	PACKET_MESSAGE,
	PACKET_CYCLE,
	PACKET_USERINFO,
	PACKET_USERLOGGINGOFF,
	PACKET_COMMAND,
	PACKET_CHANGEDBPASSWORD,
	PACKET_BOTNETVERSION,
	PACKET_BOTNETCHAT,
	PACKET_UNKNOWN_0x0C,
	PACKET_ACCOUNT,
	PACKET_CHATDROPOPTIONS
}
