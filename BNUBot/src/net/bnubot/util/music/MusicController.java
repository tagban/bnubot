/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util.music;

import net.bnubot.util.OperatingSystem;

public abstract class MusicController {
	public static MediaPlayer[] getMediaPlayers() {
		// TODO: Depending on OS and supported 
		return MediaPlayer.values();
	}
	
	public static void main(String[] args) throws IllegalStateException {
		MusicController mc = getMusicController(MediaPlayer.ITUNES);
		mc.play();
		mc.pause();
	}
	
	public static MusicController getMusicController() throws IllegalStateException {
		return getMusicController(MediaPlayer.ITUNES);
	}
	
	private static MusicController mc = null;
	public static MusicController getMusicController(MediaPlayer type) throws IllegalStateException {
		if(mc != null)
			return mc;
		return mc = createMusicController(type);
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
