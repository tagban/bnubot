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
		DISABLED,
		CONTAINS_STRING,
		STARTS_WITH_STRING;
		
		public boolean enableTC() {
			return(this != DISABLED);
		}
		
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
	public static boolean displayBattleNetMOTD;
	public static boolean displayBattleNetChannels;
	public static boolean displayJoinParts;
	public static boolean displayChannelUsers;
	public static boolean enableCLI;
	public static boolean enableGUI;
	public static TrayIconMode trayIconMode;
	public static TabCompleteMode tabCompleteMode;
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
	
	private static String lookAndFeel;
	private static String lookAndFeelTheme;
	private static String[] lookAndFeelThemes;
	private static Method setPlasticTheme = null;

	static {
		enableGUI = Settings.readBoolean(null, "enableGUI", true);
		
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
		Settings.writeInt(null, "numBots", numBots);
		Settings.writeBoolean(null, "autoConnect", autoConnect);
		Settings.write(null, "bnlsserver", bnlsServer);
		Settings.writeInt(null, "bnlsport", bnlsPort);
		Settings.writeInt(null, "colorScheme", colorScheme);
		Settings.writeBoolean(null, "displayBattleNetMOTD", displayBattleNetMOTD);
		Settings.writeBoolean(null, "displayBattleNetChannels", displayBattleNetChannels);
		Settings.writeBoolean(null, "displayJoinParts", displayJoinParts);
		Settings.writeBoolean(null, "displayChannelUsers", displayChannelUsers);
		Settings.write(null, "email", email);
		Settings.writeBoolean(null, "enableMirrorSelector", enableMirrorSelector);
		Settings.writeBoolean(null, "enableCLI", enableCLI);
		Settings.writeBoolean(null, "enableGUI", enableGUI);
		Settings.writeEnum(null, "trayIconMode", trayIconMode);
		Settings.writeEnum(null, "tabCompleteMode", tabCompleteMode);
		Settings.writeBoolean(null, "enableLegacyIcons", enableLegacyIcons);
		Settings.writeBoolean(null, "enableCommands", enableCommands);
		Settings.writeBoolean(null, "enableTrivia", enableTrivia);
		Settings.writeBoolean(null, "enableFloodProtect", enableFloodProtect);
		Settings.write(null, "lookAndFeel", lookAndFeel);
		Settings.write(null, "lookAndFeelTheme", lookAndFeelTheme);
		Settings.writeBoolean(null, "packetLog", packetLog);
		Settings.writeLong(null, "recruitAccess", recruitAccess);
		Settings.write(null, "recruitTagPrefix", recruitTagPrefix);
		Settings.write(null, "recruitTagSuffix", recruitTagSuffix);
		Settings.writeEnum(null, "releaseType", releaseType);
		Settings.writeLong(null, "triviaRoundLength", triviaRoundLength);
		Settings.write(null, "tsFormat", TimeFormatter.tsFormat);
		Settings.writeBoolean(null, "whisperBack", whisperBack);
		
		Settings.store();
	}
	
	public static void load() {
		numBots = Settings.readInt(null, "numBots", 1);
		colorScheme = (byte)Settings.readInt(null, "colorScheme", 2);
		bnlsServer =Settings.read(null, "bnlsserver", "jbls.clanbnu.net");
		bnlsPort = Settings.readInt(null, "bnlsport", 9367);
		enableMirrorSelector = Settings.readBoolean(null, "enableMirrorSelector", true);
		autoConnect = Settings.readBoolean(null, "autoConnect", true);
		displayBattleNetMOTD = Settings.readBoolean(null, "displayBattleNetMOTD", true);
		displayBattleNetChannels = Settings.readBoolean(null, "displayBattleNetChannels", false);
		displayJoinParts = Settings.readBoolean(null, "displayJoinParts", true);
		displayChannelUsers = Settings.readBoolean(null, "displayChannelUsers", false);
		email =	Settings.read(null, "email", null);
		enableCLI = Settings.readBoolean(null, "enableCLI", false);
		enableGUI = Settings.readBoolean(null, "enableGUI", true);
		trayIconMode = Settings.readEnum(TrayIconMode.class, null, "trayIconMode", TrayIconMode.ENABLED);
		tabCompleteMode = Settings.readEnum(TabCompleteMode.class, null, "tabCompleteMode", TabCompleteMode.STARTS_WITH_STRING);
		enableLegacyIcons = Settings.readBoolean(null, "enableLegacyIcons", true);
		enableCommands = Settings.readBoolean(null, "enableCommands", true);
		enableTrivia = Settings.readBoolean(null, "enableTrivia", false);
		triviaRoundLength = Settings.readLong(null, "triviaRoundLength", 100);
		enableFloodProtect = Settings.readBoolean(null, "enableFloodProtect", false);
		packetLog = Settings.readBoolean(null, "packetLog", false);
		whisperBack = Settings.readBoolean(null, "whisperBack", true);
		recruitAccess = Settings.readLong(null, "recruitAccess", 10);
		recruitTagPrefix =	Settings.read(null, "recruitTagPrefix", "BNU-");
		recruitTagSuffix =	Settings.read(null, "recruitTagSuffix", null);
		if(enableGUI) {
			setLookAndFeelTheme(Settings.read(null, "lookAndFeelTheme", "SkyKrupp"));
			setLookAndFeel(Settings.read(null, "lookAndFeel", "JGoodies Plastic XP"));
		}
		TimeFormatter.tsFormat = Settings.read(null, "tsFormat", TimeFormatter.tsFormat);
		releaseType = Settings.readEnum(ReleaseType.class, null, "releaseType", CurrentVersion.version().getReleaseType());

		// Ensure that development builds check for development, and non-development builds don't
		ReleaseType rt = CurrentVersion.version().getReleaseType();
		if(rt.isDevelopment() || releaseType.isDevelopment())
			releaseType = rt;
	}
}
