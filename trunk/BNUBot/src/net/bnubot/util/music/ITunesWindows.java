/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util.music;

import net.bnubot.util.OperatingSystem;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;

class ITunesWindows extends MusicController {
	static {
		if(!OperatingSystem.userOS.equals(OperatingSystem.WINDOWS))
			throw new IllegalStateException("Only supported by Windows");
	}
	
	private void comCommand(String cmd) {
		ComThread.InitMTA(true);
		Dispatch.call(new ActiveXComponent("iTunes.Application").getObject(), cmd);
		ComThread.Release();
	}
	
	@Override
	public void play() { comCommand("Play"); }
	
	@Override
	public void pause() { comCommand("Pause"); }
	
	@Override
	public String getCurrentlyPlaying() {
		// TODO Auto-generated method stub
		return "[error]";
	}
}