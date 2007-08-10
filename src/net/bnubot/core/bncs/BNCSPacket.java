/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.core.bncs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;

import net.bnubot.core.BNetOutputStream;
import net.bnubot.util.HexDump;
import net.bnubot.util.Out;

public class BNCSPacket extends BNetOutputStream {
	byte packetId;
	boolean packetLog;

	public BNCSPacket(byte packetId) {
		super(new ByteArrayOutputStream());
		this.packetId = packetId;
	}
	
	public void SendPacket(OutputStream out, boolean packetLog) throws IOException, SocketException {
		byte data[] = ((ByteArrayOutputStream)this.out).toByteArray();
		//BNCSOutputStream sckout = new BNCSOutputStream(out);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BNetOutputStream sckout = new BNetOutputStream(baos);
		
		if(packetId == BNCSCommandIDs.SID_CHATCOMMAND) {
			if(data.length > 0xFB) {
				Out.error(this.getClass().getName(), "Chat command is too long; ignoring.");
				return;
			}
			if(data[data.length-1] != 0x00) {
				Out.error(this.getClass().getName(), "Chat command is not null terminated; ignoring.");
				return;
			}
		}
		
		try {
			sckout.writeByte(0xFF);
			sckout.writeByte(packetId);
			sckout.writeWord(data.length + 4);
			sckout.write(data);
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		data = baos.toByteArray();
		
		if(packetLog)
			Out.info(this.getClass().getName(), "SEND\n" + HexDump.hexDump(data));
		
		out.write(data);
		out.flush();
	}
}
