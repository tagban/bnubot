package bnubot.core.bncs;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;

import bnubot.bot.EventHandler;
import bnubot.core.BNetInputStream;
import bnubot.core.BNetOutputStream;
import bnubot.core.Connection;
import bnubot.core.ConnectionSettings;

import Hashing.*;

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
	String channelName = null;

	public BNCSConnection(ConnectionSettings cs) {
		super(cs);
	}
	
	private void sendPassword() throws Exception {
		if(nlsRevision == 0) {
			int passwordHash[] = DoubleHash.doubleHash(cs.password.toLowerCase(), clientToken, serverToken);
			
			BNCSPacket p = new BNCSPacket(BNCSCommandIDs.SID_LOGONRESPONSE2);
			p.writeDWord(clientToken);
			p.writeDWord(serverToken);
			p.writeDWord(passwordHash[0]);
			p.writeDWord(passwordHash[1]);
			p.writeDWord(passwordHash[2]);
			p.writeDWord(passwordHash[3]);
			p.writeDWord(passwordHash[4]);
			p.writeNTString(cs.username);
			p.SendPacket(dos, cs.packetLog);
			
		} else {
			srp = new SRP(cs.username, cs.password);
			srp.set_NLS(nlsRevision);
			byte A[] = srp.get_A();
			
			if(A.length != 32)
				throw new Exception("Invalid A length");
			
			BNCSPacket p = new BNCSPacket(BNCSCommandIDs.SID_AUTH_ACCOUNTLOGON);
			p.write(A);
			p.writeNTString(cs.username);
			p.SendPacket(dos, cs.packetLog);
		}
	}

	public void run() {
		while(true) {
			try {
				if(!cs.autoconnect) {
					while(!isConnected())
						yield();
				}
	
				setConnected(true);
				recieveInfo("Connecting to " + cs.server + ":" + cs.port);
				s = new Socket(cs.server, cs.port);
				dis = new DataInputStream(s.getInputStream());
				dos = new DataOutputStream(s.getOutputStream());
				
				//dos.write("GET / HTTP/1.0\n\n".getBytes());
				
				// Game
				dos.writeByte(0x01);
				
				int verByte = HashMain.getVerByte(cs.product);
				
				BNCSPacket p = new BNCSPacket(BNCSCommandIDs.SID_AUTH_INFO);
				p.writeDWord(0);		// Protocol ID (0)
				p.writeDWord("IX86");	// Platform ID (IX86)
				p.writeDWord(util.Constants.prods[cs.product-1]);	// Product ID (SEXP)
				p.writeDWord(verByte);	// Version byte
				p.writeDWord("enUS");	// Product language
				p.writeDWord(0);		// Local IP
				p.writeDWord(0xf0);		// TZ bias
				p.writeDWord(0x409);	// Locale ID
				p.writeDWord(0x409);	// Language ID
				p.writeNTString("USA");	// Country abreviation
				p.writeNTString("United States");	// Country
				p.SendPacket(dos, cs.packetLog);
				
				while(s.isConnected() && connected) {
					if(dis.available() > 0) {
						BNCSPacketReader pr = new BNCSPacketReader(dis, cs.packetLog);
						BNetInputStream is = pr.getData();
						
						switch(pr.packetId) {
						case BNCSCommandIDs.SID_EXTRAWORK:
						case BNCSCommandIDs.SID_REQUIREDWORK:
							break;
							
						case BNCSCommandIDs.SID_NULL: {
							p = new BNCSPacket(BNCSCommandIDs.SID_NULL);
							p.SendPacket(dos, cs.packetLog);
							break;
						}
						
						case BNCSCommandIDs.SID_PING: {
							p = new BNCSPacket(BNCSCommandIDs.SID_PING);
							p.writeDWord(is.readDWord());
							p.SendPacket(dos, cs.packetLog);
							break;
						}
						
						case BNCSCommandIDs.SID_AUTH_INFO: {
							nlsRevision = is.readDWord();
							serverToken = is.readDWord();
							is.skip(4);	//int udpValue = is.readDWord();
							/*is.skip(8);	/*/long MPQFileTime = is.readQWord();
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
						/*
						  	String tmp = MPQFileName.substring(MPQFileName.indexOf("IX86")+5);
							tmp = tmp.substring(0,tmp.indexOf("."));
							int mpqNum = Integer.parseInt(tmp);
	                    	String files[] = HashMain.getFiles(cs.product, HashMain.PLATFORM_INTEL);
							int exeHash = CheckRevision.checkRevision(ValueStr, files, mpqNum);
						
					    	int exeVersion = HashMain.getExeVer(cs.product);
							String exeInfo = HashMain.getExeInfo(cs.product);
						
							/*/
							BNLSProtocol.OutPacketBuffer exeHashBuf = CheckRevisionBNLS.checkRevision(ValueStr, cs.product, MPQFileName, MPQFileTime);
					    	BNetInputStream exeStream = new BNetInputStream(new java.io.ByteArrayInputStream(exeHashBuf.getBuffer()));
					    	exeStream.skipBytes(3);
					    	int success = exeStream.readDWord();
					    	if(success != 1) {
					    		System.err.println(bnubot.util.HexDump.hexDump(exeHashBuf.getBuffer()));
					    		throw new Exception("BNLS failed to complete 0x1A sucessfully");
					    	}
					    	int exeVersion = exeStream.readDWord();
					    	int exeHash = exeStream.readDWord();
					    	String exeInfo = exeStream.readNTString();
					    	exeStream.readDWord(); // cookie
					    	int exeVerbyte = exeStream.readDWord();
					    	assert(exeStream.available() == 0);
					    	//*/
	
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
							p.SendPacket(dos, cs.packetLog);
							
							break;
						}
						
						case BNCSCommandIDs.SID_AUTH_CHECK: {
							int result = is.readDWord();
							String extraInfo = is.readNTString();
							assert(is.available() == 0);
							
							if(result != 0) {
								switch(result) {
								case 0x0101:
									recieveError("Invalid version");
									break;
								case 0x102:
									recieveError("Game version must be downgraded: " + extraInfo);
									break;
								case 0x200:
									recieveError("Invalid CD key");
									break;
								case 0x201:
									recieveError("CD key in use by " + extraInfo);
									break;
								case 0x202:
									recieveError("Banned key");
									break;
								case 0x203:
									recieveError("Wrong product");
									break;
								default:
									recieveError("Unknown SID_AUTH_CHECK result 0x" + Integer.toHexString(result));
									break;
								}
								setConnected(false);
							}
							
							recieveInfo("Passed CD key challenge and CheckRevision");
							sendPassword();
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
								recieveInfo("Login accepted; requires proof.");
								break;
							case 0x01:
								recieveError("Account doesn't exist");
								setConnected(false);
								break;
							case 0x05:
								recieveError("Account requires upgrade");
								setConnected(false);
								break;
							default:
								recieveError("Unknown SID_AUTH_ACCOUNTLOGON status");
								setConnected(false);
								break;
							}
							
							if(srp == null) {
								recieveError("SRP is not initialized!");
								setConnected(false);
								break;
							}
							
							if(!isConnected())
								break;
							
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
							p.SendPacket(dos, cs.packetLog);
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
								break;
							case 0x02:
								recieveError("Incorrect password");
								setConnected(false);
								break;
							case 0x0E:
								recieveError("An email address should be registered for this account.");
								break;
							case 0x0F:
								recieveError("Custom bnet error: " + additionalInfo);
								setConnected(false);
								break;
							default:
								recieveError("Unknown SID_AUTH_ACCOUNTLOGONPROOF status: 0x" + Integer.toHexString(status));
								setConnected(false);
								break;
							}
							if(!isConnected())
								break;
	
							for(int i = 0; i < 20; i++) {
								if(server_M2[i] != proof_M2[i])
									throw new Exception("Server couldn't prove password");
							}
	
							recieveInfo("Login successful; entering chat.");
	
							p = new BNCSPacket(BNCSCommandIDs.SID_ENTERCHAT);
							p.writeNTString("");
							p.writeNTString("");
							p.SendPacket(dos, cs.packetLog);
							break;
						}
						
						case BNCSCommandIDs.SID_LOGONRESPONSE2: {
							int result = is.readDWord();
							switch(result) {
							case 0x00:	// Success
								recieveInfo("Login successful; entering chat.");
	
								p = new BNCSPacket(BNCSCommandIDs.SID_ENTERCHAT);
								p.writeNTString("");
								p.writeNTString("");
								p.SendPacket(dos, cs.packetLog);
								break;
							case 0x01:	// Account doesn't exist
								recieveInfo("Account doesn't exist; creating...");
								
								int[] passwordHash = BrokenSHA1.calcHashBuffer(cs.password.toLowerCase().getBytes());
								
								p = new BNCSPacket(BNCSCommandIDs.SID_CREATEACCOUNT2);
								p.writeDWord(passwordHash[0]);
								p.writeDWord(passwordHash[1]);
								p.writeDWord(passwordHash[2]);
								p.writeDWord(passwordHash[3]);
								p.writeDWord(passwordHash[4]);
								p.writeNTString(cs.username);
								p.SendPacket(dos, cs.packetLog);
								break;
							case 0x02:	// Invalid password;
								recieveError("Incorrect password");
								setConnected(false);
								break;
							case 0x06:	// Account is cloed
								recieveError("Your account is closed.");
								setConnected(false);
								break;
							default:
								recieveError("Unknown SID_LOGONRESPONSE2 result 0x" + Integer.toHexString(result));
								setConnected(false);
								break;
							}
							break;
						}
						
						case BNCSCommandIDs.SID_CREATEACCOUNT2: {
							int status = is.readDWord();
							String suggestion = is.readNTString();
							
							switch(status) {
							case 0x00:
								recieveInfo("Account created");
								//sendPassword();
								break;
							case 0x02:
								recieveError("Name contained invalid characters");
								setConnected(false);
								break;
							case 0x03:
								recieveError("Name contained a banned word");
								setConnected(false);
								break;
							case 0x04:
								recieveError("Account already exists");
								setConnected(false);
								break;
							case 0x06:
								recieveError("Name did not contain enough alphanumeric characters");
								setConnected(false);
								break;
							default:
								recieveError("Unknown SID_CREATEACCOUNT2 status 0x" + Integer.toHexString(status));
								setConnected(false);
								break;
							}
						}
						
						case BNCSCommandIDs.SID_ENTERCHAT: {
							uniqueUserName = is.readNTString();
							statString = is.readNTString();
							accountName = is.readNTString();
							
							recieveInfo("Logged in as " + uniqueUserName);
							
							// We are officially logged in; get MOTD and join a channel!
							p = new BNCSPacket(BNCSCommandIDs.SID_NEWS_INFO);
							p.writeDWord((int)(new java.util.Date().getTime() / 1000)); // timestamp
							p.SendPacket(dos, cs.packetLog);
							
							p = new BNCSPacket(BNCSCommandIDs.SID_JOINCHANNEL);
							p.writeDWord(0); // nocreate join
							p.writeNTString("Clan BNU");
							p.SendPacket(dos, cs.packetLog);
							break;
						}
						
						case BNCSCommandIDs.SID_CLANINFO: {
							break;
						}
						
						case BNCSCommandIDs.SID_NEWS_INFO: {
							int numEntries = is.readByte();
							//int lastLogon = is.readDWord();
							//int oldestNews = is.readDWord();
							//int newestNews = is.readDWord();;
							is.skip(12);
							
							for(int i = 0; i < numEntries; i++) {
								int timeStamp = is.readDWord();
								String news = is.readNTString();
								if(timeStamp == 0)	// MOTD
									recieveInfo(news);
							}
							
							break;
						}
						
						case BNCSCommandIDs.SID_CHATEVENT: {
							int eid = is.readDWord();
							int flags = is.readDWord();
							int ping = is.readDWord();
							is.skip(12);
						//	is.readDWord();	// IP Address (defunct)
						//	is.readDWord();	// Account number (defunct)
						//	is.readDWord(); // Registration authority (defunct)
							String user = is.readNTString();
							String text = is.readNTString();
							
							switch(eid) {
							case BNCSCommandIDs.EID_SHOWUSER:
							case BNCSCommandIDs.EID_USERFLAGS:
								channelUser(user, flags, ping, text);
								break;
							case BNCSCommandIDs.EID_JOIN:
								channelJoin(user, flags, ping, text);
								break;
							case BNCSCommandIDs.EID_LEAVE:
								channelLeave(user, flags, ping, text);
								break;
							case BNCSCommandIDs.EID_TALK:
								recieveChat(user, text);
								break;
							case BNCSCommandIDs.EID_EMOTE:
								recieveEmote(user, text);
								break;
							case BNCSCommandIDs.EID_INFO:
								recieveInfo(text);
								break;
							case BNCSCommandIDs.EID_ERROR:
								recieveError(text);
								break;
							case BNCSCommandIDs.EID_CHANNEL:
								channelName = text;
								joinedChannel(text);
								break;
							default:
								recieveError("Unknown SID_CHATEVENT EID 0x" + Integer.toHexString(eid) + ": " + text);
								break;
							}
							
							break;
						}
						
						default:
							recieveError("Unknown SID 0x" + Integer.toHexString(pr.packetId));
							break;
						}
					}
					
					yield();
				}
				
				setConnected(false);
				s.close();
				recieveError("Disconnected from battle.net.");
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	public boolean isConnected() {
		return connected;
	}
	
	public void setConnected(boolean c) {
		connected = c;
		
		if(c)
			bnetConnected();
		else
			bnetDisconnected();
	}
	
	public void sendChat(String text) {
		//Remove all chars under 0x20
		byte[] data = text.getBytes();
		text = "";
		for(int i = 0; i < data.length; i++) {
			if(data[i] >= 0x20)
				text += (char)data[i];
		}
		
		//Write the packet
		try {
			BNCSPacket p = new BNCSPacket(BNCSCommandIDs.SID_CHATCOMMAND);
			p.writeNTString(text);
			p.SendPacket(dos, cs.packetLog);
			
			if(text.charAt(0) != '/')
				recieveChat(uniqueUserName, text);
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public String toString() {
		String out = accountName;
		if(channelName != null)
			out += " - " + channelName;
		return out;
	}
	
}
