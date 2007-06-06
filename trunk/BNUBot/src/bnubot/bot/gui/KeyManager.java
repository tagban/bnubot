package bnubot.bot.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import util.Buffer;

public class KeyManager {
	public static final int PRODUCT_ALLNORMAL = -1;
	public static final int PRODUCT_STAR = 1;
	public static final int PRODUCT_W2BN = 4;
	public static final int PRODUCT_D2DV = 6;
	public static final int PRODUCT_D2XP = 10;
	public static final int PRODUCT_WAR3 = 14;
	public static final int PRODUCT_W3XP = 18;
	
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
		
		public String toString() {
			return key + " - " + comment;
		}
	}
	
	private static LinkedList<CDKey> cdkeys = new LinkedList<CDKey>();
	private static boolean initialized = false;
	
	private static void initialize() {
		if(initialized)
			return;
		
		File keys = new File("cdkeys.txt");
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
				os.write("# 0123-45678-9012 my first SC key\r\n");
				os.write("# 5555-12321-5555 my second SC key\r\n");
				os.write("# 0123-4567-89AB-CDEF  my first D2 key\r\n");
				os.write("# \r\n");
				os.write("# Products for each CD key will automatically be detected\r\n");
				os.write("# \r\n");
				os.write("\r\n");
				os.close();
			}
			is = new BufferedReader(new FileReader(keys));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		do {
			String key = null;
			try {
				key = is.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(key == null)
				break;
			
			key = key.trim();
			if(key.length() == 0)
				continue;
			
			try {
				if(key.charAt(0) != '#')  {
					String comment = "";
					int i = key.indexOf(" ");
					if(i != -1) {
						comment = key.substring(i);
						key = key.substring(0, i);
					}
					
					key = key.replaceAll("-", "");
					
					Buffer b = Hashing.HashMain.hashKey(0, 0, key);
					b.removeDWord();	//length
					int prod = b.removeDWord();	//Product
					
					cdkeys.add(new CDKey(key, prod, comment));
				}
			} catch(Exception e) {
				System.out.print("Couldn't parse key line: " + key);
			}
		} while(true);
		
		try { is.close(); } catch (Exception e) {}
	}
	
	public static CDKey[] getKeys(int product) {
		initialize();
		
		LinkedList<CDKey> prodKeys = new LinkedList<CDKey>();
		Iterator<CDKey> it = cdkeys.iterator();
		while(it.hasNext()) {
			CDKey k = it.next();
			if(product == PRODUCT_ALLNORMAL) {
				switch(k.getProduct()) {
				case PRODUCT_STAR:
				case PRODUCT_W2BN:
				case PRODUCT_D2DV:
				case PRODUCT_WAR3:
					prodKeys.add(k);
					break;
				}
			} else if(k.getProduct() == product)
				prodKeys.add(k);
		}
		
		return prodKeys.toArray(new CDKey[prodKeys.size()]);
	}
}
