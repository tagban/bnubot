/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;
import net.bnubot.core.bncs.IconIDs;
import net.bnubot.core.bncs.ProductIDs;
import net.bnubot.util.crypto.HexDump;

/**
 * @author scotta
 */
public class StatStringTest extends TestCase {
	public void testSC() {
		StatString ss = new StatString("RATS 1 2 3 0 5 6 7 8 1D3W");
		/*Starcraft, Starcraft Japanese, Brood War, and Warcraft II
		These products use the same format for their statstrings as Diablo. Most of these fields are usually 0 and their meanings are not known.
		1 	Ladder Rating 	The user's current ladder rating.
		2 	Ladder rank 	The player's rank on the ladder.
		3 	Wins 	The number of wins the user has in normal games.
		4 	Spawned 	This field is 1 if the user's client is spawned, 0 otherwise.
		5 	Unknown
		6 	High Ladder Rating 	This is the user's 'highest ever' ladder rating.
		7 	Unknown
		8 	Unknown
		9 	Icon 	This value should be matched against the product values of each icon in each Battle.net Icon file that is loaded. If a match is found, the client should use this icon when displaying the user.*/
		assertEquals(6, ss.getHighLadderRating());
		assertEquals(IconIDs.ICON_W3D1, ss.getIcon());
		assertEquals(2, ss.getLadderRank());
		assertEquals(1, ss.getLadderRating());
		assertEquals(ProductIDs.STAR, ss.getProduct());
		assertEquals(false, ss.getSpawn());
		assertEquals(3, ss.getWins().intValue());

		ss = new StatString("RATS 1 2 3 1 5 6 7 8 9");
		assertEquals(ss.getSpawn(), true);
	}

	private byte[] append(byte[] arg0, byte[] arg1) {
		byte[] ret = new byte[arg0.length + arg1.length];
		int i = 0;
		for(byte b : arg0)
			ret[i++] = b;
		for(byte b : arg1)
			ret[i++] = b;
		return ret;
	}

	public void testD2() throws Exception {
		String asdf = "84 80 53 02 02 02 02 0F " +
		"FF 50 02 02 FF 02 FF FF " +
		"FF FF FF 4C FF FF FF FF " +
		"FF 14 E8 84 FF FF 01 FF FF";
		asdf = asdf.replace(" ", "");
		byte[] data = HexDump.decode(asdf);
		data = append("PX2DUSEast,EsO-SILenTNiGhT,".getBytes(), data);
		data = append(data, new byte[] {0});

		StatString ss = new StatString(new BNetInputStream(new ByteArrayInputStream(data)));
		assertEquals(20, ss.getCharLevel().intValue());
		assertEquals(ProductIDs.D2XP, ss.getProduct());
	}

	public void testW3() {
		StatString ss = new StatString("3RAW 1R3W 1 UNB");
		assertEquals(IconIDs.ICON_W3R1, ss.getIcon());
		assertEquals(1, ss.getLevel().intValue());
		assertEquals(ProductIDs.WAR3, ss.getProduct());
	}
}
