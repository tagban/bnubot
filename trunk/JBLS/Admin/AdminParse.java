/*
 * Created on Mar 4, 2005
 */
package Admin;


import java.util.StringTokenizer;
import java.util.NoSuchElementException;
import java.net.URLConnection;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.*;
import util.ZIP;
import util.Controller;
import util.Out;
import util.Constants;
import Admin.AdminList;
import util.cSettings;
import util.BNFTP;
import BNLSProtocol.BNLSConnectionThread;

public class AdminParse {

	 public static boolean bAuthed;
	 public static String sName;
	 public static String sPass;

	public AdminParse(){

	}

	/**
	 * Parses a command
	 * @param c - command line to be done
	 * @return string response to send.  If "close" is returned then close the connection
	 * @throws BNLSException if the connection should be closed
	 */
	public String parseAdminCommand(String c){
		if (c == null) { return "null"; }
		if (c.equals(null)) { return null; }
		if (bAuthed == false) {
			if (sName == null) {
				sName = c;
				return "Password:";
			} else {
				sPass = c;
				bAuthed = AdminList.CorrectLogin(sName, sPass);
				if (bAuthed) {
					return "Logged in, Welcome "+sName+".";
				}else{
					sPass = null;
					sName = null;
					return "Login failed. Connection closed.";
				}
			}
		}else{
			try {
			if (c.equals(""))
			    return null;
			StringTokenizer st=new StringTokenizer(c, " ");
			String com = st.nextToken().toLowerCase();
			Out.debug("Admin Parse","C:"+c+" com:"+com);

			if(com.equals("shutdown")){
				Controller.shutdown();
			}else if(com.equals("restart")){
				Controller.restartJBLS();
				return "Restarting JBLS Server....";
			}else if(com.equals("restartall")){
				Controller.restartAll();
				return "Restarting....";
			}else if(com.equals("currentstats")){
				return "Statistics:" + Controller.aServer.settings.getStats();

			}else if(com.equals("setip")){
				String ip=st.nextToken();
				int status=Integer.parseInt(st.nextToken());
				IpAuth.setIP(ip, status);
				return "IP: "+ip+ " set to: "+status;
			}else if(com.equals("ipban")){
				String ip=st.nextToken();
				IpAuth.setIP(ip, IpTracker.STATUSBANNED);
				return "IP: "+ip+" Set to banned";
			}else if(com.equals("ipauth")){
				try{
					IpAuth.setIpAuthStatus(Integer.parseInt(st.nextToken()));
				}catch(Exception e){
					//catch tokenizer/parse errors
				}
				String ret="Current IpAuth Status: ("+Constants.ipAuthStatus+") ";
				switch(Constants.ipAuthStatus){
					case IpAuth.IPNORESTRICTIONS:
						return ret+"No Restrictions";
					case IpAuth.IPBANNING:
						return ret+"IPBanning Enabled";
					case IpAuth.IPRESTRICTED:
						return ret+"Restricted Mode";
					case IpAuth.IPLOCALONLY:
						return ret+"Local Addresses Only";
					default:
						return ret+"Invalid Status";
				}//end switch*/

			}else if(com.equals("respond")){
				return "I responded, damnit!";
			}else if(com.equals("version")){
				return "JBLS 0x"+
				((Constants.lngServerVer & 0xF0) >> 4) + "" +
				Integer.toString((Constants.lngServerVer & 0x0F) >> 0, 16);
			}else if(com.equals("close")){
				return "close";
			}else if(com.equals("listips")){
				return IpAuth.getIpList();
			}else if(com.equals("save")){
				if(Controller.aServer!=null)
					Controller.aServer.save();
				return "Admin Settings/Data Saved.";
			//}else if(com.equals("addsetting")) {
			//	String sSet = st.nextToken();
			//	cSettings.appendSettings(sName, sSet);
			//	return "Added "+sSet+" to Setting.txt.\r\nYou must reload the settings before it will take effect.";
			}else if(com.equals("reloadsettings")) {
				cSettings.LoadSettings();
				return "Settings reloaded.";
			}else if(com.equals("reloadadmins")){
				AdminList.LoadUsers();
				return "Admin list reloaded.";
			}else if(com.equals("stopjbls")){
				Controller.stopJBLS();
				return "JBLS stopped.";
			}else if(com.equals("startjbls")){
				Controller.startJBLS();
				return "JBLS started.";
		    }else if(com.equals("broadcast")) {
                if (Controller.jServer != null)
                  Controller.jServer.Broadcast(c.substring(10), sName);
		    	return "Server message sent.";
		    }else if(com.equals("killall")) {
                if (Controller.jServer != null)
                  Controller.jServer.destroyAllConnections();
		    	return "Killed all JBLS connections.";
		    }else if(com.equals("count")) {
		       BNLSConnectionThread bCurrent = Controller.lLinkedHead;
		       int X=0;
		       while (bCurrent != null) {
		       	 X++;
		       	 bCurrent = bCurrent.getNext();
		       }
		       return "Currently there are " + X + " JBLS connections.";
		    }else if(com.equals("bnftp")){
		    	String sServer = st.nextToken();
		    	int lPort = Integer.parseInt(st.nextToken(), 10);
		    	String sFile = st.nextToken();
		    	BNFTP ftp = new BNFTP();
		    	String sResult = ftp.BNFTP(sServer, lPort, sFile);
		    	return sResult;
		    }else if(com.equals("patch")){
		    	String verByte = null;
		    	String verHash = null;
		    	String Client = st.nextToken();
		    	String FileURL = st.nextToken();
		    	try {
		    		verByte = st.nextToken();
		    	}catch(NoSuchElementException e){}
		    	String Return = Patch(Client, FileURL, verByte, sName);
		    	return Return;
			}else if(com.equals("help")){
				String sCmdsList;
				sCmdsList = "Commands for JBLS's Admin server:\n\r" +
				             "\tShutdown: \tShuts down both JBLS and Admin servers.\n\r" +
				             "\tRestart: \tRestarts the JBLS server.\n\r" +
				             "\tRestartAll: \tRestarts both the JBLS and Admin server.\n\r" +
				             "\tCurrentStats: \tReturns the current statistics of the JBLS server.\n\r" +
				             "\tSetIP: \tSets the authentication state of the given IP. \n\r" +
				             "\t\t\tUstage: SetIP <IP> <AuthState>\n\r" +
				             "\tIPBan: \tAdds the IP to the banned list.\n\r" +
				             "\tIPAuth: \tEither Sets the IPAuth Status. Or displays the current one.\n\r" +
				             "\t\t\tPossible IPAuth values:\n\r" +
				             "\t\t\t\t0: Cmpleetly unrestricted. Everyone is authed.\n\r" +
				             "\t\t\t\t1: IPBanning is enabled.\n\r"+
				             "\t\t\t\t2: Only allow authed IPs ot connect.\n\r" +
				             "\t\t\t\t4: Only allow local IPs to connect. \n\r" +
				             "\t\t\t\t   (Starting with 10, 192, 127)\n\r" +
				             "\tRespond: \tMakes the server say a snidy remark ;)\n\r" +
				             "\tVersion: \tReturnes the current version of the JBLS server.\n\r" +
				             "\tClose: \t\tCloses your connection to the Admin server.\n\r" +
				             "\tListIPs: \tReturns statistics on IPs that have connected to your server.\n\r" +
				             "\tSave: \t\tCalls the Admin settings save function.\n\r" +
				             "\tAddSetting: \tUsage: AddSetting Setting=Value\n\r" +
				             "\t\t\tAdds the specifyed settings to Settings.txt.\n\r" +
				             "\t\t\tRequires you to use the ReloadSettings command to have the \n\r" +
				             "\t\t\tsetting take effect.\n\r" +
				             "\tReloadSettings: Re-Parses Settings.txt and applies the settings.\n\r" +
				             "\tReloadAdmins: \tReloads the list of admin accounts.\n\r" +
				             "\tStopJBLS: \tDisables the JBLS server.\n\r" +
				             "\tStartJBLS: \tStarts the JBLS server.\n\r" +
				             "\tBroadcast: \tBroadcast a system message to every client that is currently\n\r" +
				             "\t\t\tconnected to the JBLS server.\n\r" +
				             "\tKillAll: \tSimply force kills all the current JBLS connections.\n\r" +
				             "\tCount: \t\tDisplays the current number of active JBLS connections.\n\r" +
				             "\tbnftp: \t\tDownloads a file from the specifyed server\n\r" +
				             "\t\t\tUsage: bnftp uswest.battle.net 6112 tos.txt\n\r" +
				             "\tpatch: \t\tAttempts to patch the hash files and version info.\n\r" +
				             "\t\t\tUsage: patch <client> <zip url> <verbyte> <verhash> \n\r" +
				             "\t\t\tExample: patch WAR3 http://hdx.no-ip.org/Hashes/WAR3.zip 14 0100109\n\r" +
				             "\t\t\tNote: this will take a vary long time to download the zip, The server will \n\r" +
				             "\t\t\t\t not respond while it is patching. This is normal." +
				             "\tHelp: \t\tDesplays this help list.";
				return sCmdsList;
			}else{
				return "Unknown command: " + com;
			}
			}catch(NoSuchElementException e) {
				System.out.println(e.toString());
				return "To few parameters";
			}
		}
		return null;
	}

