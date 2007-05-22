package core.bncs;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Date;

import BNLSProtocol.OutPacketBuffer;
import Hashing.CheckRevision;
import Hashing.CheckRevisionBNLS;
import Hashing.SCKeyDecode;

import sun.misc.HexDumpEncoder;
import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;
import util.Constants;

import core.Connection;
import core.ConnectionSettings;
import core.EventHandler;

public class BNCSConnection extends Connection {
	Socket s = null;
	DataInputStream dis = null;
	DataOutputStream dos = null;
	boolean connected = false;
	
	public BNCSConnection(ConnectionSettings cs, EventHandler e) {
		super(cs, e);
	}
	
	public void run() {
		System.out.println("BNCSConnection running");
		
		if(cs.autoconnect)
			Connect();
		
		System.out.println("BNCSConnection terminated");
	}

	public void Connect() {
		try {
			s = new Socket(cs.server, cs.port);
			dis = new DataInputStream(s.getInputStream());
			dos = new DataOutputStream(s.getOutputStream());
			
			connected = true;
			
			//dos.write("GET / HTTP/1.0\n\n".getBytes());
			
			// Game
			dos.writeByte(0x01);
			dos.flush();
			
			BNCSPacket p = new BNCSPacket(BNCSCommandIDs.SID_AUTH_INFO);
			p.writeDWord(0);		// Protocol ID (0)
			p.writeDWord("IX86");	// Platform ID (IX86)
			p.writeDWord("STAR");	// Product ID (STAR)
			p.writeDWord(0xCF);		// Version byte
			p.writeDWord(0);		// Product language
			p.writeDWord(0);		// Local IP
			p.writeDWord(0);		// TZ bias
			p.writeDWord(0);		// Locale ID
			p.writeDWord(0);		// Language ID
			p.writeNTString("USA");	// Country abreviation
			p.writeNTString("United States");	// Country
			p.SendPacket(dos);
			dos.flush();
			
			while(s.isConnected()) {
				if(dis.available() > 0) {
					BNCSPacketReader pr = new BNCSPacketReader(dis);
					BNCSInputStream is = pr.getData();
					
					switch(pr.packetId) {
					case BNCSCommandIDs.SID_PING:
						p = new BNCSPacket(BNCSCommandIDs.SID_PING);
						p.writeDWord(is.readDWord());
						p.SendPacket(dos);
						break;
					case BNCSCommandIDs.SID_AUTH_INFO:
						int logonType = is.readDWord();
						int serverToken = is.readDWord();
						int udpValue = is.readDWord();
						int MPQFiletime = is.readDWord();
						MPQFiletime = is.readDWord();
						String IX86ver = is.readNTString();
						String ValueStr = is.readNTString();
						
						// Hash the CD key
						int clientToken = 0;
						Hashing.SCKeyDecode keydecode = new SCKeyDecode(cs.cdkey);
						
						// Hash the game files
						int mpqNum = Integer.parseInt(IX86ver.substring(IX86ver.indexOf("IX86")+5).substring(0,2));
						String dir = "/Users/scott/Documents/Hashes";
				    	String[][] files = {
				          //{dir+"/WAR3/war3.exe",            dir+"/WAR3/storm.dll",    dir+"/WAR3/game.dll"},
					      //{dir+"/W2BN/Warcraft II BNE.exe", dir+"/W2BN/storm.dll",    dir+"/W2BN/battle.snp"},
					      {dir+"/STAR/starcraft.exe",       dir+"/STAR/storm.dll",    dir+"/STAR/battle.snp"}
					      //{dir+"/D2DV/game.exe",            dir+"/D2DV/Bnclient.dll", dir+"/D2DV/D2Client.dll"},
					      //{dir+"/D2XP/game.exe",            dir+"/D2XP/Bnclient.dll", dir+"/D2XP/D2Client.dll"},
					      //{dir+"/JSTR/starcraftj.exe",      dir+"/JSTR/storm.dll",    dir+"/JSTR/battle.snp"}
				        };
				        
				    	OutPacketBuffer exeHash = CheckRevisionBNLS.checkRevision(ValueStr, (int)Constants.PRODUCT_STARCRAFT, mpqNum);
						util.HexDump.hexDump(exeHash.removeBytes(exeHash.size()));
				    	
						// Respond
						p = new BNCSPacket(BNCSCommandIDs.SID_AUTH_CHECK);
						p.writeDWord(clientToken);
						//p.writeDWord(exeVersion);
						//p.writeDWord(exeHash);
						p.writeDWord(1);			// Number of keys
						p.writeDWord(0);			// Spawn?
						
						//For each key..
						p.writeDWord(cs.cdkey.length());
						p.writeDWord(keydecode.getProduct());
						p.writeDWord(keydecode.getVal1());
						p.writeDWord(keydecode.getVal2());
						int hash[] = keydecode.getKeyHash(clientToken, serverToken);
						p.writeDWord(hash[0]);
						p.writeDWord(hash[1]);
						p.writeDWord(hash[2]);
						p.writeDWord(hash[3]);
						p.writeDWord(hash[4]);
						
						//Finally,
						//p.writeNTString(exeInfo);
						p.writeNTString(cs.username);
						
						keydecode.getKeyHash(clientToken, serverToken);
						keydecode.getProduct();
						keydecode.getVal1();
						keydecode.getVal2();
						
						break;
					default:
						System.err.println("Unknown command ID!");
						break;
					}
				}
				
				yield();
			}
			
			Disconnect();
			s.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void Disconnect() {
		connected = false;
	}

}
