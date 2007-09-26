/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.util;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

/**
 * @author sanderson
 *
 */
public class URLDownloader {
	public static void downloadURL(URL url, String to) throws IOException {
		File f = new File(to);
		if(f.exists())
			return;
		
		Out.info(URLDownloader.class, "Downloading " + url.toExternalForm());
		
		DataInputStream is = new DataInputStream(new BufferedInputStream(url.openStream()));
		FileOutputStream os = new FileOutputStream(f);
		byte[] b = new byte[0x1000];
		do {
			int c = is.read(b);
			if(c == -1)
				break;
			os.write(b, 0, c);
		} while(true);
		
		os.close();
		is.close();
	}
}