	private String Patch(String Client, String URLFile, String verByte, String sAdmin){
		if (verByte != null){
			if(verByte.length() > 2)
				return "Invalid version byte string. Proper format 0B";
//			cSettings.appendSettings(sAdmin, "verbyte" + Client + "=" + verByte);
		}
		cSettings.LoadSettings();
		String sZIP = "ZIP.zip";
		String sPath = ".";

/*		if (Client.toLowerCase().equals("war3") || Client.toLowerCase().equals("w3xp")){
			sZIP = "WAR3.zip";
			sPath = GetPath(Constants.WAR3files[0]);
		}else if (Client.toLowerCase().equals("star") || Client.toLowerCase().equals("sexp")){
			sZIP = "STAR.zip";
			sPath = GetPath(Constants.STARfiles[0]);
		}else if (Client.toLowerCase().equals("w2bn")){
			sZIP = "W2BN.zip";
			sPath = GetPath(Constants.W2BNfiles[0]);
		}else if (Client.toLowerCase().equals("d2dv")){
			sZIP = "D2DV.zip";
			sPath = GetPath(Constants.D2DVfiles[0]);
		}else if (Client.toLowerCase().equals("d2xp")){
			sZIP = "D2XP.zip";
			sPath = GetPath(Constants.D2XPfiles[0]);
		}else if (Client.toLowerCase().equals("jstr")){
			sZIP = "JSTR.zip";
			sPath = GetPath(Constants.JSTRfiles[0]);
		}else if (Client.toLowerCase().equals("drtl")){
			sZIP = "DRTL.zip";
			sPath = GetPath(Constants.DRTLfiles[0]);
		}else if (Client.toLowerCase().equals("dshr")){
			sZIP = "DSHR.zip";
			sPath = GetPath(Constants.DSHRfiles[0]);
		}else if (Client.toLowerCase().equals("sshr")){
			sZIP = "SSHR.zip";
			sPath = GetPath(Constants.SSHRfiles[0]);
		}else{
			return "Invalid client name, Must be one of the following: WAR3, STAR, W2BN, D2DV, D2XP, JSTR, DRTL, DSHR, SSHR";
		}*/

		System.out.println(sPath);
		String ret = DownloadURL(URLFile, sZIP);
		if (ret.equals("success")){

			Controller.stopJBLS();
			if (Controller.jServer != null)
				Controller.jServer.destroyAllConnections();
		    ret = ZIP.ExtractZip(sZIP, sPath);
		    Controller.startJBLS();
		    if (ret.equals("success")){
		    	return "Successfully patched " + Client + " versiong files!";
		    }else{
		    	return "Failed to unzip the patch: " + ret;
		    }
		}else{
			return "Failed to download patch file: " + ret;
		}
	}

