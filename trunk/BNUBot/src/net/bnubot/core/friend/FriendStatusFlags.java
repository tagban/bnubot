/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.friend;

/**
 * @author scotta
 */
public interface FriendStatusFlags {
	public static final byte FRIENDSTATUS_MUTUAL	= (byte)0x01;
	public static final byte FRIENDSTATUS_DND		= (byte)0x02;
	public static final byte FRIENDSTATUS_AWAY		= (byte)0x04;
}
