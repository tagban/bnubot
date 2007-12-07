/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.settings;

import java.io.Serializable;

import net.bnubot.bot.gui.icons.IconsDotBniReader;

public class ConnectionSettings implements Serializable {
	private static final long serialVersionUID = -8169038278487314919L;
	
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
    public String profile;
	public String bncsServer;
	public int port;
	public String username;
	public String password;
	public String channel;
	public String cdkey;
	public String cdkey2;
	public byte product;
	
	public String myRealm;
	
	public ConnectionSettings(int botNum) {
		this.botNum = botNum;
		load();
		if(botNum == 1)
			IconsDotBniReader.initialize(this);
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

		switch(product) {
		case PRODUCT_STARCRAFT:
		case PRODUCT_BROODWAR:
		case PRODUCT_WAR2BNE:
		case PRODUCT_DIABLO2:
		case PRODUCT_LORDOFDESTRUCTION:
		case PRODUCT_JAPANSTARCRAFT:
		case PRODUCT_WARCRAFT3:
		case PRODUCT_THEFROZENTHRONE:
		case PRODUCT_DIABLO:
		case PRODUCT_DIABLOSHAREWARE:
		case PRODUCT_STARCRAFTSHAREWARE:
			break;
		default:
			return "Unsupported product";
		}
		
		switch(product) {
		case PRODUCT_DIABLO:
		case PRODUCT_DIABLOSHAREWARE:
		case PRODUCT_STARCRAFTSHAREWARE:
			break;
		default:
			if((cdkey == null) || (cdkey.length() == 0))
				return "CD key not set";
				break;
		}
		
		if((product == PRODUCT_LORDOFDESTRUCTION)
		|| (product == PRODUCT_THEFROZENTHRONE)) {
			if((cdkey2 == null) || (cdkey2.length() == 0))
				return "CD key 2 not set";
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
		Settings.write(header, "profile", profile);
		Settings.write(header, "server", bncsServer);
		Settings.writeInt(header, "port", port);
		Settings.write(header, "username", username);
		Settings.write(header, "password", password);
		Settings.write(header, "channel", channel);
		Settings.write(header, "cdkey", cdkey);
		Settings.write(header, "cdkey2", cdkey2);
		if(product != 0)
			Settings.write(header, "product", org.jbls.util.Constants.prods[product-1]);
		
		Settings.store();
	}
	
	public void load() {
		String header = Integer.toString(botNum);
		
		profile = 	Settings.read(header, "profile", "Profile" + botNum);
		bncsServer =Settings.read(header, "server", "useast.battle.net");
		port =		Settings.readInt(header, "port", 6112);
		username =	Settings.read(header, "username", null);
		password =	Settings.read(header, "password", null);
		channel =	Settings.read(header, "channel", "Clan BNU");
		cdkey =		Settings.read(header, "cdkey", null);
		cdkey2 =	Settings.read(header, "cdkey2", null);
		String prod = Settings.read(header, "product", null);
		product = 0;
		if(prod != null) {
			for(int i = 0; i < org.jbls.util.Constants.prods.length; i++) {
				if(org.jbls.util.Constants.prods[i].compareTo(prod) == 0)
					product = (byte)(i+1);
			}
		}
	}
}
