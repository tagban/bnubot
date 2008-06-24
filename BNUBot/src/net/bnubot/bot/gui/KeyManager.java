/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import net.bnubot.settings.Settings;
import net.bnubot.util.Out;

import org.jbls.util.Buffer;

public class KeyManager {
	public static final int PRODUCT_ALLNORMAL = -1;
	public static final int PRODUCT_STAR = 0x01;
	public static final int PRODUCT_W2BN = 0x04;
	public static final int PRODUCT_D2DV = 0x06;
	public static final int PRODUCT_D2XP = 0x0A;
	public static final int PRODUCT_WAR3 = 0x0E;
	public static final int PRODUCT_W3XP = 0x12;
	public static final int PRODUCT_STAR_ANTHOLOGY = 0x17;
	public static final int PRODUCT_D2DV_ANTHOLOGY = 0x18;
	public static final int PRODUCT_D2XP_ANTHOLOGY = 0x19;

	private static List<CDKey> cdkeys = new ArrayList<CDKey>();
	private static boolean initialized = false;

	public static class CDKey {
		String key;
		int product;
		String comment;
		public CDKey(String key, int product, String comment) {
			this.key = key;
			this.product = product;
			this.comment = comment;
		}

		public String getComment() {
			return comment;
		}

		public String getKey() {
			return key;
		}

		public int getProduct() {
			return product;
		}

		@Override
		public String toString() {
			return key + " - " + comment;
		}
	}

	public static void resetInitialized() {
		initialized = false;
		cdkeys.clear();
	}

	private static void initialize() {
		if(initialized)
			return;
		initialized = true;

		File keys = Settings.keysFile;
		BufferedReader is = null;

		try {
			if(!keys.exists()) {
				keys.createNewFile();

				FileWriter os = new FileWriter(keys);
				os.write("# Enter CD keys in this file.\r\n");
				os.write("# \r\n");
				os.write("# Lines beginning with '#' are regarded as comments\r\n");
				os.write("# \r\n");
				os.write("# You may add a comment next to each cd key.\r\n");
				os.write("# Example:\r\n");
				os.write("0123-45678-9012 my first SC key\r\n");
				os.write("5555-12321-5555 my second SC key\r\n");
				os.write("0123-4567-89AB-CDEF  my first D2 key\r\n");
				os.write("\r\n");
				os.close();
			}
			is = new BufferedReader(new FileReader(keys));
		} catch (Exception e) {
			Out.fatalException(e);
		}

		String lastComment = "";
		do {
			String key = null;
			try {
				key = is.readLine();
			} catch (IOException e) {
				Out.exception(e);
			}
			if(key == null)
				break;

			key = key.trim();
			if(key.length() == 0)
				continue;

			try {
				if(key.charAt(0) != '#')  {
					key = key.replaceAll("\t", " ").trim();

					String comment = "";
					int i = key.indexOf(" ");
					if(i != -1) {
						comment = key.substring(i).trim();
						key = key.substring(0, i);
					} else {
						comment = lastComment;
					}

					key = key.replaceAll("-", "");

					Buffer b = org.jbls.Hashing.HashMain.hashKey(0, 0, key);
					b.removeDWord();	//length
					int prod = b.removeDWord();	//Product

					cdkeys.add(new CDKey(key, prod, comment));
				} else {
					lastComment = key.substring(1).trim();
				}
			} catch(Exception e) {
				Out.info(KeyManager.class, "Couldn't parse line: " + key);
			}
		} while(true);

		try { is.close(); } catch (Exception e) {}

		try {
			File keys2 = new File("cdkeys.processed.txt");
			if(!keys2.exists())
				keys2.createNewFile();

			FileWriter os = new FileWriter(keys2);
			os.write("# " + new Date().toString() + "\r\n");

			int[] prods = new int[] {
					PRODUCT_STAR,
					PRODUCT_W2BN,
					PRODUCT_D2DV,
					PRODUCT_D2XP,
					PRODUCT_WAR3,
					PRODUCT_W3XP };
			for(int prod : prods) {
				os.write("\n");
				switch(prod) {
				case PRODUCT_STAR: os.write("# STAR\r\n"); break;
				case PRODUCT_W2BN: os.write("# W2BN\r\n"); break;
				case PRODUCT_D2DV: os.write("# D2DV\r\n"); break;
				case PRODUCT_D2XP: os.write("# D2XP\r\n"); break;
				case PRODUCT_WAR3: os.write("# WAR3\r\n"); break;
				case PRODUCT_W3XP: os.write("# W3XP\r\n"); break;
				default: os.write("# ??? " + prod + "\r\n"); break;
				}

				for(CDKey k : getKeys(prod))
					os.write(formatKey(k.key) + " " + k.comment + "\r\n");
			}
			os.close();
		} catch(Exception e) {
			Out.exception(e);
		}
	}

	private static String formatKey(String key) {
		key = key.toUpperCase();
		switch(key.length()) {
		case 13: // 4-5-4 (sc)
			key = key.substring(0, 4) + "-"
				+ key.substring(4, 9) + "-"
				+ key.substring(9);
			break;
		case 16: // 4-4-4-4 (w2/d2/lod)
			key = key.substring(0, 4) + "-"
				+ key.substring(4, 8) + "-"
				+ key.substring(8, 12) + "-"
				+ key.substring(12);
			break;
		case 26: // 6-4-6-4-6 (w3/tft)
			key = key.substring(0, 6) + "-"
				+ key.substring(6, 10) + "-"
				+ key.substring(10, 16) + "-"
				+ key.substring(16, 20) + "-"
				+ key.substring(20);
			break;
		}
		return key;
	}

	public static CDKey[] getKeys(int product) {
		initialize();

		List<CDKey> prodKeys = new LinkedList<CDKey>();
		for(CDKey k : cdkeys) {
			if(product == PRODUCT_ALLNORMAL) {
				switch(k.getProduct()) {
				case PRODUCT_STAR:
				case PRODUCT_W2BN:
				case PRODUCT_D2DV:
				case PRODUCT_WAR3:
				case PRODUCT_STAR_ANTHOLOGY:
				case PRODUCT_D2DV_ANTHOLOGY:
					prodKeys.add(k);
					break;
				}
			} else {
				if(k.getProduct() != product) {
					switch(k.getProduct()) {
					case PRODUCT_STAR_ANTHOLOGY:
						if(product != PRODUCT_STAR)
							continue;
						break;
					case PRODUCT_D2DV_ANTHOLOGY:
						if(product != PRODUCT_STAR)
							continue;
						break;
					case PRODUCT_D2XP_ANTHOLOGY:
						if(product != PRODUCT_D2DV)
							continue;
						break;
					default:
						continue;
					}
				}

				// If we made it this far, the key matched the filter
				prodKeys.add(k);
			}
		}

		return prodKeys.toArray(new CDKey[prodKeys.size()]);
	}
}
