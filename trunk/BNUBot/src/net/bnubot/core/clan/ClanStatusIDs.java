/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.clan;

/**
 * @author scotta
 */
public interface ClanStatusIDs {
	public static final byte CLANSTATUS_SUCCESS				= (byte)0x00;
	public static final byte CLANSTATUS_IN_USE				= (byte)0x01;
	public static final byte CLANSTATUS_TOO_SOON			= (byte)0x02;
	public static final byte CLANSTATUS_NOT_ENOUGH_MEMBERS	= (byte)0x03;
	public static final byte CLANSTATUS_INVITATION_DECLINED	= (byte)0x04;
	public static final byte CLANSTATUS_DECLINE				= (byte)0x05;
	public static final byte CLANSTATUS_ACCEPT				= (byte)0x06;
	public static final byte CLANSTATUS_NOT_AUTHORIZED		= (byte)0x07;
	public static final byte CLANSTATUS_USER_NOT_FOUND		= (byte)0x08;
	public static final byte CLANSTATUS_CLAN_IS_FULL		= (byte)0x09;
	public static final byte CLANSTATUS_BAD_TAG				= (byte)0x0A;
	public static final byte CLANSTATUS_BAD_NAME			= (byte)0x0B;
}
