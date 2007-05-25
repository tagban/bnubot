package core.bncs;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Random;

import Hashing.*;

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
	int nlsRevision = 0;
	int serverToken = 0;
	int clientToken = Math.abs(new Random().nextInt());
	SRP srp = null;
	byte proof_M2[] = null;
	String uniqueUserName = null;
	String statString = null;
	String accountName = null;

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
					case BNCSCommandIDs.SID_EXTRAWORK:
					case BNCSCommandIDs.SID_REQUIREDWORK:
						break;
						
					case BNCSCommandIDs.SID_NULL: {
						p = new BNCSPacket(BNCSCommandIDs.SID_NULL);
						p.SendPacket(dos);
						break;
					}
					
					case BNCSCommandIDs.SID_PING: {
						p = new BNCSPacket(BNCSCommandIDs.SID_PING);
						p.writeDWord(is.readDWord());
						p.SendPacket(dos);
						break;
					}
					
					case BNCSCommandIDs.SID_AUTH_INFO: {
						nlsRevision = is.readDWord();
						serverToken = is.readDWord();
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
						
						// Hash the CD key
						byte keyHash[] = HashMain.hashKey(clientToken, serverToken, cs.cdkey).getBuffer();
						
						// Hash the game files
						
						String tmp = MPQFileName.substring(MPQFileName.indexOf("IX86")+5);
						tmp = tmp.substring(0,tmp.indexOf("."));
						int mpqNum = Integer.parseInt(tmp);
                    	String files[] = HashMain.getFiles(cs.product, HashMain.PLATFORM_INTEL);
						int exeHash = CheckRevision.checkRevision(ValueStr, files, mpqNum);
					
				    	int exeVersion = HashMain.getExeVer(cs.product);
						String exeInfo = HashMain.getExeInfo(cs.product);
						
						/*
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
				    	*/

				    	//System.out.println("Version:\n\t" + exeVersion + "\n\t" + exeVersion2);
				    	//System.out.println("Info:\n\t" + exeInfo + "\n\t" + exeInfo2);

						// Respond
						p = new BNCSPacket(BNCSCommandIDs.SID_AUTH_CHECK);
						p.writeDWord(clientToken);
						p.writeDWord(exeVersion);
						p.writeDWord(exeHash);
						p.writeDWord(1);			// Number of keys
						p.writeDWord(0);			// Spawn?
						
						//For each key..
						if(keyHash.length != 36)
							throw new Exception("Invalid keyHash length");
						p.write(keyHash);
						
						//Finally,
						p.writeNTString(exeInfo);
						p.writeNTString(cs.username);
						p.SendPacket(dos);
						
						break;
					}
					
					case BNCSCommandIDs.SID_AUTH_CHECK: {
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
						if(nlsRevision == 0) {
							// ...
							int passwordHash[] = DoubleHash.doubleHash(cs.password, clientToken, serverToken);
							
							p = new BNCSPacket(BNCSCommandIDs.SID_AUTH_CHECK);
							p.writeDWord(clientToken);
							p.writeDWord(serverToken);
							p.write(passwordHash[0]);
							p.write(passwordHash[1]);
							p.write(passwordHash[2]);
							p.write(passwordHash[3]);
							p.write(passwordHash[4]);
							p.writeNTString(cs.username);
							p.SendPacket(dos);
							
						} else {
							srp = new SRP(cs.username, cs.password);
							srp.set_NLS(nlsRevision);
							byte A[] = srp.get_A();
							
							if(A.length != 32)
								throw new Exception("Invalid A length");
							
							p = new BNCSPacket(BNCSCommandIDs.SID_AUTH_ACCOUNTLOGON);
							p.write(A);
							p.writeNTString(cs.username);
							p.SendPacket(dos);
						}
						
						break;
					}
					
					case BNCSCommandIDs.SID_AUTH_ACCOUNTLOGON: {
						/* (DWORD)		 Status
						 * (BYTE[32])	 Salt (s)
						 * (BYTE[32])	 Server Key (B)
						 * 
						 * 0x00: Logon accepted, requires proof.
						 * 0x01: Account doesn't exist.
						 * 0x05: Account requires upgrade.
						 * Other: Unknown (failure).
						 */
						int status = is.readDWord();
						switch(status) {
						case 0x00:
							System.out.println("Login accepted; requires proof.");
							break;
						case 0x01:
							throw new Exception("Account doesn't exist");
						case 0x05:
							throw new Exception("Account requires upgrade");
						default:
							throw new Exception("Unknown status");
						}
						
						if(srp == null)
							throw new Exception("SRP is not initialized!");
						
						byte s[] = new byte[32];
						byte B[] = new byte[32];
						is.read(s, 0, 32);
						is.read(B, 0, 32);

						byte M1[] = srp.getM1(s, B);
						proof_M2 = srp.getM2(s, B);
						if(M1.length != 20)
							throw new Exception("Invalid M1 length");

						p = new BNCSPacket(BNCSCommandIDs.SID_AUTH_ACCOUNTLOGONPROOF);
						p.write(M1);
						p.SendPacket(dos);
						break;
					}
					
					case BNCSCommandIDs.SID_AUTH_ACCOUNTLOGONPROOF: {
						/* (DWORD)		 Status
						 * (BYTE[20])	 Server Password Proof (M2)
						 * (STRING) 	 Additional information
						 * 
						 * Status:
						 * 0x00: Logon successful.
						 * 0x02: Incorrect password.
						 * 0x0E: An email address should be registered for this account.
						 * 0x0F: Custom error. A string at the end of this message contains the error.
						 */
						int status = is.readDWord();
						byte server_M2[] = new byte[20];
						is.read(server_M2, 0, 20);
						String additionalInfo = null;
						if(is.available() != 0)
							additionalInfo = is.readNTString();
						
						switch(status) {
						case 0x00:
							System.out.println("Login successful.");
							break;
						case 0x02:
							throw new Exception("Incorrect password");
						case 0x0E:
							System.err.println("An email address should be registered for this account.");
							break;
						case 0x0F:
							throw new Exception("Custom bnet error: " + additionalInfo);
						default:
							throw new Exception("Unknown status");
						}

						for(int i = 0; i < 20; i++) {
							if(server_M2[i] != proof_M2[i])
								throw new Exception("Server couldn't prove password");
						}

						p = new BNCSPacket(BNCSCommandIDs.SID_ENTERCHAT);
						p.writeNTString("");
						p.writeNTString("");
						p.SendPacket(dos);
						break;
					}
					
					case BNCSCommandIDs.SID_ENTERCHAT: {
						uniqueUserName = is.readNTString();
						statString = is.readNTString();
						accountName = is.readNTString();
						
						// We are officially logged in and in the channel!
						p = new BNCSPacket(BNCSCommandIDs.SID_JOINCHANNEL);
						p.writeDWord(0); // nocreate join
						p.writeNTString("Clan BNU");
						p.SendPacket(dos);
						break;
					}
					
					case BNCSCommandIDs.SID_CLANINFO: {
						break;
					}
					
					case BNCSCommandIDs.SID_CHATEVENT: {
						int eventID = is.readDWord();
						int userFlags = is.readDWord();
						int ping = is.readDWord();
						is.skip(12);
					//	is.readDWord();	// IP Address (defunct)
					//	is.readDWord();	// Account number (defunct)
					//	is.readDWord(); // Registration authority (defunct)
						String userName = is.readNTString();
						String text = is.readNTString();
						
						switch(eventID) {
						case BNCSCommandIDs.EID_SHOWUSER:
						case BNCSCommandIDs.EID_JOIN:
						case BNCSCommandIDs.EID_USERFLAGS:
							System.out.println(String.format("User {1} flags {2} ping {3} text {4}", userName, userFlags, ping, text));
							break;
						case BNCSCommandIDs.EID_TALK:
							System.out.println(String.format("<{1}> {2}", userName, text));
							break;
						case BNCSCommandIDs.EID_EMOTE:
							System.out.println(String.format("<{1} {2}>", userName, text));
							break;
						default:
							System.err.println("Unknown EID 0x" + Integer.toHexString(eventID));
							break;
						}
						
						break;
					}
					
					default:
						System.err.println("Unknown SID 0x" + Integer.toHexString(pr.packetId) );
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
