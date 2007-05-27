package core.bot.gui;

import java.io.*;

import core.BNetInputStream;

public class IconsDotBniReader {
	
	public IconsDotBniReader(File f) {
		try {
			BNetInputStream is = new BNetInputStream(new DataInputStream(new FileInputStream(f)));
			int headerSize = is.readDWord();
			int bniVersion = is.readWord();
			is.readWord();	// Alignment Padding (unused)
			int numIcons = is.readDWord();
			int dataOffset = is.readDWord();
			
			System.out.println("Reading " + numIcons + " icons in format " + bniVersion + " from offset " + dataOffset);
			
			for(int i = 0; i < numIcons; i++) {
				int flags = is.readDWord();
				int xSize = is.readDWord();
				int ySize = is.readDWord();
				int products[] = new int[32];
				for(int j = 0; j < 32; j++)
					products[j] = is.readDWord();
				
				//Image in targa format
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
	}

}
