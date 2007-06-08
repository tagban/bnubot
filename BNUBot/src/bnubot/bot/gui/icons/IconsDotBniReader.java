package bnubot.bot.gui.icons;

import java.awt.*;
import java.awt.image.*;
import java.io.*;

import javax.swing.*;

import bnubot.core.BNetInputStream;
import bnubot.core.ConnectionSettings;
import bnubot.core.bnftp.BNFTPConnection;


@SuppressWarnings("serial")
public class IconsDotBniReader {
	private static boolean initialized = false;
	private static boolean initializedWindow = false;
	private static BNetIcon[] icons = null;
	private static BNetIcon[] icons_STAR = null;
	private static BNetIcon[] icons_WAR3 = null;
	private static BNetIcon[] icons_W3XP = null;
	private static BNetIcon[] legacy_icons = null;
	
	private static JFrame util = null;
	
	public static void showWindow() {
		if(!initialized)
			return;
		if(!initializedWindow) {
			initializedWindow = true;
			util.setLayout(new FlowLayout(FlowLayout.CENTER));
			
			BNetIcon[][] iconss = {icons, icons_STAR, icons_WAR3, icons_W3XP, legacy_icons};
			for(BNetIcon[] icons : iconss) {
				Box b = new Box(BoxLayout.Y_AXIS);
				for(int i = 0; i < icons.length; i++) {
					BNetIcon bni = icons[i];
					b.add(new JLabel(bni.icon));
					b.add(Box.createVerticalStrut(1));
				}
				util.add(b);
			}
			util.pack();
		}
		util.setVisible(true);
	}
	
	public static void initialize(ConnectionSettings cs) {
		if(initialized)
			return;
		initialized = true;
		
		File f;
		
		f = new File("legacy_icons.bni");
		if(f.exists())
			legacy_icons = readIconsDotBni(f);
		
		f = new File("war3_icons.bni");
		if(f.exists())
			icons_WAR3 = readIconsDotBni(f);
		
		f = new File("w3xp_icons.bni");
		if(f.exists())
			icons_W3XP = readIconsDotBni(f);
		
		f = BNFTPConnection.downloadFile(cs, "icons_STAR.bni");
		if(f.exists())
			icons_STAR = readIconsDotBni(f);
		
		f = BNFTPConnection.downloadFile(cs, "Icons.bni");
		if(f.exists())
			icons = readIconsDotBni(f);
	}
	
	public static BNetIcon[] getIcons() {
		return icons;
	}
	
	public static BNetIcon[] getIconsSTAR() {
		return icons_STAR;
	}
	
	public static BNetIcon[] getIconsWAR3() {
		return icons_WAR3;
	}
	
	public static BNetIcon[] getIconsW3XP() {
		return icons_W3XP;
	}
	
	public static BNetIcon[] getLegacyIcons() {
		return legacy_icons;
	}
	
	private static int getRealPixelPosition(int i, int height, int width) {
		int x = i % width;
		int y = (i - x) / width;
		return ((height - y - 1) * width) + x;
	}

