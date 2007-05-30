package util;

import java.io.*;
import util.Out;
import java.util.StringTokenizer;
import util.Constants;
import java.util.NoSuchElementException;

public class cSettings {
  	
   public static void LoadSettings() {
   	String file = "./settings.ini";
   	
   	//Load the Misc
   	Constants.BNLSPort = Integer.parseInt(Ini.ReadIni(file, "Main", "BNLSPort", Integer.toString(Constants.BNLSPort)), 10);
   	Constants.maxThreads = Integer.parseInt(Ini.ReadIni(file, "Main", "MaxThreads", Integer.toString(Constants.maxThreads)), 10);
   	
   	Constants.RunAdmin = Boolean.valueOf(Ini.ReadIni(file, "Main", "EnableAdmin", Boolean.toString(Constants.RunAdmin)));
   	Constants.AdminPort = Integer.parseInt(Ini.ReadIni(file, "Main", "AdminPort", Integer.toString(Constants.AdminPort)), 10);
   	Constants.maxAdminThreads = Integer.parseInt(Ini.ReadIni(file, "Main", "AdminThreads", Integer.toString(Constants.maxAdminThreads)), 10);
   	
    Constants.RunHTTP = Boolean.valueOf(Ini.ReadIni(file, "Main", "EnableHTTP", Boolean.toString(Constants.RunHTTP)));
   	Constants.HTTPPort = Integer.parseInt(Ini.ReadIni(file, "Main", "HTTPPort", Integer.toString(Constants.HTTPPort)), 10);
   	
   	Constants.ipAuthStatus = Integer.parseInt(Ini.ReadIni(file, "Main", "IPAuth", Integer.toString(Constants.ipAuthStatus)), 10);
   	Constants.requireAuthorization = Boolean.valueOf(Ini.ReadIni(file, "Main", "RequireAuth", Boolean.toString(Constants.requireAuthorization)));
   	Constants.trackStatistics = Boolean.valueOf(Ini.ReadIni(file, "Main", "Stats", Boolean.toString(Constants.trackStatistics)));
   	
   	Constants.displayPacketInfo = Boolean.valueOf(Ini.ReadIni(file, "Main", "DisplayPacketInfo", Boolean.toString(Constants.displayPacketInfo)));
   	Constants.displayParseInfo = Boolean.valueOf(Ini.ReadIni(file, "Main", "DisplayParseInfo", Boolean.toString(Constants.displayParseInfo)));
   	Constants.debugInfo = Boolean.valueOf(Ini.ReadIni(file, "Main", "DisplayDebugInfo", Boolean.toString(Constants.debugInfo)));
   	

     //Load IX86 Versioning Settings
     for(int x = 0; x < Constants.prods.length; x++){
     	Constants.IX86files[x][0] = Ini.ReadIni(file, Constants.prods[x] + "-IX86", "HashPath", Constants.IX86files[x][0]);
     	Constants.IX86files[x][1] = Ini.ReadIni(file, Constants.prods[x] + "-IX86", "Exe", Constants.IX86files[x][1]);
     	Constants.IX86files[x][2] = Ini.ReadIni(file, Constants.prods[x] + "-IX86", "Storm", Constants.IX86files[x][2]);
     	Constants.IX86files[x][3] = Ini.ReadIni(file, Constants.prods[x] + "-IX86", "Network", Constants.IX86files[x][3]);
     	Constants.IX86verbytes[x] = Integer.parseInt(Ini.ReadIni(file, Constants.prods[x] + "-IX86", "VerByte", Integer.toHexString(Constants.IX86verbytes[x])), 16);
     }
   }
   public static void SaveSettings(){
   	
   	//Save the Misc
   	Ini.WriteIni("./settings.ini", "Main", "BNLSPort", Integer.toString(Constants.BNLSPort));
   	Ini.WriteIni("./settings.ini", "Main", "MaxThreads", Integer.toString(Constants.maxThreads));
   	
   	Ini.WriteIni("./settings.ini", "Main", "EnableAdmin", Boolean.toString(Constants.RunAdmin));
   	Ini.WriteIni("./settings.ini", "Main", "AdminPort", Integer.toString(Constants.AdminPort));
   	Ini.WriteIni("./settings.ini", "Main", "AdminThreads", Integer.toString(Constants.maxAdminThreads));
   	
   	Ini.WriteIni("./settings.ini", "Main", "EnableHTTP", Boolean.toString(Constants.RunHTTP));
   	Ini.WriteIni("./settings.ini", "Main", "HTTPPort", Integer.toString(Constants.HTTPPort));
   	
   	Ini.WriteIni("./settings.ini", "Main", "IPAuth", Integer.toString(Constants.ipAuthStatus));
   	Ini.WriteIni("./settings.ini", "Main", "RequireAuth", Boolean.toString(Constants.requireAuthorization));
   	Ini.WriteIni("./settings.ini", "Main", "Stats", Boolean.toString(Constants.trackStatistics));
   	
   	Ini.WriteIni("./settings.ini", "Main", "DisplayPacketInfo", Boolean.toString(Constants.displayPacketInfo));
   	Ini.WriteIni("./settings.ini", "Main", "DisplayParseInfo", Boolean.toString(Constants.displayParseInfo));
   	Ini.WriteIni("./settings.ini", "Main", "DisplayDebugInfo", Boolean.toString(Constants.debugInfo));
   	
     //Save IX86 Versioning Settings
     for(int x = 0; x < Constants.prods.length; x++){
     	Ini.WriteIni("./settings.ini", Constants.prods[x] + "-IX86", "HashPath", Constants.IX86files[x][0]);
     	Ini.WriteIni("./settings.ini", Constants.prods[x] + "-IX86", "Exe", Constants.IX86files[x][1]);
     	Ini.WriteIni("./settings.ini", Constants.prods[x] + "-IX86", "Storm", Constants.IX86files[x][2]);
     	Ini.WriteIni("./settings.ini", Constants.prods[x] + "-IX86", "Network", Constants.IX86files[x][3]);
     	Ini.WriteIni("./settings.ini", Constants.prods[x] + "-IX86", "VerByte", 
     	  PadString.padString(Integer.toHexString(Constants.IX86verbytes[x]), 2, '0'));
     }
     
   }
  }