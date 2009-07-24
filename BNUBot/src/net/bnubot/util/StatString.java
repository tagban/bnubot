/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util;

import java.io.EOFException;
import java.io.IOException;

import net.bnubot.core.bncs.IconIDs;
import net.bnubot.core.bncs.ProductIDs;
import net.bnubot.logging.Out;
import net.bnubot.util.crypto.HexDump;

/**
 * @author scotta
 */
public class StatString {
	private static final String[] D2Classes = {"Amazon", "Sorceress", "Necromancer", "Paladin", "Barbarian", "Druid", "Assassin" };

	private boolean parsed = false;
	private String[] statString = null;
	private String[] statString2 = null;
	private String prettyStart = null;
	private String prettyEnd = "";
	private String pretty = null;
	private ProductIDs product = null;
	private int icon = 0;
	private Integer wins = null;
	private Integer level = null;
	private Integer charLevel = null;
	private int ladderRating = 0;
	private int highLadderRating = 0;
	private int ladderRank = 0;
	private boolean spawn = false;
	private BNetInputStream is = null;

	public StatString(String statString) {
		this.statString = statString.split(" ", 2);
		if(this.statString.length == 2)
			this.statString2 = this.statString[1].split(" ");

		try {
			parse();
		} catch(Exception e) {
			Out.error(StatString.class, "Error parsing statstring: " + e.getMessage() + "\n" + HexDump.hexDump(statString.getBytes()));
			Out.exception(e);
		}
	}

	public StatString(BNetInputStream is) {
		this.is = is;
		try {
			parse();
		} catch(Exception e) {
			Out.exception(e);
		}
	}

	public ProductIDs getProduct() {
		return product;
	}

	public int getIcon() {
		return icon;
	}

	public String getIconName(ProductIDs product, int icon) {
		switch(product) {
		// http://www.battle.net/war3/ladder/war3-ladder-info-laddericons.aspx
		case WAR3:
			switch(icon) {
			case IconIDs.ICON_W3H1:
			case IconIDs.ICON_W3O1:
			case IconIDs.ICON_W3N1:
			case IconIDs.ICON_W3U1:
			case IconIDs.ICON_W3R1:	return "Orc Peon";

			case IconIDs.ICON_W3H2:	return "Human Footman";
			case IconIDs.ICON_W3H3:	return "Human Knight";
			case IconIDs.ICON_W3H4:	return "Human Archmage";
			case IconIDs.ICON_W3H5:	return "Human Midvh";
			case IconIDs.ICON_W3O2:	return "Orc Grunt";
			case IconIDs.ICON_W3O3:	return "Orc Tauren";
			case IconIDs.ICON_W3O4:	return "Orc Far Seer";
			case IconIDs.ICON_W3O5:	return "Orc Thrall";
			case IconIDs.ICON_W3N2:	return "Night Elf Archer";
			case IconIDs.ICON_W3N3:	return "Night Elf Druid of the Claw";
			case IconIDs.ICON_W3N4:	return "Night Elf Priestess of the Moon";
			case IconIDs.ICON_W3N5:	return "Night Elf Furion Stormrage";
			case IconIDs.ICON_W3U2:	return "Undead Ghoul";
			case IconIDs.ICON_W3U3:	return "Undead Abomination";
			case IconIDs.ICON_W3U4:	return "Undead Lich";
			case IconIDs.ICON_W3U5:	return "Undead Tichondrius";
			case IconIDs.ICON_W3R2:	return "Green Dragon Whelp";
			case IconIDs.ICON_W3R3:	return "Blue Dragon";
			case IconIDs.ICON_W3R4:	return "Red Dragon";
			case IconIDs.ICON_W3R5:	return "Deathwing";
			}
			break;
		// http://www.battle.net/war3/ladder/w3xp-ladder-info-laddericons.aspx
		case W3XP:
			switch(icon) {
			case IconIDs.ICON_W3H1:
			case IconIDs.ICON_W3O1:
			case IconIDs.ICON_W3N1:
			case IconIDs.ICON_W3U1:
			case IconIDs.ICON_W3R1:
			case IconIDs.ICON_W3D1:	return "Orc Peon";

			case IconIDs.ICON_W3H2:	return "Human Rifleman";
			case IconIDs.ICON_W3H3:	return "Human Sorceress";
			case IconIDs.ICON_W3H4:	return "Human Spellbreaker";
			case IconIDs.ICON_W3H5:	return "Human Blood Mage";
			case IconIDs.ICON_W3H6:	return "Human Jaina";
			case IconIDs.ICON_W3O2:	return "Orc Troll Headhunter";
			case IconIDs.ICON_W3O3:	return "Orc Shaman";
			case IconIDs.ICON_W3O4:	return "Orc Spirit Walker";
			case IconIDs.ICON_W3O5:	return "Orc Shadow Hunter";
			case IconIDs.ICON_W3O6:	return "Orc Rexxar";
			case IconIDs.ICON_W3N2:	return "Night Elf Huntress";
			case IconIDs.ICON_W3N3:	return "Night Elf Druid of the Talon";
			case IconIDs.ICON_W3N4:	return "Night Elf Dryad";
			case IconIDs.ICON_W3N5:	return "Night Elf Keeper of the Grove";
			case IconIDs.ICON_W3N6:	return "Night Elf Maiev";
			case IconIDs.ICON_W3U2:	return "Undead Crypt Fiend";
			case IconIDs.ICON_W3U3:	return "Undead Banshee";
			case IconIDs.ICON_W3U4:	return "Undead Destroyer";
			case IconIDs.ICON_W3U5:	return "Undead Crypt Lord";
			case IconIDs.ICON_W3U6:	return "Undead Sylvanas";
			case IconIDs.ICON_W3R2:	return "Myrmidon";
			case IconIDs.ICON_W3R3:	return "Siren";
			case IconIDs.ICON_W3R4:	return "Dragon Turtle";
			case IconIDs.ICON_W3R5:	return "Sea Witch";
			case IconIDs.ICON_W3R6:	return "Illidan";
			case IconIDs.ICON_W3D2:	return "Felguard";
			case IconIDs.ICON_W3D3:	return "Infernal";
			case IconIDs.ICON_W3D4:	return "Doomguard";
			case IconIDs.ICON_W3D5:	return "Pit Lord";
			case IconIDs.ICON_W3D6:	return "Archimonde";
			}
			break;
		}

		return "Unknown " + HexDump.DWordToPretty(icon);
	}

