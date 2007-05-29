package bnubot.core.bnftp;

import java.io.*;
import java.net.Socket;

import bnubot.core.*;


public class BNFTPConnection {
	public static final String path = "downloads/";
	
	public static File downloadFile(ConnectionSettings cs, String fileName) {
		try {
			Socket s = new Socket(cs.server, cs.port);
			File f = downloadFile(s, fileName);
			s.close();
			return f;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}
	
	public static File downloadFile(Socket s, String fileName) {
		try {
			System.out.println("Downloading " + fileName + "...");
			
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
				if(!s.isConnected())
					throw new Exception("Download failed");
			}
	
			//Recieve the file
			is.skip(2);	//int headerLength = is.readWord();
			is.skip(2);	//int unknown = is.readWord();
			int fileSize = is.readDWord();
			is.skip(4);	//int bannersID = is.readDWord();
			is.skip(4);	//int bannersFileExt = is.readDWord();
			is.skip(8);	//long fileTime = is.readQWord();
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
			System.out.println(fileSize + " bytes recieved.");
			
			return f;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}
}
