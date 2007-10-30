/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.util;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;

import net.bnubot.vercheck.ProgressBar;

public class URLDownloader {
	public static void downloadURL(URL url, File to, SHA1Sum sha1, boolean force) throws Exception {
		// Don't download the file if it already exists
		if(to.exists()) {
			// If no MD5 sum was given
			if(sha1 == null) {
				// Return if we're not forced to download the file
				if(!force)
					return;
			} else {
				// If the MD5 sums match
				SHA1Sum fSHA1 = new SHA1Sum(to);
				if(fSHA1.equals(sha1)) {
					Out.info(URLDownloader.class, "SHA1 match for " + to.getName());
					return;	
				}
				
				Out.info(URLDownloader.class, "SHA1 mismatch for " + to.getName() + "\nExpected: " + sha1 + "\nCalculated: " + fSHA1);
			}
		}
		
		// Make sure the path to the file exists
		{
			String sep = System.getProperty("file.separator");
			String folders = to.getPath();
			String path = "";
			for(int i = 0; i < folders.length(); i++) {
				path += folders.charAt(i);
				if(path.endsWith(sep)) {
					File f = new File(path);
					if(!f.exists())
						f.mkdir();
					if(!f.isDirectory()) {
						Out.error(URLDownloader.class, path + " is not a directory!");
						return;
					}
				}
			}
		}
		
		Out.info(URLDownloader.class, "Downloading " + url.toExternalForm());
		
		URLConnection uc = url.openConnection();
		DataInputStream is = new DataInputStream(new BufferedInputStream(uc.getInputStream()));
		FileOutputStream os = new FileOutputStream(to);
		byte[] b = new byte[1024];
		
		int fileLength = uc.getHeaderFieldInt("Content-Length", 0) / b.length;
		ProgressBar pb = null;
		if(fileLength > 0)
			pb = new ProgressBar(url.toExternalForm(), fileLength, "kB");
		
		do {
			int c = is.read(b);
			if(c == -1)
				break;
			os.write(b, 0, c);
			if(pb != null)
				pb.updateProgress();
		} while(true);
		
		if(pb != null)
			pb.dispose();
		
		os.close();
		is.close();
	}
}

