/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.clan;

/**
 * @author scotta
 */
public interface ClanRankIDs {
	public static final byte CLANRANK_INITIATE	= (byte)0x00;
	public static final byte CLANRANK_PEON		= (byte)0x01;
	public static final byte CLANRANK_GRUNT		= (byte)0x02;
	public static final byte CLANRANK_SHAMAN	= (byte)0x03;
	public static final byte CLANRANK_CHIEFTAIN	= (byte)0x04;

	public static final String ClanRank[]		= {
		"Initiate",
		"Peon",
		"Grunt",
		"Shaman",
		"Chieftain"
	};
}
