/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.bot.gui.ColorScheme;

import java.awt.Color;

import net.bnubot.util.Out;

public abstract class ColorScheme {
	public static final byte COLORSCHEME_STARCRAFT = (byte)0x01;
	public static final byte COLORSCHEME_DIABLO2 = (byte)0x02;
	
	public static ColorScheme createColorScheme(byte colorScheme) {
		switch(colorScheme) {
		case COLORSCHEME_STARCRAFT:
			return new StarcraftColorScheme();
		case COLORSCHEME_DIABLO2:
			return new Diablo2ColorScheme();
		}
		Out.error(ColorScheme.class, "Unknown ColorScheme id " + Byte.toString(colorScheme));
		return null;
	}
	
	public abstract Color getBackgroundColor();
	public abstract Color getTimeStampColor();
	public abstract Color getChannelColor();
	public abstract Color getInfoColor();
	public abstract Color getErrorColor();
	public abstract Color getDebugColor();
	
	public abstract Color getUserNameColor(int flags);
	public abstract Color getUserNameListColor(int flags);
	public abstract Color getChatColor(int flags);
	public abstract Color getEmoteColor(int flags);
	public abstract Color getWhisperColor(int flags);
}
