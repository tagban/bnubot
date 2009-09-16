/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.bncs;

import java.io.IOException;
import java.io.OutputStream;

import net.bnubot.core.bnls.BNLSConnection;
import net.bnubot.core.bnls.BNLSManager;
import net.bnubot.util.BNetInputStream;

/**
 * @author scotta
 */
public class BNCSWarden {
	private final BNCSConnection c;

	public BNCSWarden(BNCSConnection c, byte[] seed) throws IOException {
		this.c = c;

		BNLSConnection bnls = BNLSManager.getWardenConnection();
		int cookie_out = c.getConnectionSettings().botNum;
		BNetInputStream is = bnls.sendWarden0(
				cookie_out,
				c.getProductID().getDword(),
				seed);

		byte u = is.readByte();
		int cookie = is.readDWord();
		byte result = is.readByte();
		short dataLen = is.readWord();
		if(u != 0)
			throw new IOException("wrong useage");
		if(cookie != cookie_out)
			throw new IOException("wrong cookie");
		if(result != 0)
			throw new IOException("server failed to initialize cookie");
		if(dataLen != 0)
			throw new IOException("dataLen != 0 (" + dataLen + ")");
	}

	public void processWardenPacket(byte[] payload, OutputStream os) throws IOException {
		BNLSConnection bnls = BNLSManager.getWardenConnection();
		int cookie_out = c.getConnectionSettings().botNum;
		BNetInputStream is = bnls.sendWarden1(cookie_out, payload);

		byte u = is.readByte();
		int cookie = is.readDWord();
		byte result = is.readByte();
		short dataLen = is.readWord();
		byte[] data = new byte[dataLen];
		is.read(data);

		if(u != 1)
			throw new IOException("wrong useage");
		if(cookie != cookie_out)
			throw new IOException("wrong cookie");
		if(result != 0)
			throw new IOException("server failed to process payload [" + new String(data) + "]");
		if(dataLen == 0)
			throw new IOException("dataLen == 0");

		// Everything looks okay; forward the payload to bnet
		BNCSPacket out = new BNCSPacket(c, BNCSPacketId.SID_WARDEN);
		out.write(data);
		out.sendPacket(os);
	}
}
