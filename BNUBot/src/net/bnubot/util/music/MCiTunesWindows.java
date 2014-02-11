/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util.music;

import net.bnubot.util.OperatingSystem;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;

/**
 * @author scotta
 */
class MCiTunesWindows implements MusicController {
	static {
		if(!OperatingSystem.userOS.equals(OperatingSystem.WINDOWS))
			throw new IllegalStateException("Only supported by Windows");
	}

	private void comCommand(String cmd) {
		try {
			ComThread.InitMTA(true);
			Dispatch.call(new ActiveXComponent("iTunes.Application").getObject(), cmd);
			ComThread.Release();
		} catch(Throwable t) {
			throw new IllegalStateException(t);
		}
	}

	@Override
	public void play() { comCommand("Play"); }

	@Override
	public void pause() { comCommand("Pause"); }

	@Override
	public String getCurrentlyPlaying() {
		// TODO Auto-generated method stub
		return "[unsupported]";
	}
}