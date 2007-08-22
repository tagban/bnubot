package util;

import util.Constants;

public class cSettings {
	private static String file = "./settings.ini";

	public static void LoadSettings() {
		// Load the Misc
		Constants.ArchivePath = Ini.ReadIni(file, "Main", "Archives",
				Constants.ArchivePath);
		Constants.DownloadPath = Ini.ReadIni(file, "Main", "DownloadPath",
				Constants.DownloadPath);

		Constants.BNLSPort = Integer.parseInt(Ini.ReadIni(file, "Main",
				"BNLSPort", Integer.toString(Constants.BNLSPort)), 10);
		Constants.maxThreads = Integer.parseInt(Ini.ReadIni(file, "Main",
				"MaxThreads", Integer.toString(Constants.maxThreads)), 10);

		Constants.RunHTTP = Boolean.valueOf(Ini.ReadIni(file, "Main",
				"EnableHTTP", Boolean.toString(Constants.RunHTTP)));
		Constants.HTTPPort = Integer.parseInt(Ini.ReadIni(file, "Main",
				"HTTPPort", Integer.toString(Constants.HTTPPort)), 10);

		Constants.ipAuthStatus = Integer.parseInt(Ini.ReadIni(file, "Main",
				"IPAuth", Integer.toString(Constants.ipAuthStatus)), 10);
		Constants.requireAuthorization = Boolean.valueOf(Ini.ReadIni(file,
				"Main", "RequireAuth", Boolean
						.toString(Constants.requireAuthorization)));
		Constants.trackStatistics = Boolean.valueOf(Ini.ReadIni(file, "Main",
				"Stats", Boolean.toString(Constants.trackStatistics)));

		Constants.displayPacketInfo = Boolean.valueOf(Ini.ReadIni(file, "Main",
				"DisplayPacketInfo", Boolean
						.toString(Constants.displayPacketInfo)));
		Constants.displayParseInfo = Boolean.valueOf(Ini.ReadIni(file, "Main",
				"DisplayParseInfo", Boolean
						.toString(Constants.displayParseInfo)));
		Constants.debugInfo = Boolean.valueOf(Ini.ReadIni(file, "Main",
				"DisplayDebugInfo", Boolean.toString(Constants.debugInfo)));

		Constants.RunAdmin = Boolean.valueOf(Ini.ReadIni(file, "Admin",
				"EnableAdmin", Boolean.toString(Constants.RunAdmin)));
		Constants.BotNetUsername = Ini.ReadIni(file, "Admin", "BotNetUsername",
				"");
		Constants.BotNetPassword = Ini.ReadIni(file, "Admin", "BotNetPassword",
				"");
		Constants.BotNetServer = Ini.ReadIni(file, "Admin", "BotNetServer",
				Constants.BotNetServer);

		// Load IX86 Versioning Settings
		for (int x = 0; x < Constants.prods.length; x++) {
			Constants.IX86files[x][0] = Ini.ReadIni(file, Constants.prods[x]
					+ "-IX86", "HashPath", Constants.IX86files[x][0]);
			Constants.IX86files[x][1] = Ini.ReadIni(file, Constants.prods[x]
					+ "-IX86", "Exe", Constants.IX86files[x][1]);
			Constants.IX86files[x][2] = Ini.ReadIni(file, Constants.prods[x]
					+ "-IX86", "Storm", Constants.IX86files[x][2]);
			Constants.IX86files[x][3] = Ini.ReadIni(file, Constants.prods[x]
					+ "-IX86", "Network", Constants.IX86files[x][3]);
			Constants.IX86files[x][4] = Ini.ReadIni(file, Constants.prods[x]
					+ "-IX86", "Screen", Constants.IX86files[x][4]);
			Constants.IX86verbytes[x] = Integer.parseInt(Ini.ReadIni(file,
					Constants.prods[x] + "-IX86", "VerByte", Integer
							.toHexString(Constants.IX86verbytes[x])), 16);
		}
	}

	public static void SaveSettings() {

		// Save the Misc
		Ini.WriteIni(file, "Main", "Archives", Constants.ArchivePath);
		Ini.WriteIni(file, "Main", "DownloadPath", Constants.DownloadPath);
		Ini.WriteIni(file, "Main", "BNLSPort", Integer
				.toString(Constants.BNLSPort));
		Ini.WriteIni(file, "Main", "MaxThreads", Integer
				.toString(Constants.maxThreads));

		Ini.WriteIni(file, "Main", "EnableHTTP", Boolean
				.toString(Constants.RunHTTP));
		Ini.WriteIni(file, "Main", "HTTPPort", Integer
				.toString(Constants.HTTPPort));

		Ini.WriteIni(file, "Main", "IPAuth", Integer
				.toString(Constants.ipAuthStatus));
		Ini.WriteIni(file, "Main", "RequireAuth", Boolean
				.toString(Constants.requireAuthorization));
		Ini.WriteIni(file, "Main", "Stats", Boolean
				.toString(Constants.trackStatistics));

		Ini.WriteIni(file, "Main", "DisplayPacketInfo", Boolean
				.toString(Constants.displayPacketInfo));
		Ini.WriteIni(file, "Main", "DisplayParseInfo", Boolean
				.toString(Constants.displayParseInfo));
		Ini.WriteIni(file, "Main", "DisplayDebugInfo", Boolean
				.toString(Constants.debugInfo));

		Ini.WriteIni(file, "Admin", "EnableAdmin", Boolean
				.toString(Constants.RunAdmin));
		Ini.WriteIni(file, "Admin", "BotNetUsername", Constants.BotNetUsername);
		Ini.WriteIni(file, "Admin", "BotNetPassword", Constants.BotNetPassword);
		Ini.WriteIni(file, "Admin", "BotNetServer", Constants.BotNetServer);

		// Save IX86 Versioning Settings
		for (int x = 0; x < Constants.prods.length; x++) {
			Ini.WriteIni(file, Constants.prods[x] + "-IX86", "HashPath",
					Constants.IX86files[x][0]);
			Ini.WriteIni(file, Constants.prods[x] + "-IX86", "Exe",
					Constants.IX86files[x][1]);
			Ini.WriteIni(file, Constants.prods[x] + "-IX86", "Storm",
					Constants.IX86files[x][2]);
			Ini.WriteIni(file, Constants.prods[x] + "-IX86", "Network",
					Constants.IX86files[x][3]);
			Ini.WriteIni(file, Constants.prods[x] + "-IX86", "Screen",
					Constants.IX86files[x][4]);
			Ini.WriteIni(file, Constants.prods[x] + "-IX86", "VerByte",
					PadString.padString(Integer
							.toHexString(Constants.IX86verbytes[x]), 2, '0'));
		}

	}
}