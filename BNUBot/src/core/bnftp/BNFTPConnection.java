package core.bnftp;

import java.io.*;
import java.net.Socket;

import core.*;
import core.bot.gui.icons.IconsDotBniReader;

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
			
			DataInputStream dis = new DataInputStream(s.getInputStream());
			DataOutputStream dos = new DataOutputStream(s.getOutputStream());
			
			// FTP
			dos.writeByte(0x02);
			
			BNetOutputStream p = new BNetOutputStream(dos);
			p.writeWord(32 + fileName.length() + 1);
			p.writeWord(0x100);		// Protocol version
			p.writeDWord("IX86");	// Platform ID
			p.writeDWord("STAR");	// Product ID
			p.writeDWord(0);		// Banners ID
			p.writeDWord(0);		// Banners File Extension
			p.writeDWord(0);		// File position
			p.writeQWord(0);		// Filetime
			p.writeNTString(fileName);
	
			//Recieve the file
			BNetInputStream is = new BNetInputStream(dis);
			int headerLength = is.readWord();
			int unknown = is.readWord();
			int fileSize = is.readDWord();
			int bannersID = is.readDWord();
			int bannersFileExt = is.readDWord();
			long fileTime = is.readQWord();
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
