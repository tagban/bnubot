package bnubot.core.bncs;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;

import bnubot.core.BNetInputStream;
import bnubot.core.BNetUser;
import bnubot.core.Connection;
import bnubot.core.ConnectionSettings;
import bnubot.core.CookieUtility;
import bnubot.core.clan.ClanMember;
import bnubot.core.clan.ClanRankIDs;
import bnubot.core.clan.ClanStatusIDs;
import bnubot.core.friend.FriendEntry;
import bnubot.core.queue.ChatQueue;

import Hashing.*;

public class BNCSConnection extends Connection {
	protected Socket s = null;
	protected DataInputStream dis = null;
	protected DataOutputStream dos = null;
	private int productID = 0;
	private int verByte;
	private int nlsRevision = -1;
	private int serverToken = 0;
	private int clientToken = Math.abs(new Random().nextInt());
	private SRP srp = null;
	private byte proof_M2[] = null;
	protected String statString = null;
	private boolean forceReconnect = false;
	protected int myFlags = 0;
	protected int myPing = -1;

	public BNCSConnection(ConnectionSettings cs, ChatQueue cq) {
		super(cs, cq);
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
		boolean allowAutoConnect = true;
		while(true) {
			try {
				if(forceReconnect)
					forceReconnect = false;
				else {
					if(!cs.autoconnect || !allowAutoConnect) {
						while(!isConnected()) {
							yield();
							sleep(10);
						}
					}
					allowAutoConnect = false;
				}
				
				setConnected(true);
				recieveInfo("Connecting to " + cs.bncsServer + ":" + cs.port);
				s = new Socket(cs.bncsServer, cs.port);
				dis = new DataInputStream(s.getInputStream());
				dos = new DataOutputStream(s.getOutputStream());
				nlsRevision = -1;
				productID = ProductIDs.ProductID[cs.product-1];
				
				//dos.write("GET / HTTP/1.0\n\n".getBytes());
				
				// Game
				dos.writeByte(0x01);
				
				verByte = HashMain.getVerByte(cs.product);
				
				BNCSPacket p;
				
				switch(cs.product) {
				case ConnectionSettings.PRODUCT_STARCRAFT:
				case ConnectionSettings.PRODUCT_BROODWAR:
				case ConnectionSettings.PRODUCT_DIABLO2:
				case ConnectionSettings.PRODUCT_LORDOFDESTRUCTION:
				case ConnectionSettings.PRODUCT_WARCRAFT3:
				case ConnectionSettings.PRODUCT_THEFROZENTHRONE:
					p = new BNCSPacket(BNCSCommandIDs.SID_AUTH_INFO);
					p.writeDWord(0);							// Protocol ID (0)
					p.writeDWord(PlatformIDs.PLATFORM_IX86);	// Platform ID (IX86)
					p.writeDWord(productID);					// Product ID
					p.writeDWord(verByte);						// Version byte
					p.writeDWord("enUS");						// Product language
					p.writeDWord(0);							// Local IP
					p.writeDWord(0xf0);							// TZ bias
					p.writeDWord(0x409);						// Locale ID
					p.writeDWord(0x409);						// Language ID
					p.writeNTString("USA");						// Country abreviation
					p.writeNTString("United States");			// Country
					p.SendPacket(dos, cs.packetLog);
					break;

				case ConnectionSettings.PRODUCT_STARCRAFTSHAREWARE:
				case ConnectionSettings.PRODUCT_JAPANSTARCRAFT:
					p = new BNCSPacket(BNCSCommandIDs.SID_CLIENTID);
					p.writeDWord(0);	// Registration Version
					p.writeDWord(0);	// Registration Authority
					p.writeDWord(0);	// Account Number
					p.writeDWord(0);	// Registration Token
					p.writeByte(0);		// LAN computer name
					p.writeByte(0);		// LAN username
					p.SendPacket(dos, cs.packetLog);
					
					p = new BNCSPacket(BNCSCommandIDs.SID_STARTVERSIONING);
					p.writeDWord(PlatformIDs.PLATFORM_IX86);	// Platform ID (IX86)
					p.writeDWord(productID);					// Product ID
					p.writeDWord(verByte);						// Version byte
					p.writeDWord(0);							// Unknown (0)
					p.SendPacket(dos, cs.packetLog);
					break;
					
				case ConnectionSettings.PRODUCT_WAR2BNE:
					p = new BNCSPacket(BNCSCommandIDs.SID_CLIENTID2);
					p.writeDWord(1);	// Server version
					p.writeDWord(0);	// Registration Version
					p.writeDWord(0);	// Registration Authority
					p.writeDWord(0);	// Account Number
					p.writeDWord(0);	// Registration Token
					p.writeByte(0);		// LAN computer name
					p.writeByte(0);		// LAN username
					p.SendPacket(dos, cs.packetLog);
					
					p = new BNCSPacket(BNCSCommandIDs.SID_LOCALEINFO);
					p.writeQWord(0);		// System time
					p.writeQWord(0);		// Local time
					p.writeDWord(0xf0);		// TZ bias
					p.writeDWord(0x409);	// SystemDefaultLCID
					p.writeDWord(0x409);	// UserDefaultLCID
					p.writeDWord(0x409);	// UserDefaultLangID	
					p.writeNTString("ena");	// Abbreviated language name		
					p.writeNTString("1");	// Country code
					p.writeNTString("USA");	// Abbreviated country name
					p.writeNTString("United States");	// Country (English)
					p.SendPacket(dos, cs.packetLog);
					
					p = new BNCSPacket(BNCSCommandIDs.SID_STARTVERSIONING);
					p.writeDWord(PlatformIDs.PLATFORM_IX86);	// Platform ID (IX86)
					p.writeDWord(productID);					// Product ID
					p.writeDWord(verByte);						// Version byte
					p.writeDWord(0);							// Unknown (0)
					p.SendPacket(dos, cs.packetLog);
					break;
					
				default:
					recieveError("Don't know how to connect with product " + productID);
					setConnected(false);
					break;
				}
				
				if(isConnected());
					connectedLoop();
			
			} catch(SocketException e) {
			} catch(Exception e) {
				recieveError("Unhandled exception: " + e.getMessage());
				e.printStackTrace();
			}

			setConnected(false);
			recieveError("Disconnected from battle.net.");
			try { s.close(); } catch (Exception e) { }
			s = null;
		}
	}
	
