/**
 * $Id$
 */
package org.jbls.util;

/**
 * @author Hdx
 * @author scotta
 */
public final class Constants {
	public static final byte PRODUCT_STARCRAFT = 0x01;
	public static final byte PRODUCT_BROODWAR = 0x02;
	public static final byte PRODUCT_WAR2BNE = 0x03;
	public static final byte PRODUCT_DIABLO2 = 0x04;
	public static final byte PRODUCT_LORDOFDESTRUCTION = 0x05;
	public static final byte PRODUCT_JAPANSTARCRAFT = 0x06;
	public static final byte PRODUCT_WARCRAFT3 = 0x07;
	public static final byte PRODUCT_THEFROZENTHRONE = 0x08;
	public static final byte PRODUCT_DIABLO = 0x09;
	public static final byte PRODUCT_DIABLOSHAREWARE = 0x0A;
	public static final byte PRODUCT_STARCRAFTSHAREWARE = 0x0B;

	public static String[] prods = {
		"STAR",
		"SEXP",
		"W2BN",
		"D2DV",
		"D2XP",
		"JSTR",
		"WAR3",
		"W3XP",
		"DRTL",
		"DSHR",
		"SSHR" };

	public static String[] prodsDisplay = {
		"Starcraft",
		"Starcraft: Brood War",
		"Warcraft II",
		"Diablo II",
		"Diablo II: Lord of Destruction",
		"Starcraft Japanese",
		"Warcraft III",
		"Warcraft III: The Frozen Throne",
		"Diablo",
		"Diablo Shareware",
		"Starcraft Shareware" };

	public static int[] IX86verbytes = {
		0xd3,
		0xd3,
		0x4f,
		0x0c,
		0x0c,
		0xa9,
		0x18,
		0x18,
		0x2a,
		0x2a,
		0xa5 };
}
