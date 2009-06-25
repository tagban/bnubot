/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.settings;

import java.io.Serializable;

import net.bnubot.bot.gui.KeyManager.CDKey;
import net.bnubot.core.bncs.ProductIDs;

/**
 * @author scotta
 */
public class ConnectionSettings implements Serializable {
	private static final String USEAST_BATTLE_NET = "useast.battle.net";
	private static final String USWEST_BATTLE_NET = "uswest.battle.net";
	private static final String EUROPE_BATTLE_NET = "europe.battle.net";
	private static final String ASIA_BATTLE_NET = "asia.battle.net";
	private static final long serialVersionUID = -8169038278487314919L;

	public static final String[] bncsServers = new String[] {
			USEAST_BATTLE_NET,
			USWEST_BATTLE_NET,
			EUROPE_BATTLE_NET,
			ASIA_BATTLE_NET,
			};

	public static final String[] dtServers = new String[] {
			"koolaid.sidoh.org",
			};

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
	public boolean enableBotNet;

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
			if(product == null)
				return "Product not set";

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

	public String getMyRealm() {
		if(connectionType.equals(ConnectionType.DigitalText))
			return "DigitalText";

		switch(product) {
		case WAR3:
		case W3XP: {
			if(server.equals(USEAST_BATTLE_NET))
				return "Azeroth";
			if(server.equals(USWEST_BATTLE_NET))
				return "Lordaeron";
			if(server.equals(EUROPE_BATTLE_NET))
				return "Northrend";
			if(server.equals(ASIA_BATTLE_NET))
				return "Kalimdor";
			break;
		}

		default: {
			if(server.equals(USEAST_BATTLE_NET))
				return "USEast";
			if(server.equals(USWEST_BATTLE_NET))
				return "USWest";
			if(server.equals(EUROPE_BATTLE_NET))
				return "Europe";
			if(server.equals(ASIA_BATTLE_NET))
				return "Asia";
			break;
		}

		}
		return null;
	}

	public void save() {
		String header = Integer.toString(botNum);
		SettingsSection ss = Settings.getSection(header);
		ss.write("profile", profile);
		ss.write("username", username);
		ss.write("password", password);
		ss.write("enablePlug", enablePlug);
		ss.write("enableBotNet", enableBotNet);
		ss.write("cdkey", cdkey);
		ss.write("cdkey2", cdkey2);
		ss.write("product", product);

		header = "Profile_" + profile;
		ss = Settings.getSection(header);
		ss.write("connectionType", connectionType);
		ss.write("server", server);
		ss.write("port", port);
		ss.write("channel", channel);
		ss.write("antiidle", antiIdle);
		ss.write("enableAntiidle", enableAntiIdle);
		ss.write("enableGreetings", enableGreetings);
		ss.write("trigger", trigger);
		ss.write("antiIdleTimer", antiIdleTimer);

		Settings.store();
	}

	public void load() {
		String header = Integer.toString(botNum);
		SettingsSection ss = Settings.getSection(header);
		profile = 	ss.read("profile", "Profile" + botNum);
		username =	ss.read("username", (String)null);
		password =	ss.read("password", (String)null);
		enablePlug =	ss.read("enablePlug", false);
		enableBotNet =	ss.read("enableBotNet", false);
		cdkey =		ss.read("cdkey", (String)null);
		cdkey2 =	ss.read("cdkey2", (String)null);
		product =	ss.read("product", ProductIDs.STAR);

		header = "Profile_" + profile;
		ss = Settings.getSection(header);
		connectionType =	ss.read("connectionType", ConnectionType.BNCS);
		server =	ss.read("server", USEAST_BATTLE_NET);
		port =	ss.read("port", 6112);
		channel =	ss.read("channel", "Clan BNU");
		trigger =	ss.read("trigger", "!");
		antiIdle =	ss.read("antiidle", "/me is a BNU-Bot %version%");
		enableAntiIdle =	ss.read("enableAntiidle", false);
		enableGreetings =	ss.read("enableGreetings", false);
		antiIdleTimer =	ss.read("antiIdleTimer", 5);
	}

	/**
	 * @return true if required values have been set
	 */
	public boolean isInitialized() {
		return (username != null);
	}

	private String formatCDKey(CDKey key) {
		if(key == null)
			return null;
		String out = new String(key.getKey());
		out = out.replaceAll("-", "");
		out = out.replaceAll(" ", "");
		out = out.replaceAll("\t", "");
		return out.toUpperCase();
	}

	public void setCDKey(CDKey key) {
		cdkey = formatCDKey(key);
	}

	public void setCDKey2(CDKey key) {
		cdkey2 = formatCDKey(key);
	}
}
