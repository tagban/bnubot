/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.vercheck;

import net.bnubot.core.Connection;
import util.Constants;

public class VersionCheck {
	protected static XMLElementDecorator elem = null;
	protected static VersionNumber vnLatest = null;
	
	public static boolean checkVersion(Connection reportTo) throws Exception {
		{
			String url = "http://www.clanbnu.ws/bnubot/version.php";
			if(CurrentVersion.revision() != null)
				url += "?svn=" + CurrentVersion.revision();
			elem = XMLElementDecorator.parse(url);
		}

		XMLElementDecorator error = elem.getChild("error");
		if(error != null) {
			System.err.println("Version check error:\n" + error.toString());
			return false;
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
					System.err.println("Verbyte for game " + game + " is updating from 0x" + Integer.toHexString(verByte) + " to 0x" + Integer.toHexString(vb));
					Constants.IX86verbytes[i] = vb;
				}
			}
		else
			System.out.println("Version check resulted in no games!");

		XMLElementDecorator verLatest = elem.getPath("bnubot/latestVersion");
		
		vnLatest = new VersionNumber(
				verLatest.getChild("major").getInt(),
				verLatest.getChild("minor").getInt(),
				verLatest.getChild("revision").getInt(),
				verLatest.getChild("alpha").getInt(),
				verLatest.getChild("beta").getInt(),
				verLatest.getChild("rc").getInt(),
				verLatest.getChild("svn").getInt());
		
		String url = verLatest.getChild("url").getString();
		VersionNumber vnCurrent = CurrentVersion.version();

		boolean update = vnLatest.isNewerThan(vnCurrent);
		if(update && reportTo != null) {
			reportTo.recieveError("Current version: " + vnCurrent.toString());
			reportTo.recieveError("Latest version: " + vnLatest.toString());
			if(url != null)
				reportTo.recieveError("Update: " + url);
		}
		return update;
	}
	
	public static VersionNumber getLatestVersion() throws Exception {
		if(vnLatest == null)
			checkVersion(null);
		return vnLatest;
	}
}
