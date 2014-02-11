/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui.icons;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.MemoryImageSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.ref.SoftReference;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import net.bnubot.core.BNFTPConnection;
import net.bnubot.logging.Out;
import net.bnubot.settings.Settings;
import net.bnubot.util.BNetInputStream;
import net.bnubot.util.BNetOutputStream;
import net.bnubot.util.crypto.HexDump;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;

/**
 * @author scotta
 */
public class IconsDotBniReader {
	private static boolean initializedWindow = false;
	private static SoftReference<BNetIcon[]> icons = null;
	private static SoftReference<BNetIcon[]> icons_STAR = null;
	private static SoftReference<BNetIcon[]> icons_WAR3 = null;
	private static SoftReference<BNetIcon[]> icons_W3XP = null;
	private static SoftReference<BNetIcon[]> legacy_icons = null;
	private static SoftReference<BNetIcon[]> icons_lag = null;
	private static SoftReference<BNetIcon[]> icons_clan = null;

	public static final int LEGACY_STARWIN = 0;
	public static final int LEGACY_LADDER = 11;
	public static final int LEGACY_LADDER2 = 12;
	public static final int LEGACY_LADDERNUM1 = 13;
	public static final int LEGACY_W2BNWIN = 14;

	private static JFrame util = null;

	public static void showWindow() {
		if(!initializedWindow) {
			initializedWindow = true;
			util.setLayout(new FlowLayout(FlowLayout.CENTER));

			BNetIcon[][] iconss = {getIcons(), getIconsSTAR(), getIconsWAR3(), getIconsW3XP(), getLegacyIcons(), getIconsLag(), getIconsClan()};
			for(BNetIcon[] icons : iconss) {
				if(icons == null)
					continue;

				Box b = new Box(BoxLayout.Y_AXIS);
				for(int i = 0; i < icons.length; i++) {
					BNetIcon bni = icons[i];
					JLabel jl = new JLabel(bni.icon);
					String text = "";
					if(bni.products != null)
						text += " " + HexDump.DWordToPretty(bni.products[0]);
					if(bni.flags != 0)
						text += " 0x" + Integer.toHexString(bni.flags);
					jl.setText(Integer.toString(i) + text);
					b.add(jl);
					b.add(Box.createVerticalStrut(1));
				}
				util.add(b);
			}
			util.pack();
		}
		util.setVisible(true);
	}

	public static BNetIcon[] getIcons() {
		if(icons != null) {
			BNetIcon[] ret = icons.get();
			if(ret != null)
				return ret;

			Out.debug(IconsDotBniReader.class, "SoftReference to Icons.bni expired");
		}

		try {
			File f = BNFTPConnection.downloadFile("Icons.bni");
			if((f != null) && f.exists()) {
				BNetIcon[] ret = readIconsDotBni(f);
				icons = new SoftReference<BNetIcon[]>(ret);
				return ret;
			}
		} catch(Exception e) {}

		return null;
	}

	public static BNetIcon[] getIconsSTAR() {
		if(icons_STAR != null) {
			BNetIcon[] ret = icons_STAR.get();
			if(ret != null)
				return ret;

			Out.debug(IconsDotBniReader.class, "SoftReference to icons_STAR.bni expired");
		}

		try {
			File f = BNFTPConnection.downloadFile("icons_STAR.bni");
			if((f != null) && f.exists()) {
				BNetIcon[] ret = readIconsDotBni(f);
				icons_STAR = new SoftReference<BNetIcon[]>(ret);
				return ret;
			}
		} catch(Exception e) {}

		return null;
	}

	public static BNetIcon[] getIconsWAR3() {
		if(icons_WAR3 != null) {
			BNetIcon[] ret = icons_WAR3.get();
			if(ret != null)
				return ret;

			Out.debug(IconsDotBniReader.class, "SoftReference to war3_icons.bni expired");
		}

		File f = new File(Settings.getRootPath() + "downloads/war3_icons.bni");
		if(f.exists()) {
			BNetIcon[] ret = readIconsDotBni(f);
			icons_WAR3 = new SoftReference<BNetIcon[]>(ret);
			return ret;
		}

		return null;
	}

