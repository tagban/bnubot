/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui.colors;

import java.awt.Color;

/**
 * @author scotta
 */
public class Diablo2ColorScheme extends ColorScheme {
	public static final Color D2White = new Color(0xD0D0D0);
	public static final Color D2Red = new Color(0xCE3E3E);
	public static final Color D2Green = new Color(0x00CE00);
	public static final Color D2Blue = new Color(0x44409C);
	//Const D2Beige1 = &H6091A1
	public static final Color D2Gray = new Color(0x555555);
	public static final Color D2Black = new Color(0x080808);
	public static final Color D2Beige2 = new Color(0xA89D65);
	//public static final Color D2Orange = new Color(0xCE8800);
	public static final Color D2LtYellow = new Color(0xCECE51);
	public static final Color D2Purple = new Color(0x8D00CE);
	public static final Color D2Cyan = new Color(0x00D0D0);
	//Const D2MedBlue = &HE8AC2C

	@Override
	public Color getBackgroundColor() {
		return D2Black;
	}

	@Override
	public Color getForegroundColor() {
		return D2White;
	}

	@Override
	public Color getChannelColor() {
		return D2Green;
	}

	@Override
	public Color getInfoColor() {
		return D2Blue;
	}

	@Override
	public Color getErrorColor() {
		return D2Red;
	}

	@Override
	public Color getDebugColor() {
		return D2LtYellow;
	}

	@Override
	public Color getUserNameColor(int flags) {
		if((flags & 0x20) != 0)	return D2Red;
		if((flags & 0x01) != 0)	return D2Cyan; //PRIORITY_BLIZZARD_REP;
		if((flags & 0x08) != 0)	return D2Green; //PRIORITY_BNET_REP;
		if((flags & 0x02) != 0)	return D2White; //PRIORITY_OPERATOR;
		if((flags & 0x04) != 0)	return D2LtYellow; //PRIORITY_SPEAKER;
		if((flags & 0x40) != 0)	return D2Purple; //PRIORITY_BIZZARD_GUEST;
		return D2Beige2; //PRIORITY_NORMAL;
	}

	@Override
	public Color getSelfUserNameColor(int flags) {
		return D2Cyan; //PRIORITY_NORMAL;
	}

	@Override
	public Color getUserNameListColor(int flags, boolean myUser) {
		if(myUser)
			return D2LtYellow;
		if((flags & 0x20) != 0)	return D2Red;
		if((flags & 0x01) != 0)	return D2Cyan; //PRIORITY_BLIZZARD_REP;
		if((flags & 0x08) != 0)	return D2Green; //PRIORITY_BNET_REP;
		if((flags & 0x02) != 0)	return D2White; //PRIORITY_OPERATOR;
		if((flags & 0x04) != 0)	return D2LtYellow; //PRIORITY_SPEAKER;
		if((flags & 0x40) != 0)	return D2Purple; //PRIORITY_BIZZARD_GUEST;
		return D2White; //PRIORITY_NORMAL;
	}

	@Override
	public Color getChatColor(int flags) {
		if((flags & 0x20) != 0)	return D2Gray;
		if((flags & 0x01) != 0)	return D2Cyan; //PRIORITY_BLIZZARD_REP;
		if((flags & 0x08) != 0)	return D2Green; //PRIORITY_BNET_REP;
		if((flags & 0x02) != 0)	return D2White; //PRIORITY_OPERATOR;
		if((flags & 0x04) != 0)	return D2LtYellow; //PRIORITY_SPEAKER;
		if((flags & 0x40) != 0)	return D2Purple; //PRIORITY_BIZZARD_GUEST;
		//if((flags & 0x800000) != 0)	return D2MedBlue;
		return D2White; //PRIORITY_NORMAL;
	}

	@Override
	public Color getEmoteColor(int flags) {
		if((flags & 0x01) != 0)	return D2Cyan; //PRIORITY_BLIZZARD_REP;
		if((flags & 0x08) != 0)	return D2Green; //PRIORITY_BNET_REP;
		if((flags & 0x02) != 0)	return D2White; //PRIORITY_OPERATOR;
		if((flags & 0x04) != 0)	return D2LtYellow; //PRIORITY_SPEAKER;
		if((flags & 0x40) != 0)	return D2Purple; //PRIORITY_BIZZARD_GUEST;
		//if((flags & 0x800000) != 0)	return D2MedBlue;
		return D2Gray; //PRIORITY_NORMAL;
	}

	@Override
	public Color getWhisperColor(int flags) {
		return D2Gray; //PRIORITY_NORMAL;
	}
}
