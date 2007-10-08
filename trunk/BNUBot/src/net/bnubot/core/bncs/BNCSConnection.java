/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.core.bncs;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import net.bnubot.bot.CommandResponseCookie;
import net.bnubot.core.ChatQueue;
import net.bnubot.core.Connection;
import net.bnubot.core.UnsupportedFeatureException;
import net.bnubot.core.bnls.BNLSCommandIDs;
import net.bnubot.core.bnls.BNLSPacket;
import net.bnubot.core.bnls.BNLSPacketReader;
import net.bnubot.core.clan.ClanMember;
import net.bnubot.core.clan.ClanRankIDs;
import net.bnubot.core.clan.ClanStatusIDs;
import net.bnubot.core.friend.FriendEntry;
import net.bnubot.settings.ConnectionSettings;
import net.bnubot.util.BNetInputStream;
import net.bnubot.util.BNetUser;
import net.bnubot.util.CookieUtility;
import net.bnubot.util.HexDump;
import net.bnubot.util.Out;
import net.bnubot.util.StatString;
import net.bnubot.util.TimeFormatter;
import net.bnubot.vercheck.CurrentVersion;

import org.jbls.Hashing.BrokenSHA1;
import org.jbls.Hashing.DoubleHash;
import org.jbls.Hashing.HashMain;
import org.jbls.Hashing.SRP;

public class BNCSConnection extends Connection {
	public static final String[] clanRanks = {"Initiate", "Peon", "Grunt", "Shaman", "Chieftain"};
	
	protected InputStream bnlsInputStream = null;
	protected OutputStream bnlsOutputStream = null;
	protected InputStream bncsInputStream = null;
	protected DataOutputStream bncsOutputStream = null;
	
	private int productID = 0;
	private int verByte;
	private Integer nlsRevision = null;
	private int serverToken = 0;
	private int clientToken = Math.abs(new Random().nextInt());
	private SRP srp = null;
	private byte proof_M2[] = null;
	private boolean forceReconnect = false;
	protected StatString myStatString = null;
	protected int myClan = 0;
	protected Byte myClanRank = null; 
	protected long lastNullPacket;

	public BNCSConnection(ConnectionSettings cs, ChatQueue cq) {
		super(cs, cq);
	}
	
	private void sendKeyOrPassword() throws Exception {
		BNCSPacket p;
		
		switch(productID) {
		case ProductIDs.PRODUCT_JSTR:
			p = new BNCSPacket(BNCSCommandIDs.SID_CDKEY);
			p.writeDWord(0); //Spawn
			p.writeNTString(cs.cdkey);
			p.writeNTString(cs.username);
			p.SendPacket(bncsOutputStream);
			break;
			
		case ProductIDs.PRODUCT_W2BN:
			byte[] keyHash = HashMain.hashW2Key(clientToken, serverToken, cs.cdkey).getBuffer();
			if(keyHash.length != 40)
				throw new Exception("Invalid keyHash length");
			
			p = new BNCSPacket(BNCSCommandIDs.SID_CDKEY2);
			p.writeDWord(0); //Spawn
			p.write(keyHash);
			p.writeNTString(cs.username);
			p.SendPacket(bncsOutputStream);
			break;
			
		default:
			sendPassword();
			break;
		}
	}
	
	private void sendPassword() throws Exception {
		if((nlsRevision == null) || (nlsRevision == 0)) {
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
			p.SendPacket(bncsOutputStream);
		} else {
			srp = new SRP(cs.username, cs.password);
			srp.set_NLS(nlsRevision);
			byte A[] = srp.get_A();
			
			if(A.length != 32)
				throw new Exception("Invalid A length");
			
			BNCSPacket p = new BNCSPacket(BNCSCommandIDs.SID_AUTH_ACCOUNTLOGON);
			p.write(A);
			p.writeNTString(cs.username);
			p.SendPacket(bncsOutputStream);
		}
	}
	
	private void enterChat() throws Exception {
		BNCSPacket p = new BNCSPacket(BNCSCommandIDs.SID_ENTERCHAT);
		p.writeNTString("");
		p.writeNTString("");
		p.SendPacket(bncsOutputStream);
	}
	
	private void getChannelList() throws Exception {
		BNCSPacket p = new BNCSPacket(BNCSCommandIDs.SID_GETCHANNELLIST);
		p.writeDWord(productID);
		p.SendPacket(bncsOutputStream);
	}

