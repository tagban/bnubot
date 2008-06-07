/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core;

import java.io.File;
import java.io.FileOutputStream;
import java.net.Socket;
import java.util.Date;

import net.bnubot.core.bncs.ProductIDs;
import net.bnubot.settings.ConnectionSettings;
import net.bnubot.settings.Settings;
import net.bnubot.util.BNetInputStream;
import net.bnubot.util.BNetOutputStream;
import net.bnubot.util.Out;
import net.bnubot.util.TimeFormatter;

public class BNFTPConnection {
	public static final String defaultPath = Settings.getRootPath() + "downloads/";

	/**
	 * Download a file using BNFTP
	 * @param cs The ConnectionSettings to connect to battle.net with
	 * @param fileName The file's name
	 * @return The File, or null if there was an error
	 */
	public static File downloadFile(ConnectionSettings cs, String fileName) throws Exception {
		return downloadFile(cs, fileName, null, defaultPath);
	}

	public static File downloadFile(ConnectionSettings cs, String fileName, Date lastModified) throws Exception {
		return downloadFile(cs, fileName, lastModified, defaultPath);
	}

	/**
	 * Download a file using BNFTP
	 * @param cs The ConnectionSettings to connect to battle.net with
	 * @param fileName The file's name
	 * @param path The folder to download the file to
	 * @return The File, or null if there was an error
	 */
	public static File downloadFile(ConnectionSettings cs, String fileName, Date lastModified, String path)  throws Exception {
		File f = new File(path + fileName);
		if(f.exists()) {
			if(lastModified == null)
				return f;
			if(f.lastModified() == lastModified.getTime())
				return f;
			Out.error(BNFTPConnection.class, fileName + " LM: " + new Date(f.lastModified()).toString() + " - " + f.lastModified());
			Out.error(BNFTPConnection.class, fileName + " TARGET: " + lastModified.toString() + lastModified.getTime());
		}

		Socket s = new Socket(cs.server, cs.port);
		f = downloadFile(s, cs.product, fileName, path);
		s.close();
		return f;
	}


	/**
	 * Download a file using BNFTP
	 * @param s The Socket which has already established a connection to Battle.net
	 * @param fileName The file's name
	 * @param path The folder to download the file to
	 * @return The File, or null if there was an error
	 */
	public static File downloadFile(Socket s, ProductIDs product, String fileName, String path) throws Exception {
		Out.info(BNFTPConnection.class, "Downloading " + fileName + "...");

		BNetInputStream is = new BNetInputStream(s.getInputStream());
		BNetOutputStream os = new BNetOutputStream(s.getOutputStream());

		//FTP
		os.writeByte(0x02);

		//File request
		os.writeWord(32 + fileName.length() + 1);
		os.writeWord(0x100);		// Protocol version
		os.writeDWord("IX86");	// Platform ID
		os.writeDWord(product.getDword());	// Product ID
		os.writeDWord(0);		// Banners ID
		os.writeDWord(0);		// Banners File Extension
		os.writeDWord(0);		// File position
		os.writeQWord(0);		// Filetime
		os.writeNTString(fileName);

		long startTime = System.currentTimeMillis();
		while(is.available() == 0) {
			if(s.isClosed() || (System.currentTimeMillis() - startTime) > 500)
				throw new Exception("BNFTP download failed");
		}

		//Receive the file
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
	}
}
