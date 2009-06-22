/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui.colors;

import java.awt.Color;

import net.bnubot.logging.Out;
import net.bnubot.settings.GlobalSettings;

/**
 * @author scotta
 */
public abstract class ColorScheme {
	public static final byte COLORSCHEME_STARCRAFT = (byte)0x01;
	public static final byte COLORSCHEME_DIABLO2 = (byte)0x02;
	public static final byte COLORSCHEME_INVIG = (byte)0x03;

	public static ColorScheme colors = null;
	public static ColorScheme getColors() {
		if(colors == null)
			return createColorScheme(GlobalSettings.colorScheme);
		return colors;
	}

	private static ColorScheme createColorScheme(byte colorScheme) {
		switch(colorScheme) {
		case COLORSCHEME_STARCRAFT:
			return new StarcraftColorScheme();
		case COLORSCHEME_DIABLO2:
			return new Diablo2ColorScheme();
		case COLORSCHEME_INVIG:
			return new InvigorationColorScheme();
		}
		Out.error(ColorScheme.class, "Unknown ColorScheme id " + Byte.toString(colorScheme));
		return null;
	}

	public abstract Color getBackgroundColor();
	public abstract Color getForegroundColor();
	public abstract Color getChannelColor();
	public abstract Color getInfoColor();
	public abstract Color getErrorColor();
	public abstract Color getDebugColor();
	public Color getTypeColor() {
		return Diablo2ColorScheme.D2Purple;
	}

	public abstract Color getUserNameColor(int flags);
	public abstract Color getSelfUserNameColor(int flags);
	public abstract Color getUserNameListColor(int flags, boolean myUser);
	public abstract Color getChatColor(int flags);
	public abstract Color getEmoteColor(int flags);
	public abstract Color getWhisperColor(int flags);
}