	public static BNetIcon[] getIconsW3XP() {
		if(icons_W3XP != null) {
			BNetIcon[] ret = icons_W3XP.get();
			if(ret != null)
				return ret;

			Out.debug(IconsDotBniReader.class, "SoftReference to w3xp_icons.bni expired");
		}

		File f = new File(Settings.getRootPath() + "downloads/w3xp_icons.bni");
		if(f.exists()) {
			BNetIcon[] ret = readIconsDotBni(f);
			icons_W3XP = new SoftReference<BNetIcon[]>(ret);
			return ret;
		}

		return null;
	}

	public static BNetIcon[] getLegacyIcons() {
		if(legacy_icons != null) {
			BNetIcon[] ret = legacy_icons.get();
			if(ret != null)
				return ret;

			Out.debug(IconsDotBniReader.class, "SoftReference to legacy_icons.bni expired");
		}

		File f = new File(Settings.getRootPath() + "downloads/legacy_icons.bni");
		if(f.exists()) {
			BNetIcon[] ret = readIconsDotBni(f);
			legacy_icons = new SoftReference<BNetIcon[]>(ret);
			return ret;
		}

		return null;
	}

	public static BNetIcon[] getIconsLag() {
		if(icons_lag != null) {
			BNetIcon[] ret = icons_lag.get();
			if(ret != null)
				return ret;

			Out.debug(IconsDotBniReader.class, "SoftReference to icons_lag.bni expired");
		}

		File f = new File(Settings.getRootPath() + "downloads/icons_lag.bni");
		if(f.exists()) {
			BNetIcon[] ret = readIconsDotBni(f);
			icons_lag = new SoftReference<BNetIcon[]>(ret);
			return ret;
		}

		return null;
	}

	public static BNetIcon[] getIconsClan() {
		if(icons_clan != null) {
			BNetIcon[] ret = icons_clan.get();
			if(ret != null)
				return ret;

			Out.debug(IconsDotBniReader.class, "SoftReference to icons_clan.bni expired");
		}

		File f = new File(Settings.getRootPath() + "downloads/icons_clan.bni");
		if(f.exists()) {
			BNetIcon[] ret = readIconsDotBni(f);
			icons_clan = new SoftReference<BNetIcon[]>(ret);
			return ret;
		}

		return null;
	}

	private static int getRealPixelPosition(int i, int height, int width) {
		int x = i % width;
		//int y = (i - x) / width;
		//return ((height - y - 1) * width) + x;
		return ((height - 1) * width) - i + (x * 2);
	}

	public static void writeIconsDotBni(File f, BNetIcon[] icons) {
		for(BNetIcon icon : icons) {
			icon.xSize = icon.getIcon().getIconWidth();
			icon.ySize = icon.getIcon().getIconHeight();
		}

		try (
			BNetOutputStream os = new BNetOutputStream(new FileOutputStream(f));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			BNetOutputStream headerStream = new BNetOutputStream(baos);
		) {
			Out.debug(IconsDotBniReader.class, "Writing " + f.getName());

			headerStream.writeWord(1); // BNI version
			headerStream.writeWord(0); // Alignment Padding (unused)
			headerStream.writeDWord(icons.length); // numIcons
			headerStream.writeDWord(-1); // dataOffset

			for(BNetIcon icon : icons) {
				headerStream.writeDWord(icon.flags);
				headerStream.writeDWord(icon.xSize);
				headerStream.writeDWord(icon.ySize);

				//Write up to 32 products; stop if we see a null
				for(int product : icon.products) {
					if(product == 0)
						break;
					headerStream.writeDWord(product);
				}
				headerStream.writeDWord(0);
			}


			byte[] header = baos.toByteArray();
			os.writeDWord(header.length);
			os.write(header);

			int width = icons[0].getIcon().getIconWidth();
			int height = icons[0].getIcon().getIconHeight() * icons.length;

			//Image in targa format
			String info = "";
			os.writeByte(info.length());	// infolength
			os.writeByte(0);	// ColorMapType
			os.writeByte(0x0A);	// run-length true-color image types = 0x0A
			os.write(new byte[5]);	// ColorMapSpecification - color map data
			os.writeWord(0);	// xOrigin
			os.writeWord(0);	// yOrigin
			os.writeWord(width);
			os.writeWord(height);
			os.writeByte(24);	// 24 bit depth is good
			os.writeByte(0x30);	// descriptor; bits 5 and 4 (00110000) specify the corner to start coloring pixels - 00=bl, 01=br, 10=tl, 11=tr
			os.write(info.getBytes());

			//Pixel data
			BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			for(int i = 0; i < icons.length; i++) {
				BNetIcon icon = icons[i];
				ImageIcon img = (ImageIcon)icon.getIcon();
				bi.getGraphics().drawImage(img.getImage(), 0, i * icon.ySize, null);
			}

			DataBufferInt dbi = (DataBufferInt)(bi.getRaster().getDataBuffer());
			int[] pixelData = dbi.getData();

			int currentPixel = 0;
			while(currentPixel < pixelData.length) {
				//byte packetHeader = is.readByte();	// if bit 7 (0x80) is set, run-length packet;
				int len = pixelData.length - currentPixel;
				if(len > 0x80)
					len = 0x80;
				byte packetHeader = (byte)(len - 1); //(packetHeader & 0x7F) + 1;
				os.writeByte(packetHeader);

				for(int i = 0; i < len; i++) {
					Color col = new Color(pixelData[getRealPixelPosition(currentPixel++, height, width)]);
					os.writeByte(col.getBlue());
					os.writeByte(col.getGreen());
					os.writeByte(col.getRed());
				}
			}
		} catch (Exception e) {
			Out.fatalException(e);
		}
	}

