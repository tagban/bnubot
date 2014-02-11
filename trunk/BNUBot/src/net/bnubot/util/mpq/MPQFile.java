/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.util.mpq;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import net.bnubot.util.BNetInputStream;
import net.bnubot.util.BNetOutputStream;

/**
 * http://www.zezula.net/en/mpq/techinfo.html
 * @author scotta
 */
public class MPQFile implements MPQConstants {
	/**
	 * Indicates a sub-file
	 */
	private static final String FILE_SEPERATOR = "\\";

	public static void main(String[] args) throws IOException {
//		test(new MPQFile(new File("bncache.dat")));
//		test(new MPQFile(new File("War3ROC_122a_123a_English.exe")));
		test(new MPQFile(new File("C:\\Users\\Scott\\Desktop\\SystemCheck_enUS.exe")));
	}

	private static void test(MPQFile mpq) {
		try {
			mpq.suggestFileNames(new FileInputStream(new File("LIST.TXT")));
		} catch(Exception e) {}
		try {
			mpq.readListFile();
		} catch(Exception e) {}
		for(int i = 0; i < mpq.file_names.length; i++) {
			int flags = mpq.block_table[(i<<2)+3];
			if((flags & MPQ_FILE_EXISTS) == 0)
				continue; // File was deleted

			try {
				MPQFile mpq2 = mpq.readMPQ(i);
				test(mpq2);
			} catch(Exception e) {
				if(mpq.file_names[i] == null)
					System.err.println(mpq.getFileName(i));
			}
		}
	}

	private final String fileName;
	private BNetInputStream is;
	private final int[] hash_table;
	private final int[] block_table;
	private final String[] file_names;

	public MPQFile(File file) throws IOException {
		this(new FileInputStream(file), file.getName());
	}

	private MPQFile(InputStream is0, String fileName) throws IOException {
		if(is0 == null)
			throw new NullPointerException();

		this.fileName = fileName;

		// Make sure mark support is available
		if(!is0.markSupported())
			is0 = new BufferedInputStream(is0);
		is = new BNetInputStream(is0);
		is.mark(Integer.MAX_VALUE);

		// Search for the MPQ header
		final int offset_mpq = findHeader();

		// Jump to the beginning of the archive
		is.reset();
		is.skip(offset_mpq);

		// 32-byte header
		is.skip(4); //int file_format = is.readDWord();
		is.skip(4); //int header_size = is.readDWord();
		int archive_size = is.readDWord();
		is.skip(2); //short format_version = is.readWord();
		is.skip(2); //short block_size = is.readWord();
		final int offset_htbl = is.readDWord();
		final int offset_btbl = is.readDWord();
		final int count_htbl = is.readDWord() << 2;
		final int num_files = is.readDWord();
		final int count_btbl = num_files << 2;

		// Jump to the beginning of the archive
		is.reset();
		is.skip(offset_mpq);

		// Read the entire archive
		byte[] data = new byte[archive_size];
		is.readFully(data);

		// Rebuild the InputStream with just the archive data
		is = new BNetInputStream(new ByteArrayInputStream(data));
		is.mark(archive_size);

		// Jump to the hash table
		is.reset();
		is.skip(offset_htbl);

		// Read the hash table
		hash_table = new int[count_htbl];
		for(int i = 0; i < count_htbl; i++)
			hash_table[i] = is.readDWord();

		// Decrypt the hash table
		MPQUtils.decrypt(hash_table,MPQUtils.crc(HASH_TABLE,3));

		// Jump to the block table
		is.reset();
		is.skip(offset_btbl);

		// Read the block table
		block_table = new int[count_btbl];
		for(int i = 0; i < count_btbl; i++)
			block_table[i] = is.readDWord();

		// Decrypt the block table
		MPQUtils.decrypt(block_table,MPQUtils.crc(BLOCK_TABLE,3));

		// Try to figure out the file names from a list file
		file_names = new String[num_files];
	}

