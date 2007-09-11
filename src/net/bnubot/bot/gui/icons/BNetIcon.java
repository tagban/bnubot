/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.bot.gui.icons;

import javax.swing.Icon;

import net.bnubot.util.HexDump;

public class BNetIcon {
	int flags;
	int xSize;
	int ySize;
	int products[];
	Icon icon;
	int sortIndex;
	
	public boolean useFor(int flags, int product) {
		if((flags & this.flags) != 0)
			return true;
		
		if(products == null)
			return false;
		
		for(int element : products) {
			if(product == element)
				return true;
		}
		
		return false;
	}
	
	public String toString() {
		String out = "Icon[flags=0x" + Integer.toHexString(flags)  + ",xSize=" + xSize + ",ySize=" + ySize;
		if(products != null) {
			out += ",products=[";
			for (int element : products)
				out += HexDump.DWordToPretty(element) + ",";
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