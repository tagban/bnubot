/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util.music;

import net.bnubot.util.OperatingSystem;

/**
 * @author scotta
 */
public class MusicControllerFactory {
	private static MusicController mc = null;

	public static MusicController getMusicController() throws IllegalStateException {
		if(mc == null)
			mc = createMusicController();
		return mc;
	}

	private static MusicController createMusicController() throws IllegalStateException {
		try {
			switch(OperatingSystem.userOS) {
			case OSX:
				return new MCiTunesOSX();
			case WINDOWS:
				// TODO: let windows users select between iTunes and Winamp
				//return new MCiTunesWindows();
				return new MCJDIC();
			default:
				return new MCJDIC();
			}
		} catch(Exception e) {
			throw new IllegalStateException("Unsupported OS/MediaPlayer combination", e);
		}
	}
}
