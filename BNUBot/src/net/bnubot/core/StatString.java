/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.core;

import net.bnubot.core.bncs.ProductIDs;
import net.bnubot.util.HexDump;

public class StatString {
	private static final String[] D2Classes = {"Amazon", "Sorceress", "Necromancer", "Paladin", "Barbarian", "Druid", "Assassin" };
	
	private boolean parsed = false;
	private String[] statString;
	private String[] statString2;
	private String pretty = null;
	private int product = 0;
	private int icon = 0;
	private Integer wins = null;
	private Integer level = null;
	private Integer charLevel = null;
	private int ladderRating = 0;
	private int highLadderRating = 0;
	private int ladderRank = 0;
	private boolean spawn = false;
	
	public StatString(String statString) {
		this.statString = statString.split(" ", 2);
		if(this.statString.length == 2)
			this.statString2 = this.statString[1].split(" ");
		
		try {
			parse();
		} catch(Exception e) {
			System.err.println("Error parsing statstring: " + statString);
			System.err.println(HexDump.hexDump(statString.getBytes()));
			e.printStackTrace();
		}
	}
	
	public int getProduct() {
		return product;
	}
	
	public int getIcon() {
		return icon;
	}
	
	public String getIconName(int product, int icon) {
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
			case ProductIDs.ICON_W3R2:	return "Green Dragon Whelp";
			case ProductIDs.ICON_W3R3:	return "Blue Dragon";
			case ProductIDs.ICON_W3R4:	return "Red Dragon";
			case ProductIDs.ICON_W3R5:	return "Deathwing";
			}
			break;
		// http://www.battle.net/war3/ladder/w3xp-ladder-info-laddericons.aspx
		case ProductIDs.PRODUCT_W3XP:
			switch(icon) {
			case ProductIDs.ICON_W3H1:
			case ProductIDs.ICON_W3O1:
			case ProductIDs.ICON_W3N1:
			case ProductIDs.ICON_W3U1:
			case ProductIDs.ICON_W3R1:
			case ProductIDs.ICON_W3D1:	return "Orc Peon";
			
			case ProductIDs.ICON_W3H2:	return "Human Rifleman";
			case ProductIDs.ICON_W3H3:	return "Human Sorceress";
			case ProductIDs.ICON_W3H4:	return "Human Spellbreaker";
			case ProductIDs.ICON_W3H5:	return "Human Blood Mage";
			case ProductIDs.ICON_W3H6:	return "Human Jaina";
			case ProductIDs.ICON_W3O2:	return "Orc Troll Headhunter";
			case ProductIDs.ICON_W3O3:	return "Orc Shaman";
			case ProductIDs.ICON_W3O4:	return "Orc Spirit Walker";
			case ProductIDs.ICON_W3O5:	return "Orc Shadow Hunter";
			case ProductIDs.ICON_W3O6:	return "Orc Rexxar";
			case ProductIDs.ICON_W3N2:	return "Night Elf Huntress";
			case ProductIDs.ICON_W3N3:	return "Night Elf Druid of the Talon";
			case ProductIDs.ICON_W3N4:	return "Night Elf Dryad";
			case ProductIDs.ICON_W3N5:	return "Night Elf Keeper of the Grove";
			case ProductIDs.ICON_W3N6:	return "Night Elf Maiev";
			case ProductIDs.ICON_W3U2:	return "Undead Crypt Fiend";
			case ProductIDs.ICON_W3U3:	return "Undead Banshee";
			case ProductIDs.ICON_W3U4:	return "Undead Destroyer";
			case ProductIDs.ICON_W3U5:	return "Undead Crypt Lord";
			case ProductIDs.ICON_W3U6:	return "Undead Sylvanas";
			case ProductIDs.ICON_W3R2:	return "Myrmidon";
			case ProductIDs.ICON_W3R3:	return "Siren";
			case ProductIDs.ICON_W3R4:	return "Dragon Turtle";
			case ProductIDs.ICON_W3R5:	return "Sea Witch";
			case ProductIDs.ICON_W3R6:	return "Illidan";
			case ProductIDs.ICON_W3D2:	return "Felguard";
			case ProductIDs.ICON_W3D3:	return "Infernal";
			case ProductIDs.ICON_W3D4:	return "Doomguard";
			case ProductIDs.ICON_W3D5:	return "Pit Lord";
			case ProductIDs.ICON_W3D6:	return "Archimonde";
			}
			break;
		}
		
		return "Unknown " + HexDump.DWordToPretty(icon);
	}
	
	public void parse() {
		if(parsed)
			return;
		parsed = true;
		
		if(statString.length == 0)
			return;
		
		pretty = " using ";
		
		try {
			product = HexDump.StringToDWord(statString[0]);
			icon = product;
		} catch(Exception e) {
			pretty = e.getMessage();
			return;
		}
		
		switch(product) {
		case ProductIDs.PRODUCT_DSHR:
			pretty += "Diablo Shareware";
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
		case ProductIDs.PRODUCT_SSHR:
			pretty += "Starcraft Shareware";
			break;
		case ProductIDs.PRODUCT_W2BN:
			pretty += "Warcraft II";
			break;
		case ProductIDs.PRODUCT_D2DV:
			pretty += "Diablo II";
			break;
		case ProductIDs.PRODUCT_D2XP:
			pretty += "Diablo II: Lord of Destruction";
			break;
		case ProductIDs.PRODUCT_WAR3:
			pretty += "Warcraft III";
			break;
		case ProductIDs.PRODUCT_W3XP:
			pretty += "Warcraft III: The Frozen Throne";
			break;
		default:
			pretty += "[" + HexDump.DWordToPretty(product) + "]";
			break;
		}

		if((statString.length > 1) || (statString[0].length() > 4)) {
			switch(product) {
			case ProductIDs.PRODUCT_STAR:
			case ProductIDs.PRODUCT_SEXP:
			case ProductIDs.PRODUCT_JSTR:
			case ProductIDs.PRODUCT_W2BN:
			case ProductIDs.PRODUCT_SSHR:
			case ProductIDs.PRODUCT_DSHR:
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
				if(statString2.length != 9) {
					pretty += "\ninvalid length " + statString2.length;
					return;
				}
	
				try {
					ladderRating = Integer.parseInt(statString2[0]);
					ladderRank = Integer.parseInt(statString2[1]);
					wins = Integer.parseInt(statString2[2]);
					spawn = !statString2[3].equals("0");
					long unknown5 = Integer.parseInt(statString2[4]);
					highLadderRating = Integer.parseInt(statString2[5]);
					long unknown7 = Integer.parseInt(statString2[6]);
					long unknown8 = Integer.parseInt(statString2[7]);
					icon = HexDump.StringToDWord(statString2[8]);
		
					if(ladderRating != 0)
						pretty += ", Ladder rating " + ladderRating;
					if(ladderRank != 0)
						pretty += ", Ladder rank " + ladderRank;
					if(wins != 0)
						pretty += ", " + wins + " wins";
					if(spawn)
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
				} catch(Exception e) {
					pretty += statString[1];
				}
				break;
	
			case ProductIDs.PRODUCT_D2DV:
			case ProductIDs.PRODUCT_D2XP:
				statString2 = statString[0].substring(4).split(",", 3);
				//PX2DUSEast,EsO-SILenTNiGhT,„€ÿ'ÿÿÿÿÿÿÿÿÿÿÿÿÿÿSèšÿÿÿÿ
				//PX2DUSEast,getoutof_myway ,„€ÿ++ÿÿÿÿÿÿÿÿÿÿÿÿÿTèžÿÿÿÿ
				if(statString2.length != 3) {
					pretty += "unknown statstr2 len: " + statString2.length;
					break;
				}
				pretty += ", Realm " + statString2[0];
				//TODO: Prefix
				pretty += ", ";
				
			    //                                       CC                                  CL FL AC
			    //84 80 53 02 02 02 02 0F FF 50 02 02 FF 02 FF FF FF FF FF 4C FF FF FF FF FF 14 E8 84 FF FF 01 FF FF - „€S.....ÿP..ÿ.ÿÿÿÿÿLÿÿÿÿÿ.è„ÿÿ.ÿÿ
				//84 80 FF FF FF FF FF FF FF FF FF FF FF 03 FF FF FF FF FF FF FF FF FF FF FF 01 C5 80 80 80 01 FF FF - „€ÿÿÿÿÿÿÿÿÿÿÿ.ÿÿÿÿÿÿÿÿÿÿÿ.Å€€€.ÿÿ
			    //84 80 3B 02 02 02 02 14 FF FF 03 03 60 03 FF FF FF FF FF FF FF FF FF FF 32 13 E4 84 FF FF 01 FF FF - „€;.....ÿÿ..`.ÿÿÿÿÿÿÿÿÿÿ2.ä„ÿÿ.ÿÿ
			    //00             05             10             15             20             25             30
				byte[] data = statString2[2].getBytes();
				
				byte version = (byte)(data[0] & 0x7F);
				if(version != 4) {
					pretty += " error: version != 4";
					break;
				}
				
				byte charClass = (byte)(data[13]-1);
				if((charClass < 0) || (charClass > 6))
					charClass = 7;
				boolean female = ((charClass == 0) || (charClass == 1) || (charClass == 6));
			    charLevel = (int)data[25];
			    
			    byte charFlags = data[26];
			    boolean hardcore = (charFlags & 0x04) != 0;
			    boolean dead = (charFlags & 0x08) != 0;
			    boolean expansion = (charFlags & 0x20) != 0;
			    boolean ladder = (charFlags & 0x40) != 0;
			    
			    byte actsCompleted = (byte)((data[27] & 0x3E) >> 2);
			    
			    byte difficulty;
			    if(expansion) {
			    	difficulty = (byte)(actsCompleted / 5);
			    	actsCompleted = (byte)((actsCompleted % 5) + 1);
			    } else {
			    	difficulty = (byte)(actsCompleted / 4);
			    	actsCompleted = (byte)((actsCompleted % 45) + 1);
			    }
			    
			    if(expansion) {
			        switch(difficulty) {
			        case 0: break;
		            case 1: pretty += hardcore ? "Destroyer " : "Slayer "; break;
		            case 2: pretty += hardcore ? "Conquer " : "Champion " ; break;
		            case 3: pretty += hardcore ? "Guardian " : (female ? "Matriarch " : "Patriarch "); break;
		            default: pretty += "?d=" + difficulty; break;
			        }
			    } else {
			        switch(difficulty) {
		        	case 0: break;
		            case 1: pretty += (female ? (hardcore ? "Countess " : "Dame ") : (hardcore ? "Count " : "Sir ")); break;
		            case 2: pretty += (female ? (hardcore ? "Duchess " : "Lady ") : (hardcore ? "Duke " : "Lord ")); break;
		            case 3: pretty += (female ? (hardcore ? "Queen " : "Baroness ") : (hardcore ? "King " : "Baron ")); break;
		            default: pretty += "??d=" + difficulty; break;
			        }
			    }
			    
			    pretty += statString2[1];
			    
			    pretty += ", a ";
			    
			    if(dead)
			    	pretty += "dead ";
			    if(hardcore)
			    	pretty += "hardcore ";
			    if(ladder)
			    	pretty += "ladder ";
			    
			    pretty += "level " + charLevel + " " + D2Classes[charClass] + " (" + (expansion ? "Expansion" : "Classic") + ")";
			    
			    if(difficulty < 3) {
			    	pretty += " currently in ";
			        switch(difficulty) {
			            case 0: pretty += "Normal"; break;
			            case 1: pretty += "Nightmare"; break;
			            case 2: pretty += "Hell"; break;
			            default: pretty += "?"; break;
			        }
			        pretty += " act " + actsCompleted;
			    } else {
			        pretty += " who has beaten the game";
			    }
				
				
				break;
				
			case ProductIDs.PRODUCT_WAR3:
			case ProductIDs.PRODUCT_W3XP:
				if(statString2.length >= 2) {
					//3RAW 1R3W 1 UNB
					icon = HexDump.StringToDWord(statString2[0]);
					level = Integer.parseInt(statString2[1]);
	
					if(icon != 0)
						pretty += ", " + getIconName(product, icon) + " icon";
					if(level != 0)
						pretty += ", Level " + level;
					
					if(statString2.length >= 3) {
						byte[] bytes = statString2[2].getBytes();
						String clan = "";
						for(int j = bytes.length-1; j >= 0; j--)
							clan += (char)bytes[j];
							
						pretty += ", in Clan " + clan;
					}
				} else {
					pretty += "\n";
					pretty += statString[1];
				}
				break;
				
			default:
				pretty += ", statstr = " + statString[1];
				break;
			}
		}
	}

	public String toString() {
		return pretty;
	}
	
	public Integer getWins() {
		return wins;
	}
	
	public Integer getLevel() {
		return level;
	}
	
	public Integer getCharLevel() {
		return charLevel;
	}

	public int getLadderRank() {
		return ladderRank;
	}

	public int getLadderRating() {
		return ladderRating;
	}

	public int getHighLadderRating() {
		return highLadderRating;
	}

	public boolean getSpawn() {
		return spawn;
	}
}
