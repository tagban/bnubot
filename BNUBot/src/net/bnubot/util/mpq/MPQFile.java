/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.util.mpq;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import net.bnubot.util.BNetInputStream;

/**
 * http://www.zezula.net/en/mpq/techinfo.html
 * @author scotta
 */
public class MPQFile implements MPQConstants {

	public static void main(String[] args) throws IOException {
		new MPQFile(new File("IX86Archimonde.mpq"));
		MPQFile mpq = new MPQFile(new File("bncache.dat"));
		mpq = new MPQFile(mpq.readFile("ver-IX86-2.mpq"));
		mpq.readFile("ver-IX86-2.dll");
	}

	private int getHashTablePosition(String str) {
		final int table_size = hash_table.length;

	    final int nHash = MPQUtils.crc(str, 0);
	    final int nHashA = MPQUtils.crc(str, 1);
	    final int nHashB = MPQUtils.crc(str, 2);
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

	private BNetInputStream is;
	private final int[] hash_table;
	private final int[] block_table;
	private final String[] file_names;

	public MPQFile(File file) throws IOException {
		this(new FileInputStream(file));
	}

	public MPQFile(InputStream is0) throws IOException {
		if(is0 == null)
			throw new NullPointerException();

		// Make sure mark support is available
		if(!is0.markSupported())
			is0 = new BufferedInputStream(is0);
		is = new BNetInputStream(is0);
		is.mark(is.available());

		int offset_mpq = 0;

		// Search for the MPQ header
		find_header: while(true) {
			if(is.available() < 4)
				throw new IOException("Invalid MPQ archive");

			int file_format = is.readDWord();
			switch(file_format) {
			//case ID_MPQ_SHUNT:
			//	throw new IllegalStateException("Not sure how to process MPQ SHUNT header");
			case ID_BN3:
			case ID_MPQ:
				// Found the archive header
				break find_header;
			default:
				// Keep searching
				offset_mpq += 4;
				break;
			}
		}

		// 32-byte header (already read 4 bytes)
		is.skip(4); // Unknown
		int archive_size = is.readDWord();
		is.skip(2); //short format_version = is.readWord();
		is.skip(2); //short block_size = is.readWord();
		final int offset_htbl = is.readDWord();
		final int offset_btbl = is.readDWord();
		final int count_htbl = is.readDWord() << 2;
		final int num_files = is.readDWord();
		final int count_btbl = num_files << 2;

		if(true) {
			// Jump to the beginning of the archive
			is.reset();
			is.skip(offset_mpq);

			// Read the entire archive
			byte[] data = new byte[archive_size];
			is.readFully(data);

			// Rebuild the InputStream with just the archive data
			is = new BNetInputStream(new ByteArrayInputStream(data));
			is.mark(archive_size);
		}

		// Jump to the hash table
		is.reset();
		is.skip(offset_htbl);

		// Read the hash table
		hash_table = new int[count_htbl];
		for(int i = 0; i < count_htbl; i++)
			hash_table[i] = is.readDWord();

		// Decrypt the hash table
		MPQUtils.decrypt(hash_table,MPQUtils.crc(HASH_TABLE,3),count_htbl);

		// Jump to the block table
		is.reset();
		is.skip(offset_btbl);

		// Read the block table
		block_table = new int[count_btbl];
		for(int i = 0; i < count_btbl; i++)
			block_table[i] = is.readDWord();

		// Decrypt the block table
		MPQUtils.decrypt(block_table,MPQUtils.crc(BLOCK_TABLE,3),count_btbl);

		// Try to figure out the file names from a list file
		file_names = new String[num_files];

		try {
			BufferedReader f = new BufferedReader(new InputStreamReader(readFile(LISTFILE)));
			while(true) {
				try {
					final String fn = f.readLine();
					if(fn == null)
						break;
					suggestFileName(fn);
				} catch(EOFException e) {
					break;
				}
			}
		} catch(FileNotFoundException e) {
			// No listfile
			System.out.println("Archive has no listfile");
		}
	}

	public int getNumFiles() {
		return file_names.length;
	}

	private void suggestFileName(String fileName) {
		int i = getHashTablePosition(fileName);
		if(i != -1)
			file_names[hash_table[i+3]] = fileName;
	}

	public InputStream readFile(String fileName) throws IOException {
		int i = getHashTablePosition(fileName);
		if(i == -1)
			throw new FileNotFoundException(fileName);

		file_names[hash_table[i+3]] = fileName;
		return readFile(hash_table[i+3]);
	}

	public InputStream readFile(int fileNum) throws IOException {
		int offset = block_table[fileNum<<2];
		int size_packed = block_table[(fileNum<<2)+1];
		int size_unpacked = block_table[(fileNum<<2)+2];
		int flags = block_table[(fileNum<<2)+3];

		if((flags & MPQ_FILE_EXISTS) != 0)
			System.out.println("WARNING: " + file_names[fileNum] + " was deleted!");

		System.out.println(file_names[fileNum] + ": " +
				size_packed + "/" + size_unpacked + " = " +
				(int)(100f*size_packed/size_unpacked) + "%");

		int crc_file = 0;
		if((flags & MPQ_FILE_ENCRYPTED) != 0) {
			// If file is coded, calculate its crc
			String fn = file_names[fileNum];
			if(fn != null) {
				// Calculate crc_file for identified file:
				int i = fn.indexOf('\\');
				if(i != -1)
					fn = fn.substring(i+1);

				// calculate crc_file (for Diablo I MPQs)
				crc_file = MPQUtils.crc(fn, 3);
				if((flags & MPQ_FILE_FIXSEED) != 0) {
					// calculate crc_file (for Starcraft MPQs)
					crc_file=(crc_file+offset)^size_unpacked;
				}

			} else {
				// calculate crc_file for not identified file:
				//crc_file=getUnknowCrc(entry);
				throw new IllegalStateException("Can't calculate CRC for unidentified files");
			}
		}

		is.reset();
		is.skip(offset);

		if((flags & (MPQ_FILE_IMPLODE | MPQ_FILE_COMPRESS)) != 0) {
			throw new IllegalStateException("Can't read packed files");
		}

		final byte buf[] = new byte[size_packed];
		is.readFully(buf);
		return new ByteArrayInputStream(buf);
	}
}
