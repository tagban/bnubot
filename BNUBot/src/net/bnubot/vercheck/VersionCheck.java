/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.vercheck;

import java.io.File;
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
		
		if(CurrentVersion.fromJar()) {
			XMLElementDecorator downloads = elem.getPath("bnubot/downloads");
			if(downloads != null) {
				for(XMLElementDecorator file : downloads.getChildren("file")) {
					XMLElementDecorator sha1Element = file.getChild("sha1");
					SHA1Sum sha1 = null;
					if(sha1Element != null)
						sha1 = new SHA1Sum(sha1Element.getString());
					URLDownloader.downloadURL(
						new URL(file.getChild("from").getString()),
						new File(file.getChild("to").getString()),
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
				verLatest.getChild("built").getString());

		String url = verLatest.getChild("url").getString();
		if(forceDownload) {
			if(url == null)
				return false;
			URLDownloader.downloadURL(new URL(url), new File("BNUBot.jar"), null, true);
			return true;
		}
			
		if(!vnLatest.isNewerThan(CurrentVersion.version()))
			return false;
		
		Out.error(VersionCheck.class, "Latest version: " + vnLatest.toString());
		
		if(url != null) {
			try {
				File thisJar = new File("BNUBot.jar");
				if(thisJar.exists()) {
					String msg = "There is an update to BNU-Bot avalable.\nWould you like to update to version " + vnLatest.toString() + "?";
					if(JOptionPane.showConfirmDialog(null, msg, "Update?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
						URLDownloader.downloadURL(new URL(url), new File("BNUBot.jar"), null, true);
						JOptionPane.showMessageDialog(null, "Update complete. Please restart BNU-Bot.");
						System.exit(0);
					}
					return true;
				}
			} catch(Exception e) {
				Out.exception(e);
			}
			
			Out.error(VersionCheck.class, "Update: " + url);
		}
		return true;
	}
}
