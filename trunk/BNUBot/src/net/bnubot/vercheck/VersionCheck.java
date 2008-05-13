/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.vercheck;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.swing.JOptionPane;

import net.bnubot.settings.GlobalSettings;
import net.bnubot.settings.Settings;
import net.bnubot.util.OperatingSystem;
import net.bnubot.util.Out;
import net.bnubot.util.SHA1Sum;
import net.bnubot.util.URLDownloader;

import org.jbls.util.Constants;

public class VersionCheck {
	private static final String VERSION_CHECK_TIME = "versionCheckTime";

	protected static XMLElementDecorator elem = null;
	protected static VersionNumber vnLatest = null;

	/**
	 * Helper method to the version check
	 * @param force If enabled, do the version check no matter what. Otherwise, only do it once every six hours
	 * @return True if there was an update; false if none available or no version check was performed
	 * @throws Exception If an error occurred
	 */
	public static boolean checkVersion(boolean force) throws Exception {
		if(!force) {
			final long lastVersionCheck = Settings.read(null, VERSION_CHECK_TIME, 0l);
			long now = System.currentTimeMillis();
			// Wait 6 hours
			if(now - lastVersionCheck < 6 * 60 * 60 * 1000)
				return false;
		}

		return checkVersion(false, GlobalSettings.releaseType);
	}

	/**
	 * Helper method to the version check
	 * @see #doCheckVersion(boolean, ReleaseType, String, String)
	 */
	private static boolean checkVersion(boolean forceDownload, ReleaseType rt) throws Exception {
		if(CurrentVersion.fromJar()) {
			String path = System.getProperty("net.bnubot.jarpath", "BNUBot.jar");
			if(!new File(path).exists())
				throw new FileNotFoundException(path);
			return checkVersion(forceDownload, rt, path, null);
		}

		return checkVersion(false, rt, null, null);
	}

	/**
	 * Helper method to the version check
	 * This method is protected because it is used by InstallMain
	 * @see #doCheckVersion(boolean, ReleaseType, String, String)
	 */
	protected static boolean checkVersion(boolean forceDownload, ReleaseType rt, String jarFileName, String downloadFolder) throws Exception {
		boolean cv = doCheckVersion(forceDownload, rt, jarFileName, downloadFolder);
		URLDownloader.flush();
		if(!cv)
			Out.debug(VersionCheck.class, "No update available.");
		return cv;
	}

