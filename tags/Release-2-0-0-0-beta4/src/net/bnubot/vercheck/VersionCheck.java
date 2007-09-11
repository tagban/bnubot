/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.vercheck;

import org.jbls.util.Constants;

import net.bnubot.util.Out;

public class VersionCheck {
	protected static XMLElementDecorator elem = null;
	protected static VersionNumber vnLatest = null;
	
	public static boolean checkVersion(ReleaseType release) throws Exception {
		{
			String url = "http://www.clanbnu.ws/bnubot/version.php";
			if(CurrentVersion.version().revision() != null)
				url += "?svn=" + CurrentVersion.version().revision();
			url += "&release=" + release.toString();
			elem = XMLElementDecorator.parse(url);
		}

		XMLElementDecorator error = elem.getChild("error");
		if(error != null) {
			Out.error(VersionCheck.class, error.toString());
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
					Out.error(VersionCheck.class, "Verbyte for game " + game + " is updating from 0x" + Integer.toHexString(verByte) + " to 0x" + Integer.toHexString(vb));
					Constants.IX86verbytes[i] = vb;
				}
			}

		XMLElementDecorator verLatest = elem.getPath("bnubot/latestVersion");
		
		vnLatest = new VersionNumber(
				Enum.valueOf(ReleaseType.class, verLatest.getChild("type").getString()),
				verLatest.getChild("major").getInt(),
				verLatest.getChild("minor").getInt(),
				verLatest.getChild("revision").getInt(),
				verLatest.getChild("alpha").getInt(),
				verLatest.getChild("beta").getInt(),
				verLatest.getChild("rc").getInt(),
				verLatest.getChild("svn").getInt(),
				verLatest.getChild("built").getString());
		
		VersionNumber vnCurrent = CurrentVersion.version();

		if(!vnLatest.isNewerThan(vnCurrent))
			return false;
		
		Out.error(VersionCheck.class, "Current version: " + vnCurrent.toString());
		Out.error(VersionCheck.class, "Latest version (" + release.toString() + "): " + vnLatest.toString());
		
		String url = verLatest.getChild("url").getString();
		if(url != null)
			Out.error(VersionCheck.class, "Update: " + url);
		return true;
	}
}
