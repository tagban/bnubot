/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.mcp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import net.bnubot.core.RealmConnection;
import net.bnubot.logging.Out;
import net.bnubot.util.BNetInputStream;
import net.bnubot.util.BNetOutputStream;
import net.bnubot.util.StatString;
import net.bnubot.util.crypto.HexDump;

/**
 * @author scotta
 */
public class MCPConnection extends RealmConnection {
	protected int[] MCPChunk1;
	protected String server;
	protected int port;
	protected int[] MCPChunk2;
	protected String uniqueName;

	protected Socket s;
	protected int serverToken;
	protected BNetInputStream dis = null;
	protected DataOutputStream dos = null;
	protected boolean connected = false;

	public MCPConnection(int[] MCPChunk1, int ip, int port, int[] MCPChunk2, String uniqueName) {
		this.MCPChunk1 = MCPChunk1;
		this.server = HexDump.DWordToIP(ip);
		this.port = port;
		this.MCPChunk2 = MCPChunk2;
		this.uniqueName = uniqueName;
	}

	@Override
	public void run() {
		try {
			if((MCPChunk1.length != 4) || (MCPChunk2.length != 12))
				throw new Exception("Assertion failed: ((MCPChunk1.length != 4) || (MCPChunk2.length != 12))");

			Out.info(MCPConnection.class, "Connecting to MCP server " + server + ":" + port);

			s = new Socket(server, port);
			dis = new BNetInputStream(s.getInputStream());
			dos = new DataOutputStream(s.getOutputStream());

			connected = true;

			//MCP
			dos.writeByte(1);

			try (MCPPacket p = new MCPPacket(MCPPacketID.MCP_STARTUP)) {
				for(int i = 0; i < 4; i++)
					p.writeDWord(MCPChunk1[i]);
				for(int i = 0; i < 12; i++)
					p.writeDWord(MCPChunk2[i]);
				p.writeNTString(uniqueName);
				p.sendPacket(dos);
			}

			while(!s.isClosed() && connected) {
				if(dis.available() > 0) {
					MCPPacketReader pr = new MCPPacketReader(dis);
					BNetInputStream is = pr.getData();
					switch(pr.packetId) {
					case MCP_STARTUP: {
						/* (DWORD)		 Result
						 *
						 * 0x00: Success
						 * 0x02, 0x0A-0x0D: Realm Unavailable: No Battle.net connection detected.
						 * 0x7E: CDKey banned from realm play.
						 * 0x7F: Temporary IP ban "Your connection has been
						 *  temporarily restricted from this realm. Please
						 *  try to log in at another time"
						 */
						int result = is.readDWord();
						switch(result) {
						case 0:
							recieveRealmInfo("Realm logon success");

							try (MCPPacket p = new MCPPacket(MCPPacketID.MCP_CHARLIST2)) {
								p.writeDWord(8);	//Nubmer of chars to list
								p.sendPacket(dos);
							}
							break;
						case 0x02:
						case 0x0A:
						case 0x0B:
						case 0x0C:
						case 0x0D:
							recieveRealmError("Realm server did not detect a Battle.net connection");
							setConnected(false);
							break;
						case 0x7E:
							recieveRealmError("Your CD-Key is banned from realm play");
							setConnected(false);
							break;
						case 0x7F:
							recieveRealmError("You are temporarily banned from the realm");
							setConnected(false);
							break;
						}
						break;
					}

					case MCP_CHARLIST2: {
						/* (WORD)		 Number of characters requested
						 * (DWORD)		 Number of characters that exist on this account
						 * (WORD)		 Number of characters returned
						 *
						 * For each character:
						 * (DWORD)		 Seconds since January 1 00:00:00 UTC 1970
						 * (STRING) 	 Name
						 * (WORD)		 Flags
						 * (STRING) 	 Character statstring
						 */
						is.readWord();
						is.readDWord();
						int numChars = is.readWord();

						List<MCPCharacter> chars = new ArrayList<MCPCharacter>(numChars);

						for(int i = 0; i < numChars; i++) {
							final long time = (1000L * is.readDWord()) - System.currentTimeMillis();
							final String name = is.readNTString();

							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							byte[] data = new byte[33];
							is.read(data);
							if(is.readByte() != 0)
								throw new Exception("invalid statstr format\n" + HexDump.hexDump(baos.toByteArray()));

							try (BNetOutputStream bos = new BNetOutputStream(baos)) {
								bos.write(("PX2D[Realm]," + name + ",").getBytes());
								bos.write(data);
								bos.writeByte(0);
							}
							final StatString statstr = new StatString(new BNetInputStream(new ByteArrayInputStream(baos.toByteArray())));

							MCPCharacter c = new MCPCharacter(time, name, statstr);

							if(Out.isDebug(getClass()))
								Out.debugAlways(getClass(), c.toString());

							chars.add(c);
						}

						recieveCharacterList(chars);
						break;
					}

					case MCP_CHARLOGON: {
						/* (DWORD) Result
						 *
						 * 0x00: Success
						 * 0x46: Player not found
						 * 0x7A: Logon failed
						 * 0x7B: Character expired
						 */
						int result = is.readDWord();
						switch(result) {
						case 0x00:
							recieveRealmInfo("Character logon success");
							break;
						case 0x46:
							recieveRealmError("Player not found");
							break;
						case 0x7A:
							recieveRealmError("Character logon failed");
							break;
						case 0x7B:
							recieveRealmError("Character expired");
							break;
						default:
							recieveRealmError("Unknown MCP_CHARLOGON result: 0x" + Integer.toHexString(result));
							break;
						}
						break;
					}

					default:
						recieveRealmError("Unknown packet " + pr.packetId.name() + "\n" + HexDump.hexDump(pr.data));
						break;
					}
				} else {
					sleep(200);
					yield();
				}
			}

		} catch (Exception e) {
			Out.fatalException(e);
		}
	}

	@Override
	public void sendLogonCharacter(String c) {
		try (MCPPacket p = new MCPPacket(MCPPacketID.MCP_CHARLOGON)) {
			p.writeNTString(c);
			p.sendPacket(dos);
		} catch(Exception e) {
			Out.exception(e);
			setConnected(false);
		}
	}
}
