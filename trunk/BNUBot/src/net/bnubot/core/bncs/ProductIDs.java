/**
 * 
 */
package net.bnubot.core.bncs;

/**
 * @author sanderson
 *
 */
public enum ProductIDs {
	STAR(0x53544152, "Starcraft"), 
	SEXP(0x53455850, "Starcraft: Brood War"),
	W2BN(0x5732424E, "Warcraft II"),
	D2DV(0x44324456, "Diablo II"),
	D2XP(0x44325850, "Diablo II: Lord of Destruction"),
	JSTR(0x4A535452, "Starcraft Japanese"),
	WAR3(0x57415233, "Warcraft III"),
	W3XP(0x57335850, "Warcraft III: The Frozen Throne"),
	DRTL(0x4452544C, "Diablo"),
	DSHR(0x44534852, "Diablo Shareware"),
	SSHR(0x53534852, "Starcraft Shareware"),
	CHAT(0x43484154, "Chat");
	
	int dword;
	String name;
	
	ProductIDs(int dword, String name) {
		this.dword = dword;
		this.name = name;
	}

	public int getDword() {
		return this.dword;
	}

	public int getBnls() {
		if(this == CHAT)
			throw new IllegalStateException();
		return ordinal();
	}
	
	public String toString() {
		return this.name;
	}

	/**
	 * @param stringToDWord
	 * @return
	 */
	public static ProductIDs fromDWord(int dword) {
		for(ProductIDs p : values())
			if(dword == p.dword)
				return p;
		throw new IllegalStateException();
	}
}
