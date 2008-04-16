/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.settings;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.bnubot.JARLoader;
import net.bnubot.util.Out;
import net.bnubot.util.TimeFormatter;
import net.bnubot.vercheck.CurrentVersion;
import net.bnubot.vercheck.ReleaseType;

public class GlobalSettings {
	public static enum TrayIconMode {
		DISABLED,
		ENABLED,
		ALWAYS_DISPLAY_POPUPS;
		
		public boolean enableTray() {
			return(this != DISABLED);
		}
		
		public boolean alwaysDisplayPopups() {
			return(this == ALWAYS_DISPLAY_POPUPS);
		}
	}
	
	public static enum TabCompleteMode {
		CONTAINS_STRING,
		STARTS_WITH_STRING;
		
		public boolean beginsWithMode() {
			return(this == STARTS_WITH_STRING);
		}
	}
	
	public static int numBots;
	public static byte colorScheme;
	public static String email;
	public static String bnlsServer;
	public static int bnlsPort;
	public static boolean enableMirrorSelector;
	public static boolean autoConnect;
	public static boolean autoRejoin;
	public static boolean displayBattleNetMOTD;
	public static boolean displayBattleNetChannels;
	public static boolean displayJoinParts;
	public static boolean displayChannelUsers;
	public static boolean enableCLI;
	public static boolean enableGUI;
	public static boolean enableSWT;
	public static TrayIconMode trayIconMode;
	public static boolean trayMinimizeTo;
	public static boolean trayDisplayConnectDisconnect;
	public static boolean trayDisplayChannel;
	public static boolean trayDisplayJoinPart;
	public static boolean trayDisplayChatEmote;
	public static boolean trayDisplayWhisper;
	public static boolean enableTabCompleteUser;
	public static boolean enableTabCompleteCommand;
	public static TabCompleteMode tabCompleteMode;
	public static boolean enableLegacyIcons;
	public static boolean enableCommands;
	public static long triviaRoundLength;
	public static boolean enableFloodProtect;
	public static boolean packetLog;
	public static boolean whisperBack;
	public static int recruitAccess;
	public static String recruitTagPrefix;
	public static String recruitTagSuffix;
	public static ReleaseType releaseType;
	public static int bnUserToString;
	public static int bnUserToStringUserList;
	public static int bnUserToStringCommandResponse;
	public static String botNetServer;
	public static int botNetPort;
	
	private static String lookAndFeel;
	private static String lookAndFeelTheme;
	private static String[] lookAndFeelThemes;
	private static Method setPlasticTheme = null;

	static {
		enableGUI = Settings.read(null, "enableGUI", true);
		
		if(enableGUI)
			try {
				Class<?> PlasticLookAndFeel = JARLoader.forName("com.jgoodies.looks.plastic.PlasticLookAndFeel");
				
				// getInstalledThemes()
				Method getInstalledThemes = PlasticLookAndFeel.getMethod("getInstalledThemes");
				List<?> themes = (List<?>)getInstalledThemes.invoke(null);
				List<String> themes2 = new ArrayList<String>(themes.size());
				for(Object theme : themes)
					themes2.add(theme.getClass().getSimpleName());
				lookAndFeelThemes = themes2.toArray(new String[themes2.size()]);

				// setPlasticTheme(PlasticTheme)
				Class<?> PlasticTheme = JARLoader.forName("com.jgoodies.looks.plastic.PlasticTheme");
				setPlasticTheme = PlasticLookAndFeel.getMethod("setPlasticTheme", PlasticTheme);
			} catch(Exception e) {
				Out.exception(e);
			}
		
		load();
	}
	
	/**
	 * Set the Look and Feel to the proper LaF, and adjust the ClassLoader
	 * @param lafi
	 */
	public static void setLookAndFeel(LookAndFeelInfo lafi) {
		try {
			try {
				// Try to load the LaF from the class path
				UIManager.setLookAndFeel(lafi.getClassName());
				UIManager.getDefaults().put("ClassLoader", lafi.getClass().getClassLoader());
			} catch(ClassNotFoundException ex) {
				// Couldn't find it; check the JARLoader
				LookAndFeel laf = (LookAndFeel)JARLoader.forName(lafi.getClassName()).newInstance();
				UIManager.setLookAndFeel(laf);
				UIManager.getDefaults().put("ClassLoader", JARLoader.getClassLoader());
			}
			
			lookAndFeel = lafi.getName();
		} catch(Exception ex) {
			Out.exception(ex);
		}
	}
	
