/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.core.bncs;

public interface BNCSChatEventIDs {
	public static final byte EID_SHOWUSER				= (byte)0x01;
	public static final byte EID_JOIN					= (byte)0x02;
	public static final byte EID_LEAVE					= (byte)0x03;
	public static final byte EID_WHISPER				= (byte)0x04;
	public static final byte EID_TALK					= (byte)0x05;
	public static final byte EID_BROADCAST				= (byte)0x06;
	public static final byte EID_CHANNEL				= (byte)0x07;
	public static final byte EID_USERFLAGS				= (byte)0x09;
	public static final byte EID_WHISPERSENT			= (byte)0x0A;
	public static final byte EID_CHANNELFULL			= (byte)0x0D;
	public static final byte EID_CHANNELDOESNOTEXIST	= (byte)0x0E;
	public static final byte EID_CHANNELRESTRICTED		= (byte)0x0F;
	public static final byte EID_INFO					= (byte)0x12;
	public static final byte EID_ERROR					= (byte)0x13;
	public static final byte EID_EMOTE					= (byte)0x17;
}
