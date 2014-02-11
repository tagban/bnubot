/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.mcp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import net.bnubot.logging.Out;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetOutputStream;
import net.bnubot.util.crypto.HexDump;

/**
 * @author scotta
 */
public class MCPPacket extends BNetOutputStream {
	MCPPacketID packetId;

	public MCPPacket(MCPPacketID packetId) {
		super(new ByteArrayOutputStream());
		this.packetId = packetId;
	}

	public void sendPacket(OutputStream out) {
		byte data[] = ((ByteArrayOutputStream)this.out).toByteArray();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try (BNetOutputStream sckout = new BNetOutputStream(baos)) {
			sckout.writeWord(data.length + 3);
			sckout.writeByte(packetId.ordinal());
			sckout.write(data);
		} catch(IOException e) {
			Out.fatalException(e);
		}

		data = baos.toByteArray();

		if(GlobalSettings.packetLog) {
			String msg = "SEND " + packetId.name();
			if(Out.isDebug())
				msg += "\n" + HexDump.hexDump(data);
			Out.debugAlways(getClass(), msg);
		}

		try {
			out.write(data);
			out.flush();
		} catch (Exception e) {
			Out.fatalException(e);
		}
	}
}
