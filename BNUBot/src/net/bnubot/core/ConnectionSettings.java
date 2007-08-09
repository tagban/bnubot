/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.core;

import java.io.Serializable;

import net.bnubot.util.Settings;


public class ConnectionSettings implements Serializable {
	private static final long serialVersionUID = 0L;

	// These are for BNLS/JBLS
    public static final byte PRODUCT_STARCRAFT         = (byte)0x01; //Fully supported
    public static final byte PRODUCT_BROODWAR          = (byte)0x02; //Fully Supported
    public static final byte PRODUCT_WAR2BNE           = (byte)0x03; //Fully Supported
    public static final byte PRODUCT_DIABLO2           = (byte)0x04; //Fully Supported
    public static final byte PRODUCT_LORDOFDESTRUCTION = (byte)0x05; //Fully Supported
    public static final byte PRODUCT_JAPANSTARCRAFT    = (byte)0x06; //Fully Supported
    public static final byte PRODUCT_WARCRAFT3         = (byte)0x07; //Fully Supported
    public static final byte PRODUCT_THEFROZENTHRONE   = (byte)0x08; //Fully Supported
    public static final byte PRODUCT_DIABLO            = (byte)0x09; //Fully Supported
    public static final byte PRODUCT_DIABLOSHAREWARE   = (byte)0x0A; //Fully Supported
    public static final byte PRODUCT_STARCRAFTSHAREWARE= (byte)0x0B; //Fully Supported

    public int botNum;
	public String bncsServer;
	public int port;
	public String bnlsServer;
	public String username;
	public String password;
	public String email;
	public String channel;
	public String cdkey;
	public String cdkeyLOD;
	public String cdkeyTFT;
	public byte product;
	public byte colorScheme;
	public String trigger;
	public int antiIdleTimer;
	public boolean enableAntiIdle;
	public boolean enableGreetings;
	public String antiIdle;
	public boolean autoconnect;
	public boolean enableCLI;
	public boolean enableGUI;
	public boolean enableCommands;
	public boolean enableTrivia;
	public long triviaRoundLength;
	public boolean enableFloodProtect;
	public boolean packetLog;
	public boolean whisperBack;
	public long recruitAccess;
	public String recruitTagPrefix;
	public String recruitTagSuffix;
	
	public String myRealm;
	
	public ConnectionSettings() {
		
	}
	
	public String isValid() {
		if((bncsServer == null) || (bncsServer.length() == 0))
			return "Server not set";
		
		if(port <= 0)
			return "Port invalid";
		
		if((username == null) || (username.length() == 0))
			return "Username not set";
		
		if((password == null) || (password.length() == 0))
			return "Password not set";
		
		if((channel == null) || (channel.length() == 0))
			return "Channel unset";
		
		if((trigger == null) || (trigger.length() != 1))
			return "Trigger invalid";

		switch(product) {
		case PRODUCT_DIABLO2:
		case PRODUCT_LORDOFDESTRUCTION:
		case PRODUCT_WARCRAFT3:
		case PRODUCT_THEFROZENTHRONE:
		case PRODUCT_STARCRAFT:
		case PRODUCT_BROODWAR:
		case PRODUCT_WAR2BNE:
			break;
		default:
			return "Unsupported product";	
		}
		
		switch(product) {
		case PRODUCT_DIABLOSHAREWARE:
		case PRODUCT_STARCRAFTSHAREWARE:
			break;
		default:
			if((cdkey == null) || (cdkey.length() == 0))
				return "CD key not set";
				break;
		}
		
		if(product == PRODUCT_LORDOFDESTRUCTION) {
			if((cdkeyLOD == null) || (cdkeyLOD.length() == 0))
				return "LOD CD key not set";
		}
		
		if(product == PRODUCT_THEFROZENTHRONE) {
			if((cdkeyTFT == null) || (cdkeyTFT.length() == 0))
				return "TFT CD key not set";
		}
		
		myRealm = getMyRealm();
		if(myRealm == null)
			return "I don't know what realm I will be on";
		
		return null;
	}
	
	private String getMyRealm() {
		switch(product) {
		case PRODUCT_WARCRAFT3:
		case PRODUCT_THEFROZENTHRONE: {
			if(bncsServer.equals("useast.battle.net"))
				return "Azeroth";
			if(bncsServer.equals("uswest.battle.net"))
				return "Lordaeron";
			if(bncsServer.equals("europe.battle.net"))
				return "Northrend";
			if(bncsServer.equals("asia.battle.net"))
				return "Kalimdor";
			break;
		}
		
		default: {
			if(bncsServer.equals("useast.battle.net"))
				return "USEast";
			if(bncsServer.equals("uswest.battle.net"))
				return "USWest";
			if(bncsServer.equals("europe.battle.net"))
				return "Europe";
			if(bncsServer.equals("asia.battle.net"))
				return "Asia";
			break;
		}
		
		}
		return null;
	}
	
