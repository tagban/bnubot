/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.vercheck;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

import javax.swing.JOptionPane;

import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.Out;
import net.bnubot.util.SHA1Sum;
import net.bnubot.util.URLDownloader;

import org.jbls.util.Constants;

public class VersionCheck {
	protected static XMLElementDecorator elem = null;
	protected static VersionNumber vnLatest = null;
	
	public static boolean checkVersion() throws Exception {
		return checkVersion(false, GlobalSettings.releaseType);
	}
	
	public static boolean checkVersion(boolean forceDownload, ReleaseType rt) throws Exception {
		if(CurrentVersion.fromJar()) {
			String path = System.getProperty("net.bnubot.jarpath", "BNUBot.jar");
			if(!new File(path).exists())
				throw new FileNotFoundException(path);
			return checkVersion(forceDownload, rt, path, null);
		}
		
		return checkVersion(false, rt, null, null);
	}

	public static boolean checkVersion(boolean forceDownload, ReleaseType rt, String jarFileName, String downloadFolder) throws Exception {
		boolean cv = doCheckVersion(forceDownload, rt, jarFileName, downloadFolder);
		URLDownloader.flush();
		if(!cv)
			Out.debug(VersionCheck.class, "No update available.");
		return cv;
	}
	
	/**
	 * @return whether an update is available
	 */
	public static boolean doCheckVersion(boolean forceDownload, ReleaseType rt, String jarFileName, String downloadFolder) throws Exception {
		try {
			String url = "http://www.clanbnu.net/bnubot/version.php?";
			if(!forceDownload && (CurrentVersion.version().revision() != null))
				url += "svn=" + CurrentVersion.version().revision() + "&";
			url += "release=" + rt.toString();
			Out.debug(VersionCheck.class, "Requesting latest version from " + url);
			elem = XMLElementDecorator.parse(url);
		} catch(Exception e) {
			Out.error(VersionCheck.class, "Failed to get latest version: " + e.getClass().getSimpleName() + ".");
			return false;
		}

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
				XMLElementDecorator[] files = downloads.getChildren("file");
				for(XMLElementDecorator file : files) {
					XMLElementDecorator sha1Element = file.getChild("sha1");
					SHA1Sum sha1 = null;
					if(sha1Element != null)
						sha1 = new SHA1Sum(sha1Element.getString());
					String to = file.getChild("to").getString();
					if(downloadFolder != null)
						to = downloadFolder + "/" + to;
					URLDownloader.downloadURL(
						new URL(file.getChild("from").getString()),
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
		
		if(url != null) {
			try {
				if(jarFileName != null) {
					File thisJar = new File(jarFileName);
					if(thisJar.exists()) {
						String msg = "BNU-Bot version " + vnLatest.toString() + " is available.\nWould you like to update?";
						if(JOptionPane.showConfirmDialog(null, msg, "BNU-Bot update available", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
							URLDownloader.downloadURL(new URL(url), thisJar, sha1, true);
							URLDownloader.flush();
							JOptionPane.showMessageDialog(null, "Update complete. Please restart BNU-Bot.");
							System.exit(0);
						}
						return true;
					}
				}
			} catch(Exception e) {
				Out.exception(e);
			}
			
			Out.error(VersionCheck.class, "Update: " + url);
		}
		return true;
	}
}
