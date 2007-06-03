package bnubot.bot.gui.ColorScheme;

import java.awt.Color;

public class Diablo2ColorConstants {
	public static final Color D2White = new Color(0xD0D0D0);
	public static final Color D2Red = new Color(0xCE3E3E);
	public static final Color D2Green = new Color(0x00CE00);
	public static final Color D2Blue = new Color(0x44409C); 
	//Const D2Beige1 = &H6091A1
	public static final Color D2Gray = new Color(0x555555); 
	public static final Color D2Black = new Color(0x080808);
	public static final Color D2Beige2 = new Color(0xA89D65);
	//Const D2Orange = &H88CE&
	public static final Color D2LtYellow = new Color(0xCECE51);
	//Const D2Purple = &HCE008D
	public static final Color D2Cyan = new Color(0x00FFFF); 
	//Const D2MedBlue = &HE8AC2C
	
	/*
Function GetUNListColor(Flags, Product)
	GetUNListColor = D2White
	'If Product <> bot.GetProduct Then GetUNListColor = &HA0A0A0
	If (Flags And &H800000)	Then GetUNListColor = D2MedBlue
	If (Flags And &H44)	Then GetUNListColor = D2LtYellow
	If (Flags And 2)	Then GetUNListColor = D2White
	If (Flags And 9)	Then GetUNListColor = D2Cyan
	If (Flags And &H20)	Then GetUNListColor = D2Red
End Function

Function GetWhisperColor(Flags)
	GetWhisperColor = D2Gray
	If (Flags And &H44)	Then GetWhisperColor = D2LtYellow
	If (Flags And 9)	Then GetWhisperColor = D2Cyan
	If (Flags And &H20)	Then GetWhisperColor = D2Gray
End Function

Function GetEmoteColor(Flags)
	GetEmoteColor = D2Gray
	If (Flags And &H800000)	Then GetEmoteColor = D2MedBlue
	If (Flags And &H44)	Then GetEmoteColor = D2LtYellow
	If (Flags And 2)	Then GetEmoteColor = D2White
	If (Flags And 9)	Then GetEmoteColor = D2Cyan
	If (Flags And &H20)	Then GetEmoteColor = D2Red
End Function
	 */
	public static Color getUserNameColor(int flags) {
		if((flags & 0x20) != 0)	return D2Red;
		if((flags & 0x01) != 0)	return D2Cyan; //PRIORITY_BLIZZARD_REP;
		if((flags & 0x08) != 0)	return D2Cyan; //PRIORITY_BNET_REP;
		if((flags & 0x02) != 0)	return D2White; //PRIORITY_OPERATOR;
		if((flags & 0x04) != 0)	return D2LtYellow; //PRIORITY_SPEAKER;
		if((flags & 0x40) != 0)	return D2LtYellow; //PRIORITY_BIZZARD_GUEST;
		return D2Beige2; //PRIORITY_NORMAL;
	}
	
	public static Color getUserNameListColor(int flags) {
		if((flags & 0x20) != 0)	return D2Red;
		if((flags & 0x01) != 0)	return D2Cyan; //PRIORITY_BLIZZARD_REP;
		if((flags & 0x08) != 0)	return D2Cyan; //PRIORITY_BNET_REP;
		if((flags & 0x02) != 0)	return D2White; //PRIORITY_OPERATOR;
		if((flags & 0x04) != 0)	return D2LtYellow; //PRIORITY_SPEAKER;
		if((flags & 0x40) != 0)	return D2LtYellow; //PRIORITY_BIZZARD_GUEST;
		return D2White; //PRIORITY_NORMAL;
	}
	
	public static Color getChatColor(int flags) {
		if((flags & 0x20) != 0)	return D2Gray;
		if((flags & 0x01) != 0)	return D2Cyan; //PRIORITY_BLIZZARD_REP;
		if((flags & 0x08) != 0)	return D2Cyan; //PRIORITY_BNET_REP;
		if((flags & 0x02) != 0)	return D2White; //PRIORITY_OPERATOR;
		if((flags & 0x04) != 0)	return D2LtYellow; //PRIORITY_SPEAKER;
		if((flags & 0x40) != 0)	return D2LtYellow; //PRIORITY_BIZZARD_GUEST;
		//if((flags & 0x800000) != 0)	return D2MedBlue;
		return D2White; //PRIORITY_NORMAL;
	}
}