	private void connectedLoop() throws Exception {
		BNCSPacket p;
		lastAntiIdle = new Date().getTime();
		while(s.isConnected() && connected) {
			if(channelName != null) {
				long timeSinceAntiIdle = new Date().getTime() - lastAntiIdle;
				
				//Wait 5 minutes
				timeSinceAntiIdle /= 1000;
				timeSinceAntiIdle /= 60;
				if(timeSinceAntiIdle > 5) {
					lastAntiIdle = new Date().getTime();
					sendChat(cs.antiIdle);
				}
			}
			
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
				
				case BNCSCommandIDs.SID_AUTH_INFO:
				case BNCSCommandIDs.SID_STARTVERSIONING: {
					if(pr.packetId == BNCSCommandIDs.SID_AUTH_INFO) {
						nlsRevision = is.readDWord();
						serverToken = is.readDWord();
						is.skip(4);	//int udpValue = is.readDWord();
					}
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
					byte keyHash[] = null;
					byte keyHash2[] = null;
					if(nlsRevision != -1) {
						keyHash = HashMain.hashKey(clientToken, serverToken, cs.cdkey).getBuffer();
						if(cs.product == ConnectionSettings.PRODUCT_LORDOFDESTRUCTION)
							keyHash2 = HashMain.hashKey(clientToken, serverToken, cs.cdkeyLOD).getBuffer();
						if(cs.product == ConnectionSettings.PRODUCT_THEFROZENTHRONE)
							keyHash2 = HashMain.hashKey(clientToken, serverToken, cs.cdkeyTFT).getBuffer();
					}
					
					// Hash the game files
				  	String tmp = MPQFileName.substring(MPQFileName.indexOf("IX86")+5);
					tmp = tmp.substring(0,tmp.indexOf("."));
                	String files[] = HashMain.getFiles(cs.product, HashMain.PLATFORM_INTEL);

					int mpqNum = -1;
                	int exeHash;
                	int exeVersion;
                	String exeInfo;
                	
                	try {
						mpqNum = Integer.parseInt(tmp);
                    	exeHash = CheckRevision.checkRevision(ValueStr, files, mpqNum);
				    	exeVersion = HashMain.getExeVer(cs.product);
						exeInfo = HashMain.getExeInfo(cs.product);
                	} catch(Exception e) {
                		recieveError("Local hashing failed (" + e.getMessage() + "). Trying BNLS server.");
                		
                		BNLSProtocol.OutPacketBuffer exeHashBuf;
                		if((cs.bnlsServer == null)
                		|| (cs.bnlsServer.length() == 0)) {
                			exeHashBuf = CheckRevisionBNLS.checkRevision(ValueStr, cs.product, MPQFileName, MPQFileTime);
                		} else {
                			exeHashBuf = CheckRevisionBNLS.checkRevision(ValueStr, cs.product, MPQFileName, MPQFileTime, cs.bnlsServer);
                		}
				    	BNetInputStream exeStream = new BNetInputStream(new java.io.ByteArrayInputStream(exeHashBuf.getBuffer()));
				    	exeStream.skipBytes(3);
				    	int success = exeStream.readDWord();
				    	if(success != 1) {
				    		System.err.println(bnubot.util.HexDump.hexDump(exeHashBuf.getBuffer()));
				    		throw new Exception("BNLS failed to complete 0x1A sucessfully");
				    	}
				    	exeVersion = exeStream.readDWord();
				    	exeHash = exeStream.readDWord();
				    	exeInfo = exeStream.readNTString();
				    	exeStream.readDWord(); // cookie
				    	/*int exeVerbyte =*/ exeStream.readDWord();
				    	assert(exeStream.available() == 0);
                	}

					// Respond
                	if(nlsRevision != -1) {
						p = new BNCSPacket(BNCSCommandIDs.SID_AUTH_CHECK);
						p.writeDWord(clientToken);
						p.writeDWord(exeVersion);
						p.writeDWord(exeHash);
						if(keyHash2 == null)
							p.writeDWord(1);		// Number of keys
						else
							p.writeDWord(2);		// Number of keys
						p.writeDWord(0);			// Spawn?
						
						//For each key..
						if(keyHash.length != 36)
							throw new Exception("Invalid keyHash length");
						p.write(keyHash);
						if(keyHash2 != null) {
							if(keyHash2.length != 36)
								throw new Exception("Invalid keyHash2 length");
							p.write(keyHash2);
						}
						
						//Finally,
						p.writeNTString(exeInfo);
						p.writeNTString(cs.username);
						p.SendPacket(dos, cs.packetLog);
                	} else {
                		/* (DWORD)		 Platform ID
                		 * (DWORD)		 Product ID
                		 * (DWORD)		 Version Byte
                		 * (DWORD)		 EXE Version
                		 * (DWORD)		 EXE Hash
                		 * (STRING) 	 EXE Information
                		 */
                		p = new BNCSPacket(BNCSCommandIDs.SID_REPORTVERSION);
                		p.writeDWord(PlatformIDs.PLATFORM_IX86);
                		p.writeDWord(productID);
                		p.writeDWord(verByte);
						p.writeDWord(exeVersion);
						p.writeDWord(exeHash);
						p.writeNTString(exeInfo);
						p.SendPacket(dos, cs.packetLog);
                	}
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
							recieveError("Wrong product for CD key");
							break;
						case 0x210:
							recieveError("Invalid second CD key");
							break;
						case 0x211:
							recieveError("Second CD key in use by " + extraInfo);
							break;
						case 0x212:
							recieveError("Banned second key");
							break;
						case 0x213:
							recieveError("Wrong product for second CD key");
							break;
						default:
							recieveError("Unknown SID_AUTH_CHECK result 0x" + Integer.toHexString(result));
							break;
						}
						setConnected(false);
						break;
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
						recieveError("Account doesn't exist; creating...");
						
						if(srp == null) {
							recieveError("SRP is not initialized!");
							setConnected(false);
							break;
						}

				        byte[] salt = new byte[32];
				        new Random().nextBytes(salt);
				        byte[] verifier = srp.get_v(salt).toByteArray();

				        if(salt.length != 32)
				        	throw new Exception("Salt length wasn't 32!");
				        if(verifier.length != 32)
				        	throw new Exception("Verifier length wasn't 32!");
				        
				        p = new BNCSPacket(BNCSCommandIDs.SID_AUTH_ACCOUNTCREATE);
				        p.write(salt);
				        p.write(verifier);
				        p.writeNTString(cs.username);
				        p.SendPacket(dos, cs.packetLog);
						
						break;
					case 0x05:
						recieveError("Account requires upgrade");
						setConnected(false);
						break;
					default:
						recieveError("Unknown SID_AUTH_ACCOUNTLOGON status 0x" + Integer.toHexString(status));
						setConnected(false);
						break;
					}
					
					if(status != 0)
						break;
					
					if(srp == null) {
						recieveError("SRP is not initialized!");
						setConnected(false);
						break;
					}
					
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
				
				case BNCSCommandIDs.SID_AUTH_ACCOUNTCREATE: {
					/* 
					 * (DWORD)		 Status
					 * 0x00: Successfully created account name.
					 * 0x04: Name already exists.
					 * 0x07: Name is too short/blank.
					 * 0x08: Name contains an illegal character.
					 * 0x09: Name contains an illegal word.
					 * 0x0a: Name contains too few alphanumeric characters.
					 * 0x0b: Name contains adjacent punctuation characters.
					 * 0x0c: Name contains too many punctuation characters.
					 * Any other: Name already exists.
					 */
					int status = is.readDWord();
					switch(status) {
					case 0x00:
						recieveInfo("Create account succeeded; logging in.");
						sendPassword();
						break;
					default:
						recieveError("Create account failed with error code 0x" + Integer.toHexString(status));
						break;
					}
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
						registerEmail();
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
				
				case BNCSCommandIDs.SID_CLIENTID: {
					//Sends new registration values; no longer used
					break;
				}
				
				case BNCSCommandIDs.SID_LOGONCHALLENGE: {
					serverToken = is.readDWord();
					break;
				}
				
				case BNCSCommandIDs.SID_LOGONCHALLENGEEX: {
					/*int udpToken =*/ is.readDWord();
					serverToken = is.readDWord();
					break;
				}
				
				case BNCSCommandIDs.SID_CREATEACCOUNT2: {
					int status = is.readDWord();
					/*String suggestion =*/ is.readNTString();
					
					switch(status) {
					case 0x00:
						recieveInfo("Account created");
						sendPassword();
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
					break;
				}
				
				case BNCSCommandIDs.SID_SETEMAIL: {
					recieveError("An email address should be registered for this account.");
					registerEmail();
					break;
				}
				
				case BNCSCommandIDs.SID_ENTERCHAT: {
					String uniqueUserName = is.readNTString();
					statString = is.readNTString();
					/*String accountName =*/ is.readNTString();
					
					myUser = new BNetUser(uniqueUserName, cs.myRealm);
					recieveInfo("Logged in as " + myUser.getFullLogonName());
					
					// We are officially logged in!
					
					// Get MOTD
					p = new BNCSPacket(BNCSCommandIDs.SID_NEWS_INFO);
					p.writeDWord((int)(new java.util.Date().getTime() / 1000)); // timestamp
					p.SendPacket(dos, cs.packetLog);
					
					// Get friends list
					p = new BNCSPacket(BNCSCommandIDs.SID_FRIENDSLIST);
					p.SendPacket(dos, cs.packetLog);
					
					// Join home channel
					joinChannel(cs.channel);
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
						String news = is.readNTString().trim();
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
					String username = is.readNTString();
					String text = is.readNTString();

					BNetUser user = null;
					switch(eid) {
					case BNCSChatEventIDs.EID_SHOWUSER:
					case BNCSChatEventIDs.EID_USERFLAGS:
					case BNCSChatEventIDs.EID_JOIN:
					case BNCSChatEventIDs.EID_LEAVE:
					case BNCSChatEventIDs.EID_TALK:
					case BNCSChatEventIDs.EID_EMOTE:
					case BNCSChatEventIDs.EID_WHISPERSENT:
					case BNCSChatEventIDs.EID_WHISPER:
						user = new BNetUser(username, cs.myRealm);
						break;
					}
					
					switch(eid) {
					case BNCSChatEventIDs.EID_SHOWUSER:
					case BNCSChatEventIDs.EID_USERFLAGS:
						if(user.equals(myUser)) {
							myFlags = flags;
							myPing = ping;
						}
						channelUser(user, flags, ping, text);
						break;
					case BNCSChatEventIDs.EID_JOIN:
						channelJoin(user, flags, ping, text);
						break;
					case BNCSChatEventIDs.EID_LEAVE:
						channelLeave(user, flags, ping, text);
						break;
					case BNCSChatEventIDs.EID_TALK:
						recieveChat(user, flags, ping, text);
						break;
					case BNCSChatEventIDs.EID_EMOTE:
						recieveEmote(user, flags, ping, text);
						break;
					case BNCSChatEventIDs.EID_INFO:
						recieveInfo(text);
						break;
					case BNCSChatEventIDs.EID_ERROR:
						recieveError(text);
						break;
					case BNCSChatEventIDs.EID_CHANNEL:
						channelName = text;
						joinedChannel(text);
						break;
					case BNCSChatEventIDs.EID_WHISPERSENT:
						whisperSent(user, flags, ping, text);
						break;
					case BNCSChatEventIDs.EID_WHISPER:
						whisperRecieved(user, flags, ping, text);
						break;
					case BNCSChatEventIDs.EID_CHANNELDOESNOTEXIST:
						p = new BNCSPacket(BNCSCommandIDs.SID_JOINCHANNEL);
						p.writeDWord(2); // create join
						p.writeNTString(text);
						p.SendPacket(dos, cs.packetLog);
						break;
					case BNCSChatEventIDs.EID_CHANNELRESTRICTED:
						recieveError("Channel " + text + " is restricted");
						break;
					case BNCSChatEventIDs.EID_CHANNELFULL:
						recieveError("Channel " + text + " is full");
						break;
					default:
						recieveError("Unknown SID_CHATEVENT EID 0x" + Integer.toHexString(eid) + ": " + text);
						break;
					}
					
					break;
				}
				
				case BNCSCommandIDs.SID_MESSAGEBOX: {
					/*int style =*/ is.readDWord();
					String text = is.readNTString();
					String caption = is.readNTString();
					
					recieveInfo("<" + caption + "> " + text);
					break;
				}
				
				case BNCSCommandIDs.SID_FLOODDETECTED: {
					recieveError("You have been disconnected for flooding.");
					setConnected(false);
					break;
				}
				
				/*	.-----------.
				 *	|  Friends  |
				 *	'-----------'
				 */
				
				case BNCSCommandIDs.SID_FRIENDSLIST: {
					/* (BYTE)		 Number of Entries
					 * 
					 * For each member:
					 * (STRING) 	 Account
					 * (BYTE)		 Status
					 * (BYTE)		 Location
					 * (DWORD)		 ProductID
					 * (STRING) 	 Location name
					 */
					byte numEntries = is.readByte();
					FriendEntry[] entries = new FriendEntry[numEntries];
					
					for(int i = 0; i < numEntries; i++) {
						String uAccount = is.readNTString();
						byte uStatus = is.readByte();
						byte uLocation = is.readByte();
						int uProduct = is.readDWord();
						String uLocationName = is.readNTString();
						
						entries[i] = new FriendEntry(uAccount, uStatus, uLocation, uProduct, uLocationName);
					}
					
					friendsList(entries);
					break;
				}
				
				case BNCSCommandIDs.SID_FRIENDSUPDATE: {
					/* (BYTE)		 Entry number
					 * (BYTE)		 Friend Location
					 * (BYTE)		 Friend Status
					 * (DWORD)		 ProductID
					 * (STRING) 	 Location
					 */
					byte fEntry = is.readByte();
					byte fLocation = is.readByte();
					byte fStatus = is.readByte();
					int fProduct = is.readDWord();
					String fLocationName = is.readNTString();
					
					friendsUpdate(fEntry, fLocation, fStatus, fProduct, fLocationName);
					break;
				}
				
				case BNCSCommandIDs.SID_FRIENDSADD: {
					// TODO: friendsAdd(...);
					break;
				}
				
				case BNCSCommandIDs.SID_FRIENDSREMOVE: {
					// TODO: friendsRemove(...);
					break;
				}
				
				case BNCSCommandIDs.SID_FRIENDSPOSITION: {
					// TODO: friendsPosition(...);
					break;
				}
				
				/*	.--------.
				 *	|  Clan  |
				 *	'--------'
				 */

				// SID_CLANFINDCANDIDATES
				// SID_CLANINVITEMULTIPLE
				// SID_CLANCREATIONINVITATION
				// SID_CLANDISBAND
				// SID_CLANMAKECHIEFTAIN
				
				case BNCSCommandIDs.SID_CLANINFO: {
					/* (BYTE)		 Unknown (0)
					 * (DWORD)		 Clan tag
					 * (BYTE)		 Rank
					 */
					is.readByte();
					int myClan = is.readDWord();
					byte myClanRank = is.readByte();
					
					// TODO: clanInfo(myClan, myClanRank);
					
					// Get clan list
					p = new BNCSPacket(BNCSCommandIDs.SID_CLANMEMBERLIST);
					p.writeDWord(0);	// Cookie
					p.SendPacket(dos, cs.packetLog);
					break;
				}
				
				// SID_CLANQUITNOTIFY
				// SID_CLANINVITATION
				// SID_CLANREMOVEMEMBER
				// SID_CLANINVITATIONRESPONSE
				
				case BNCSCommandIDs.SID_CLANRANKCHANGE: {
					int cookie = is.readDWord();
					byte status = is.readByte();
					
					Object obj = CookieUtility.destroyCookie(cookie);
					String statusCode = null;
					switch(status) {
					case ClanStatusIDs.CLANSTATUS_SUCCESS:
						statusCode = "Successfully changed rank";
						break;
					case 0x01:
						statusCode = "Failed to change rank";
						break;
					case ClanStatusIDs.CLANSTATUS_TOO_SOON:
						statusCode = "Cannot change user's rank yet";
						break;
					case ClanStatusIDs.CLANSTATUS_NOT_AUTHORIZED:
						statusCode = "Not authorized to change user rank*";
						break;
					case 0x08:
						statusCode = "Not allowed to change user rank**";
						break;
					default:	statusCode = "Unknown ClanStatusID 0x" + Integer.toHexString(status);
					}
					
					recieveInfo(statusCode + "\n" + obj.toString());
					// TODO: clanRankChange(obj, status)
					
					break;
				}
				
				// TODO: SID_CLANMOTD
				
				case BNCSCommandIDs.SID_CLANMEMBERLIST: {
					/* (DWORD)		 Cookie
					 * (BYTE)		 Number of Members
					 * 
					 * For each member:
					 * (STRING) 	 Username
					 * (BYTE)		 Rank
					 * (BYTE)		 Online Status
					 * (STRING) 	 Location
					 */
					is.readDWord();
					byte numMembers = is.readByte();
					ClanMember[] members = new ClanMember[numMembers];
					
					for(int i = 0; i < numMembers; i++) {
						String uName = is.readNTString();
						byte uRank = is.readByte();
						byte uOnline = is.readByte();
						String uLocation = is.readNTString();
						
						members[i] = new ClanMember(uName, uRank, uOnline, uLocation);
					}
					
					clanMemberList(members);
					break;
				}
				
				// TODO: SID_CLANMEMBERREMOVED
				// TODO: SID_CLANMEMBERSTATUSCHANGE
				
				case BNCSCommandIDs.SID_CLANMEMBERRANKCHANGE: {
					/* (BYTE)		 Old rank
					 * (BYTE)		 New rank
					 * (STRING) 	 Clan member who changed your rank
					 */
					byte oldRank = is.readByte();
					byte newRank = is.readByte();
					String user = is.readNTString();
					recieveInfo("Rank changed from " + ClanRankIDs.ClanRank[oldRank] + " to " + ClanRankIDs.ClanRank[newRank] + " by " + user);
					clanMemberRankChange(oldRank, newRank, user);
					break;
				}
				
				// TODO: SID_CLANMEMBERINFORMATION
				
				default:
					recieveError("Unknown SID 0x" + Integer.toHexString(pr.packetId) + "\n" + bnubot.util.HexDump.hexDump(pr.data));
					break;
				}
			}
			
			sleep(10);
			yield();
		}
	}
	
	public boolean isOp() {
		return (myFlags & 0x02) == 0x02;
	}
	
	private void registerEmail() throws Exception {
		if(cs.email == null)
			return;
		if(cs.email.length() == 0)
			return;
		recieveInfo("Register email address: " + cs.email);
		BNCSPacket p = new BNCSPacket(BNCSCommandIDs.SID_SETEMAIL);
		p.writeNTString(cs.email);
		p.SendPacket(dos, cs.packetLog);
	}

	public void joinChannel(String channel) throws Exception {
		BNCSPacket p = new BNCSPacket(BNCSCommandIDs.SID_JOINCHANNEL);
		p.writeDWord(0); // nocreate join
		p.writeNTString(channel);
		p.SendPacket(dos, cs.packetLog);
	}
	
	public void sendChat(String text) {
		text = cleanText(text);
		
		try {
			if(text.substring(0, 3).equals("/j ")) {
				System.out.println("Sending join packet");
				joinChannel(text.substring(3));
				return;
			}
			if(text.substring(0, 6).equals("/join ")) {
				System.out.println("Sending join packet");
				joinChannel(text.substring(6));
				return;
			}
		} catch(Exception e) {}
		
		super.sendChat(text);
	}
	
	public void sendChatNow(String text) {
		super.sendChatNow(text);
		
		//Write the packet
		try {
			BNCSPacket p = new BNCSPacket(BNCSCommandIDs.SID_CHATCOMMAND);
			p.writeNTString(text);
			p.SendPacket(dos, cs.packetLog);
			
			if(text.charAt(0) != '/')
				recieveChat(myUser, myFlags, myPing, text);
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void setClanRank(String user, int newRank) throws Exception {
		LinkedList<Object> obj = new LinkedList<Object>();
		obj.add("This is the cookie for setRank:");
		obj.add(user);
		obj.add((Integer) newRank);
		
		int id = CookieUtility.createCookie(obj);
		
		BNCSPacket p = new BNCSPacket(BNCSCommandIDs.SID_CLANRANKCHANGE);
		p.writeDWord(id);		//Cookie
		p.writeNTString(user);	//Username
		p.writeByte(newRank);	//New rank
		p.SendPacket(dos, cs.packetLog);
	}
	
	public String toString() {
		if(myUser == null) {
			if(channelName != null)
				return channelName;
		} else {
			String out = myUser.getShortLogonName();
			if(channelName != null)
				out += " - " + channelName;
			return out;
		}
		
		return "BNU-Bot";
	}
	
	public void reconnect() {
		forceReconnect = true;
		setConnected(false);
	}
}
