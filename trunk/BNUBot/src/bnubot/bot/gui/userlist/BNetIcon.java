package bnubot.bot.gui.userlist;

import javax.swing.Icon;

public class BNetIcon {
	int flags;
	int xSize;
	int ySize;
	String products[];
	Icon icon;
	int sortIndex;
	
	public boolean useFor(int flags, String product) {
		if((flags & this.flags) != 0)
			return true;
		
		for(int i = 0; i < products.length; i++) {
			if(product.compareTo(products[i]) == 0)
				return true;
		}
		
		return false;
	}
	
	private String strReverse(String s) {
		String out = "";
		for(int i = 0; i < s.length(); i++)
			out = s.charAt(i) + out;
		return out;
	}
	
	public String toString() {
		String out = "Icon[flags=0x" + Integer.toHexString(flags)  + ",xSize=" + xSize + ",ySize=" + ySize;
		if(products.length > 0) {
			out += ",products=[";
			for(int i = 0; i < products.length; i++)
				out += strReverse(products[i]) + ",";
			out = out.substring(0, out.length() - 1);
			out += "]";
		}
		out += "]";
		return out;
	}

	public Icon getIcon() {
		return icon;
	}
	
	public int getSortIndex() {
		return sortIndex;
	}
}