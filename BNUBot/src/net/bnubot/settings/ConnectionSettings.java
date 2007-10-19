/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.settings;

import java.io.Serializable;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.bnubot.util.Out;
import net.bnubot.util.TimeFormatter;
import net.bnubot.vercheck.CurrentVersion;
import net.bnubot.vercheck.ReleaseType;

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
	public String bncsServer;
	public int port;
	public String bnlsServer;
	public int bnlsPort;
	public String username;
	public String password;
	public String email;
	public String channel;
	public String cdkey;
	public String cdkeyLOD;
	public String cdkeyTFT;
	public byte product;

	public static byte colorScheme;
	public static String trigger;
	public static String antiIdle;
	public static boolean enableGreetings;
	public static int antiIdleTimer;
	public static boolean enableAntiIdle;
	public static boolean autoconnect;
	public static boolean enableCLI;
	public static boolean enableGUI;
	public static boolean enableLegacyIcons;
	public static boolean enableCommands;
	public static boolean enableTrivia;
	public static long triviaRoundLength;
	public static boolean enableFloodProtect;
	public static boolean packetLog;
	public static boolean whisperBack;
	public static long recruitAccess;
	public static String recruitTagPrefix;
	public static String recruitTagSuffix;
	public static ReleaseType releaseType;
	
	public String myRealm;
	
	private static String lookAndFeel;
	
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
	
	public static String isValidGlobal() {
		if((trigger == null) || (trigger.length() != 1))
			return "Trigger invalid";
		
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
	
	public static void setLookAndFeel(LookAndFeelInfo lafi) {
		try {
			UIManager.setLookAndFeel(lafi.getClassName());
			lookAndFeel = lafi.getName();
		} catch(Exception ex) {
			Out.exception(ex);
		}
	}
	
	public static String getLookAndFeel() {
		return lookAndFeel;
	}
	
	public void save() {
		String header = Integer.toString(botNum);
		Settings.write(header, "server", bncsServer);
		Settings.write(header, "port", Integer.toString(port));
		Settings.write(header, "bnlsserver", bnlsServer);
		Settings.write(header, "bnlsport", Integer.toString(bnlsPort));
		Settings.write(header, "username", username);
		Settings.write(header, "password", password);
		Settings.write(header, "email", email);
		Settings.write(header, "channel", channel);
		Settings.write(header, "cdkey", cdkey);
		Settings.write(header, "cdkeyLOD", cdkeyLOD);
		Settings.write(header, "cdkeyTFT", cdkeyTFT);
		if(product != 0)
			Settings.write(header, "product", org.jbls.util.Constants.prods[product-1]);
		
		Settings.store();
	}
	
	public static void globalSave() {
		Settings.write(null, "antiidle", antiIdle);
		Settings.write(null, "enableAntiidle", Boolean.toString(enableAntiIdle));
		Settings.write(null, "enableGreetings", Boolean.toString(enableGreetings));
		Settings.write(null, "antiIdleTimer", Integer.toString(antiIdleTimer));
		Settings.write(null, "autoconnect", Boolean.toString(autoconnect));
		Settings.write(null, "colorScheme", Byte.toString(colorScheme));
		Settings.write(null, "trigger", trigger);
		Settings.write(null, "enableCLI", Boolean.toString(enableCLI));
		Settings.write(null, "enableGUI", Boolean.toString(enableGUI));
		Settings.write(null, "enableLegacyIcons", Boolean.toString(enableLegacyIcons));
		Settings.write(null, "enableCommands", Boolean.toString(enableCommands));
		Settings.write(null, "enableTrivia", Boolean.toString(enableTrivia));
		Settings.write(null, "triviaRoundLength", Long.toString(triviaRoundLength));
		Settings.write(null, "enableFloodProtect", Boolean.toString(enableFloodProtect));
		Settings.write(null, "packetLog", Boolean.toString(packetLog));
		Settings.write(null, "whisperBack", Boolean.toString(whisperBack));
		Settings.write(null, "recruitAccess", Long.toString(recruitAccess));
		Settings.write(null, "recruitTagPrefix", recruitTagPrefix);
		Settings.write(null, "recruitTagSuffix", recruitTagSuffix);
		Settings.write(null, "lookAndFeel", lookAndFeel);
		Settings.write(null, "tsFormat", TimeFormatter.tsFormat);
		Settings.write(null, "releaseType", releaseType.toString());
	}
	
	public void load(int botNum) {
		String header = Integer.toString(botNum);
		
		this.botNum = botNum;
		bncsServer =Settings.read(header, "server", "useast.battle.net");
		port = Integer.parseInt(
					Settings.read(header, "port", "6112"));
		bnlsServer =Settings.read(header, "bnlsserver", "jbls.clanbnu.net");
		bnlsPort = Integer.parseInt(
					Settings.read(header, "bnlsport", "9367"));
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
			for(int i = 0; i < org.jbls.util.Constants.prods.length; i++) {
				if(org.jbls.util.Constants.prods[i].compareTo(prod) == 0)
					product = (byte)(i+1);
			}
		}
		
		globalLoad();
	}
	
	public static void globalLoad() {
		colorScheme = Byte.parseByte(
				Settings.read(null, "colorScheme", "1"));
		trigger = 	Settings.read(null, "trigger", "!");
		antiIdle = 	Settings.read(null, "antiidle", "/me is a BNU-Bot %version%");
		enableAntiIdle = Boolean.parseBoolean(
				Settings.read(null, "enableAntiidle", "false"));
		enableGreetings = Boolean.parseBoolean(
				Settings.read(null, "enableGreetings", "true"));
		antiIdleTimer = Integer.parseInt(
				Settings.read(null, "antiIdleTimer", "5"));
		autoconnect = Boolean.parseBoolean(
				Settings.read(null, "autoconnect", "true"));
		enableCLI = Boolean.parseBoolean(
				Settings.read(null, "enableCLI", "false"));
		enableGUI = Boolean.parseBoolean(
				Settings.read(null, "enableGUI", "true"));
		enableLegacyIcons = Boolean.parseBoolean(
				Settings.read(null, "enableLegacyIcons", "true"));
		enableCommands = Boolean.parseBoolean(
				Settings.read(null, "enableCommands", "false"));
		enableTrivia = Boolean.parseBoolean(
				Settings.read(null, "enableTrivia", "false"));
		triviaRoundLength = Long.parseLong(
				Settings.read(null, "triviaRoundLength", "100"));
		enableFloodProtect = Boolean.parseBoolean(
				Settings.read(null, "enableFloodProtect", "true"));
		packetLog = Boolean.parseBoolean(
				Settings.read(null, "packetLog", "false"));
		whisperBack = Boolean.parseBoolean(
				Settings.read(null, "whisperBack", "true"));
		recruitAccess = Long.parseLong(
				Settings.read(null, "recruitAccess", "10"));
		recruitTagPrefix =	Settings.read(null, "recruitTagPrefix", "BNU-");
		recruitTagSuffix =	Settings.read(null, "recruitTagSuffix", null);
		if(enableGUI) {
			String laf = Settings.read(null, "lookAndFeel", "Metal");
			for(LookAndFeelInfo lafi : UIManager.getInstalledLookAndFeels())
				if(lafi.getName().equals(laf))
					setLookAndFeel(lafi);
		}
		TimeFormatter.tsFormat =
			Settings.read(null, "tsFormat", TimeFormatter.tsFormat);
		releaseType = Enum.valueOf(ReleaseType.class,
				Settings.read(null, "releaseType", CurrentVersion.version().getReleaseType().name()));

		// Ensure that development builds check for development, and non-development builds don't
		ReleaseType rt = CurrentVersion.version().getReleaseType();
		if(rt.isDevelopment() || releaseType.isDevelopment())
			releaseType = rt;
	}
}
