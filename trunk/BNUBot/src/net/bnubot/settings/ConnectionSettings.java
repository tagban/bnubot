/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.settings;

import java.io.Serializable;

import net.bnubot.bot.gui.icons.IconsDotBniReader;
import net.bnubot.core.bncs.ProductIDs;

public class ConnectionSettings implements Serializable {
	private static final long serialVersionUID = -8169038278487314919L;
	
	public enum ConnectionType {
		BNCS,
		DigitalText
	}

    // Connection-specific stuff
    public int botNum;
    public String profile;
	public String username;
	public String password;
	public boolean enablePlug;
	public String cdkey;
	public String cdkey2;
	public ProductIDs product;
	
	// Profile-specific stuff
	public ConnectionType connectionType;
	public String server;
	public int port;
	public String channel;
	public boolean enableAntiIdle;
	public String antiIdle;
	public String trigger;
	public boolean enableGreetings;
	public int antiIdleTimer;
	
	public String myRealm;
	
	public ConnectionSettings(int botNum) {
		this.botNum = botNum;
		load();
		if(botNum == 1)
			IconsDotBniReader.initialize(this);
	}
	
	public String isValid() {
		if((server == null) || (server.length() == 0))
			return "Server not set";
		
		if(port <= 0)
			return "Port invalid";
		
		if((username == null) || (username.length() == 0))
			return "Username not set";
		
		if((password == null) || (password.length() == 0))
			return "Password not set";
		
		if((channel == null) || (channel.length() == 0))
			return "Channel unset";

		switch(connectionType) {
		case DigitalText:
			break;
		case BNCS:
			switch(product) {
			case DRTL:
			case DSHR:
			case SSHR:
				break;
			default:
				if((cdkey == null) || (cdkey.length() == 0))
					return "CD key not set";
					break;
			}
			
			switch(product) {
			case D2XP:
			case W3XP:
				if((cdkey2 == null) || (cdkey2.length() == 0))
					return "CD key 2 not set";
			}
			
			break;
		}
		
		myRealm = getMyRealm();
		if(myRealm == null)
			return "I don't know what realm I will be on";
		
		if((trigger == null) || (trigger.length() != 1))
			return "Trigger invalid";
		
		return null;
	}
	
	private String getMyRealm() {
		if(connectionType.equals(ConnectionType.DigitalText))
			return "DigitalText";
		
		switch(product) {
		case WAR3:
		case W3XP: {
			if(server.equals("useast.battle.net"))
				return "Azeroth";
			if(server.equals("uswest.battle.net"))
				return "Lordaeron";
			if(server.equals("europe.battle.net"))
				return "Northrend";
			if(server.equals("asia.battle.net"))
				return "Kalimdor";
			break;
		}
		
		default: {
			if(server.equals("useast.battle.net"))
				return "USEast";
			if(server.equals("uswest.battle.net"))
				return "USWest";
			if(server.equals("europe.battle.net"))
				return "Europe";
			if(server.equals("asia.battle.net"))
				return "Asia";
			break;
		}
		
		}
		return null;
	}
	
	public void save() {
		String header = Integer.toString(botNum);
		Settings.write(header, "profile", profile);
		Settings.write(header, "username", username);
		Settings.write(header, "password", password);
		Settings.write(header, "enablePlug", enablePlug);
		Settings.write(header, "cdkey", cdkey);
		Settings.write(header, "cdkey2", cdkey2);
		Settings.write(header, "product", product);

		header = "Profile_" + profile;
		Settings.write(header, "connectionType", connectionType);
		Settings.write(header, "server", server);
		Settings.write(header, "port", port);
		Settings.write(header, "channel", channel);
		Settings.write(header, "antiidle", antiIdle);
		Settings.write(header, "enableAntiidle", enableAntiIdle);
		Settings.write(header, "enableGreetings", enableGreetings);
		Settings.write(header, "trigger", trigger);
		Settings.write(header, "antiIdleTimer", antiIdleTimer);
		
		Settings.store();
	}
	
	public void load() {
		String header = Integer.toString(botNum);
		
		profile = 	Settings.read(header, "profile", "Profile" + botNum);
		username =	Settings.read(header, "username", (String)null);
		password =	Settings.read(header, "password", (String)null);
		enablePlug = Settings.read(header, "enablePlug", false);
		cdkey =		Settings.read(header, "cdkey", (String)null);
		cdkey2 =	Settings.read(header, "cdkey2", (String)null);
		product = Settings.read(header, "product", ProductIDs.STAR);

		header = "Profile_" + profile;
		connectionType = Settings.read(header, "connectionType", ConnectionType.BNCS);
		server =	Settings.read(header, "server", "useast.battle.net");
		port =		Settings.read(header, "port", 6112);
		channel =	Settings.read(header, "channel", "Clan BNU");
		trigger = 	Settings.read(header, "trigger", "!");
		antiIdle = 	Settings.read(header, "antiidle", "/me is a BNU-Bot %version%");
		enableAntiIdle = Settings.read(header, "enableAntiidle", false);
		enableGreetings = Settings.read(header, "enableGreetings", false);
		antiIdleTimer = Settings.read(header, "antiIdleTimer", 5);
	}
}
