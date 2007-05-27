package core.bot.gui.icons;

import java.awt.Image;

public class BNetIcon {
	int flags;
	int xSize;
	int ySize;
	String products[];
	Image img;
	
	public String toString() {
		String out = "Icon[flags=0x" + Integer.toHexString(flags)  + ",xSize=" + xSize + ",ySize=" + ySize;
		if(products.length > 0) {
			out += ",products=[";
			for(int i = 0; i < products.length; i++)
				out += products[i] + ",";
			out = out.substring(0, out.length() - 1);
			out += "]";
		}
		out += "]";
		return out;
	}
}