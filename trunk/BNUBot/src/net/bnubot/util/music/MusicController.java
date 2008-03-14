/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util.music;

import net.bnubot.util.OperatingSystem;

public abstract class MusicController {
	private static MusicController mc = createMusicController(MediaPlayer.ITUNES);
	
	public static MusicController getMusicController() throws IllegalStateException {
		return mc;
	}
	
	private static MusicController createMusicController(MediaPlayer type) throws IllegalStateException {
		switch(type) {
		case ITUNES:
			switch(OperatingSystem.userOS) {
			case OSX: return new ITunesOSX();
			case WINDOWS: return new ITunesWindows();
			}
		}
		throw new IllegalStateException("Unsupported OS/MediaPlayer combination");
	}

	public abstract void play();
	public abstract void pause();
	public abstract String getCurrentlyPlaying();
}