	public void save() {
		String header = Integer.toString(botNum);
		Settings.write(header, "server", bncsServer);
		Settings.write(header, "port", Integer.toString(port));
		Settings.write(header, "bnlsserver", bnlsServer);
		Settings.write(header, "username", username);
		Settings.write(header, "password", password);
		Settings.write(header, "email", email);
		Settings.write(header, "channel", channel);
		Settings.write(header, "cdkey", cdkey);
		Settings.write(header, "cdkeyLOD", cdkeyLOD);
		Settings.write(header, "cdkeyTFT", cdkeyTFT);
		if(product != 0)
		Settings.write(header, "product", util.Constants.prods[product-1]);
		Settings.write(header, "autoconnect", Boolean.toString(autoconnect));
		
		if(botNum == 1) {
			Settings.write(header, "antiidle", antiIdle);
			Settings.write(header, "enableAntiidle", Boolean.toString(enableAntiIdle));
			Settings.write(header, "enableGreetings", Boolean.toString(enableGreetings));
			Settings.write(header, "antiIdleTimer", Integer.toString(antiIdleTimer));
			Settings.write(header, "autoconnect", Boolean.toString(autoconnect));
			Settings.write(header, "colorScheme", Byte.toString(colorScheme));
			Settings.write(header, "trigger", trigger);
			Settings.write(header, "enableCLI", Boolean.toString(enableCLI));
			Settings.write(header, "enableGUI", Boolean.toString(enableGUI));
			Settings.write(header, "enableCommands", Boolean.toString(enableCommands));
			Settings.write(header, "enableTrivia", Boolean.toString(enableTrivia));
			Settings.write(header, "enableFloodProtect", Boolean.toString(enableTrivia));
			Settings.write(header, "triviaRoundLength", Long.toString(triviaRoundLength));
			Settings.write(header, "packetLog", Boolean.toString(packetLog));
			Settings.write(header, "whisperBack", Boolean.toString(whisperBack));
			Settings.write(header, "recruitAccess", Long.toString(recruitAccess));
			Settings.write(header, "recruitTagPrefix", recruitTagPrefix);
			Settings.write(header, "recruitTagSuffix", recruitTagSuffix);
		}
		
		Settings.store();
	}
	
	public void load(int botNum) {
		String header = Integer.toString(botNum);
		
		this.botNum = botNum;
		bncsServer =Settings.read(header, "server", "useast.battle.net");
		port = Integer.parseInt(
					Settings.read(header, "port", "6112"));
		bnlsServer =Settings.read(header, "bnlsserver", "bnls.valhallalegends.com");
		username =	Settings.read(header, "username", null);
		password =	Settings.read(header, "password", null);
		email =		Settings.read(header, "email", null);
		channel =	Settings.read(header, "channel", "Clan BNU");
		cdkey =		Settings.read(header, "cdkey", null);
		cdkeyLOD =	Settings.read(header, "cdkeyLOD", null);
		cdkeyTFT =	Settings.read(header, "cdkeyTFT", null);
		String prod = Settings.read(header, "product", null);
		product = 0;
		if(prod != null) {
			for(int i = 0; i < util.Constants.prods.length; i++) {
				if(util.Constants.prods[i].compareTo(prod) == 0)
					product = (byte)(i+1);
			}
		}
		colorScheme = Byte.parseByte(
					Settings.read(header, "colorScheme", "1"));
		trigger = 	Settings.read(header, "trigger", "!");
		antiIdle = 	Settings.read(header, "antiidle", "/me is a BNU-Bot %version%");
		enableAntiIdle = Boolean.parseBoolean(
				Settings.read(header, "enableAntiidle", "false"));
		enableGreetings = Boolean.parseBoolean(
				Settings.read(header, "enableGreetings", "true"));
		antiIdleTimer = Integer.parseInt(
				Settings.read(header, "antiIdleTimer", "5"));
		autoconnect = Boolean.parseBoolean(
					Settings.read(header, "autoconnect", "true"));
		enableCLI = Boolean.parseBoolean(
					Settings.read(header, "enableCLI", "false"));
		enableGUI = Boolean.parseBoolean(
					Settings.read(header, "enableGUI", "true"));
		enableCommands = Boolean.parseBoolean(
					Settings.read(header, "enableCommands", "false"));
		enableTrivia = Boolean.parseBoolean(
				Settings.read(header, "enableTrivia", "false"));
		triviaRoundLength = Long.parseLong(
				Settings.read(header, "triviaRoundLength", "100"));
		enableFloodProtect = Boolean.parseBoolean(
				Settings.read(header, "enableFloodProtect", "true"));
		packetLog = Boolean.parseBoolean(
					Settings.read(header, "packetLog", "false"));
		whisperBack = Boolean.parseBoolean(
					Settings.read(header, "whisperBack", "true"));
		recruitAccess = Long.parseLong(
				Settings.read(header, "recruitAccess", "10"));
		recruitTagPrefix =	Settings.read(header, "recruitTagPrefix", "BNU-");
		recruitTagSuffix =	Settings.read(header, "recruitTagSuffix", null);
	}
}
