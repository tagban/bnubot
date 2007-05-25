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

import core.BNetInputStream;
import core.Connection;
import core.ConnectionSettings;
import core.EventHandler;

public class BNCSConnection extends Connection {
	Socket s = null;
	DataInputStream dis = null;
	DataOutputStream dos = null;
	EventHandler e = null;
	boolean connected = false;
 	
	public BNCSConnection(ConnectionSettings cs, EventHandler e) {
		super(cs);
		this.e = e;
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
			
			int verByte = HashMain.getVerByte(cs.product);
			
			BNCSPacket p = new BNCSPacket(BNCSCommandIDs.SID_AUTH_INFO);
			p.writeDWord(0);		// Protocol ID (0)
			p.writeDWord("IX86");	// Platform ID (IX86)
			p.writeDWord("WAR3");	// Product ID (SEXP)
			p.writeDWord(verByte);	// Version byte
			p.writeDWord("enUS");	// Product language
			p.writeDWord(0);		// Local IP
			p.writeDWord(0xf0);		// TZ bias
			p.writeDWord(0x409);	// Locale ID
			p.writeDWord(0x409);	// Language ID
			p.writeNTString("USA");	// Country abreviation
			p.writeNTString("United States");	// Country
			p.SendPacket(dos);
			
			while(s.isConnected()) {
				if(dis.available() > 0) {
					BNCSPacketReader pr = new BNCSPacketReader(dis);
					BNetInputStream is = pr.getData();
					
					switch(pr.packetId) {
					case BNCSCommandIDs.SID_NULL:
						p = new BNCSPacket(BNCSCommandIDs.SID_NULL);
						p.SendPacket(dos);
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
						byte extraData[] = null;
						if(is.available() == 0x80) {
							extraData = new byte[0x80];
							is.read(extraData, 0, 0x80);
						}
						assert(is.available() == 0);
						
						//logonType = 2;
						//serverToken = 0xC07AA8C5;
						//MPQFileName = "ver-IX86-5.mpq";
						//ValueStr = "C=3607986392 A=733117271 B=3628884414 4 A=A^S B=B^C C=C^A A=A^B";
						
						// Hash the CD key
						//int clientToken = 0x07EA279E;
						int clientToken = Math.abs(new Random().nextInt());
						byte keyHash[] = HashMain.hashKey(clientToken, serverToken, cs.cdkey).getBuffer();
						
						// Hash the game files
						
					/*	String tmp = MPQFileName.substring(MPQFileName.indexOf("IX86")+5);
						tmp = tmp.substring(0,tmp.indexOf("."));
						int mpqNum = Integer.parseInt(tmp);
                    	String files[] = HashMain.getFiles(PRODUCT_DIABLO2, HashMain.PLATFORM_INTEL);
						int exeHash = CheckRevision.checkRevision(ValueStr, files, mpqNum);
						
				    	int exeVersion = HashMain.getExeVer(PRODUCT_DIABLO2);
						String exeInfo = HashMain.getExeInfo(PRODUCT_DIABLO2);
					*/
						
				    	OutPacketBuffer exeHashBuf = CheckRevisionBNLS.checkRevision(ValueStr, cs.product, MPQFileName, MPQFileTime);
				    	BNetInputStream exeStream = new BNetInputStream(new ByteArrayInputStream(exeHashBuf.getBuffer()));
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