	private String GetPath(String fullPath){
		for (int X = fullPath.length() - 1; X >= 0; X--){
			if (fullPath.charAt(X) == '/' ||
				fullPath.charAt(X) == '\\'){
					return fullPath.substring(0, X);
				}
		}
		return ".";
	}

	private String DownloadURL(String URLFile, String sFile) {
		try {
			Out.println("Admin Parse", "Atempting to download: " + URLFile);
			/*String sFile = null;
			for (int X = URLFile.length() - 1; X >= 0; X--){
				if (URLFile.charAt(X) == '\\' || URLFile.charAt(X) == '/'){
					sFile = URLFile.substring(X+1);
					break;
				}
			}
			if (sFile == null){
				Out.println("Admin Parse", "Failed to extract file name.");
				return "Failed to extract file name.";
			}*/

			URLConnection connection = new URL(URLFile).openConnection();
			int fileSize = connection.getContentLength();
			Out.println("Admin Parse", "retrived file size: " + fileSize);
			FileOutputStream file = new FileOutputStream(sFile);
			int bytesRead = 0;
			DataInputStream in = new DataInputStream(connection.getInputStream());
			while (bytesRead < fileSize){
				int i = in.read();
				if (i == -1) {
					Out.println("Admin Parse", "Failed to get eintire file: " + bytesRead + "/" + fileSize);
					return "Failed to get eintire file: " + bytesRead + "/" + fileSize;
				}
				file.write((byte)i);
				bytesRead++;
			}
			file.close();
			Out.println("Admin Parse", "Successfully downloaded file: " + URLFile);
			return "success";
		}catch (MalformedURLException e){
			Out.println("Admin Parse", "MalformedURL: " + e.toString());
			return "MalformedURL: " + e.toString();
		}catch (IOException e){
			Out.println("Admin Parse", "IO Excaption: " + e.toString());
			return "IOException: " + e.toString();
		}
	}
}
