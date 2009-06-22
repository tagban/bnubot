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
import java.util.LinkedList;
import java.util.List;

import net.bnubot.logging.Out;
import net.bnubot.util.task.Task;
import net.bnubot.util.task.TaskManager;

/**
 * @author scotta
 */
public class URLDownloader {
	public static List<FileDownload> queue = new LinkedList<FileDownload>();

	private static class FileDownload {
		URL url;
		File to;
		SHA1Sum sha1;
		boolean force;

		public FileDownload(URL url, File to, SHA1Sum sha1, boolean force) {
			this.url = url;
			this.to = to;
			this.sha1 = sha1;
			this.force = force;
		}
	}

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
					if(Out.isDebug(URLDownloader.class))
						Out.debugAlways(URLDownloader.class, "SHA1 match for " + to.getName());
					return;
				}

				Out.error(URLDownloader.class, "SHA1 mismatch for " + to.getName() + "\nExpected: " + sha1 + "\nCalculated: " + fSHA1);
			}
		}

		queue.add(new FileDownload(url, to, sha1, force));
	}

	public static void flush() throws Exception {
		int num = queue.size();
		if(num <= 0)
			return;

		Task t = TaskManager.createTask("Download", num, "files");
		for(FileDownload fd : queue) {
			downloadURLNow(fd.url, fd.to, fd.sha1, fd.force);
			t.advanceProgress();
		}
		t.complete();
		queue.clear();
	}

	public static void downloadURLNow(URL url, File to, SHA1Sum sha1, boolean force) throws Exception {
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
		Task task = null;
		if(fileLength > 0)
			task = TaskManager.createTask(url.toExternalForm(), fileLength, "kB");

		do {
			int c = is.read(b);
			if(c == -1)
				break;
			os.write(b, 0, c);
			if(task != null)
				task.advanceProgress();
		} while(true);

		if(task != null)
			task.complete();

		os.close();
		is.close();
	}
}

