package bnubot.bot.gui.userlist;

import java.awt.*;
import java.awt.image.*;
import java.io.*;

import javax.swing.*;

import bnubot.core.BNetInputStream;


@SuppressWarnings("serial")
public class IconsDotBniReader {
	private static int getRealPixelPosition(int i, int height, int width) {
		int x = i % width;
		int y = (i - x) / width;
		return ((height - y - 1) * width) + x;
	}

	public static BNetIcon[] readIconsDotBni(File f) {
		try {
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
				String products[] = new String[32];
				for(numProducts = 0; numProducts < 32; numProducts++) {
					int p = is.readDWord();
					if(p == 0)
						break;
					
					byte[] b = new byte[4];
					b[0] = (byte)((p & 0x000000FF) >> 0);
					b[1] = (byte)((p & 0x0000FF00) >> 8);
					b[2] = (byte)((p & 0x00FF0000) >> 16);
					b[3] = (byte)((p & 0xFF000000) >> 24);
					products[numProducts] = new String(b);
				}
				icon.products = new String[numProducts];
				for(int j = 0; j < numProducts; j++)
					icon.products[j] = products[j];
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
			byte descriptor = is.readByte();	// bits 5 and 4 (00110000) specify the corner to start coloring pixels - 00=bl, 01=br, 10=tl, 11=tr
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
			JFrame util = new JFrame();
			//util.setLayout(new FlowLayout(FlowLayout.CENTER));
			for(int i = 0; i < numIcons; i++) {
				BNetIcon bni = icons[i];
				bni.icon = new ImageIcon(
						util.createImage(
								new MemoryImageSource(bni.xSize, bni.ySize, pixelData, currentPixel, bni.xSize)));
				currentPixel += bni.xSize * bni.ySize;
				//util.add(new JLabel(bni.icon));
			}
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
