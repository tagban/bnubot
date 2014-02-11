/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util.music;

import java.util.List;

import org.jdesktop.jdic.mpcontrol.IMediaPlayer;
import org.jdesktop.jdic.mpcontrol.MediaPlayerService;

/**
 * @author scotta
 */
class MCJDIC implements MusicController {
	private IMediaPlayer mp = null;

	@SuppressWarnings("unchecked")
	MCJDIC() throws Exception {
		MediaPlayerService mps = MediaPlayerService.getInstance();
		List<IMediaPlayer> mediaPlayers = mps.getMediaPlayers();
		for(IMediaPlayer mp : mediaPlayers) {
			// Make sure the native libraries exist
			if(!mp.isAvailableMediaPlayer())
				continue;

			// Initialize the controller
			mp.init();

			// Start it if it's not running
			if(!mp.isRunning() && !mp.startPlayerProcess())
				continue;

			this.mp = mp;
			break;
		}
		if(this.mp == null)
			throw new IllegalStateException("Failed to initialize a JDIC media player!");
	}

	@Override
	public String getCurrentlyPlaying() {
		if(!mp.isRunning())
			return mp.getName() + ": Not running";
		if(!mp.isPlaying())
			return mp.getName() + ": Stopped";
		return mp.getCurrentSong().getSongTitle();
	}

	@Override
	public void pause() {
		if(!mp.isRunning() || !mp.isPlaying())
			return;
		mp.init();
		mp.pause();
	}

	@Override
	public void play() {
		if(!mp.isRunning() || mp.isPlaying())
			return;
		mp.init();
		mp.play();
	}

}
