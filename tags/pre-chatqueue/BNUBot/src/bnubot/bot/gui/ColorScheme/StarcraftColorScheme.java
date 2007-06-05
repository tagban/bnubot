package bnubot.bot.gui.ColorScheme;

import java.awt.Color;

public class StarcraftColorScheme extends ColorScheme {
	public static final Color SCWhite = Color.WHITE;
	public static final Color SCRed = Color.RED;
	public static final Color SCGreen = Color.GREEN;
	public static final Color SCBlue = Color.BLUE;
	public static final Color SCLtGray = Color.LIGHT_GRAY; 
	public static final Color SCGray = Color.GRAY; 
	public static final Color SCBlack = Color.BLACK;
	public static final Color SCCyan = Color.CYAN; 
	public static final Color SCYellow = Color.YELLOW; 

	public Color getBackgroundColor() {
		return SCBlack;
	}

	public Color getTimeStampColor() {
		return SCLtGray;
	}
	
	public Color getChannelColor() {
		return SCGreen;
	}
	
	public Color getInfoColor() {
		return SCBlue;
	}
	
	public Color getErrorColor() {
		return SCRed;
	}
	
	public Color getUserNameColor(int flags) {
		if((flags & 0x20) != 0)	return SCRed;
		if((flags & 0x01) != 0)	return SCCyan; //PRIORITY_BLIZZARD_REP;
		if((flags & 0x08) != 0)	return SCCyan; //PRIORITY_BNET_REP;
		if((flags & 0x02) != 0)	return SCWhite; //PRIORITY_OPERATOR;
		if((flags & 0x04) != 0)	return SCYellow; //PRIORITY_SPEAKER;
		if((flags & 0x40) != 0)	return SCYellow; //PRIORITY_BIZZARD_GUEST;
		return SCYellow; //PRIORITY_NORMAL;
	}
	
	public Color getUserNameListColor(int flags) {
		if((flags & 0x20) != 0)	return SCRed;
		if((flags & 0x01) != 0)	return SCCyan; //PRIORITY_BLIZZARD_REP;
		if((flags & 0x08) != 0)	return SCCyan; //PRIORITY_BNET_REP;
		if((flags & 0x02) != 0)	return SCWhite; //PRIORITY_OPERATOR;
		//if((flags & 0x04) != 0)	return SCLtYellow; //PRIORITY_SPEAKER;
		//if((flags & 0x40) != 0)	return SCLtYellow; //PRIORITY_BIZZARD_GUEST;
		return SCLtGray; //PRIORITY_NORMAL;
	}
	
	public Color getChatColor(int flags) {
		if((flags & 0x20) != 0)	return SCGray;
		if((flags & 0x01) != 0)	return SCCyan; //PRIORITY_BLIZZARD_REP;
		if((flags & 0x08) != 0)	return SCCyan; //PRIORITY_BNET_REP;
		if((flags & 0x02) != 0)	return SCWhite; //PRIORITY_OPERATOR;
		if((flags & 0x04) != 0)	return SCYellow; //PRIORITY_SPEAKER;
		if((flags & 0x40) != 0)	return SCYellow; //PRIORITY_BIZZARD_GUEST;
		//if((flags & 0x800000) != 0)	return SCMedBlue;
		return SCWhite; //PRIORITY_NORMAL;
	}
	
	public Color getEmoteColor(int flags) {
		if((flags & 0x01) != 0)	return SCCyan; //PRIORITY_BLIZZARD_REP;
		if((flags & 0x08) != 0)	return SCCyan; //PRIORITY_BNET_REP;
		if((flags & 0x02) != 0)	return SCWhite; //PRIORITY_OPERATOR;
		if((flags & 0x04) != 0)	return SCYellow; //PRIORITY_SPEAKER;
		if((flags & 0x40) != 0)	return SCYellow; //PRIORITY_BIZZARD_GUEST;
		//if((flags & 0x800000) != 0)	return SCMedBlue;
		return SCYellow; //PRIORITY_NORMAL;
	}
	
	public Color getWhisperColor(int flags) {
		if((flags & 0x01) != 0)	return SCCyan; //PRIORITY_BLIZZARD_REP;
		if((flags & 0x08) != 0)	return SCCyan; //PRIORITY_BNET_REP;
		if((flags & 0x04) != 0)	return SCYellow; //PRIORITY_SPEAKER;
		if((flags & 0x40) != 0)	return SCYellow; //PRIORITY_BIZZARD_GUEST;
		return SCGray; //PRIORITY_NORMAL;
	}

}
