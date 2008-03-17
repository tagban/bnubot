/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util.music;

import net.bnubot.util.OperatingSystem;

public class MusicControllerFactory {
	private static MusicController mc = createMusicController(MediaPlayer.JDIC);
	
	public static MusicController getMusicController() throws IllegalStateException {
		return mc;
	}
	
	private static MusicController createMusicController(MediaPlayer type) throws IllegalStateException {
		try {
			switch(type) {
			case ITUNES:
				switch(OperatingSystem.userOS) {
				case OSX: return new MCiTunesOSX();
				case WINDOWS: return new MCiTunesWindows();
				}
				break;
			case JDIC:
				return new MCJDIC();
			}
			throw new IllegalStateException("Unsupported OS/MediaPlayer combination");
		} catch(Exception e) {
			throw new IllegalStateException(e);
		}		
	}
}
