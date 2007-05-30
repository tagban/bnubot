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
	
	public String server;
	public int port;
	public String username;
	public String password;
	public String email;
	public String cdkey;
	public String cdkeyLOD;
	public String cdkeyTFT;
	public byte product;
	public boolean autoconnect;
	public boolean enableCLI;
	public boolean enableGUI;
	public boolean packetLog;
	
	public ConnectionSettings() {
		
	}
	
	public boolean isValid() {
		if(server == null)
			return false;
		if(server.length() == 0)
			return false;
		if(port <= 0)
			return false;
		if(username == null)
			return false;
		if(username.length() == 0)
			return false;
		if(password == null)
			return false;
		if(password.length() == 0)
			return false;
		
		switch(product) {
		case PRODUCT_DIABLOSHAREWARE:
		case PRODUCT_STARCRAFTSHAREWARE:
			break;
		default:
			if(cdkey == null)
				return false;
			if(cdkey.length() == 0)
				return false;
				break;
		}
		
		if(product == PRODUCT_LORDOFDESTRUCTION) {
			if(cdkeyLOD == null)
				return false;
			if(cdkeyLOD.length() == 0)
				return false;
		}
		
		if(product == PRODUCT_THEFROZENTHRONE) {
			if(cdkeyTFT == null)
				return false;
			if(cdkeyTFT.length() == 0)
				return false;
		}
		
		return true;
	}
	
	public void save() {
		String file = "settings.ini";
		String header = "test";
		Ini.WriteIni(file, header, "server", server);
		Ini.WriteIni(file, header, "port", Integer.toString(port));
		Ini.WriteIni(file, header, "username", username);
		Ini.WriteIni(file, header, "password", password);
		Ini.WriteIni(file, header, "email", email);
		Ini.WriteIni(file, header, "cdkey", cdkey);
		Ini.WriteIni(file, header, "cdkeyLOD", cdkeyLOD);
		Ini.WriteIni(file, header, "cdkeyTFT", cdkeyTFT);
		if(product != 0)
		Ini.WriteIni(file, header, "product", util.Constants.prods[product-1]);
		Ini.WriteIni(file, header, "autoconnect", Boolean.toString(autoconnect));
		Ini.WriteIni(file, header, "enableCLI", Boolean.toString(enableCLI));
		Ini.WriteIni(file, header, "enableGUI", Boolean.toString(enableGUI));
		Ini.WriteIni(file, header, "packetLog", Boolean.toString(packetLog));
	}
	
	public void load() {
		String file = "settings.ini";
		String header = "test";

		server =	Ini.ReadIni(file, header, "server", "useast.battle.net");
		port = Integer.parseInt(
					Ini.ReadIni(file, header, "port", "6112"));
		username =	Ini.ReadIni(file, header, "username", null);
		password =	Ini.ReadIni(file, header, "password", null);
		email =		Ini.ReadIni(file, header, "email", null);
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
		autoconnect = Boolean.parseBoolean(
					Ini.ReadIni(file, header, "autoconnect", "false"));
		enableCLI = Boolean.parseBoolean(
					Ini.ReadIni(file, header, "enableCLI", "false"));
		enableGUI = Boolean.parseBoolean(
					Ini.ReadIni(file, header, "enableGUI", "true"));
		packetLog = Boolean.parseBoolean(
					Ini.ReadIni(file, header, "packetLog", "false"));
	}
}
