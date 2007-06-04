package bnubot.bot.gui.ColorScheme;

import java.awt.Color;

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
		System.err.println("Unknown ColorScheme id " + Byte.toString(colorScheme));
		return null;
	}
	
	public abstract Color getBackgroundColor();
	public abstract Color getTimeStampColor();
	public abstract Color getChannelColor();
	public abstract Color getInfoColor();
	public abstract Color getErrorColor();
	
	public abstract Color getUserNameColor(int flags);
	public abstract Color getUserNameListColor(int flags);
	public abstract Color getChatColor(int flags);
	public abstract Color getEmoteColor(int flags);
	public abstract Color getWhisperColor(int flags);
}
