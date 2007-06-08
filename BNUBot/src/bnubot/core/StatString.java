package bnubot.core;

import bnubot.core.bncs.ProductIDs;
import bnubot.util.HexDump;

public class StatString {
	private boolean parsed = false;
	private String[] statString;
	private String pretty = null;
	private int product = 0;
	private int icon = 0;
	private int wins = 0;
	private int level = 0;
	private int ladderRating = 0;
	private int ladderRank = 0;
	private int spawn = 0;
	
	public StatString(String statString) {
		this.statString = statString.split(" ");
		parse();
	}
	
	public int getProduct() {
		return product;
	}
	
	public int getIcon() {
		return icon;
	}
	
	public String getIconName(int product, int icon) {
		// I am not sure that the strings are matched to the icons properly
		switch(product) {
		// http://www.battle.net/war3/ladder/war3-ladder-info-laddericons.aspx
		case ProductIDs.PRODUCT_WAR3:
			switch(icon) {
			case ProductIDs.ICON_W3H1:
			case ProductIDs.ICON_W3O1:
			case ProductIDs.ICON_W3N1:
			case ProductIDs.ICON_W3U1:
			case ProductIDs.ICON_W3R1:	return "Orc Peon";
			
			case ProductIDs.ICON_W3H2:	return "Human Footman";
			case ProductIDs.ICON_W3H3:	return "Human Knight";
			case ProductIDs.ICON_W3H4:	return "Human Archmage";
			case ProductIDs.ICON_W3H5:	return "Human Midvh";
			case ProductIDs.ICON_W3O2:	return "Orc Grunt";
			case ProductIDs.ICON_W3O3:	return "Orc Tauren";
			case ProductIDs.ICON_W3O4:	return "Orc Far Seer";
			case ProductIDs.ICON_W3O5:	return "Orc Thrall";
			case ProductIDs.ICON_W3N2:	return "Night Elf Archer";
			case ProductIDs.ICON_W3N3:	return "Night Elf Druid of the Claw";
			case ProductIDs.ICON_W3N4:	return "Night Elf Priestess of the Moon";
			case ProductIDs.ICON_W3N5:	return "Night Elf Furion Stormrage";
			case ProductIDs.ICON_W3U2:	return "Undead Ghoul";
			case ProductIDs.ICON_W3U3:	return "Undead Abomination";
			case ProductIDs.ICON_W3U4:	return "Undead Lich";
			case ProductIDs.ICON_W3U5:	return "Undead Tichondrius";
			case ProductIDs.ICON_W3R2:	return "Random Green Dragon Whelp";
			case ProductIDs.ICON_W3R3:	return "Random Blue Dragon";
			case ProductIDs.ICON_W3R4:	return "Random Red Dragon";
			case ProductIDs.ICON_W3R5:	return "Random Deathwing";
			}
			break;
		// http://www.battle.net/war3/ladder/w3xp-ladder-info-laddericons.aspx
		case ProductIDs.PRODUCT_W3XP:
			switch(icon) {
			case ProductIDs.ICON_W3H1:
			case ProductIDs.ICON_W3O1:
			case ProductIDs.ICON_W3N1:
			case ProductIDs.ICON_W3U1:
			case ProductIDs.ICON_W3R1:	return "Orc Peon";
			
			case ProductIDs.ICON_W3H2:	return "Human Rifleman";
			case ProductIDs.ICON_W3H3:	return "Human Sorceress";
			case ProductIDs.ICON_W3H4:	return "Human Spellbreaker";
			case ProductIDs.ICON_W3H5:	return "Human Midvh";
		/*	case ProductIDs.ICON_W3O2:	return "Orc Grunt";
			case ProductIDs.ICON_W3O3:	return "Orc Tauren";
			case ProductIDs.ICON_W3O4:	return "Orc Far Seer";
			case ProductIDs.ICON_W3O5:	return "Orc Thrall";
			case ProductIDs.ICON_W3N2:	return "Night Elf Archer";
			case ProductIDs.ICON_W3N3:	return "Night Elf Druid of the Claw";
			case ProductIDs.ICON_W3N4:	return "Night Elf Priestess of the Moon";
			case ProductIDs.ICON_W3N5:	return "Night Elf Furion Stormrage";
			case ProductIDs.ICON_W3U2:	return "Undead Ghoul";
			case ProductIDs.ICON_W3U3:	return "Undead Abomination";
			case ProductIDs.ICON_W3U4:	return "Undead Lich";
			case ProductIDs.ICON_W3U5:	return "Undead Tichondrius";
			case ProductIDs.ICON_W3R2:	return "Random Green Dragon Whelp";
			case ProductIDs.ICON_W3R3:	return "Random Blue Dragon";
			case ProductIDs.ICON_W3R4:	return "Random Red Dragon";
			case ProductIDs.ICON_W3R5:	return "Random Deathwing";
		*/	}
			break;
		}
		
		return "Unknown " + HexDump.DWordToPretty(icon);
	}
	
