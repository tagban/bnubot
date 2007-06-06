package bnubot.core.bncs;

import util.Constants;

public abstract class ProductIDs {
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
