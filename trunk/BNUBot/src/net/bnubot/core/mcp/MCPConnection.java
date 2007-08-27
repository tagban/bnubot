/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.core.mcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Date;

import net.bnubot.core.RealmConnection;
import net.bnubot.util.BNetInputStream;
import net.bnubot.util.HexDump;
import net.bnubot.util.Out;
import net.bnubot.util.StatString;
import net.bnubot.util.TimeFormatter;

public class MCPConnection extends RealmConnection {
	protected int[] MCPChunk1;
	protected String server;
	protected int port;
	protected int[] MCPChunk2;
	protected String uniqueName;
	
	protected Socket s;
	protected String realm;
	protected int serverToken;
	protected DataInputStream dis = null;
	protected DataOutputStream dos = null;
	protected boolean connected = false;
	protected boolean packetLog;
	
	public MCPConnection(int[] MCPChunk1, int ip, int port, int[] MCPChunk2, String uniqueName, boolean packetLog) {
		this.MCPChunk1 = MCPChunk1;
		this.server = HexDump.DWordToIP(ip);
		this.port = port;
		this.MCPChunk2 = MCPChunk2;
		this.uniqueName = uniqueName;
		this.packetLog = packetLog;
	}
	
	public void run() {
		try {
			if((MCPChunk1.length != 4) || (MCPChunk2.length != 12))
				throw new Exception("Assertion failed: ((MCPChunk1.length != 4) || (MCPChunk2.length != 12))"); 
			
			Out.info(MCPConnection.class, "Connecting to MCP server " + server + ":" + port);
			
			s = new Socket(server, port);
			dis = new DataInputStream(s.getInputStream());
			dos = new DataOutputStream(s.getOutputStream());
			
			connected = true;
			
			//MCP
			dos.writeByte(1);
			
			MCPPacket p = new MCPPacket(MCPCommandIDs.MCP_STARTUP);
			for(int i = 0; i < 4; i++)
				p.writeDWord(MCPChunk1[i]);
			for(int i = 0; i < 12; i++)
				p.writeDWord(MCPChunk2[i]);
			p.writeNTString(uniqueName);
		    p.SendPacket(dos, packetLog);
		    
			while(!s.isClosed() && connected) {
				if(dis.available() > 0) {
					MCPPacketReader pr = new MCPPacketReader(dis, packetLog);
					BNetInputStream is = pr.getData();
					switch(pr.packetId) {
					case MCPCommandIDs.MCP_STARTUP: {
						/* (DWORD)		 Result
						 * 
						 * 0x00: Success
						 * 0x0C: No Battle.net connection detected
						 * 0x7F: Temporary IP ban "Your connection has been
						 *  temporarily restricted from this realm. Please
						 *  try to log in at another time"
						 */
						int result = is.readDWord();
						switch(result) {
						case 0:
							recieveRealmInfo("Realm logon success");
							
							p = new MCPPacket(MCPCommandIDs.MCP_CHARLIST2);
							p.writeDWord(8);	//Nubmer of chars to list
							p.SendPacket(dos, packetLog);
							break;
						case 0x0C:
							recieveRealmError("Realm server did not detect a Battle.net connection");
							setConnected(false);
							break;
						case 0x7F:
							recieveRealmError("You are temporarily banned from the realm");
							setConnected(false);
							break;
						}
						break;
					}
					
					case MCPCommandIDs.MCP_CHARLIST2: {
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
						
						long minTime = 0;
						String maxCharname = null;
						
						for(int i = 0; i < numChars; i++) {
							int secs = is.readDWord();
							String charname = is.readNTString();
							StatString statstr = new StatString("PX2D[Realm]," + charname + "," + is.readNTString());
							
							long time = new Date().getTime();
							time = (((long)secs) * 1000) - time;
							
							recieveRealmInfo(TimeFormatter.formatTime(time) + " - " + charname + " - " + statstr.toString());
							
							if(((minTime > time) || (minTime == 0)) && (time >= 0)) {
								minTime = time;
								maxCharname = charname;
							}
						}
						
						if(maxCharname != null) {
							p = new MCPPacket(MCPCommandIDs.MCP_CHARLOGON);
							p.writeNTString(maxCharname);
							p.SendPacket(dos, packetLog);
						}
						
						break;
					}
					
					case MCPCommandIDs.MCP_CHARLOGON: {
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
						recieveRealmError("Unknown MCP packet 0x" + Integer.toHexString(pr.packetId) + "\n" + HexDump.hexDump(pr.data));
						break;
					}
				} else {
					sleep(10);
					yield();
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