	private int findHeader() throws IOException {
		int offset_mpq = 0;
		while(true) {
			if(offset_mpq > 0x200) {
				int rem = 0x200 - (offset_mpq & 0x1FF);
				if(is.available() < rem)
					throw new IOException("Invalid MPQ archive");
				if(rem != 0) {
					is.skipBytes(rem);
					offset_mpq += rem;
				}
			}

			if(is.available() < 4)
				throw new IOException("Invalid MPQ archive");

			int file_format = is.readDWord();
			switch(file_format) {
			case ID_MPQ_SHUNT:
				throw new IllegalStateException("Not sure how to process MPQ SHUNT header");
			case ID_BN3:
			case ID_MPQ:
				int header_size = is.readDWord();
				is.skipBytes(4); //int archive_size = is.readDWord();
				short format_version = is.readWord();
				is.skipBytes(2); //short block_size = is.readWord();

				if(((format_version == 0) && (header_size == 0x20))
				|| ((format_version == 1) && (header_size == 0x2C)))
					return offset_mpq;

				offset_mpq += 16;
				break;
			default:
				// Keep searching
				offset_mpq += 4;
				break;
			}
		}
	}

	public String getFileName(int fileNum) {
		if(file_names[fileNum] != null)
			return this.fileName + FILE_SEPERATOR + "[" + fileNum + "]" + file_names[fileNum];

		if(!listFileRead)
			try {
				readListFile();
				if(file_names[fileNum] != null)
					return this.fileName + FILE_SEPERATOR + "[" + fileNum + "]" + file_names[fileNum];
			} catch(IOException e) {}

		String fileName = "0000" + fileNum;
		fileName = fileName.substring(fileName.length() - 5);
		return this.fileName + FILE_SEPERATOR + "[" + fileNum + "]unknown\\unk" + fileName + ".xxx";
	}

	private boolean listFileRead = false;
	private void readListFile() throws IOException {
		suggestFileNames(readFile(LISTFILE));
		listFileRead = true;
	}

	private void suggestFileNames(InputStream is) {
		BufferedReader f = new BufferedReader(new InputStreamReader(is));
		while(true) {
			try {
				final String fn = f.readLine();
				if(fn == null)
					break;
				suggestFileName(fn);
			} catch(IOException e) {
				break;
			}
		}
	}

	private int getFileNumber(String str) {
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
	        && (hash_table[nHashPos + 1] == nHashB))
	            return hash_table[nHashPos+3];

