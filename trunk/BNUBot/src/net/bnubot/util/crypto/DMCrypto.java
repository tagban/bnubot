/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util.crypto;

import net.bnubot.util.Out;

/**
 * @author scotta
 *
 */
public class DMCrypto {
	public static byte[] decode(byte[] data) {
		Out.debugAlways(DMCrypto.class, "\n" + HexDump.hexDump(data));
		return data;
	}

	public static byte[] encode(byte[] data) {
		return decode(data);
	}
}
