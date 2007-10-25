/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.settings;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.bnubot.util.Out;
import net.bnubot.util.TimeFormatter;
import net.bnubot.vercheck.CurrentVersion;
import net.bnubot.vercheck.ReleaseType;

public class GlobalSettings {
	public static int numBots;
	public static byte colorScheme;
	public static String trigger;
	public static String antiIdle;
	public static String bnlsServer;
	public static int bnlsPort;
	public static boolean enableGreetings;
	public static int antiIdleTimer;
	public static boolean enableAntiIdle;
	public static boolean autoConnect;
	public static boolean enableCLI;
	public static boolean enableGUI;
	public static boolean enableTrayIcon;
	public static boolean enableTrayPopups;
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
	
	public static String isValid() {
		if((trigger == null) || (trigger.length() != 1))
			return "Trigger invalid";
		
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
	
	public static void save() {
		Settings.write(null, "numBots", Integer.toString(numBots));
		Settings.write(null, "antiidle", antiIdle);
		Settings.write(null, "bnlsserver", bnlsServer);
		Settings.write(null, "bnlsport", Integer.toString(bnlsPort));
		Settings.write(null, "enableAntiidle", Boolean.toString(enableAntiIdle));
		Settings.write(null, "enableGreetings", Boolean.toString(enableGreetings));
		Settings.write(null, "antiIdleTimer", Integer.toString(antiIdleTimer));
		Settings.write(null, "autoConnect", Boolean.toString(autoConnect));
		Settings.write(null, "colorScheme", Byte.toString(colorScheme));
		Settings.write(null, "trigger", trigger);
		Settings.write(null, "enableCLI", Boolean.toString(enableCLI));
		Settings.write(null, "enableGUI", Boolean.toString(enableGUI));
		Settings.write(null, "enableTrayIcon", Boolean.toString(enableTrayIcon));
		Settings.write(null, "enableTrayPopups", Boolean.toString(enableTrayPopups));
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
		
		Settings.store();
	}
	
	public static void load() {
		numBots = Integer.parseInt(
				Settings.read(null, "numBots", "1"));
		colorScheme = Byte.parseByte(
				Settings.read(null, "colorScheme", "1"));
		trigger = 	Settings.read(null, "trigger", "!");
		antiIdle = 	Settings.read(null, "antiidle", "/me is a BNU-Bot %version%");
		bnlsServer =Settings.read(null, "bnlsserver", "jbls.clanbnu.net");
		bnlsPort = Integer.parseInt(
					Settings.read(null, "bnlsport", "9367"));
		enableAntiIdle = Boolean.parseBoolean(
				Settings.read(null, "enableAntiidle", "false"));
		enableGreetings = Boolean.parseBoolean(
				Settings.read(null, "enableGreetings", "true"));
		antiIdleTimer = Integer.parseInt(
				Settings.read(null, "antiIdleTimer", "5"));
		autoConnect = Boolean.parseBoolean(
				Settings.read(null, "autoConnect", "true"));
		enableCLI = Boolean.parseBoolean(
				Settings.read(null, "enableCLI", "false"));
		enableGUI = Boolean.parseBoolean(
				Settings.read(null, "enableGUI", "true"));
		enableTrayIcon = Boolean.parseBoolean(
				Settings.read(null, "enableTrayIcon", "true"));
		enableTrayPopups = Boolean.parseBoolean(
				Settings.read(null, "enableTrayPopups", "true"));
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