	/**
	 * Go ahead and do the version check for real now
	 * @param forceDownload Install mode: don't ask any questions, just download everything
	 * @param rt The ReleaseType to check for
	 * @param jarFileName Location of BNUBot.jar
	 * @param downloadFolder Location of the install path
	 * @return whether an update is available
	 * @throws Exception if an error occurred
	 */
	private static boolean doCheckVersion(boolean forceDownload, ReleaseType rt, String jarFileName, String downloadFolder) throws Exception {
		Settings.write(null, VERSION_CHECK_TIME, System.currentTimeMillis());
		Settings.store();

		try {
			String url = "http://www.clanbnu.net/bnubot/version.php?";
			if(!forceDownload && (CurrentVersion.version().revision() != null))
				url += "svn=" + CurrentVersion.version().revision() + "&";
			url += "release=" + rt.toString();
			url += "&os=" + OperatingSystem.userOS.name();
			Out.debug(VersionCheck.class, "Requesting latest version from " + url);
			elem = XMLElementDecorator.parse(url);
		} catch(Exception e) {
			Out.error(VersionCheck.class, "Failed to get latest version: " + e.getClass().getSimpleName() + ".");
			return false;
		}

		if(downloadFolder != null)
			jarFileName = downloadFolder + File.separatorChar + jarFileName;

		XMLElementDecorator error = elem.getChild("error");
		if(error != null) {
			Out.error(VersionCheck.class, error.getString());
			return false;
		}

		XMLElementDecorator motd = elem.getPath("bnubot/motd");
		if((motd != null) && (motd.getString() != null))
			Out.info(VersionCheck.class, motd.getString());

		if(forceDownload || CurrentVersion.fromJar()) {
			XMLElementDecorator downloads = elem.getPath("bnubot/downloads");
			if(downloads != null) {
				for(XMLElementDecorator file : downloads.getChildren("file")) {
					XMLElementDecorator sha1Element = file.getChild("sha1");
					SHA1Sum sha1 = null;
					if(sha1Element != null)
						sha1 = new SHA1Sum(sha1Element.getString());
					String from = file.getChild("from").getString();
					String to = file.getChild("to").getString();
					if(downloadFolder != null)
						to = downloadFolder + File.separatorChar + to;
					URLDownloader.downloadURL(
						new URL(from),
						new File(to),
						sha1,
						false);
				}
			}
		}

		XMLElementDecorator gamesElem = elem.getPath("bnubot/games");
		if(gamesElem != null)
			for(int i = 0; i < Constants.prods.length; i++) {
				String game = Constants.prods[i];
				int verByte = Constants.IX86verbytes[i];

				XMLElementDecorator gameElem = gamesElem.getPath(game);
				if(gameElem == null)
					continue;

				int vb = gameElem.getPath("verbyte").getInt();

				if(verByte != vb) {
					Out.error(VersionCheck.class, "Verbyte for game " + game + " is updating from 0x" + Integer.toHexString(verByte) + " to 0x" + Integer.toHexString(vb));
					Constants.IX86verbytes[i] = vb;
				}
			}

		XMLElementDecorator verLatest = elem.getPath("bnubot/latestVersion");
		if(verLatest == null)
			return false;

		vnLatest = new VersionNumber(
				Enum.valueOf(ReleaseType.class, verLatest.getChild("type").getString()),
				verLatest.getChild("major").getInt(),
				verLatest.getChild("minor").getInt(),
				verLatest.getChild("revision").getInt(),
				verLatest.getChild("release").getInt(),
				verLatest.getChild("svn").getInt(),
				verLatest.getChild("built").getDate());

		String url = verLatest.getChild("url").getString();

		XMLElementDecorator sha1Element = verLatest.getChild("sha1");
		SHA1Sum sha1 = null;
		if((sha1Element != null) && (sha1Element.getString() != null))
			sha1 = new SHA1Sum(sha1Element.getString());

		if(forceDownload) {
			if(url == null)
				return false;

			URLDownloader.downloadURL(new URL(url), new File(jarFileName), sha1, true);
			return true;
		}

		if(!vnLatest.isNewerThan(CurrentVersion.version()))
			return false;

		Out.error(VersionCheck.class, "Latest version: " + vnLatest.toString());

		if(url == null)
			return true;

		try {
			if(jarFileName != null) {
				File thisJar = new File(jarFileName);
				if(thisJar.exists()) {
					String msg = "BNU-Bot version " + vnLatest.toString() + " is available.\nWould you like to update?";
					if(JOptionPane.showConfirmDialog(null, msg, "BNU-Bot update available", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
						// Find the parent folder
						String parentFolder = thisJar.getAbsolutePath();
						parentFolder = parentFolder.substring(0, parentFolder.lastIndexOf(File.separatorChar) + 1);

						// Download to download.jar
						File to = new File(parentFolder + "download.jar");
						URLDownloader.downloadURL(new URL(url), to, sha1, true);
						URLDownloader.flush();

						// Swap the files
						renameFile(thisJar, new File(parentFolder + CurrentVersion.version().toFileName()));
						renameFile(to, thisJar);
						to.delete();

						// Show complete notification
						JOptionPane.showMessageDialog(null, "Update complete. Please restart BNU-Bot.");
						restart();
					}
					return true;
				}
			}
		} catch(Exception e) {
			Out.exception(e);
		}

		Out.error(VersionCheck.class, "Update: " + url);
		return true;
	}

	private static void renameFile(File from, File to) throws IOException {
		if(from.renameTo(to))
			return;

		// Windows doesn't allow renaming open files; copy the contents instead
		FileInputStream is = new FileInputStream(from);
		FileOutputStream os = new FileOutputStream(to);
		byte[] b = new byte[1024];

		do {
			int c = is.read(b);
			if(c == -1)
				break;
			os.write(b, 0, c);
		} while(true);

		os.close();
		is.close();

		to.setLastModified(from.lastModified());
	}

	/**
	 * This method NEVER returns normally!
	 */
	public static void restart() {
		String command;
		switch(OperatingSystem.userOS) {
		case WINDOWS:
			command = "BNUBot.exe";
			break;
		case OSX:
			command = "Contents/MacOS/JavaApplicationStub";
			break;
		default:
			command = "./run.sh";
			break;
		}
		try {
			System.err.println(command);
			Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			Out.exception(e);
		}
		System.exit(0);
	}
}