	        // Increment the hash position
	        nHashPos++;
	        nHashPos %= table_size;
	        // We went all the way around the table and got back to where we started
	        if (nHashPos == nHashStart)
	            return -1;
	    }
	}

	private void suggestFileName(String fileName) {
		int i = getFileNumber(fileName);
		if(i != -1)
			file_names[i] = fileName;
	}

	public File writeFile(String fileName) throws IOException {
		// Read the file
		InputStream is = readFile(fileName);
		byte[] data = new byte[is.available()];
		is.read(data);

		// Write the file
		System.out.println("Writing " + fileName + ", " + data.length + " bytes");
		File f = new File(fileName.substring(fileName.lastIndexOf('\\')+1));
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(data);
		fos.close();

		// All done
		return f;
	}

	public InputStream readFile(String fileName) throws IOException {
		int i = getFileNumber(fileName);
		if(i == -1)
			throw new FileNotFoundException(fileName);

		file_names[i] = fileName;
		return readFile(i);
	}

	public MPQFile readMPQ(String fileName) throws IOException {
		int i = getFileNumber(fileName);
		if(i == -1)
			throw new FileNotFoundException(fileName);

		file_names[i] = fileName;
		return readMPQ(i);
	}

	public MPQFile readMPQ(int fileNum) throws IOException {
		return new MPQFile(readFile(fileNum), getFileName(fileNum));
	}

	public InputStream readFile(final int fileNum) throws IOException {
		final int offset = block_table[fileNum<<2];
		final int size_packed = block_table[(fileNum<<2)+1];
		final int size_unpacked = block_table[(fileNum<<2)+2];
		final int flags = block_table[(fileNum<<2)+3];

		final boolean f_imploded = ((flags & MPQ_FILE_IMPLODE) != 0);
		final boolean f_compressed = ((flags & MPQ_FILE_COMPRESS) != 0);
		final boolean f_encrypted = ((flags & MPQ_FILE_ENCRYPTED) != 0);
		final boolean f_fixseed = ((flags & MPQ_FILE_FIXSEED) != 0);
		final boolean f_single_unit = ((flags & MPQ_FILE_SINGLE_UNIT) != 0);
		final boolean f_dummy_file = ((flags & MPQ_FILE_DUMMY_FILE) != 0);
		final boolean f_has_extra = ((flags & MPQ_FILE_HAS_EXTRA) != 0);
		final boolean f_exists = ((flags & MPQ_FILE_EXISTS) != 0);

		String pretty_file_name = getFileName(fileNum);

		if(f_dummy_file)
			throw new IOException(pretty_file_name + " is a dummy file");

		if(!f_exists)
			throw new FileNotFoundException(pretty_file_name + " was deleted");

		System.out.println(pretty_file_name + ": " +
				size_packed + "/" + size_unpacked + " = " +
				(int)(100f*size_packed/size_unpacked) + "%");

		int crc_file = 0;
		if(f_encrypted) {
			// If file is encrypted, calculate its crc
			String fn = file_names[fileNum];
			if(fn != null) {
				// Calculate crc_file for identified file:
				int i = fn.lastIndexOf('\\');
				if(i != -1)
					fn = fn.substring(i+1);

				// calculate crc_file (for Diablo I MPQs)
				crc_file = MPQUtils.crc(fn, 3);
				if(f_fixseed) {
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

		try (
			ByteArrayOutputStream baos = new ByteArrayOutputStream(size_unpacked);
			BNetOutputStream os = new BNetOutputStream(baos);
		) {
			if(f_single_unit) {
				byte[] data = new byte[size_packed];
				is.readFully(data);
				if(f_encrypted)
					MPQUtils.decrypt(data, crc_file-1);

				if(data.length==size_unpacked) {
					// The block is unpacked
				} else {
					// Block is packed
					if(f_compressed) {
						// Multiple compressions are possible
						data = MPQUtils.unpack(data, size_unpacked);
					} else {
						// Just DCLib
						data = Explode.explode(data, 0, data.length, size_unpacked);
					}
				}
				os.write(data);
			} else if(f_imploded | f_compressed) {
				final int block_size = 0x1000;

				int num_blocks = ((size_unpacked-1)/block_size)+2;
				int header[] = new int[num_blocks + (f_has_extra ? 1 : 0)];
				for(int i = 0; i < header.length; i++)
					header[i] = is.readDWord();
				if(f_encrypted)
					MPQUtils.decrypt(header, crc_file-1);

				for(int i = 0; i < num_blocks-1; i++) {
					int length_read=header[i+1]-header[i];
					byte[] data = new byte[length_read];
					is.readFully(data);
					if(f_encrypted)
						MPQUtils.decrypt(data, crc_file++);

					final int out_size;
					if(i==num_blocks-2) {
						if((size_unpacked & 0xFFF) == 0) {
							// The last block could be either [0] or [block_size]
							out_size = size_unpacked-(block_size*i);
						} else {
							out_size = (size_unpacked & 0xFFF);
						}
					} else {
						out_size = block_size;
					}

					if(length_read==out_size) {
						// The block is unpacked
					} else {
						// Block is packed
						if(f_compressed) {
							// Multiple compressions are possible
							data = MPQUtils.unpack(data, out_size);
						} else {
							// Just DCLib
							data = Explode.explode(data, 0, data.length, out_size);
						}
					}
					os.write(data);
				}
			} else {
				// File is not compressed
				int block_size = f_encrypted ? 0x1000 : 0x60000;
				for(int pos=0; pos<size_packed; pos += block_size) {
					int length_read = block_size;
					if(length_read + pos > size_packed)
						length_read = size_packed % block_size;

					byte[] data = new byte[length_read];
					is.readFully(data);
					if(f_encrypted)
						MPQUtils.decrypt(data, crc_file++);
					os.write(data);
				}
			}
			return new ByteArrayInputStream(baos.toByteArray());
		}
	}

	@Override
	public String toString() {
		return fileName;
	}
}
