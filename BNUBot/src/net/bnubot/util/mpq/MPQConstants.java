/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.util.mpq;

/**
 * @author scotta
 */
public interface MPQConstants {
	/**
	 * File header for MPQs
	 */
	public static final int ID_MPQ = 0x1A51504D;
	/**
	 * File header shunt for MPQs
	 */
	public static final int ID_MPQ_SHUNT = 0x1B51504D;
	/**
	 * File header for bncache.dat
	 */
	public static final int ID_BN3 = 0x1A334E42;

	/**
	 * Key used to decrypt the hash table
	 */
	public static final String HASH_TABLE = "(hash table)";
	/**
	 * Key used to decrypt the block table
	 */
	public static final String BLOCK_TABLE = "(block table)";

	/**
	 * Some MPQs contain a listfile containing archived filenames
	 */
	public static final String LISTFILE = "(listfile)";

	/**
	 * File is compressed using PKWARE Data compression library
	 */
	public static final int MPQ_FILE_IMPLODE = 0x00000100;
	/**
	 * File is compressed using combination of compression methods
	 */
	public static final int MPQ_FILE_COMPRESS = 0x00000200;
	/**
	 * The file is encrypted
	 */
	public static final int MPQ_FILE_ENCRYPTED = 0x00010000;
	/**
	 * The decryption key for the file is altered according to the position of the file in the archive
	 */
	public static final int MPQ_FILE_FIXSEED = 0x00020000;
	/**
	 * Instead of being divided to 0x1000-bytes blocks, the file is stored as single unit
	 */
	public static final int MPQ_FILE_SINGLE_UNIT = 0x01000000;
	/**
	 * The file has length of 0 or 1 byte and its name is a hash
	 */
	public static final int MPQ_FILE_DUMMY_FILE = 0x02000000;
	/**
	 * The file has extra data appended after regular data. Must be a compressed file
	 */
	public static final int MPQ_FILE_HAS_EXTRA = 0x04000000;
	/**
	 * Set if file exists, reset when the file was deleted
	 */
	public static final int MPQ_FILE_EXISTS = 0x80000000;
}