	public void parse() {
		if(parsed)
			return;
		parsed = true;
		
		pretty = "using ";
		
		try {
			product = HexDump.StringToDWord(statString[0]);
			icon = product;
		} catch(Exception e) {
			pretty = e.getMessage();
			return;
		}
		
		switch(product) {
		case ProductIDs.PRODUCT_WAR3:
			pretty += "Warcraft III";
			break;
		case ProductIDs.PRODUCT_W3XP:
			pretty += "Warcraft III: The Frozen Throne";
			break;
		case ProductIDs.PRODUCT_STAR:
			pretty += "Starcraft";
			break;
		case ProductIDs.PRODUCT_SEXP:
			pretty += "Starcraft: Brood War";
			break;
		case ProductIDs.PRODUCT_JSTR:
			pretty += "Starcraft Japanese";
			break;
		case ProductIDs.PRODUCT_W2BN:
			pretty += "Warcraft II";
			break;
		default:
			pretty = "[" + HexDump.DWordToPretty(product) + "]";
			for(int i = 0; i < statString.length; i++)
				pretty += statString[i] + " ";
			break;
		}
		
		switch(product) {
		case ProductIDs.PRODUCT_STAR:
		case ProductIDs.PRODUCT_SEXP:
		case ProductIDs.PRODUCT_JSTR:
		case ProductIDs.PRODUCT_W2BN:
			/*
			Starcraft, Starcraft Japanese, Brood War, and Warcraft II
			These products use the same format for their statstrings as Diablo. Most of these fields are usually 0 and their meanings are not known.
			1 	Ladder Rating 	The user's current ladder rating.
			2 	Ladder rank 	The player's rank on the ladder.
			3 	Wins 	The number of wins the user has in normal games.
			4 	Spawned 	This field is 1 if the user's client is spawned, 0 otherwise.
			5 	Unknown 	
			6 	High Ladder Rating 	This is the user's 'highest ever' ladder rating.
			7 	Unknown 	
			8 	Unknown 	
			9 	Icon 	This value should be matched against the product values of each icon in each Battle.net Icon file that is loaded. If a match is found, the client should use this icon when displaying the user.
			*/
			if(statString.length != 10) {
				pretty += "\ninvalid length " + statString.length;
				return;
			}

			ladderRating = Integer.parseInt(statString[1]);
			ladderRank = Integer.parseInt(statString[2]);
			wins = Integer.parseInt(statString[3]);
			spawn = Integer.parseInt(statString[4]);
			int unknown5 = Integer.parseInt(statString[5]);
			int highLadderRating = Integer.parseInt(statString[6]);
			int unknown7 = Integer.parseInt(statString[7]);
			int unknown8 = Integer.parseInt(statString[8]);
			icon = HexDump.StringToDWord(statString[9]);

			if(ladderRating != 0)
				pretty += ", Ladder rating " + ladderRating;
			if(ladderRank != 0)
				pretty += ", Ladder rank " + ladderRank;
			if(wins != 0)
				pretty += ", " + wins + " wins";
			if(spawn != 0)
				pretty += ", Spawn";
			if(unknown5 != 0)
				pretty += ", unknown5=" + unknown5;
			if((highLadderRating != 0) && (highLadderRating != ladderRating))
				pretty += ", Ladder highest ever rating " + highLadderRating;
			if(unknown7 != 0)
				pretty += ", unknown7=" + unknown7;
			if(unknown8 != 0)
				pretty += ", unknown8=" + unknown8;
			if((icon != 0) && (icon != product))
				pretty += ", " + HexDump.DWordToPretty(icon) + " icon";
			
			break;
			
		case ProductIDs.PRODUCT_WAR3:
		case ProductIDs.PRODUCT_W3XP:
			if(statString.length >= 3) {
				//3RAW 1R3W 1 UNB
				icon = HexDump.StringToDWord(statString[1]);
				level = Integer.parseInt(statString[2]);

				if(icon != 0)
					pretty += ", " + getIconName(product, icon) + " icon";
				if(level != 0)
					pretty += ", Level " + level;
				
				if(statString.length >= 4) {
					byte[] bytes = statString[3].getBytes();
					String clan = "";
					for(int j = bytes.length-1; j >= 0; j--)
						clan += (char)bytes[j];
						
					pretty += ", in Clan " + clan;
				}
				
			} else {
				pretty += "\n";
				for(int i = 0; i < statString.length; i++)
					pretty += statString[i] + " ";
			}
			break;
		}
	}

	public String toString() {
		return pretty;
	}
	
	public int getWins() {
		return wins;
	}
	
	public int getLevel() {
		return level;
	}

	public int getLadderRank() {
		return ladderRank;
	}

	public int getLadderRating() {
		return ladderRating;
	}

	public int getSpawn() {
		return spawn;
	}
}