	public void run() {
		while(true) {
			try {
				if(cs.isValid() != null) {
					recieveError(cs.isValid());
					int i = 1000;
					while(--i > 0) {
						yield();
						sleep(10);
					}
					continue;
				}
				
				if(forceReconnect) {
					forceReconnect = false;
				} else {
					if(!ConnectionSettings.autoconnect) {
						while(!isConnected()) {
							yield();
							sleep(10);
						}
					}
				}
				
				nlsRevision = null;
				productID = ProductIDs.ProductID[cs.product-1];
				verByte = HashMain.getVerByte(cs.product);
				
				// Set up BNLS
				setBNLSConnected(true);
				bnlsInputStream = bnlsSocket.getInputStream();
				bnlsOutputStream = bnlsSocket.getOutputStream();
				
				BNLS_REQUESTVERSIONBYTE: {
					BNLSPacket vbPacket = new BNLSPacket(BNLSCommandIDs.BNLS_REQUESTVERSIONBYTE);
					vbPacket.writeDWord(cs.product);
					vbPacket.SendPacket(bnlsOutputStream);
					
					BNetInputStream vbInputStream = new BNLSPacketReader(bnlsInputStream).getInputStream();
					int vbProduct = vbInputStream.readDWord();
					if(vbProduct == 0) {
						recieveError("BNLS_REQUESTVERSIONBYTE failed.");
						break BNLS_REQUESTVERSIONBYTE;
					}
					int vb = vbInputStream.readWord();
					
					if(vb != verByte) {
						recieveInfo("BNLS_REQUESTVERSIONBYTE: 0x" + Integer.toHexString(vb) + ".");
						verByte = vb;
					}
				}
				
				// Set up BNCS
				setConnected(true);
				bncsInputStream = socket.getInputStream();
				bncsOutputStream = new DataOutputStream(socket.getOutputStream());
				
				// Game
				bncsOutputStream.writeByte(0x01);
				
				BNCSPacket p;
				Locale loc = Locale.getDefault();
				String prodLang = loc.getLanguage() + loc.getCountry();
				int tzBias = TimeZone.getDefault().getOffset(System.currentTimeMillis()) / -60000;
				
				switch(cs.product) {
				case ConnectionSettings.PRODUCT_STARCRAFT:
				case ConnectionSettings.PRODUCT_BROODWAR:
				case ConnectionSettings.PRODUCT_DIABLO2:
				case ConnectionSettings.PRODUCT_LORDOFDESTRUCTION:
				case ConnectionSettings.PRODUCT_WARCRAFT3:
				case ConnectionSettings.PRODUCT_THEFROZENTHRONE: {
					p = new BNCSPacket(BNCSCommandIDs.SID_AUTH_INFO);
					p.writeDWord(0);							// Protocol ID (0)
					p.writeDWord(PlatformIDs.PLATFORM_IX86);	// Platform ID (IX86)
					p.writeDWord(productID);					// Product ID
					p.writeDWord(verByte);						// Version byte
					p.writeDWord(prodLang);						// Product language
					p.writeDWord(0);							// Local IP
					p.writeDWord(tzBias);						// TZ bias
					p.writeDWord(0x409);						// Locale ID
					p.writeDWord(0x409);						// Language ID
					p.writeNTString(loc.getISO3Country());		// Country abreviation
					p.writeNTString(loc.getDisplayCountry());	// Country
					p.SendPacket(bncsOutputStream);
					break;
				}

				case ConnectionSettings.PRODUCT_DIABLO:
				case ConnectionSettings.PRODUCT_DIABLOSHAREWARE:
				case ConnectionSettings.PRODUCT_STARCRAFTSHAREWARE:
				case ConnectionSettings.PRODUCT_JAPANSTARCRAFT:
				case ConnectionSettings.PRODUCT_WAR2BNE: {
					if(cs.product == ConnectionSettings.PRODUCT_STARCRAFTSHAREWARE) {
						p = new BNCSPacket(BNCSCommandIDs.SID_CLIENTID);
						p.writeDWord(0);	// Registration Version
						p.writeDWord(0);	// Registration Authority
						p.writeDWord(0);	// Account Number
						p.writeDWord(0);	// Registration Token
						p.writeByte(0);		// LAN computer name
						p.writeByte(0);		// LAN username
						p.SendPacket(bncsOutputStream);
					} else {
						p = new BNCSPacket(BNCSCommandIDs.SID_CLIENTID2);
						p.writeDWord(1);	// Server version
						p.writeDWord(0);	// Registration Version
						p.writeDWord(0);	// Registration Authority
						p.writeDWord(0);	// Account Number
						p.writeDWord(0);	// Registration Token
						p.writeByte(0);		// LAN computer name
						p.writeByte(0);		// LAN username
						p.SendPacket(bncsOutputStream);
					}
					
					p = new BNCSPacket(BNCSCommandIDs.SID_LOCALEINFO);
					p.writeQWord(0);		// System time
					p.writeQWord(0);		// Local time
					p.writeDWord(tzBias);	// TZ bias
					p.writeDWord(0x409);	// SystemDefaultLCID
					p.writeDWord(0x409);	// UserDefaultLCID
					p.writeDWord(0x409);	// UserDefaultLangID
					p.writeNTString("ena");	// Abbreviated language name
					p.writeNTString("1");	// Country code
					p.writeNTString(loc.getISO3Country());	// Abbreviated country name
					p.writeNTString(loc.getDisplayCountry());	// Country (English)
					p.SendPacket(bncsOutputStream);
					
					// TODO: JSTR/SSHR: SID_SYSTEMINFO
					
					p = new BNCSPacket(BNCSCommandIDs.SID_STARTVERSIONING);
					p.writeDWord(PlatformIDs.PLATFORM_IX86);	// Platform ID (IX86)
					p.writeDWord(productID);					// Product ID
					p.writeDWord(verByte);						// Version byte
					p.writeDWord(0);							// Unknown (0)
					p.SendPacket(bncsOutputStream);
					break;
				}
					
				default:
					recieveError("Don't know how to connect with product " + productID);
					setConnected(false);
					break;
				}
				
				//The connection loop
				connectedLoop();
					
				//Connection closed; do cleanup
				BNetUser.clearCache();
				myStatString = null;
				myUser = null;
				myClan = 0;
				myClanRank = null;
				titleChanged();
			} catch(SocketException e) {
			} catch(Exception e) {
				recieveError("Unhandled " + e.getClass().getSimpleName() + ": " + e.getMessage());
				Out.exception(e);
			}

			try { setConnected(false); } catch (Exception e) { }
			yield();

			//Wait a short time before allowing a reconnect
			int waitTime = 15000;
			if(forceReconnect)
				waitTime = 2000;
			try { sleep(waitTime); } catch (InterruptedException e1) { }
		}
	}
	
