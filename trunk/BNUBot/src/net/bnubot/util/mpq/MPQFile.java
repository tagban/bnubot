/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.util.mpq;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import net.bnubot.util.BNetInputStream;

/**
 * http://www.zezula.net/en/mpq/techinfo.html
 * @author scotta
 */
public class MPQFile {
	private static final String HASH_TABLE = "(hash table)";
	private static final String BLOCK_TABLE = "(block table)";

	public static void main(String[] args) throws IOException {
		new MPQFile(new File("IX86Archimonde.mpq"));
		new MPQFile(new File("bncache.dat"));
	}

	private static int[] crypt_table = null;
	private static void build_crypt_table() {
		if(crypt_table != null)
			return;

		crypt_table = new int[0x500];

		int r = 0x100001;
		for(int i=0;i<0x100;i++) {
			for(int j=0;j<5;j++) {
				r = (r*125+3) % 0x002AAAAB;
				int s1 = (r & 0xFFFF) << 0x10;
				r = (r*125+3) % 0x002AAAAB;
				s1 = s1 | (r&0xFFFF);
				crypt_table[i+0x100*j]=s1;
			}
		}
	}

	private static int crc(String str, int hash_type) {
		final int hash_offset = hash_type<<8;
		build_crypt_table();

		int seed1 = 0x7FED7FED, seed2 = 0xEEEEEEEE;

		for(byte b : str.getBytes()) {
			// toUpper()
			if(b>0x60 && b<0x7B)
				b-=0x20;

			seed1 = crypt_table[hash_offset+b]^(seed1+seed2);
			seed2 += seed1+(seed2<<5)+b+3;
		}
		return seed1;
	}

	private static void decrypt(int[] data, int key, int length) {
		build_crypt_table();

		int seed = 0xEEEEEEEE;
		for(int i = 0; i < (length); i++) {
			seed += crypt_table[0x400 + (key & 0xFF)];
			int ch = data[i];
			ch ^= (key + seed);
			data[i] = ch;

			key = ((~key << 0x15) + 0x11111111) | (key >>> 0x0B);
			seed = ch + seed + (seed << 5) + 3;
		}
	}

	private int getHashTablePosition(String str, int[] hash_table) {
		final int table_size = hash_table.length;

	    final int nHash = crc(str, 0);
	    final int nHashA = crc(str, 1);
	    final int nHashB = crc(str, 2);
		int nHashStart = nHash % table_size;
		if(nHashStart < 0)
			nHashStart += table_size;
		int nHashPos = nHashStart;

	    while(true) {
	        if((hash_table[nHashPos] == nHashA)
	        && (hash_table[(nHashPos + 1) % table_size] == nHashB))
	            return nHashPos;

	        // Increment the hash position
	        nHashPos = (nHashPos + 1) % table_size;
	        // We went all the way around the table and got back to where we started
	        if (nHashPos == nHashStart)
	            return -1;
	    }
	}

	final int[] hash_table;
	final int[] block_table;
	final String[] file_names;

	public MPQFile(File file) throws IOException {
		if(file == null)
			throw new NullPointerException();

		// Read the file
		BNetInputStream is = new BNetInputStream(new BufferedInputStream(new FileInputStream(file)));
		is.mark(0x100000);

		// 32-byte header
		int file_format = is.readDWord();
		if((file_format != 0x1A334E42) // "BN3\x1A" for bncache.dat
		&& (file_format != 0x1A51504D)) // "MPQ\x1A" for MPQs
			throw new IOException("Invalid file");
		is.skip(4); // Unknown
		/*int file_length =*/ is.readDWord();
		is.skip(4); // Unknown
		final int offset_htbl = is.readDWord();
		final int offset_btbl = is.readDWord();
		final int count_htbl = is.readDWord() << 2;
		final int num_files = is.readDWord();
		final int count_btbl = num_files << 2;

		// Jump to the hash table
		is.reset();
		is.skip(offset_htbl);

		// Read the hash table
		hash_table = new int[count_htbl];
		for(int i = 0; i < count_htbl; i++)
			hash_table[i] = is.readDWord();

		// Decrypt the hash table
		decrypt(hash_table,crc(HASH_TABLE,3),count_htbl);

		// Jump to the block table
		is.reset();
		is.skip(offset_btbl);

		// Read the block table
		block_table = new int[count_btbl];
		for(int i = 0; i < count_btbl; i++)
			block_table[i] = is.readDWord();

		// Decrypt the block table
		decrypt(block_table,crc(BLOCK_TABLE,3),count_btbl);

		// Try to figure out the file names from a list file
		Collection<String> suggested_file_names = new HashSet<String>();
		suggested_file_names.add("(listfile)");

		BufferedReader f = new BufferedReader(new FileReader(new File("listfile.txt")));
		while(true) {
			final String fn;
			try {
				fn = f.readLine();
				if(fn == null)
					break;
			} catch(EOFException e) {
				break;
			}

			if(!suggested_file_names.contains(fn))
				suggested_file_names.add(fn);
		}

		file_names = new String[num_files];
		for(String fn : suggested_file_names) {
			int i = getHashTablePosition(fn, hash_table);
			if(i != -1)
				file_names[hash_table[i+3]] = fn;
		}

		int j = 1;
		for(int i = 0; i < num_files; i++) {
			if(file_names[i] == null) {
				file_names[i] = "unknow\\unk" + j + ".xxx";
				j++;
			}
		}

		int i = 0;
		for(String fn : file_names) {
			System.out.print(fn);

			//int offset = block_table[i<<2];
			int size_packed = block_table[(i<<2)+1];
			int size_unpacked = block_table[(i<<2)+2];
			int flags = block_table[(i<<2)+3];
			i++;

			//System.out.print(" flags=0x" + Integer.toHexString(flags));
			if((flags & 0x30000) != 0)
				System.out.print(" coded");
			if((flags & 0x300) != 0)
				System.out.print(" packed=" + (int)(100f*size_packed/size_unpacked) + "%");

			System.out.println();
		}
	}
}
