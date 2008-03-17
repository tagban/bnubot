/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util.music;

public interface MusicController {
	public void play();
	public void pause();
	public abstract String getCurrentlyPlaying();
}
