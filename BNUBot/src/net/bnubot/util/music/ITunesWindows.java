/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util.music;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import net.bnubot.JARLoader;
import net.bnubot.util.OperatingSystem;

class ITunesWindows extends MusicController {
	private static Class<?> ActiveXComponent;
	private static Constructor<?> ActiveXComponent_constructor;
	private static Method ActiveXComponent_getObject;
	private static Class<?> ComThread;
	private static Method ComThread_InitMTA;
	private static Method ComThread_Release;
	private static Class<?> Dispatch;
	private static Method Dispatch_call;
	static {
		if(!OperatingSystem.userOS.equals(OperatingSystem.WINDOWS))
			throw new IllegalStateException("Only supported by Windows");

		try {
			ActiveXComponent = JARLoader.forName("com.jacob.activeX.ActiveXComponent");
			ActiveXComponent_constructor = ActiveXComponent.getConstructor(String.class);
			ActiveXComponent_getObject = ActiveXComponent.getMethod("getObject");
			ComThread = JARLoader.forName("com.jacob.com.ComThread");
			ComThread_InitMTA = ComThread.getMethod("InitMTA", boolean.class);
			ComThread_Release = ComThread.getMethod("Release");
			Dispatch = JARLoader.forName("com.jacob.com.Dispatch");
			Dispatch_call = Dispatch.getMethod("call", Dispatch, String.class);
		} catch(Throwable t) {
			throw new IllegalStateException(t);
		}

	}

	private void comCommand(String cmd) {
		try {
			ComThread_InitMTA.invoke(null, true);
			Object iTunesAX = ActiveXComponent_constructor.newInstance("iTunes.Application");
			Object iTunesDispatch = ActiveXComponent_getObject.invoke(iTunesAX);
			Dispatch_call.invoke(null, iTunesDispatch, cmd);
			ComThread_Release.invoke(null);
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
		return "[error]";
	}
}