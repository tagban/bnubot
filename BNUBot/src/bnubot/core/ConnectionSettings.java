package bnubot.core;

import java.io.Serializable;

import bnubot.util.Ini;


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
	public boolean enableAntiIdle;
	public String antiIdle;
	public boolean autoconnect;
	public boolean enableCLI;
	public boolean enableGUI;
	public boolean enableCommands;
	public boolean enableTrivia;
	public boolean packetLog;
	public boolean whisperBack;
	public long recruitAccess;
	public String recruitTagPrefix;
	public String recruitTagSuffix;
	
	public String myRealm;
	
	public ConnectionSettings() {
		
	}
	
	public String isValid() {
		if(bncsServer == null)
			return "Server not set";
		if(bncsServer.length() == 0)
			return "Server not set";
		
		if(port <= 0)
			return "Port invalid";
		
		if(username == null)
			return "Username not set";
		if(username.length() == 0)
			return "Username not set";
		
		if(password == null)
			return "Password not set";
		if(password.length() == 0)
			return "Password not set";
		
		if(channel == null)
			return "Channel unset";
		if(channel.length() == 0)
			return "Channel unset";
		
		if(trigger == null)
			return "Trigger not set";
		if(trigger.length() != 1)
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
			if(cdkey == null)
				return "CD key not set";
			if(cdkey.length() == 0)
				return "CD key not set";
				break;
		}
		
		if(product == PRODUCT_LORDOFDESTRUCTION) {
			if(cdkeyLOD == null)
				return "LOD CD key not set";
			if(cdkeyLOD.length() == 0)
				return "LOD CD key not set";
		}
		
		if(product == PRODUCT_THEFROZENTHRONE) {
			if(cdkeyTFT == null)
				return "TFT CD key not set";
			if(cdkeyTFT.length() == 0)
				return "TFT CD key not set";
		}
		
		myRealm = getMyRealm();
		if(myRealm == null) {
			return "I don't know what realm I will be on";
		}
		
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
		String file = "settings.ini";
		String header = Integer.toString(botNum);
		Ini.WriteIni(file, header, "server", bncsServer);
		Ini.WriteIni(file, header, "port", Integer.toString(port));
		Ini.WriteIni(file, header, "bnlsserver", bnlsServer);
		Ini.WriteIni(file, header, "username", username);
		Ini.WriteIni(file, header, "password", password);
		Ini.WriteIni(file, header, "email", email);
		Ini.WriteIni(file, header, "channel", channel);
		Ini.WriteIni(file, header, "cdkey", cdkey);
		Ini.WriteIni(file, header, "cdkeyLOD", cdkeyLOD);
		Ini.WriteIni(file, header, "cdkeyTFT", cdkeyTFT);
		if(product != 0)
		Ini.WriteIni(file, header, "product", util.Constants.prods[product-1]);
		Ini.WriteIni(file, header, "colorScheme", Byte.toString(colorScheme));
		Ini.WriteIni(file, header, "trigger", trigger);
		Ini.WriteIni(file, header, "antiidle", antiIdle);
		Ini.WriteIni(file, header, "enableAntiidle", Boolean.toString(enableAntiIdle));
		Ini.WriteIni(file, header, "autoconnect", Boolean.toString(autoconnect));
		Ini.WriteIni(file, header, "enableCLI", Boolean.toString(enableCLI));
		Ini.WriteIni(file, header, "enableGUI", Boolean.toString(enableGUI));
		Ini.WriteIni(file, header, "enableCommands", Boolean.toString(enableCommands));
		Ini.WriteIni(file, header, "enableTrivia", Boolean.toString(enableTrivia));
		Ini.WriteIni(file, header, "packetLog", Boolean.toString(packetLog));
		Ini.WriteIni(file, header, "whisperBack", Boolean.toString(whisperBack));
		Ini.WriteIni(file, header, "recruitAccess", Long.toString(recruitAccess));
		Ini.WriteIni(file, header, "recruitTagPrefix", recruitTagPrefix);
		Ini.WriteIni(file, header, "recruitTagSuffix", recruitTagSuffix);
	}
	
	public void load(int botNum) {
		String file = "settings.ini";
		String header = Integer.toString(botNum);
		
		this.botNum = botNum;
		bncsServer =Ini.ReadIni(file, header, "server", "useast.battle.net");
		port = Integer.parseInt(
					Ini.ReadIni(file, header, "port", "6112"));
		bnlsServer =Ini.ReadIni(file, header, "bnlsserver", "logon.berzerkerjbls.com");
		username =	Ini.ReadIni(file, header, "username", null);
		password =	Ini.ReadIni(file, header, "password", null);
		email =		Ini.ReadIni(file, header, "email", null);
		channel =	Ini.ReadIni(file, header, "channel", "Clan BNU");
		cdkey =		Ini.ReadIni(file, header, "cdkey", null);
		cdkeyLOD =	Ini.ReadIni(file, header, "cdkeyLOD", null);
		cdkeyTFT =	Ini.ReadIni(file, header, "cdkeyTFT", null);
		String prod = Ini.ReadIni(file, header, "product", null);
		product = 0;
		if(prod != null) {
			for(int i = 0; i < util.Constants.prods.length; i++) {
				if(util.Constants.prods[i].compareTo(prod) == 0)
					product = (byte)(i+1);
			}
		}
		colorScheme = Byte.parseByte(
					Ini.ReadIni(file, header, "colorScheme", "1"));
		trigger = 	Ini.ReadIni(file, header, "trigger", "!");
		antiIdle = 	Ini.ReadIni(file, header, "antiidle", "/me is a BNU-Bot");
		enableAntiIdle = Boolean.parseBoolean(
				Ini.ReadIni(file, header, "enableAntiidle", "false"));
		autoconnect = Boolean.parseBoolean(
					Ini.ReadIni(file, header, "autoconnect", "false"));
		enableCLI = Boolean.parseBoolean(
					Ini.ReadIni(file, header, "enableCLI", "false"));
		enableGUI = Boolean.parseBoolean(
					Ini.ReadIni(file, header, "enableGUI", "true"));
		enableCommands = Boolean.parseBoolean(
					Ini.ReadIni(file, header, "enableCommands", "false"));
		enableTrivia = Boolean.parseBoolean(
				Ini.ReadIni(file, header, "enableTrivia", "false"));
		packetLog = Boolean.parseBoolean(
					Ini.ReadIni(file, header, "packetLog", "false"));
		whisperBack = Boolean.parseBoolean(
					Ini.ReadIni(file, header, "whisperBack", "true"));
		recruitAccess = Long.parseLong(
				Ini.ReadIni(file, header, "recruitAccess", "10"));
		recruitTagPrefix =	Ini.ReadIni(file, header, "recruitTagPrefix", "BNU-");
		recruitTagSuffix =	Ini.ReadIni(file, header, "recruitTagSuffix", null);
	}
}
