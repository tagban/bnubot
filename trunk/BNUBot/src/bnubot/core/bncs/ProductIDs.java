package bnubot.core.bncs;

import util.Constants;

public abstract class ProductIDs {
	public static final int	PRODUCT_CHAT	= 0x43484154;
	public static final int	PRODUCT_STAR	= 0x53544152;
	public static final int	PRODUCT_SEXP	= 0x53455850;
	public static final int	PRODUCT_W2BN	= 0x5732424E;
	public static final int	PRODUCT_D2DV	= 0x44324456;
	public static final int	PRODUCT_D2XP	= 0x44325850;
	public static final int	PRODUCT_JSTR	= 0x4A535452;
	public static final int	PRODUCT_WAR3	= 0x57415233;
	public static final int	PRODUCT_W3XP	= 0x57335850;
	public static final int	PRODUCT_DRTL	= 0x4452544C;
	public static final int	PRODUCT_DSHR	= 0x44534852;
	public static final int	PRODUCT_SSHR	= 0x53534852;

	public static final int ICON_W3H1		= 0x57334831;
	public static final int ICON_W3H2		= 0x57334832;
	public static final int ICON_W3H3		= 0x57334833;
	public static final int ICON_W3H4		= 0x57334834;
	public static final int ICON_W3H5		= 0x57334835;
	public static final int ICON_W3H6		= 0x57334836;
	
	public static final int ICON_W3O1		= 0x57334F31;
	public static final int ICON_W3O2		= 0x57334F32;
	public static final int ICON_W3O3		= 0x57334F33;
	public static final int ICON_W3O4		= 0x57334F34;
	public static final int ICON_W3O5		= 0x57334F35;
	public static final int ICON_W3O6		= 0x57334F36;
	
	public static final int ICON_W3N1		= 0x57334E31;
	public static final int ICON_W3N2		= 0x57334E32;
	public static final int ICON_W3N3		= 0x57334E33;
	public static final int ICON_W3N4		= 0x57334E34;
	public static final int ICON_W3N5		= 0x57334E35;
	public static final int ICON_W3N6		= 0x57334E36;
	
	public static final int ICON_W3U1		= 0x57335531;
	public static final int ICON_W3U2		= 0x57335532;
	public static final int ICON_W3U3		= 0x57335533;
	public static final int ICON_W3U4		= 0x57335534;
	public static final int ICON_W3U5		= 0x57335535;
	public static final int ICON_W3U6		= 0x57335536;
	
	public static final int ICON_W3R1		= 0x57335231;
	public static final int ICON_W3R2		= 0x57335232;
	public static final int ICON_W3R3		= 0x57335233;
	public static final int ICON_W3R4		= 0x57335234;
	public static final int ICON_W3R5		= 0x57335235;
	public static final int ICON_W3R6		= 0x57335236;
	
	public static final int ICON_W3D1		= 0x57334431;
	public static final int ICON_W3D2		= 0x57334432;
	public static final int ICON_W3D3		= 0x57334433;
	public static final int ICON_W3D4		= 0x57334434;
	public static final int ICON_W3D5		= 0x57334435;
	public static final int ICON_W3D6		= 0x57334436;
	
	public static final int ProductID[] = {
		PRODUCT_STAR,
		PRODUCT_SEXP,
		PRODUCT_W2BN,
		PRODUCT_D2DV,
		PRODUCT_D2XP,
		PRODUCT_JSTR,
		PRODUCT_WAR3,
		PRODUCT_W3XP,
		PRODUCT_DRTL,
		PRODUCT_DSHR,
		PRODUCT_SSHR
	};
	
	public String getProdStr(int prod) {
		switch(prod) {
		case PRODUCT_CHAT:	return "CHAT";
		case PRODUCT_STAR:	return Constants.prods[Constants.PRODUCT_STARCRAFT - 1];
		case PRODUCT_SEXP:	return Constants.prods[Constants.PRODUCT_BROODWAR - 1];
		case PRODUCT_W2BN:	return Constants.prods[Constants.PRODUCT_WAR2BNE - 1];
		case PRODUCT_D2DV:	return Constants.prods[Constants.PRODUCT_DIABLO2 - 1];
		case PRODUCT_D2XP:	return Constants.prods[Constants.PRODUCT_LORDOFDESTRUCTION - 1];
		case PRODUCT_JSTR:	return Constants.prods[Constants.PRODUCT_JAPANSTARCRAFT - 1];
		case PRODUCT_WAR3:	return Constants.prods[Constants.PRODUCT_WARCRAFT3 - 1];
		case PRODUCT_W3XP:	return Constants.prods[Constants.PRODUCT_THEFROZENTHRONE - 1];
		case PRODUCT_DRTL:	return Constants.prods[Constants.PRODUCT_DIABLO - 1];
		case PRODUCT_DSHR:	return Constants.prods[Constants.PRODUCT_DIABLOSHAREWARE - 1];
		case PRODUCT_SSHR:	return Constants.prods[Constants.PRODUCT_STARCRAFTSHAREWARE - 1];
		default:
			return "????";
		}
	}
}
