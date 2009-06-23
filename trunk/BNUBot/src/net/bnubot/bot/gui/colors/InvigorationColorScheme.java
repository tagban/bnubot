/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.gui.colors;

import java.awt.Color;

/**
 * Invigoration Color Scheme Test for BNU`Bot 2.0
 * @author John Leighow
 */
public class InvigorationColorScheme extends ColorScheme {
	public static final Color InvigGray = new Color(0x242424);
	public static final Color InvigGray2 = new Color(0xAFAFAF);
	public static final Color InvigGreen = new Color(0x00CE00);
	public static final Color InvigRed = new Color(0x3E3ECE);
	public static final Color InvigWhite = new Color(0xFFFFFF);
	public static final Color InvigBlue = new Color(0x9C4044);
	public static final Color InvigBeige2 = new Color(0x659DA8);
	public static final Color InvigOrange = new Color(0x0088CE);
	public static final Color InvigLtYellow = new Color(0x51CECE);
	public static final Color InvigPurple = new Color(0xCE008D);
	public static final Color InvigCyan = new Color(0xFFFF00);
	public static final Color InvigMedBlue = new Color(0xE8AC2C);
	public static final Color InvigLtBlue = new Color(0xC0C000);
	public static final Color InvigHEXPINK = new Color(0x9900FF);
	public static final Color InvigBlack = new Color(0x000000);

	@Override
	public Color getBackgroundColor() {
		return InvigGray;
	}

	@Override
	public Color getForegroundColor() {
		return InvigWhite;
	}

	@Override
	public Color getChannelColor() {
		return InvigGreen;
	}

	@Override
	public Color getInfoColor() {
		return InvigLtBlue;
	}

	@Override
	public Color getErrorColor() {
		return InvigRed;
	}

	@Override
	public Color getDebugColor() {
		return InvigOrange;
	}

	@Override
	public Color getUserNameColor(int flags) {
		if((flags & 0x20) != 0)	return InvigRed; //Ignored User
		if((flags & 0x01) != 0)	return InvigCyan; //PRIORITY_BLIZZARD_REP;
		if((flags & 0x08) != 0)	return InvigGreen; //PRIORITY_BNET_REP;
		if((flags & 0x02) != 0)	return InvigWhite; //PRIORITY_OPERATOR;
		if((flags & 0x04) != 0)	return InvigLtYellow; //PRIORITY_SPEAKER;
		if((flags & 0x40) != 0)	return InvigPurple; //PRIORITY_BIZZARD_GUEST;
		return InvigBeige2; //PRIORITY_NORMAL;
	}

	@Override
	public Color getSelfUserNameColor(int flags) {
		return InvigMedBlue; //PRIORITY_NORMAL;
	}

	@Override
	public Color getUserNameListColor(int flags, boolean myUser) {
		if(myUser)
			return InvigLtBlue;
		if((flags & 0x20) != 0)	return InvigRed;
		if((flags & 0x01) != 0)	return InvigCyan; //PRIORITY_BLIZZARD_REP;
		if((flags & 0x08) != 0)	return InvigGreen; //PRIORITY_BNET_REP;
		if((flags & 0x02) != 0)	return InvigWhite; //PRIORITY_OPERATOR;
		if((flags & 0x04) != 0)	return InvigLtYellow; //PRIORITY_SPEAKER;
		if((flags & 0x40) != 0)	return InvigPurple; //PRIORITY_BIZZARD_GUEST;
		return InvigBeige2; //PRIORITY_NORMAL;
	}

	@Override
	public Color getChatColor(int flags) {
		if((flags & 0x20) != 0)	return InvigGray2;
		if((flags & 0x01) != 0)	return InvigCyan; //PRIORITY_BLIZZARD_REP;
		if((flags & 0x08) != 0)	return InvigGreen; //PRIORITY_BNET_REP;
		if((flags & 0x02) != 0)	return InvigWhite; //PRIORITY_OPERATOR;
		if((flags & 0x04) != 0)	return InvigLtYellow; //PRIORITY_SPEAKER;
		if((flags & 0x40) != 0)	return InvigPurple; //PRIORITY_BIZZARD_GUEST;
		//if((flags & 0x800000) != 0)	return InvigMedBlue;
		return InvigWhite; //PRIORITY_NORMAL;
	}

	@Override
	public Color getEmoteColor(int flags) {
		if((flags & 0x01) != 0)	return InvigCyan; //PRIORITY_BLIZZARD_REP;
		if((flags & 0x08) != 0)	return InvigGreen; //PRIORITY_BNET_REP;
		if((flags & 0x02) != 0)	return InvigWhite; //PRIORITY_OPERATOR;
		if((flags & 0x04) != 0)	return InvigLtYellow; //PRIORITY_SPEAKER;
		if((flags & 0x40) != 0)	return InvigPurple; //PRIORITY_BIZZARD_GUEST;
		//if((flags & 0x800000) != 0)	return InvigMedBlue;
		return InvigBeige2; //PRIORITY_NORMAL;
	}

	@Override
	public Color getWhisperColor(int flags) {
		return InvigGray2; //PRIORITY_NORMAL;
	}
}
