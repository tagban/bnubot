package core;

import java.io.Serializable;

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
	
	public String server;
	public int port;
	public String username;
	public String password;
	public String cdkey;
	public byte product;
	public boolean autoconnect;
}