	/**
	 * Set the Look and Feel to the named LaF, and adjust the ClassLoader
	 * @param laf The name of the Look and Feel to swithc to
	 */
	private static void setLookAndFeel(String laf) {
		for(LookAndFeelInfo lafi : UIManager.getInstalledLookAndFeels())
			if(lafi.getName().equals(laf)) {
				setLookAndFeel(lafi);
				return;
			}
		Out.error(GlobalSettings.class, "Invalid Look and Feel: " + laf);
	}
	
	public static String getLookAndFeel() {
		return lookAndFeel;
	}
	
	public static void setLookAndFeelTheme(String laft) {
		if(setPlasticTheme == null)
			return;
		
		try {
			Class<?> theme = JARLoader.forName("com.jgoodies.looks.plastic.theme." + laft);
			setPlasticTheme.invoke(null, theme.newInstance());
			lookAndFeelTheme = laft;
		} catch(ClassNotFoundException e) {
			Out.error(GlobalSettings.class, "Invalid Look and Feel Theme: " + laft);
		} catch(Exception e) {
			Out.exception(e);
		}
	}
	
	public static String getLookAndFeelTheme() {
		return lookAndFeelTheme;
	}

	public static String[] getLookAndFeelThemes() {
		return lookAndFeelThemes;
	}
	
	public static void save() {
		Settings.write(null, "numBots", numBots);
		Settings.write(null, "autoConnect", autoConnect);
		Settings.write(null, "autoRejoin", autoRejoin);
		Settings.write(null, "botNetServer", botNetServer);
		Settings.write(null, "botNetPort", botNetPort);
		Settings.write(null, "bnlsserver", bnlsServer);
		Settings.write(null, "bnlsport", bnlsPort);
		Settings.write(null, "colorScheme", colorScheme);
		Settings.write(null, "displayBattleNetMOTD", displayBattleNetMOTD);
		Settings.write(null, "displayBattleNetChannels", displayBattleNetChannels);
		Settings.write(null, "displayJoinParts", displayJoinParts);
		Settings.write(null, "displayChannelUsers", displayChannelUsers);
		Settings.write(null, "email", email);
		Settings.write(null, "enableMirrorSelector", enableMirrorSelector);
		Settings.write(null, "enableCLI", enableCLI);
		Settings.write(null, "enableGUI", enableGUI);
		Settings.write(null, "enableSWT", enableSWT);
		Settings.write(null, "trayIconMode", trayIconMode);
		Settings.write(null, "trayMinimizeTo", trayMinimizeTo);
		Settings.write(null, "trayDisplayConnectDisconnect", trayDisplayConnectDisconnect);
		Settings.write(null, "trayDisplayChannel", trayDisplayChannel);
		Settings.write(null, "trayDisplayJoinPart", trayDisplayJoinPart);
		Settings.write(null, "trayDisplayChatEmote", trayDisplayChatEmote);
		Settings.write(null, "trayDisplayWhisper", trayDisplayWhisper);
		Settings.write(null, "enableTabCompleteUser", enableTabCompleteUser);
		Settings.write(null, "enableTabCompleteCommand", enableTabCompleteCommand);
		Settings.write(null, "tabCompleteMode", tabCompleteMode);
		Settings.write(null, "enableLegacyIcons", enableLegacyIcons);
		Settings.write(null, "enableCommands", enableCommands);
		Settings.write(null, "enableFloodProtect", enableFloodProtect);
		Settings.write(null, "lookAndFeel", lookAndFeel);
		Settings.write(null, "lookAndFeelTheme", lookAndFeelTheme);
		Settings.write(null, "packetLog", packetLog);
		Settings.write(null, "recruitAccess", recruitAccess);
		Settings.write(null, "recruitTagPrefix", recruitTagPrefix);
		Settings.write(null, "recruitTagSuffix", recruitTagSuffix);
		Settings.write(null, "bnUserToString", bnUserToString);
		Settings.write(null, "bnUserToStringUserList", bnUserToStringUserList);
		Settings.write(null, "bnUserToStringCommandResponse", bnUserToStringCommandResponse);
		Settings.write(null, "releaseType", releaseType);
		Settings.write(null, "triviaRoundLength", triviaRoundLength);
		Settings.write(null, "tsFormat", TimeFormatter.tsFormat);
		Settings.write(null, "whisperBack", whisperBack);
		
		Settings.store();
	}
	
