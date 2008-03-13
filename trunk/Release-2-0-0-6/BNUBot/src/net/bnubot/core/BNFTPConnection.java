/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.core;

import java.io.File;
import java.io.FileOutputStream;
import java.net.Socket;
import java.util.Date;

import net.bnubot.settings.ConnectionSettings;
import net.bnubot.util.BNetInputStream;
import net.bnubot.util.BNetOutputStream;
import net.bnubot.util.Out;
import net.bnubot.util.TimeFormatter;

public class BNFTPConnection {
	public static final String defaultPath = "downloads/";
	
	public static File downloadFile(ConnectionSettings cs, String fileName) {
		return downloadFile(cs, fileName, defaultPath);
	}
	
	public static File downloadFile(ConnectionSettings cs, String fileName, String path) {
		File f = new File(path + fileName);
		if(f.exists())
			return f;
		
		try {
			Socket s = new Socket(cs.bncsServer, cs.port);
			f = downloadFile(s, fileName, path);
			s.close();
			return f;
		} catch (Exception e) {
			Out.fatalException(e);
		}
		return null;
	}
	
	public static File downloadFile(Socket s, String fileName, String path) {
		try {
			Out.info(BNFTPConnection.class, "Downloading " + fileName + "...");
			
			BNetInputStream is = new BNetInputStream(s.getInputStream());
			BNetOutputStream os = new BNetOutputStream(s.getOutputStream());
			
			//FTP
			os.writeByte(0x02);
			
			//File request
			os.writeWord(32 + fileName.length() + 1);
			os.writeWord(0x100);		// Protocol version
			os.writeDWord("IX86");	// Platform ID
			os.writeDWord("STAR");	// Product ID
			os.writeDWord(0);		// Banners ID
			os.writeDWord(0);		// Banners File Extension
			os.writeDWord(0);		// File position
			os.writeQWord(0);		// Filetime
			os.writeNTString(fileName);
			
			while(is.available() == 0) {
				if(s.isClosed())
					throw new Exception("Download failed");
			}
	
			//Recieve the file
			is.skip(2);	//int headerLength = is.readWord();
			is.skip(2);	//int unknown = is.readWord();
			int fileSize = is.readDWord();
			is.skip(4);	//int bannersID = is.readDWord();
			is.skip(4);	//int bannersFileExt = is.readDWord();
			Date fileTime = TimeFormatter.fileTime(is.readQWord());
			fileName = is.readNTString();
	
			//The rest is the data
			new File(path).mkdir();
			File f = new File(path + fileName);
			FileOutputStream fw = new FileOutputStream(f);
			for(int i = 0; i < fileSize; i++) {
				int b = is.readByte();
				b = b & 0xFF;
				fw.write(b);
			}
			fw.close();
			
			Out.info(BNFTPConnection.class, fileTime.toString());
			f.setLastModified(fileTime.getTime());
			
			Out.info(BNFTPConnection.class, fileSize + " bytes recieved.");
			
			return f;
		} catch (Exception e) {
			Out.fatalException(e);
		}
		return null;
	}
}