	private static ArrayList<String> antiIdles = null;
	private String getAntiIdle() {
		if(antiIdles == null) {
			antiIdles = new ArrayList<String>();
			BufferedReader is = null;
			try {
				File f = new File("anti-idle.txt");
				if(!f.exists()) {
					f.createNewFile();
					
					FileWriter os = new FileWriter(f);
					os.write("# Enter anti-idle messages in this file.\r\n");
					os.write("# \r\n");
					os.write("# Lines beginning with '#' are regarded as comments\r\n");
					os.write("# \r\n");
					os.write("\r\n");
					os.close();
				}
				is = new BufferedReader(new FileReader(f));
			} catch (Exception e) {
				Out.fatalException(e);
			}
			
			do {
				String line = null;
				try {
					line = is.readLine();
				} catch (IOException e) {
					Out.fatalException(e);
				}
				if(line == null)
					break;
				
				line = line.trim();
				if(line.length() == 0)
					continue;
				
				if(line.charAt(0) != '#')
					antiIdles.add(line);
			} while(true);
			
			try { is.close(); } catch (Exception e) {}
		}
		
		//grab one
		int i = antiIdles.size();
		if(i == 0)
			return ConnectionSettings.antiIdle;
		i = (int)Math.floor(Math.random() * i);
		return antiIdles.get(i);
	}
	