	public static void load() {
		numBots = Settings.read(null, "numBots", 1);
		colorScheme = (byte)Settings.read(null, "colorScheme", 2);
		bnlsServer =Settings.read(null, "bnlsserver", "jbls.clanbnu.net");
		bnlsPort = Settings.read(null, "bnlsport", 9367);
		enableMirrorSelector = Settings.read(null, "enableMirrorSelector", true);
		autoConnect = Settings.read(null, "autoConnect", true);
		autoRejoin = Settings.read(null, "autoRejoin", true);
		botNetServer = Settings.read(null, "botNetServer", "botnet.valhallalegends.com");
		botNetPort = Settings.read(null, "botNetPort", 0x5555);
		displayBattleNetMOTD = Settings.read(null, "displayBattleNetMOTD", true);
		displayBattleNetChannels = Settings.read(null, "displayBattleNetChannels", false);
		displayJoinParts = Settings.read(null, "displayJoinParts", true);
		displayChannelUsers = Settings.read(null, "displayChannelUsers", false);
		email =	Settings.read(null, "email", (String)null);
		enableCLI = Settings.read(null, "enableCLI", false);
		enableGUI = Settings.read(null, "enableGUI", true);
		enableSWT = Settings.read(null, "enableSWT", false);
		trayIconMode = Settings.read(null, "trayIconMode", TrayIconMode.ENABLED);
		trayMinimizeTo = Settings.read(null, "trayMinimizeTo", true);
		trayDisplayConnectDisconnect = Settings.read(null, "trayDisplayConnectDisconnect", false);
		trayDisplayChannel = Settings.read(null, "trayDisplayChannel", false);
		trayDisplayJoinPart = Settings.read(null, "trayDisplayJoinPart", false);
		trayDisplayChatEmote = Settings.read(null, "trayDisplayChatEmote", true);
		trayDisplayWhisper = Settings.read(null, "trayDisplayWhisper", true);
		enableTabCompleteUser = Settings.read(null, "enableTabCompleteUser", true);
		enableTabCompleteCommand = Settings.read(null, "enableTabCompleteCommand", false);
		tabCompleteMode = Settings.read(null, "tabCompleteMode", TabCompleteMode.STARTS_WITH_STRING);
		enableLegacyIcons = Settings.read(null, "enableLegacyIcons", true);
		enableCommands = Settings.read(null, "enableCommands", true);
		triviaRoundLength = Settings.read(null, "triviaRoundLength", 100);
		enableFloodProtect = Settings.read(null, "enableFloodProtect", false);
		packetLog = Settings.read(null, "packetLog", false);
		whisperBack = Settings.read(null, "whisperBack", true);
		recruitAccess = Settings.read(null, "recruitAccess", 10);
		recruitTagPrefix =	Settings.read(null, "recruitTagPrefix", "BNU-");
		recruitTagSuffix =	Settings.read(null, "recruitTagSuffix", (String)null);
		if(enableGUI) {
			setLookAndFeelTheme(Settings.read(null, "lookAndFeelTheme", "SkyKrupp"));
			setLookAndFeel(Settings.read(null, "lookAndFeel", "JGoodies Plastic XP"));
		}
		TimeFormatter.tsFormat = Settings.read(null, "tsFormat", TimeFormatter.tsFormat);
		bnUserToString = Settings.read(null, "bnUserToString", 3);
		bnUserToStringUserList = Settings.read(null, "bnUserToStringUserList", 1);
		bnUserToStringCommandResponse = Settings.read(null, "bnUserToStringCommandResponse", 4);
		
		// Get the release type to check for when doing version checks
		ReleaseType currentRelease = CurrentVersion.version().getReleaseType();
		releaseType = Settings.read(null, "releaseType", currentRelease);
		
		if(CurrentVersion.fromJar()) {
			// If from a JAR, force at least Alpha
			if(releaseType.isDevelopment())
				releaseType = ReleaseType.Alpha;
		} else {
			// Non-jar is always development
			releaseType = ReleaseType.Development;
		}
	}
}
