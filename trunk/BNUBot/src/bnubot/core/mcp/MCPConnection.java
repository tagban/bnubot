package bnubot.core.mcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Date;

import bnubot.core.BNetInputStream;
import bnubot.core.RealmConnection;
import bnubot.core.StatString;
import bnubot.util.HexDump;

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

	
	public MCPConnection(int[] MCPChunk1, int ip, int port, int[] MCPChunk2, String uniqueName) {
		this.MCPChunk1 = MCPChunk1;
		this.server = HexDump.DWordToIP(ip);
		this.port = port;
		this.MCPChunk2 = MCPChunk2;
		this.uniqueName = uniqueName;
	}
	
	public void run() {
		try {
			if((MCPChunk1.length != 4) || (MCPChunk2.length != 12))
				throw new Exception("Assertion failed: ((MCPChunk1.length != 4) || (MCPChunk2.length != 12))"); 
			
			System.out.println("Connecting to MCP server " + server + ":" + port);
			
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
		    p.SendPacket(dos, true);
		    
			while(!s.isClosed() && connected) {
				if(dis.available() > 0) {
					MCPPacketReader pr = new MCPPacketReader(dis, true);
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
							p.SendPacket(dos, true);
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
						recieveRealmError(bnubot.util.HexDump.hexDump(pr.data));
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
						
						for(int i = 0; i < numChars; i++) {
							int secs = is.readDWord();
							String charname = is.readNTString();
							int flags = is.readWord();
							StatString statstr = new StatString("PX2D[Realm]," + charname + "," + is.readNTString());
							
							recieveRealmInfo(new Date(secs).toString());
							recieveRealmInfo(charname + " (0x" + Integer.toHexString(flags) + ")");
							recieveRealmInfo(statstr.toString());
						}
						
						break;
					}
					default:
						recieveRealmError("Unknown MCP packet 0x" + Integer.toHexString(pr.packetId) + "\n" + bnubot.util.HexDump.hexDump(pr.data));
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
