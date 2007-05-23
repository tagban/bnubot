package core.bncs;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.Random;

import BNLSProtocol.OutPacketBuffer;
import Hashing.CheckRevision;
import Hashing.CheckRevisionBNLS;
import Hashing.HashMain;
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

	// These are for BNLS/JBLS
    private static final byte PRODUCT_STARCRAFT         = 0x01; //Fully supported
    private static final byte PRODUCT_BROODWAR          = 0x02; //Fully Supported
    private static final byte PRODUCT_WAR2BNE           = 0x03; //Fully Supported
    private static final byte PRODUCT_DIABLO2           = 0x04; //Fully Supported
    private static final byte PRODUCT_LORDOFDESTRUCTION = 0x05; //Fully Supported
    private static final byte PRODUCT_JAPANSTARCRAFT    = 0x06; //Fully Supported
    private static final byte PRODUCT_WARCRAFT3         = 0x07; //Fully Supported
    private static final byte PRODUCT_THEFROZENTHRONE   = 0x08; //Fully Supported
    private static final byte PRODUCT_DIABLO            = 0x09; //Fully Supported
    private static final byte PRODUCT_DIABLOSHAREWARE   = 0x0A; //Fully Supported
    private static final byte PRODUCT_STARCRAFTSHAREWARE= 0x0B; //Fully Supported
	
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
			
			int verByte = 0xd1; //HashMain.getVerByte(PRODUCT_STARCRAFT);
			
			BNCSPacket p = new BNCSPacket(BNCSCommandIDs.SID_AUTH_INFO);
			p.writeDWord(0);		// Protocol ID (0)
			p.writeDWord("IX86");	// Platform ID (IX86)
			p.writeDWord("STAR");	// Product ID (STAR)
			p.writeDWord(verByte);	// Version byte
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
					case BNCSCommandIDs.SID_NULL:
						p = new BNCSPacket(BNCSCommandIDs.SID_NULL);
						p.SendPacket(dos);
						System.out.println("SEND/RECV NULL");
						break;
						
					case BNCSCommandIDs.SID_PING:
						p = new BNCSPacket(BNCSCommandIDs.SID_PING);
						p.writeDWord(is.readDWord());
						p.SendPacket(dos);
						break;
						
					case BNCSCommandIDs.SID_AUTH_INFO:
						int logonType = is.readDWord();
						int serverToken = is.readDWord();
						int udpValue = is.readDWord();
						long MPQFileTime = is.readQWord();
						String MPQFileName = is.readNTString();
						String ValueStr = is.readNTString();
						assert(is.available() == 0);
						
						
						// Hash the CD key
						int clientToken = Math.abs(new Random().nextInt());
						byte keyHash[] = HashMain.hashKey(clientToken, serverToken, cs.cdkey).getBuffer();
						
						// Hash the game files
						//int mpqNum = Integer.parseInt(IX86ver.substring(IX86ver.indexOf("IX86")+5).substring(0,2));
				        
				    	OutPacketBuffer exeHashBuf = CheckRevisionBNLS.checkRevision(ValueStr, PRODUCT_STARCRAFT, MPQFileName, MPQFileTime);
				    	BNCSInputStream exeStream = new BNCSInputStream(new ByteArrayInputStream(exeHashBuf.getBuffer()));
				    	exeStream.skipBytes(3);
				    	int success = exeStream.readDWord();
				    	if(success != 1) {
				    		System.err.println(util.HexDump.hexDump(exeHashBuf.getBuffer()));
				    		throw new Exception("BNLS failed to complete 0x1A sucessfully");
				    	}
				    	int exeVersion = exeStream.readDWord();
				    	int exeHash = exeStream.readDWord();
				    	String exeInfo = exeStream.readNTString();
				    	exeStream.readDWord(); // cookie
				    	int exeVerbyte = exeStream.readDWord();
				    	assert(exeStream.available() == 0);
				    	
				    	// Alternatively,
				    	//int exeVersion2 = HashMain.getExeVer(PRODUCT_STARCRAFT);
						//String exeInfo2 = HashMain.getExeInfo(PRODUCT_STARCRAFT);

						// Respond
						p = new BNCSPacket(BNCSCommandIDs.SID_AUTH_CHECK);
						p.writeDWord(clientToken);
						p.writeDWord(exeVersion);
						p.writeDWord(exeHash);
						p.writeDWord(1);			// Number of keys
						p.writeDWord(0);			// Spawn?
						
						//For each key..
						assert(keyHash.length == 36);
						p.write(keyHash);
						
						//Finally,
						p.writeNTString(exeInfo);
						p.writeNTString(cs.username);
						p.SendPacket(dos);
						
						break;

					case BNCSCommandIDs.SID_AUTH_CHECK:
						int result = is.readDWord();
						String extraInfo = is.readNTString();
						assert(is.available() == 0);
						
						if(result != 0) {
							switch(result) {
							case 0x0101:
								System.err.println("Invalid version");
								break;
							case 0x102:
								System.err.println("Game version must be downgraded: " + extraInfo);
								break;
							case 0x200:
								System.err.println("Invalid CD key");
								break;
							case 0x201:
								System.err.println("CD key in use by " + extraInfo);
								break;
							case 0x202:
								System.err.println("Banned key");
								break;
							case 0x203:
								System.err.println("Wrong product");
								break;
							default:
								System.err.println("Failed check");
								break;
							}
							throw new Exception("Login failed");
						}
						
						System.out.println("Passed CD key challenge and CheckRevision");
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