	public void parse() throws IOException {
		if(parsed)
			return;
		parsed = true;

		if(is == null) {
			try {
				product = ProductIDs.fromDWord(HexDump.StringToDWord(statString[0]));
				icon = product.getDword();
			} catch(Exception e) {
				prettyEnd = e.getMessage();
				return;
			}
		} else {
			try {
				product = ProductIDs.fromDWord(is.readDWord());
			} catch(EOFException e) {
				prettyEnd = e.getMessage();
				return;
			}
		}

		prettyStart = " using ";
		prettyStart += product.toString();

		StringBuilder prettyEnd = new StringBuilder();

		if((is != null) || (statString.length > 1) || (statString[0].length() > 4)) {
			switch(product) {
			case STAR:
			case SEXP:
			case JSTR:
			case W2BN:
			case SSHR:
			case DSHR:
			case DRTL:
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
				if(is != null) {
					if(is.readByte() == 0) // discard the space, or terminate the string
						break;
					statString2 = is.readNTString().split(" ");
				}

				if(statString2.length != 9) {
					this.prettyEnd = "\ninvalid length " + statString2.length;
					return;
				}

				try {
					ladderRating = Integer.parseInt(statString2[0]);
					ladderRank = Integer.parseInt(statString2[1]);
					wins = Integer.parseInt(statString2[2]);
					spawn = statString2[3].equals("1");
					long unknown5 = Integer.parseInt(statString2[4]);
					highLadderRating = Integer.parseInt(statString2[5]);
					long unknown7 = Integer.parseInt(statString2[6]);
					long unknown8 = Integer.parseInt(statString2[7]);
					try {
						icon = HexDump.StringToDWord(statString2[8]);
					} catch(Exception e) {}

					if(ladderRating != 0)
						prettyEnd.append(", Ladder rating " + ladderRating);
					if(ladderRank != 0)
						prettyEnd.append(", Ladder rank " + ladderRank);
					if(wins != 0)
						prettyEnd.append(", " + wins + " wins");
					if(spawn)
						prettyEnd.append(", Spawn");
					if(unknown5 != 0)
						prettyEnd.append(", unknown5=").append(unknown5);
					if((highLadderRating != 0) && (highLadderRating != ladderRating))
						prettyEnd.append(", Ladder highest ever rating ").append(highLadderRating);
					if(unknown7 != 0)
						prettyEnd.append(", unknown7=").append(unknown7);
					if(unknown8 != 0)
						prettyEnd.append(", unknown8=").append(unknown8);
					if((icon != 0) && (icon != product.getDword()))
						prettyEnd.append(", ").append(HexDump.DWordToPretty(icon)).append(" icon");
				} catch(Exception e) {
					Out.exception(e);
				}
				break;

			case D2DV:
			case D2XP:
				byte[] data = null;

				if(is == null) {
					statString2 = statString[0].substring(4).split(",", 3);
					data = statString2[2].getBytes();

					if(statString2.length != 3) {
						prettyEnd.append("unknown statstr2 len: ").append(statString2.length);
						break;
					}
				} else {
					statString2 = new String[2];
					try {
						statString2[0] = is.readCommaTermString();
					} catch(IOException e) {
						// No realm was found
						break;
					}
					statString2[1] = is.readCommaTermString();
					data = new byte[33];
					is.read(data);
					if(is.readByte() != 0)
						throw new IOException("after read 33 bytes of data, no null found");
				}

				prettyStart += ", Realm " + statString2[0];
				prettyStart += ", ";

				if(data.length != 33)
					throw new IOException("data.length != 33 (" + data.length + ")");

				byte version = (byte)(data[0] & 0x7F);
				if(version != 4)
					throw new IOException("version != 4 (" + version + ")");

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

			    byte actsCompleted = (byte)((data[27] & 0x3E) >>> 1);

			    byte difficulty;
			    if(expansion) {
			    	difficulty = (byte)(actsCompleted / 5);
			    	actsCompleted = (byte)((actsCompleted % 5) + 1);
			    } else {
			    	difficulty = (byte)(actsCompleted / 4);
			    	actsCompleted = (byte)((actsCompleted % 4) + 1);
			    }

			    if(expansion) {
			        switch(difficulty) {
			        case 0: break;
		            case 1: prettyEnd.append(hardcore ? "Destroyer " : "Slayer "); break;
		            case 2: prettyEnd.append(hardcore ? "Conquer " : "Champion "); break;
		            case 3: prettyEnd.append(hardcore ? "Guardian " : (female ? "Matriarch " : "Patriarch ")); break;
		            default: prettyEnd.append("?d=").append(difficulty); break;
			        }
			    } else {
			        switch(difficulty) {
		        	case 0: break;
		            case 1: prettyEnd.append(female ? (hardcore ? "Countess " : "Dame ") : (hardcore ? "Count " : "Sir ")); break;
		            case 2: prettyEnd.append(female ? (hardcore ? "Duchess " : "Lady ") : (hardcore ? "Duke " : "Lord ")); break;
		            case 3: prettyEnd.append(female ? (hardcore ? "Queen " : "Baroness ") : (hardcore ? "King " : "Baron ")); break;
		            default: prettyEnd.append("??d=").append(difficulty); break;
			        }
			    }

			    prettyEnd.append(statString2[1]);

			    prettyEnd.append(", a ");

			    // Dead
			    if(dead)
			    	prettyEnd.append("dead ");

			    // Hardcore/softcore
			    if(hardcore)
			    	prettyEnd.append("hardcore ");
			    else
			    	prettyEnd.append("softcore ");

			    // Ladder/non-ladder
			    if(!ladder)
			    	prettyEnd.append("non-");
			    prettyEnd.append("ladder ");

				prettyEnd.append("level ").append(charLevel);
				prettyEnd.append(" ").append(D2Classes[charClass]);
				prettyEnd.append(" (");
				if(expansion)
					prettyEnd.append("Expansion");
				else
					prettyEnd.append("Classic");
				prettyEnd.append(")");

			    if(difficulty < 3) {
			    	prettyEnd.append(" currently in ");
			        switch(difficulty) {
			            case 0: prettyEnd.append("Normal"); break;
			            case 1: prettyEnd.append("Nightmare"); break;
			            case 2: prettyEnd.append("Hell"); break;
			            default: prettyEnd.append("?"); break;
			        }
			        prettyEnd.append(" act ").append(actsCompleted);
			    } else {
			    	prettyEnd.append(" who has beaten the game");
			    }


				break;

			case WAR3:
			case W3XP:
				if(is != null) {
					if(is.readByte() == 0) // discard the space, or terminate the string
						break;
					statString2 = is.readNTString().split(" ");
				}

				if(statString2.length >= 2) {
					//3RAW 1R3W 1 UNB
					icon = HexDump.StringToDWord(statString2[0]);
					level = Integer.parseInt(statString2[1]);

					if(icon != 0)
						prettyEnd.append(", ").append(getIconName(product, icon)).append(" icon");
					if(level != 0)
						prettyEnd.append(", Level ").append(level);

					if(statString2.length >= 3) {
						byte[] bytes = statString2[2].getBytes();
						String clan = "";
						for(int j = bytes.length-1; j >= 0; j--)
							clan += (char)bytes[j];

						prettyEnd.append(", in Clan ").append(clan);
					}
				} else {
					prettyEnd.append("\n");
					prettyEnd.append(statString[1]);
				}
				break;

			default:
				prettyEnd.append(", statstr = ");
				if(is == null) {
					prettyEnd.append(statString[1]);
				} else {
					prettyEnd.append(is.readNTString());
				}
				break;
			}
			this.prettyEnd = prettyEnd.toString();
		}
	}

	@Override
	public String toString() {
		if(pretty != null)
			return pretty;

		pretty = prettyStart;

		if(pretty == null) {
			pretty = prettyEnd;
		} else {
			if(prettyEnd != null)
				pretty += prettyEnd;
		}

		if(pretty == null)
			pretty = new String();

		return pretty;
	}

	public String toString2() {
		if(prettyEnd != null)
			return prettyEnd;

		return new String();
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
