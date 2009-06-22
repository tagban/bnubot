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

import net.bnubot.core.ChatQueue;
import net.bnubot.core.PluginManager;
import net.bnubot.logging.Out;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.settings.Settings;
import net.bnubot.util.BNetUser;
import net.bnubot.util.OperatingSystem;
import net.bnubot.util.SHA1Sum;
import net.bnubot.util.URLDownloader;

import org.jbls.util.Constants;

/**
 * @author scotta
 */
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
		return checkVersion(force, null, false);
	}

	public static boolean checkVersion(boolean force, BNetUser bnSubject, boolean whisperBack) throws Exception {
		if(!force) {
			final long lastVersionCheck = Settings.getSection(null).read(VERSION_CHECK_TIME, 0l);
			long now = System.currentTimeMillis();
			// Wait 6 hours
			if(now - lastVersionCheck < 6 * 60 * 60 * 1000)
				return false;
		}

		return checkVersion(false, GlobalSettings.releaseType, bnSubject, whisperBack);
	}

	/**
	 * Helper method to the version check
	 * @see #doCheckVersion(boolean, ReleaseType, String, String)
	 */
	private static boolean checkVersion(boolean forceDownload, ReleaseType rt, BNetUser bnSubject, boolean whisperBack) throws Exception {
		if(CurrentVersion.fromJar()) {
			String path = System.getProperty("net.bnubot.jarpath", "BNUBot.jar");
			if(!new File(path).exists())
				throw new FileNotFoundException(path);
			return checkVersion(forceDownload, rt, path, null, bnSubject, whisperBack);
		}

		return checkVersion(false, rt, null, null, bnSubject, whisperBack);
	}

	/**
	 * Helper method to the version check
	 * This method is protected because it is used by InstallMain
	 * @see #doCheckVersion(boolean, ReleaseType, String, String)
	 */
	protected static boolean checkVersion(boolean forceDownload, ReleaseType rt, String jarFileName, String downloadFolder) throws Exception {
		return checkVersion(forceDownload, rt, jarFileName, downloadFolder, null, false);
	}

	private static boolean checkVersion(boolean forceDownload, ReleaseType rt, String jarFileName, String downloadFolder, BNetUser bnSubject, boolean whisperBack) throws Exception {
		boolean cv = doCheckVersion(forceDownload, rt, jarFileName, downloadFolder, bnSubject, whisperBack);
		URLDownloader.flush();
		if(!cv)
			if(bnSubject == null)
				Out.debug(VersionCheck.class, "No update available.");
			else
				bnSubject.sendChat("No update available.", whisperBack);
		return cv;
	}

	/**
	 * Go ahead and do the version check for real now
	 * @param forceDownload Install mode: don't ask any questions, just download everything
	 * @param rt The ReleaseType to check for
	 * @param jarFileName Location of BNUBot.jar
	 * @param downloadFolder Location of the install path
	 * @param bnSubject The user to send information to
	 * @param whisperBack Was the initial command a whisper?
	 * @return whether an update is available
	 * @throws Exception if an error occurred
	 */
	private static boolean doCheckVersion(boolean forceDownload, ReleaseType rt, String jarFileName, String downloadFolder, BNetUser bnSubject, boolean whisperBack) throws Exception {
		if(!forceDownload) {
			Settings.getSection(null).write(VERSION_CHECK_TIME, System.currentTimeMillis());
			Settings.store();
		}

		try {
			String url = "http://www.clanbnu.net/bnubot/version.php?";
			if(!forceDownload && (CurrentVersion.version().getSvnRevision() != null))
				url += "svn=" + CurrentVersion.version().getSvnRevision() + "&";
			url += "release=" + rt.toString();
			url += "&os=" + OperatingSystem.userOS.name();
			if(Out.isDebug(VersionCheck.class))
				Out.debugAlways(VersionCheck.class, "Requesting latest version from " + url);
			elem = XMLElementDecorator.parse(url);
		} catch(Exception e) {
			if(bnSubject == null)
				Out.error(VersionCheck.class, "Failed to get latest version: " + e.getClass().getSimpleName() + ".");
			else
				bnSubject.sendChat("Failed to get latest version: " + e.getClass().getSimpleName() + ".", whisperBack);
			return false;
		}

		if(downloadFolder != null)
			jarFileName = downloadFolder + File.separatorChar + jarFileName;

		XMLElementDecorator error = elem.getChild("error");
		if(error != null) {
			if(bnSubject == null)
				Out.error(VersionCheck.class, error.getString());
			else
				bnSubject.sendChat(error.getString(), whisperBack);
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

		String releaseType = verLatest.getChild("type").getString();
		// FIXME: this is for logging, to collect data
		if(releaseType == null)
			throw new NullPointerException("type is null\n" + verLatest.toString());
		vnLatest = new VersionNumber(
				Enum.valueOf(ReleaseType.class, releaseType),
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

		if(jarFileName != null) {
			File thisJar = new File(jarFileName);
			if(thisJar.exists()) {
				boolean doUpdate = true;
				try {
					if((bnSubject == null) && PluginManager.getEnableGui()) {
						String msg = "BNU-Bot version " + vnLatest.toString() + " is available.\nWould you like to update?";
						doUpdate = JOptionPane.showConfirmDialog(null, msg, "BNU-Bot update available", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
					}
				} catch(Exception e) {
					// GUI is probably broken
					Out.exception(e);
				}
				if(doUpdate) {
					// Find the parent folder
					String parentFolder = thisJar.getAbsolutePath();
					parentFolder = parentFolder.substring(0, parentFolder.lastIndexOf(File.separatorChar) + 1);

					// Download to download.jar
					File to = new File(parentFolder + "download.jar");
					URLDownloader.downloadURL(new URL(url), to, sha1, true);
					URLDownloader.flush();

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

					// Swap the files
					renameFile(thisJar, new File(parentFolder + CurrentVersion.version().toFileName()));
					renameFile(to, thisJar);
					to.delete();

					// Show complete notification
					try {
						if((bnSubject == null) && PluginManager.getEnableGui())
							JOptionPane.showMessageDialog(null, "Update complete. BNU-Bot will now attempt to restart.");
					} catch(Exception e) {
						// GUI is probably broken
						Out.exception(e);
					}

					if(bnSubject != null) {
						bnSubject.sendChat("Updated to " + vnLatest.toString() + "; restarting", whisperBack);

						// Wait a maximum of 10 seconds for the queue to empty
						long target = System.currentTimeMillis() + 10000;
						ChatQueue cq = bnSubject.getConnection().getProfile().getChatQueue();
						while((cq.size() > 0) && (target > System.currentTimeMillis())) {
							Thread.sleep(100);
							Thread.yield();
						}
					}

					try {
						Runtime.getRuntime().exec(command);
					} catch (Throwable e) {
						// Restart failed; oh well, nothing we can do about it now!
						Out.exception(e);
					}
					System.exit(0);
				}
				return true;
			}
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
}
