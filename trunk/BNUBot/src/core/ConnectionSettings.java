package core;

import java.io.Serializable;

import util.Ini;

public class ConnectionSettings implements Serializable {
	private static final long serialVersionUID = 0L;

	// These are for BNLS/JBLS
    public static final byte PRODUCT_STARCRAFT         = 0x01; //Fully supported
    public static final byte PRODUCT_BROODWAR          = 0x02; //Fully Supported
    public static final byte PRODUCT_WAR2BNE           = 0x03; //Fully Supported
    public static final byte PRODUCT_DIABLO2           = 0x04; //Fully Supported
    public static final byte PRODUCT_LORDOFDESTRUCTION = 0x05; //Fully Supported
    public static final byte PRODUCT_JAPANSTARCRAFT    = 0x06; //Fully Supported
    public static final byte PRODUCT_WARCRAFT3         = 0x07; //Fully Supported
    public static final byte PRODUCT_THEFROZENTHRONE   = 0x08; //Fully Supported
    public static final byte PRODUCT_DIABLO            = 0x09; //Fully Supported
    public static final byte PRODUCT_DIABLOSHAREWARE   = 0x0A; //Fully Supported
    public static final byte PRODUCT_STARCRAFTSHAREWARE= 0x0B; //Fully Supported
	
	public ConnectionSettings() {
		
	}
	
	public void save() {
		String file = "settings.ini";
		String header = "test";
		Ini.WriteIni(file, header, "server", server);
		Ini.WriteIni(file, header, "port", Integer.toString(port));
		Ini.WriteIni(file, header, "username", username);
		Ini.WriteIni(file, header, "password", password);
		Ini.WriteIni(file, header, "cdkey", cdkey);
		Ini.WriteIni(file, header, "product", Byte.toString(product));
		Ini.WriteIni(file, header, "autoconnect", Boolean.toString(autoconnect));
	}
	
	public void load() {
		String file = "settings.ini";
		String header = "test";

		server =	Ini.ReadIni(file, header, "server", "useast.battle.net");
		port = Integer.parseInt(
					Ini.ReadIni(file, header, "port", "6112"));
		username =	Ini.ReadIni(file, header, "username", null);
		password =	Ini.ReadIni(file, header, "password", null);
		cdkey =		Ini.ReadIni(file, header, "cdkey", null);
		product = Byte.parseByte(
					Ini.ReadIni(file, header, "product", "-1"));
		autoconnect = Boolean.parseBoolean(
					Ini.ReadIni(file, header, "autoconnect", "false"));
	}
	
	public String server;
	public int port;
	public String username;
	public String password;
	public String cdkey;
	public byte product;
	public boolean autoconnect;
}
