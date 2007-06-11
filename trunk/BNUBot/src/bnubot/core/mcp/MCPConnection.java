package bnubot.core.mcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.security.InvalidParameterException;
import java.util.Date;

import bnubot.core.BNetInputStream;
import bnubot.core.RealmConnection;
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
			p.writeDWord(MCPChunk1[0]);
			p.writeDWord(MCPChunk1[1]);
			p.writeDWord(MCPChunk1[2]);
			p.writeDWord(MCPChunk1[3]);
			p.writeDWord(MCPChunk2[0]);
			p.writeDWord(MCPChunk2[1]);
			p.writeDWord(MCPChunk2[2]);
			p.writeDWord(MCPChunk2[3]);
			p.writeDWord(MCPChunk2[4]);
			p.writeDWord(MCPChunk2[5]);
			p.writeDWord(MCPChunk2[6]);
			p.writeDWord(MCPChunk2[7]);
			p.writeDWord(MCPChunk2[8]);
			p.writeDWord(MCPChunk2[9]);
			p.writeDWord(MCPChunk2[10]);
			p.writeDWord(MCPChunk2[11]);
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
							recieveInfo("Realm logon success");
							break;
						case 0x0C:
							recieveError("Realm server did not detect a Battle.net connection");
							setConnected(false);
							break;
						case 0x7F:
							recieveError("You are temporarily banned from the realm");
							setConnected(false);
							break;
						}
						break;
					}
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