	private void connectedLoop() throws Exception {
		lastAntiIdle = System.currentTimeMillis();
		lastNullPacket = System.currentTimeMillis();
		
		while(connected && !socket.isClosed()) {
			long timeNow = System.currentTimeMillis();
			
			//Send null packets every 30 seconds
			if(true) {
				long timeSinceNullPacket = timeNow - lastNullPacket;
				//Wait 30 seconds
				timeSinceNullPacket /= 1000;
				if(timeSinceNullPacket > 30) {
					lastNullPacket = timeNow;
					BNCSPacket p = new BNCSPacket(BNCSCommandIDs.SID_NULL);
					p.SendPacket(bncsOutputStream);
				}
			}
			
			//Send anti-idles every 5 minutes
			if((channelName != null) && ConnectionSettings.enableAntiIdle) {
				long timeSinceAntiIdle = timeNow - lastAntiIdle;
				
				//Wait 5 minutes
				timeSinceAntiIdle /= 1000;
				timeSinceAntiIdle /= 60;
				if(timeSinceAntiIdle >= ConnectionSettings.antiIdleTimer) {
					lastAntiIdle = timeNow;
					sendChat(getAntiIdle());
				}
			}
			
			if(bncsInputStream.available() > 0) {
				BNCSPacketReader pr = new BNCSPacketReader(bncsInputStream);
				BNetInputStream is = pr.getData();
				
				switch(pr.packetId) {
				case BNCSCommandIDs.SID_OPTIONALWORK:
				case BNCSCommandIDs.SID_EXTRAWORK:
				case BNCSCommandIDs.SID_REQUIREDWORK:
					break;
					
				case BNCSCommandIDs.SID_NULL: {
					lastNullPacket = timeNow;
					BNCSPacket p = new BNCSPacket(BNCSCommandIDs.SID_NULL);
					p.SendPacket(bncsOutputStream);
					break;
				}
				
				case BNCSCommandIDs.SID_PING: {
					BNCSPacket p = new BNCSPacket(BNCSCommandIDs.SID_PING);
					p.writeDWord(is.readDWord());
					p.SendPacket(bncsOutputStream);
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
					byte[] ValueStr = is.readNTBytes();
					
					Out.debug(getClass(), "MPQ: " + MPQFileName);
				
					byte extraData[] = null;
					if(is.available() == 0x80) {
						extraData = new byte[0x80];
						is.read(extraData, 0, 0x80);
					}
					assert(is.available() == 0);
					
					// Hash the CD key
					byte keyHash[] = null;
					byte keyHash2[] = null;
					if(nlsRevision != null) {
						keyHash = HashMain.hashKey(clientToken, serverToken, cs.cdkey).getBuffer();
						if(cs.product == ConnectionSettings.PRODUCT_LORDOFDESTRUCTION)
							keyHash2 = HashMain.hashKey(clientToken, serverToken, cs.cdkeyLOD).getBuffer();
						if(cs.product == ConnectionSettings.PRODUCT_THEFROZENTHRONE)
							keyHash2 = HashMain.hashKey(clientToken, serverToken, cs.cdkeyTFT).getBuffer();
					}
					
					int exeHash = 0;
                	int exeVersion = 0;
                	byte[] exeInfo = null;
                	
                	try {
                		BNLSPacket bnlsOut = new BNLSPacket(BNLSCommandIDs.BNLS_VERSIONCHECKEX2);
                		bnlsOut.writeDWord(cs.product);
                		bnlsOut.writeDWord(0);	// Flags
                		bnlsOut.writeDWord(0);	// Cookie
                		bnlsOut.writeQWord(MPQFileTime);
                		bnlsOut.writeNTString(MPQFileName);
                		bnlsOut.writeNTString(ValueStr);
                		bnlsOut.SendPacket(bnlsSocket.getOutputStream());

                		InputStream bnlsInputStream = bnlsSocket.getInputStream();
                		long startTime = System.currentTimeMillis();
                		while(bnlsInputStream.available() < 3) {
                			Thread.sleep(10);
                			Thread.yield();

                			long timeElapsed = System.currentTimeMillis() - startTime;
                			if(timeElapsed > 5000)
                				throw new Exception("BNLS_VERSIONCHECKEX2 timeout");
                		}

                		BNLSPacketReader bpr = new BNLSPacketReader(bnlsInputStream);
                		BNetInputStream bnlsIn = bpr.getInputStream();
                		int success = bnlsIn.readDWord();
                		if(success != 1) {
                			Out.error(getClass(), "BNLS_VERSIONCHECKEX2 Failed\n" + HexDump.hexDump(bpr.getData()));
                			throw new Exception("BNLS failed to complete BNLS_VERSIONCHECKEX2 sucessfully");
                		}
                		exeVersion = bnlsIn.readDWord();
                		exeHash = bnlsIn.readDWord();
                		exeInfo = bnlsIn.readNTBytes();
                		bnlsIn.readDWord(); // cookie
                		int exeVerbyte = bnlsIn.readDWord();
                		assert(bnlsIn.available() == 0);

                		recieveInfo("Recieved version check from BNLS.");
                		
                		int verByte = HashMain.getVerByte(cs.product);
                		if(exeVerbyte != verByte)
                			recieveError("BNLS reported a different verByte(0x" + Integer.toHexString(exeVerbyte) + ") from the one we used (0x" + Integer.toHexString(verByte) + ")");

                		bnlsSocket.close();
                		bnlsSocket = null;
                	} catch(UnknownHostException e) {
                		recieveError("BNLS connection failed: " + e.getMessage());
                		setConnected(false);
                		break;
                	}
                	
                	if((exeVersion == 0) || (exeHash == 0) || (exeInfo == null) || (exeInfo.length == 0)) {
                		recieveError("Checkrevision failed.");
                		setConnected(false);
                		break;
                	}

					// Respond
                	if(nlsRevision != null) {
						BNCSPacket p = new BNCSPacket(BNCSCommandIDs.SID_AUTH_CHECK);
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
						p.SendPacket(bncsOutputStream);
                	} else {
                		/* (DWORD)		 Platform ID
                		 * (DWORD)		 Product ID
                		 * (DWORD)		 Version Byte
                		 * (DWORD)		 EXE Version
                		 * (DWORD)		 EXE Hash
                		 * (STRING) 	 EXE Information
                		 */
                		BNCSPacket p = new BNCSPacket(BNCSCommandIDs.SID_REPORTVERSION);
                		p.writeDWord(PlatformIDs.PLATFORM_IX86);
                		p.writeDWord(productID);
                		p.writeDWord(verByte);
						p.writeDWord(exeVersion);
						p.writeDWord(exeHash);
						p.writeNTString(exeInfo);
						p.SendPacket(bncsOutputStream);
                	}
					break;
				}

				case BNCSCommandIDs.SID_REPORTVERSION:
				case BNCSCommandIDs.SID_AUTH_CHECK: {
					int result = is.readDWord();
					String extraInfo = is.readNTString();
					assert(is.available() == 0);
					
					if(pr.packetId == BNCSCommandIDs.SID_AUTH_CHECK) {
						if(result != 0) {
							switch(result) {
							case 0x0101:
								recieveError("Invalid version.");
								break;
							case 0x102:
								recieveError("Game version must be downgraded: " + extraInfo);
								break;
							case 0x200:
								recieveError("Invalid CD key.");
								break;
							case 0x201:
								recieveError("CD key in use by " + extraInfo);
								break;
							case 0x202:
								recieveError("Banned key.");
								break;
							case 0x203:
								recieveError("Wrong product for CD key.");
								break;
							case 0x210:
								recieveError("Invalid second CD key.");
								break;
							case 0x211:
								recieveError("Second CD key in use by " + extraInfo);
								break;
							case 0x212:
								recieveError("Banned second key.");
								break;
							case 0x213:
								recieveError("Wrong product for second CD key.");
								break;
							default:
								recieveError("Unknown SID_AUTH_CHECK result 0x" + Integer.toHexString(result));
								break;
							}
							setConnected(false);
							break;
						}
						recieveInfo("Passed CD key challenge and CheckRevision.");
					} else {
						if(result != 2) {
							switch(result) {
							case 0:
								recieveError("Failed version check.");
								break;
							case 1:
								recieveError("Old game version.");
								break;
							case 3:
								recieveError("Reinstall required.");
								break;

							default:
								recieveError("Unknown SID_REPORTVERSION result 0x" + Integer.toHexString(result));
								break;
							}
							setConnected(false);
							break;
						}
						recieveInfo("Passed CheckRevision.");
					}
					
					sendKeyOrPassword();
					break;
				}

				case BNCSCommandIDs.SID_CDKEY:
				case BNCSCommandIDs.SID_CDKEY2: {
					/* (DWORD) Result
					 * (STRING) Key owner
					 * 
					 * 0x01: Ok
					 * 0x02: Invalid key
					 * 0x03: Bad product
					 * 0x04: Banned
					 * 0x05: In use 
					 */
					int result = is.readDWord();
					String keyOwner = is.readNTString();
					
					if(result != 1) {
						switch(result) {
						case 0x02:
							recieveError("Invalid CD key.");
							break;
						case 0x03:
							recieveError("Bad CD key product.");
							break;
						case 0x04:
							recieveError("CD key banned.");
							break;
						case 0x05:
							recieveError("CD key in use by " + keyOwner);
							break;
						default:
							recieveError("Unknown SID_CDKEY response 0x" + Integer.toHexString(result));
							break;
						}
						setConnected(false);
						break;
					}
					
					recieveInfo("CD key accepted.");
					sendPassword();
					break;
				}
				
				case BNCSCommandIDs.SID_AUTH_ACCOUNTLOGON: {
					/* (DWORD)		 Status
					 * (BYTE[32])	 Salt (socket)
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
				        
				        BNCSPacket p = new BNCSPacket(BNCSCommandIDs.SID_AUTH_ACCOUNTCREATE);
				        p.write(salt);
				        p.write(verifier);
				        p.writeNTString(cs.username);
				        p.SendPacket(bncsOutputStream);
						
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

					BNCSPacket p = new BNCSPacket(BNCSCommandIDs.SID_AUTH_ACCOUNTLOGONPROOF);
					p.write(M1);
					p.SendPacket(bncsOutputStream);
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
						sendKeyOrPassword();
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
						additionalInfo = is.readNTStringUTF8();
					
					switch(status) {
					case 0x00:
						break;
					case 0x02:
						recieveError("Incorrect password.");
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
					enterChat();
					break;
				}
				
				case BNCSCommandIDs.SID_LOGONRESPONSE2: {
					int result = is.readDWord();
					switch(result) {
					case 0x00:	// Success
						recieveInfo("Login successful; entering chat.");
						enterChat();
						getChannelList();
						joinChannel(cs.channel);
						break;
					case 0x01:	// Account doesn't exist
						recieveInfo("Account doesn't exist; creating...");
						
						int[] passwordHash = BrokenSHA1.calcHashBuffer(cs.password.toLowerCase().getBytes());
						
						BNCSPacket p = new BNCSPacket(BNCSCommandIDs.SID_CREATEACCOUNT2);
						p.writeDWord(passwordHash[0]);
						p.writeDWord(passwordHash[1]);
						p.writeDWord(passwordHash[2]);
						p.writeDWord(passwordHash[3]);
						p.writeDWord(passwordHash[4]);
						p.writeNTString(cs.username);
						p.SendPacket(bncsOutputStream);
						break;
					case 0x02:	// Invalid password;
						recieveError("Incorrect password.");
						setConnected(false);
						break;
					case 0x06:	// Account is closed
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
						sendKeyOrPassword();
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
					myStatString = new StatString(is.readNTString());
					/*String accountName =*/ is.readNTString();
					
					myUser = BNetUser.getBNetUser(uniqueUserName, cs.myRealm);
					recieveInfo("Logged in as " + myUser.getFullLogonName() + ".");
					titleChanged();
					
					// We are officially logged in!
					
					// Get MOTD
					BNCSPacket p = new BNCSPacket(BNCSCommandIDs.SID_NEWS_INFO);
					p.writeDWord((int)(new java.util.Date().getTime() / 1000)); // timestamp
					p.SendPacket(bncsOutputStream);
					
					// Get friends list
					p = new BNCSPacket(BNCSCommandIDs.SID_FRIENDSLIST);
					p.SendPacket(bncsOutputStream);

					// Join home channel
					if(nlsRevision != null) {
						getChannelList();
						joinChannel(cs.channel);
					}
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
						String news = is.readNTStringUTF8().trim();
						if(timeStamp == 0)	// MOTD
							recieveInfo(news);
					}
					
					break;
				}
				
				case BNCSCommandIDs.SID_GETCHANNELLIST: {
					String channelList = null;
					do {
						String s = is.readNTString();
						if(s.length() == 0)
							break;
						if(channelList == null)
							channelList = s;
						else
							channelList += ", " + s;
					} while(true);
					recieveInfo("Channels: " + channelList + ".");
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
					String text = null;
					StatString statstr = null;
					
					switch(eid) {
					case BNCSChatEventIDs.EID_SHOWUSER:
					case BNCSChatEventIDs.EID_USERFLAGS:
					case BNCSChatEventIDs.EID_JOIN:
						statstr = is.readStatString();
						break;
					default:
						text = is.readNTString();
						break;
					}
					
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
						switch(productID) {
						case ProductIDs.PRODUCT_D2DV:
						case ProductIDs.PRODUCT_D2XP:
							int asterisk = username.indexOf('*');
							if(asterisk >= 0)
								username = username.substring(asterisk+1);
							break;
						}
						
						user = BNetUser.getBNetUser(username, cs.myRealm);
						user.setFlags(flags);
						user.setPing(ping);
						break;
					}
					
					switch(eid) {
					case BNCSChatEventIDs.EID_SHOWUSER:
					case BNCSChatEventIDs.EID_USERFLAGS:
						channelUser(user, statstr);
						break;
					case BNCSChatEventIDs.EID_JOIN:
						channelJoin(user, statstr);
						break;
					case BNCSChatEventIDs.EID_LEAVE:
						channelLeave(user);
						break;
					case BNCSChatEventIDs.EID_TALK:
						recieveChat(user, text);
						break;
					case BNCSChatEventIDs.EID_EMOTE:
						recieveEmote(user, text);
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
						titleChanged();
						break;
					case BNCSChatEventIDs.EID_WHISPERSENT:
						whisperSent(user, text);
						break;
					case BNCSChatEventIDs.EID_WHISPER:
						whisperRecieved(user, text);
						break;
					case BNCSChatEventIDs.EID_CHANNELDOESNOTEXIST:
						BNCSPacket p = new BNCSPacket(BNCSCommandIDs.SID_JOINCHANNEL);
						p.writeDWord(2); // create join
						p.writeNTString(text);
						p.SendPacket(bncsOutputStream);
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
					String text = is.readNTStringUTF8();
					String caption = is.readNTStringUTF8();
					
					recieveInfo("<" + caption + "> " + text);
					break;
				}
				
				case BNCSCommandIDs.SID_FLOODDETECTED: {
					recieveError("You have been disconnected for flooding.");
					setConnected(false);
					break;
				}
				
				/*	.----------.
				 *	|  Realms  |
				 *	'----------'
				 */
				case BNCSCommandIDs.SID_QUERYREALMS2: {
					/* (DWORD)		 Unknown0
					 * (DWORD)		 Number of Realms
					 * 
					 * For each realm:
					 * (DWORD)		 UnknownR0
					 * (STRING) 	 Realm Name
					 * (STRING) 	 Realm Description
					 */
					is.readDWord();
					int numRealms = is.readDWord();
					String realms[] = new String[numRealms];
					for(int i = 0; i < numRealms; i++) {
						is.readDWord();
						realms[i] = is.readNTStringUTF8();
						is.readNTStringUTF8();
					}
					queryRealms2(realms);
					break;
				}
				
				case  BNCSCommandIDs.SID_LOGONREALMEX: {
					/* (DWORD)		 Cookie
					 * (DWORD)		 Status
					 * (DWORD[2])	 MCP Chunk 1
					 * (DWORD)		 IP
					 * (DWORD)		 Port
					 * (DWORD[12])	 MCP Chunk 2
					 * (STRING) 	 BNCS unique name
					 * (WORD)		 Unknown
					 */
					if(pr.packetLength < 12)
						throw new Exception("pr.packetLength < 12");
					else if(pr.packetLength == 12) {
						/*int cookie =*/ is.readDWord();
						int status = is.readDWord();
						switch(status) {
						case 0x80000001:
							recieveError("Realm is unavailable.");
							break;
						case 0x80000002:
							recieveError("Realm logon failed");
							break;
						default:
							throw new Exception("Unknown status code 0x" + Integer.toHexString(status));
						}
					} else {
						int MCPChunk1[] = new int[4];
						MCPChunk1[0] = is.readDWord();
						MCPChunk1[1] = is.readDWord();
						MCPChunk1[2] = is.readDWord();
						MCPChunk1[3] = is.readDWord();
						int ip = is.readDWord();
						int port = is.readDWord();
						port = ((port & 0xFF00) >> 8) | ((port & 0x00FF) << 8);
						int MCPChunk2[] = new int[12];
						MCPChunk2[0] = is.readDWord();
						MCPChunk2[1] = is.readDWord();
						MCPChunk2[2] = is.readDWord();
						MCPChunk2[3] = is.readDWord();
						MCPChunk2[4] = is.readDWord();
						MCPChunk2[5] = is.readDWord();
						MCPChunk2[6] = is.readDWord();
						MCPChunk2[7] = is.readDWord();
						MCPChunk2[8] = is.readDWord();
						MCPChunk2[9] = is.readDWord();
						MCPChunk2[10] = is.readDWord();
						MCPChunk2[11] = is.readDWord();
						String uniqueName = is.readNTString();
						/*int unknown =*/ is.readWord();
						logonRealmEx(MCPChunk1, ip, port, MCPChunk2, uniqueName);
					}
					
					break;
				}
				
				/*	.-----------.
				 *	|  Profile  |
				 *	'-----------'
				 */
				
				case BNCSCommandIDs.SID_READUSERDATA: {
					/* (DWORD)		 Number of accounts
					 * (DWORD)		 Number of keys
					 * (DWORD)		 Request ID
					 * (STRING[])	 Requested Key Values
					 */
					int numAccounts = is.readDWord();
					int numKeys = is.readDWord();
					@SuppressWarnings("unchecked")
					ArrayList<String> keys = (ArrayList<String>)CookieUtility.destroyCookie(is.readDWord());
					
					if(numAccounts != 1)
						throw new IllegalStateException("SID_READUSERDATA with numAccounts != 1");
					
					recieveInfo("Profile for " + keys.remove(0));
					for(int i = 0; i < numKeys; i++) {
						String key = keys.get(i);
						String value = is.readNTStringUTF8();
						if((key == null) || (key.length() == 0) || (value.length() == 0))
							continue;
						value = prettyProfileValue(key, value);
						recieveInfo(key + " = " + value);
					}
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
						String uLocationName = is.readNTStringUTF8();
						
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
					String fLocationName = is.readNTStringUTF8();
					
					friendsUpdate(new FriendEntry(fEntry, fStatus, fLocation, fProduct, fLocationName));
					break;
				}
				
				case BNCSCommandIDs.SID_FRIENDSADD: {
					/* (STRING) 	 Account
					 * (BYTE)		 Friend Type
					 * (BYTE)		 Friend Status
					 * (DWORD)		 ProductID
					 * (STRING) 	 Location
					 */
					String fAccount = is.readNTString();
					byte fLocation = is.readByte();
					byte fStatus = is.readByte();
					int fProduct = is.readDWord();
					String fLocationName = is.readNTStringUTF8();

					friendsAdd(new FriendEntry(fAccount, fStatus, fLocation, fProduct, fLocationName));
					break;
				}
				
				case BNCSCommandIDs.SID_FRIENDSREMOVE: {
					/* (BYTE)		 Entry Number
					 */
					byte entry = is.readByte();
					
					friendsRemove(entry);
					break;
				}
				
				case BNCSCommandIDs.SID_FRIENDSPOSITION: {
					/* (BYTE)		 Old Position
					 * (BYTE)		 New Position
					 */
					byte oldPosition = is.readByte();
					byte newPosition = is.readByte();
					
					friendsPosition(oldPosition, newPosition);
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
					myClan = is.readDWord();
					myClanRank = is.readByte();
					titleChanged();
					
					//TODO: clanInfo(myClan, myClanRank);
					
					// Get clan list
					BNCSPacket p = new BNCSPacket(BNCSCommandIDs.SID_CLANMEMBERLIST);
					p.writeDWord(0);	// Cookie
					p.SendPacket(bncsOutputStream);
					break;
				}
				
				// SID_CLANQUITNOTIFY
				case BNCSCommandIDs.SID_CLANINVITATION: {
					Object cookie = CookieUtility.destroyCookie(is.readDWord());
					byte status = is.readByte();
					
					String result;
					switch(status) {
					case 0x00:
						result = "Invitation accepted";
						break;
					case 0x04:
						result = "Invitation declined";
						break;
					case 0x05:
						result = "Failed to invite user";
						break;
					case 0x09:
						result = "Clan is full";
						break;
					default:
						result = "Unknown response 0x" + Integer.toHexString(status);
						break;
					}
					
					if(cookie instanceof CommandResponseCookie)
						((CommandResponseCookie)cookie).sendChat(this, result);
					else
						Out.info(getClass(), result);
					
					break;
				}
				
				// SID_CLANREMOVEMEMBER
				
				case BNCSCommandIDs.SID_CLANINVITATIONRESPONSE: {
					/* (DWORD) Cookie
					 * (DWORD) Clan tag
					 * (STRING) Clan name
					 * (STRING) Inviter 
					 */
					int cookie = is.readDWord();
					int clanTag = is.readDWord();
					String clanName = is.readNTString();
					String inviter = is.readNTString();
					
					recieveInfo("You were invited to Clan " + HexDump.DWordToPretty(clanTag) + " (" + clanName + ") by " + inviter);
					
					/* (DWORD) Cookie
					 * (DWORD) Clan tag
					 * (STRING) Inviter
					 * (BYTE) Response
					 * 
					 * Response:
					 * 0x04: Decline
					 * 0x06: Accept
					 */
					BNCSPacket p = new BNCSPacket(BNCSCommandIDs.SID_CLANINVITATIONRESPONSE);
					p.writeDWord(cookie);
					p.writeDWord(clanTag);
					p.writeNTString(inviter);
					p.writeByte(0x06);
					p.SendPacket(bncsOutputStream);
					break;
				}
				
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
						statusCode = "Cannot change user'socket rank yet";
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
				
				case BNCSCommandIDs.SID_CLANMOTD: {
					/* (DWORD)		 Cookie
					 * (DWORD)		 Unknown (0)
					 * (STRING) 	 MOTD
					 */
					int cookieId = is.readDWord();
					is.readDWord();
					String text = is.readNTStringUTF8();
					
					Object cookie = CookieUtility.destroyCookie(cookieId);
					clanMOTD(cookie, text);
					break;
				}
				
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
						String uLocation = is.readNTStringUTF8();
						
						members[i] = new ClanMember(uName, uRank, uOnline, uLocation);
					}
					
					clanMemberList(members);
					break;
				}
				
				case BNCSCommandIDs.SID_CLANMEMBERREMOVED: {
					/* (STRING) 	 Username
					 */
					String username = is.readNTString();
					clanMemberRemoved(username);
					break;
				}

				case BNCSCommandIDs.SID_CLANMEMBERSTATUSCHANGE: {
					/* (STRING) 	 Username
					 * (BYTE)		 Rank
					 * (BYTE)		 Status
					 * (STRING) 	 Location
					 */
					String username = is.readNTString();
					byte rank = is.readByte();
					byte status = is.readByte();
					String location = is.readNTStringUTF8();
					
					clanMemberStatusChange(new ClanMember(username, rank, status, location));
					break;
				}
				
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
					recieveError("Unknown SID 0x" + Integer.toHexString(pr.packetId) + "\n" + HexDump.hexDump(pr.data));
					break;
				}
			} else {
				sleep(10);
				yield();
			}
		}
	}
	
	public boolean isOp() {
		Integer myFlags = myUser.getFlags();
		if(myFlags == null)
			return false;
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
		p.SendPacket(bncsOutputStream);
	}

	public void joinChannel(String channel) throws Exception {
		BNCSPacket p = new BNCSPacket(BNCSCommandIDs.SID_JOINCHANNEL);
		p.writeDWord(0); // nocreate join
		p.writeNTString(channel);
		p.SendPacket(bncsOutputStream);
	}
	
	public void sendChat(String text) {
		text = cleanText(text);
		
		try {
			if(text.substring(0, 3).equals("/j ")) {
				joinChannel(text.substring(3));
				return;
			}
			if(text.substring(0, 6).equals("/join ")) {
				joinChannel(text.substring(6));
				return;
			}
		} catch(Exception e) {}
		
		super.sendChat(text);
	}
	
	public void sendChatNow(String text) {
		super.sendChatNow(text);
		

		switch(productID) {
		case ProductIDs.PRODUCT_D2DV:
		case ProductIDs.PRODUCT_D2XP:
			if((text.length() > 1) && (text.charAt(0) == '/')) {
				String cmd = text.substring(1);
				int i = cmd.indexOf(' ');
				if(i != -1) {
					String theRest = cmd.substring(i+1);
					cmd = cmd.substring(0, i);
				
					if(cmd.equals("w")
					|| cmd.equals("m")
					|| cmd.equals("whois")
					|| cmd.equals("ignre")
					|| cmd.equals("squelch")) {
						if(theRest.charAt(0) != '*')
							text = '/' + cmd + " *" + theRest;
					}
					
				}
			}
			break;
		}
		
		//Write the packet
		try {
			BNCSPacket p = new BNCSPacket(BNCSCommandIDs.SID_CHATCOMMAND);
			p.writeNTString(text);
			p.SendPacket(bncsOutputStream);
		} catch(SocketException e) {
			if(ConnectionSettings.autoconnect)
				reconnect();
			else
				setConnected(false);
			return;
		} catch (IOException e) {
			Out.fatalException(e);
		}

		if(text.charAt(0) != '/')
			recieveChat(myUser, text);
	}

	public void sendClanInvitation(Object cookie, String user) throws Exception {
		switch(productID) {
		case ProductIDs.PRODUCT_WAR3:
		case ProductIDs.PRODUCT_W3XP:
			break;
		default:
			throw new UnsupportedFeatureException("Only WAR3/W3XP support clans.");
		}
		
		if(myClanRank == null)
			throw new UnsupportedFeatureException("Must be in a clan");
		if(myClanRank < 3)
			throw new UnsupportedFeatureException("Must be " + clanRanks[3] + " or " + clanRanks[4] + " to invite");
		
		BNCSPacket p = new BNCSPacket(BNCSCommandIDs.SID_CLANINVITATION);
		p.writeDWord(CookieUtility.createCookie(cookie));	//Cookie
		p.writeNTString(user);	//Username
		p.SendPacket(bncsOutputStream);
	}
	
	public void sendClanRankChange(Object cookie, String user, int newRank) throws Exception {
		switch(productID) {
		case ProductIDs.PRODUCT_WAR3:
		case ProductIDs.PRODUCT_W3XP:
			break;
		default:
			throw new UnsupportedFeatureException("Only WAR3/W3XP support clans.");
		}
		
		BNCSPacket p = new BNCSPacket(BNCSCommandIDs.SID_CLANRANKCHANGE);
		p.writeDWord(CookieUtility.createCookie(cookie));	//Cookie
		p.writeNTString(user);	//Username
		p.writeByte(newRank);	//New rank
		p.SendPacket(bncsOutputStream);
	}

	public void sendClanMOTD(Object cookie) throws Exception {
		switch(productID) {
		case ProductIDs.PRODUCT_WAR3:
		case ProductIDs.PRODUCT_W3XP:
			break;
		default:
			throw new UnsupportedFeatureException("Only WAR3/W3XP support MOTD.");
		}
		
		BNCSPacket p = new BNCSPacket(BNCSCommandIDs.SID_CLANMOTD);
		p.writeDWord(CookieUtility.createCookie(cookie));
		p.SendPacket(bncsOutputStream);
	}
	
	public void sendClanSetMOTD(String text) throws Exception {
		switch(productID) {
		case ProductIDs.PRODUCT_WAR3:
		case ProductIDs.PRODUCT_W3XP:
			break;
		default:
			throw new UnsupportedFeatureException("Only WAR3/W3XP support MOTD.");
		}
		
		BNCSPacket p = new BNCSPacket(BNCSCommandIDs.SID_CLANSETMOTD);
		p.writeDWord(0);	//Cookie
		p.writeNTString(text);
		p.SendPacket(bncsOutputStream);
	}
	
	public void sendQueryRealms() throws Exception {
		switch(productID) {
		case ProductIDs.PRODUCT_D2DV:
		case ProductIDs.PRODUCT_D2XP:
			break;
		default:
			throw new UnsupportedFeatureException("Only D2DV/D2XP support realms");
		}
		
		/* (DWORD)		 Unused (0)
		 * (DWORD)		 Unused (0)
		 * (STRING) 	 Unknown (empty)
		 */
		BNCSPacket p = new BNCSPacket(BNCSCommandIDs.SID_QUERYREALMS2);
		p.SendPacket(bncsOutputStream);
	}
	
	public void sendLogonRealmEx(String realmTitle) throws Exception {
		switch(productID) {
		case ProductIDs.PRODUCT_D2DV:
		case ProductIDs.PRODUCT_D2XP:
			break;
		default:
			throw new UnsupportedFeatureException("Only D2DV/D2XP support realms");
		}
		
		/* (DWORD)		 Client key
		 * (DWORD[5])	 Hashed realm password
		 * (STRING) 	 Realm title
		 */
		int[] hash = DoubleHash.doubleHash("password", clientToken, serverToken);
		
		BNCSPacket p = new BNCSPacket(BNCSCommandIDs.SID_LOGONREALMEX);
		p.writeDWord(clientToken);
		p.writeDWord(hash[0]);
		p.writeDWord(hash[1]);
		p.writeDWord(hash[2]);
		p.writeDWord(hash[3]);
		p.writeDWord(hash[4]);
		p.writeNTString(realmTitle);
		p.SendPacket(bncsOutputStream);
	}
	
	private String prettyProfileValue(String key, String value) {
		if("System\\Account Created".equals(key)
		|| "System\\Last Logon".equals(key)
		|| "System\\Last Logoff".equals(key)) {
			String parts[] = value.split(" ", 2);
			long time = Long.parseLong(parts[0]);
			time <<= 32;
			time += Long.parseLong(parts[1]);
			
			return TimeFormatter.fileTime(time).toString();
		} else
		if("System\\Time Logged".equals(key)) {
			long time = Long.parseLong(value);
			time *= 1000;
			return TimeFormatter.formatTime(time);
		}
		
		return value;
	}
	public void sendProfile(String user) throws Exception {
		/*BNCSPacket p = new BNCSPacket(BNCSCommandIDs.SID_PROFILE);
		p.writeDWord(CookieUtility.createCookie(user));
		p.writeNTString(user);
		p.SendPacket(bncsOutputStream);*/
		/* (DWORD)		 Number of Accounts
		 * (DWORD)		 Number of Keys
		 * (DWORD)		 Request ID
		 * (STRING[])	 Requested Accounts
		 * (STRING[])	 Requested Keys
		 */
		ArrayList<String> keys = new ArrayList<String>();
		keys.add(user);
		keys.add("profile\\sex");
		keys.add("profile\\age");
		keys.add("profile\\location");
		keys.add("profile\\description");
		keys.add("profile\\dbkey1");
		keys.add("profile\\dbkey2");
		if(myUser.equals(user)) {
			keys.add("System\\Account Created");
			keys.add("System\\Last Logon");
			keys.add("System\\Last Logoff");
			keys.add("System\\Time Logged");
			keys.add("System\\Username");
		}
		
		BNCSPacket p = new BNCSPacket(BNCSCommandIDs.SID_READUSERDATA);
		p.writeDWord(1);
		p.writeDWord(keys.size() - 1);
		p.writeDWord(CookieUtility.createCookie(keys));
		p.writeNTString(user);
		for(int i = 1; i < keys.size(); i++)
			p.writeNTString(keys.get(i));
		p.SendPacket(bncsOutputStream);
	 	
	}
	
	public String toString() {
		String out = "BNU-Bot " + CurrentVersion.version();
		
		if(channelName != null)
			out += " - [ #" + channelName + " ]";
		
		if(myUser != null) {
			out += " - [ ";
			
			if(myClanRank != null) {
				out += "Clan ";
				out += HexDump.DWordToPretty(myClan);
				out += " ";
				out += clanRanks[myClanRank];
				out += " ";
			}
			out += myUser.getShortLogonName() + " ]";
		}
		
		return out;
	}
	
	public void reconnect() {
		forceReconnect = true;
		setConnected(false);
	}

	public int getProductID() {
		return productID;
	}
}