	private static BNetIcon[] readIconsDotBni(File f) {
		try {
			System.out.println("Reading " + f.getName());
			
			BNetInputStream is = new BNetInputStream(new FileInputStream(f));
			is.skip(4); //int headerSize = is.readDWord();
			int bniVersion = is.readWord();
			is.skip(2);	// Alignment Padding (unused)
			int numIcons = is.readDWord();
			is.skip(4);	//int dataOffset = is.readDWord();
			
			if(bniVersion != 1)
				throw new Exception("Unknown BNI version");
			
			//System.out.println("Reading " + numIcons + " icons in format " + bniVersion + " from offset " + dataOffset);

			//Image headers
			/*if(icons == null)
				icons = new BNetIcon[numIcons];
			else {
				BNetIcon[] newIcons = new BNetIcon[numIcons + icons.length];
				for(int i = numIcons; i < numIcons + icons.length; i++) {
					newIcons[i] = icons[i-numIcons];
				}
				icons = newIcons;
			}*/
			BNetIcon[] icons = new BNetIcon[numIcons];
			
			for(int i = 0; i < numIcons; i++) {
				BNetIcon icon = new BNetIcon();
				icon.flags = is.readDWord();
				icon.xSize = is.readDWord();
				icon.ySize = is.readDWord();
				if(icon.flags != 0)
					icon.sortIndex = i;
				else
					icon.sortIndex = numIcons;
				int numProducts;
				int products[] = new int[32];
				
				//Read in up to 32 products; stop if we see a null 
				for(numProducts = 0; numProducts < 32; numProducts++) {
					products[numProducts] = is.readDWord();
					if(products[numProducts] == 0)
						break;
				}
				
				if(numProducts > 0) {
					icon.products = new int[numProducts];
					for(int j = 0; j < numProducts; j++)
						icon.products[j] = products[j];
				} else
					icon.products = null;
				icons[i] = icon;
				//System.out.println(icon.toString());
			}
			
			//Image in targa format
			byte infoLength = is.readByte();
			is.skip(1);							// ColorMapType
			byte imageType = is.readByte();		// run-length true-color image types = 0x0A
			is.skip(5);							// ColorMapSpecification - color map data
			is.skip(2);	//int xOrigin = is.readWord();
			is.skip(2);	//int yOrigin = is.readWord();
			int width = is.readWord();
			int height = is.readWord();
			byte depth = is.readByte();			// 24 bit depth is good
			/*byte descriptor =*/ is.readByte();	// bits 5 and 4 (00110000) specify the corner to start coloring pixels - 00=bl, 01=br, 10=tl, 11=tr
			is.skip(infoLength);	//String info = is.readFixedLengthString(infoLength);
			
			if(imageType != 0x0A)
				throw new Exception("Unknown image type");
			if(depth != 24)
				throw new Exception("Unknown depth");
			
			//Pixel data
			int[] pixelData = new int[width*height];
			int currentPixel = 0;
			
			while(currentPixel < pixelData.length) {
				byte packetHeader = is.readByte();	// if bit 7 (0x80) is set, run-length packet;
				int len = (packetHeader & 0x7F) + 1;
				if((packetHeader & 0x80) != 0) {
					//Run-length packet
					int blue = ((int)is.readByte()) & 0xFF;
					int green = ((int)is.readByte()) & 0xFF;
					int red = ((int)is.readByte()) & 0xFF;
					int col = new Color(red, green, blue).getRGB();
					for(int i = 0; i < len; i++)
						pixelData[getRealPixelPosition(currentPixel++, height, width)] = col;
				} else {
					for(int i = 0; i < len; i++) {
						int blue = ((int)is.readByte()) & 0xFF;
						int green = ((int)is.readByte()) & 0xFF;
						int red = ((int)is.readByte()) & 0xFF;
						int col = new Color(red, green, blue).getRGB();
						pixelData[getRealPixelPosition(currentPixel++, height, width)] = col;
					}
				}
			}
			
			//Split up the big image in to individual images
			currentPixel = 0;
			
			if(util == null) {
				util = new JFrame("Icons");
			//	util.setLayout(new FlowLayout(FlowLayout.CENTER));
				util.setLayout(null);
			}
			
			//Box b = new Box(BoxLayout.Y_AXIS);
			//b.add(new JLabel(f.getName()));
			for(int i = 0; i < numIcons; i++) {
				BNetIcon bni = icons[i];
				bni.icon = new ImageIcon(
						util.createImage(
								new MemoryImageSource(bni.xSize, bni.ySize, pixelData, currentPixel, bni.xSize)));
				currentPixel += bni.xSize * bni.ySize;
			//	b.add(new JLabel(bni.icon));
			//	b.add(Box.createVerticalStrut(1));
			}
			//util.add(b);
			//util.pack();
			//util.setVisible(true);
			
			return icons;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}

}
