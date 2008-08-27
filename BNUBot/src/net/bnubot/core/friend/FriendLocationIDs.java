/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.friend;

/**
 * @author scotta
 */
public interface FriendLocationIDs {
	public static final byte FRIENDLOCATION_OFFLINE							= (byte)0x00;
	public static final byte FRIENDLOCATION_NOT_IN_CHAT						= (byte)0x01;
	public static final byte FRIENDLOCATION_IN_CHAT							= (byte)0x02;
	public static final byte FRIENDLOCATION_IN_A_PUBLIC_GAME				= (byte)0x03;
	public static final byte FRIENDLOCATION_IN_A_PRIVATE_GAME_NOT_MUTUAL	= (byte)0x04;
	public static final byte FRIENDLOCATION_IN_A_PRIVATE_GAME_MUTUAL		= (byte)0x05;
}
