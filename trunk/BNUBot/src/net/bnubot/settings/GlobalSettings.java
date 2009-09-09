/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.settings;

import java.awt.HeadlessException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.bnubot.JARLoader;
import net.bnubot.bot.gui.GuiDesktop;
import net.bnubot.bot.swt.SWTDesktop;
import net.bnubot.core.PluginManager;
import net.bnubot.logging.Out;
import net.bnubot.util.TimeFormatter;
import net.bnubot.vercheck.CurrentVersion;
import net.bnubot.vercheck.ReleaseType;

/**
 * @author scotta
 */
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

	public static enum SOCKSType {
		SOCKS4,
		SOCKS4a
	}

	public static boolean firstRun;
	public static int numBots;
	public static byte colorScheme;
	public static String email;
	public static String bnlsServer;
	public static int bnlsPort;
	public static boolean enableMirrorSelector;
	public static boolean autoConnect;
	public static boolean autoRejoin;
	public static boolean displaySlashCommands;
	public static boolean displayBattleNetMOTD;
	public static boolean displayBattleNetChannels;
	private static boolean displayJoinParts;
	public static boolean displayChannelUsers;
	public static String guiFontFamily;
	public static int guiFontSize;
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
	public static int triviaRoundLength;
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
	public static String botNetUsername;
	public static String botNetPassword;
	public static String botNetDatabase;
	public static String botNetDatabasePassword;

	public static boolean socksEnabled;
	public static SOCKSType socksType;
	public static String socksHost;
	public static int socksPort;

	private static String lookAndFeel;
	private static String lookAndFeelTheme;
	private static String[] lookAndFeelThemes = null;
	private static Method setPlasticTheme = null;

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
		SettingsSection ss = Settings.getSection(null);
		ss.write("firstRun", firstRun);
		ss.write("numBots", numBots);
		ss.write("autoConnect", autoConnect);
		ss.write("autoRejoin", autoRejoin);
		ss.write("botNetServer", botNetServer);
		ss.write("botNetPort", botNetPort);
		ss.write("botNetUsername", botNetUsername);
		ss.write("botNetPassword", botNetPassword);
		ss.write("botNetDatabase", botNetDatabase);
		ss.write("botNetDatabasePassword", botNetDatabasePassword);
		ss.write("bnlsserver", bnlsServer);
		ss.write("bnlsport", bnlsPort);
		ss.write("colorScheme", colorScheme);
		ss.write("displaySlashCommands", displaySlashCommands);
		ss.write("displayBattleNetMOTD", displayBattleNetMOTD);
		ss.write("displayBattleNetChannels", displayBattleNetChannels);
		ss.write("displayJoinParts", displayJoinParts);
		ss.write("displayChannelUsers", displayChannelUsers);
		ss.write("email", email);
		ss.write("enableMirrorSelector", enableMirrorSelector);
		ss.write("guiFontFamily", guiFontFamily);
		ss.write("guiFontSize", guiFontSize);
		ss.write("trayIconMode", trayIconMode);
		ss.write("trayMinimizeTo", trayMinimizeTo);
		ss.write("trayDisplayConnectDisconnect", trayDisplayConnectDisconnect);
		ss.write("trayDisplayChannel", trayDisplayChannel);
		ss.write("trayDisplayJoinPart", trayDisplayJoinPart);
		ss.write("trayDisplayChatEmote", trayDisplayChatEmote);
		ss.write("trayDisplayWhisper", trayDisplayWhisper);
		ss.write("enableTabCompleteUser", enableTabCompleteUser);
		ss.write("enableTabCompleteCommand", enableTabCompleteCommand);
		ss.write("tabCompleteMode", tabCompleteMode);
		ss.write("enableLegacyIcons", enableLegacyIcons);
		ss.write("enableFloodProtect", enableFloodProtect);
		ss.write("lookAndFeel", lookAndFeel);
		ss.write("lookAndFeelTheme", lookAndFeelTheme);
		ss.write("packetLog", packetLog);
		ss.write("recruitAccess", recruitAccess);
		ss.write("recruitTagPrefix", recruitTagPrefix);
		ss.write("recruitTagSuffix", recruitTagSuffix);
		ss.write("bnUserToString", bnUserToString);
		ss.write("bnUserToStringUserList", bnUserToStringUserList);
		ss.write("bnUserToStringCommandResponse", bnUserToStringCommandResponse);
		ss.write("releaseType", releaseType);
		ss.write("triviaRoundLength", triviaRoundLength);
		ss.write("tsFormat", TimeFormatter.tsFormat);
		ss.write("timeZone", TimeFormatter.timeZone);
		ss.write("whisperBack", whisperBack);
		ss.write("socksEnabled", socksEnabled);
		ss.write("socksType", socksType);
		ss.write("socksHost", socksHost);
		ss.write("socksPort", socksPort);

		Settings.store();
	}

	public static void load() {
		if(lookAndFeelThemes == null)
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
			} catch(Throwable t) {
				Out.exception(t);
			}

		SettingsSection ss = Settings.getSection(null);
		firstRun = ss.read("firstRun", true);
		numBots = ss.read("numBots", 1);
		colorScheme = (byte)ss.read("colorScheme", 2);
		bnlsServer =ss.read("bnlsserver", "jbls.clanbnu.net");
		bnlsPort = ss.read("bnlsport", 9367);
		enableMirrorSelector = ss.read("enableMirrorSelector", true);
		autoConnect = ss.read("autoConnect", true);
		autoRejoin = ss.read("autoRejoin", true);
		botNetServer = ss.read("botNetServer", "botnet.valhallalegends.com");
		botNetPort = ss.read("botNetPort", 0x5555);
		botNetUsername = ss.read("botNetUsername", (String)null);
		botNetPassword = ss.read("botNetPassword", (String)null);
		botNetDatabase = ss.read("botNetDatabase", "PubEternalChat");
		botNetDatabasePassword = ss.read("botNetDatabasePassword", "f9q07r89iahdfjg47af9od");
		displaySlashCommands = ss.read("displaySlashCommands", false);
		displayBattleNetMOTD = ss.read("displayBattleNetMOTD", true);
		displayBattleNetChannels = ss.read("displayBattleNetChannels", false);
		displayJoinParts = ss.read("displayJoinParts", true);
		displayChannelUsers = ss.read("displayChannelUsers", false);
		email =	ss.read("email", (String)null);
		guiFontFamily = ss.read("guiFontFamily", "verdana");
		guiFontSize = ss.read("guiFontSize", 10);
		trayIconMode = ss.read("trayIconMode", TrayIconMode.ENABLED);
		trayMinimizeTo = ss.read("trayMinimizeTo", true);
		trayDisplayConnectDisconnect = ss.read("trayDisplayConnectDisconnect", false);
		trayDisplayChannel = ss.read("trayDisplayChannel", false);
		trayDisplayJoinPart = ss.read("trayDisplayJoinPart", false);
		trayDisplayChatEmote = ss.read("trayDisplayChatEmote", true);
		trayDisplayWhisper = ss.read("trayDisplayWhisper", true);
		enableTabCompleteUser = ss.read("enableTabCompleteUser", true);
		enableTabCompleteCommand = ss.read("enableTabCompleteCommand", false);
		tabCompleteMode = ss.read("tabCompleteMode", TabCompleteMode.STARTS_WITH_STRING);
		enableLegacyIcons = ss.read("enableLegacyIcons", false);
		triviaRoundLength = ss.read("triviaRoundLength", 100);
		enableFloodProtect = ss.read("enableFloodProtect", false);
		packetLog = ss.read("packetLog", false);
		whisperBack = ss.read("whisperBack", true);
		socksEnabled = ss.read("socksEnabled", false);
		socksType = ss.read("socksType", SOCKSType.SOCKS4);
		socksHost = ss.read("socksHost", "localhost");
		socksPort = ss.read("socksPort", 6111);
		recruitAccess = ss.read("recruitAccess", 10);
		recruitTagPrefix =	ss.read("recruitTagPrefix", "BNU-");
		recruitTagSuffix =	ss.read("recruitTagSuffix", (String)null);
		if(PluginManager.getEnableGui()) {
			setLookAndFeelTheme(ss.read("lookAndFeelTheme", "DarkStar"));
			setLookAndFeel(ss.read("lookAndFeel", "JGoodies Plastic XP"));
			try {
				GuiDesktop.getInstance();
			} catch(HeadlessException e) {
				Out.error(GlobalSettings.class, "Failed to initialize GUI; switching to command-line interface");
				PluginManager.setEnableGui(false);
				PluginManager.setEnableCli(true);
			}
		}
		if(PluginManager.getEnableSwt())
			SWTDesktop.getInstance();
		TimeFormatter.tsFormat = ss.read("tsFormat", TimeFormatter.tsFormat);
		TimeFormatter.timeZone = ss.read("timeZone", TimeFormatter.timeZone);
		bnUserToString = ss.read("bnUserToString", 1); // BNLogin
		bnUserToStringUserList = ss.read("bnUserToStringUserList", 5); // Account (BNLogin)
		bnUserToStringCommandResponse = ss.read("bnUserToStringCommandResponse", 4); // Account

		// Get the release type to check for when doing version checks
		ReleaseType currentRelease = CurrentVersion.version().getReleaseType();
		releaseType = ss.read("releaseType", currentRelease);

		if(CurrentVersion.fromJar()) {
			// If from a JAR, force Nightly
			if(releaseType.isDevelopment())
				releaseType = ReleaseType.Nightly;
		} else {
			// Non-jar is always development
			releaseType = ReleaseType.Development;
		}
	}

	public static boolean getDisplayJoinParts() {
		return displayJoinParts;
	}

	public static void setDisplayJoinParts(boolean displayJoinParts) {
		GlobalSettings.displayJoinParts = displayJoinParts;
		if(PluginManager.getEnableGui())
			GuiDesktop.updateDisplayJoinPartsMenuChecked();
	}
}