	private static BNetIcon[] readIconsDotBni(File f) {
		try (BNetInputStream is = new BNetInputStream(new FileInputStream(f))) {
			Out.debug(IconsDotBniReader.class, "Reading " + f.getName());

			is.skip(4); //int headerSize = is.readDWord();
			int bniVersion = is.readWord();
			is.skip(2);	// Alignment Padding (unused)
			int numIcons = is.readDWord();
			is.skip(4);	//int dataOffset = is.readDWord();

			if(bniVersion != 1)
				throw new Exception("Unknown BNI version");

			Out.debug(IconsDotBniReader.class, "Reading " + numIcons + " icons in format " + bniVersion);

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
				Out.debug(IconsDotBniReader.class, icon.toString());
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
					int blue = is.readByte() & 0xFF;
					int green = is.readByte() & 0xFF;
					int red = is.readByte() & 0xFF;
					int col = new Color(red, green, blue).getRGB();
					for(int i = 0; i < len; i++)
						pixelData[getRealPixelPosition(currentPixel++, height, width)] = col;
				} else {
					for(int i = 0; i < len; i++) {
						int blue = is.readByte() & 0xFF;
						int green = is.readByte() & 0xFF;
						int red = is.readByte() & 0xFF;
						int col = new Color(red, green, blue).getRGB();
						pixelData[getRealPixelPosition(currentPixel++, height, width)] = col;
					}
				}
			}

			//Split up the big image in to individual images
			currentPixel = 0;

			if(util == null) {
				util = new JFrame("Icons");
				util.setLayout(null);
			}

			for(BNetIcon bni : icons) {
				// AWT
				bni.awt_image = util.createImage(new MemoryImageSource(bni.xSize, bni.ySize, pixelData, currentPixel, bni.xSize));
				bni.icon = new ImageIcon(bni.awt_image);

				// SWT
				try {
					int pos = 0;
					ImageData imageData = new ImageData(bni.xSize, bni.ySize, 32, new PaletteData(0xFF0000, 0x00FF00, 0x0000FF));
					for(int y = 0; y < bni.ySize; y++) {
						for(int x = 0; x < bni.xSize; x++) {
							imageData.setPixel(x, y, pixelData[currentPixel + pos++]);
						}
					}
					bni.image = new Image(null, imageData);
					currentPixel += pos;
				} catch(Throwable t) {
					//Out.exception(new Exception(t));
					currentPixel += bni.xSize * bni.ySize;
				}
			}

			return icons;
		} catch (Exception e) {
			Out.fatalException(e);
		}
		return null;
	}

}
