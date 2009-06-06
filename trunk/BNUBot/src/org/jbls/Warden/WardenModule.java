/**
 * This file is distributed under the GPL
 * $Id$
 */

package org.jbls.Warden;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.zip.Inflater;

import org.jbls.util.BigIntegerEx;
import org.jbls.util.Buffer;
import org.jbls.util.IntFromByteArray;
import org.jbls.util.PadString;

/**
 * @author iago
 */
public class WardenModule {
	private int currentLength = 0;
	private int compressedSize = 0;
	private byte[] md5Hash = null;
	private byte[] decryptionSeed = null;
	private String name = null;
	private final File exe;

	public WardenModule(int size, byte[] md5, byte[] decryptor, String game_exe) {
		this.compressedSize = size;
		this.md5Hash = md5;
		this.decryptionSeed = decryptor;
		this.exe = new File(game_exe);
		StringBuffer nametmp = new StringBuffer();
		for (int x = 0; x < 0x10; x++)
			nametmp.append(PadString.padHex(md5[x], 2));
		name = nametmp.toString();
		saveFile(name + ".decr", decryptor);

		if(!exe.exists())
			throw new IllegalStateException();
	}

	public Buffer handleRequest(Buffer in) throws IOException {
		in.removeByte();
		Buffer ret = new Buffer();
		int checksum = 0;
		while (in.size() >= 7) {
			/*int command =*/ in.removeWord();
			int address = in.removeDWord();
			int length = in.removeByte();
//			if(Out.isDebug())
//				Out.debug(getClass(), "Command: 0x" + PadString.padHex(command, 4)
//						+ ", " + "Address: 0x" + PadString.padHex(address, 8)
//						+ ", " + "Length: " + length);
			ret.addByte((byte) 0);
			ret.addBytes(getFileData(address - 0x400000, length));
			switch (address) {
			case 0x00497FB0:
				checksum += 1;
				break;
			case 0x0049C33D:
				checksum += 2;
				break;
			case 0x004A2FF7:
				checksum += 3;
				break;
			}
			checksum *= 10;
		}
		ret.addDWord(checksum / 10);
		return ret;
	}

	private byte[] getFileData(int i, int length) throws IOException {
		InputStream in = new FileInputStream(exe);
		in.skip(i);
		byte[] ret = new byte[length];
		in.read(ret);
		in.close();
		return ret;
	}

	public void setup() {
		if (fileExists(name + ".mod")) {
			if (md5verify()) {
				try {
					SimpleCrypto crypt = new SimpleCrypto(decryptionSeed);
					byte[] data = crypt.do_crypt(readFile(name + ".mod"));
					saveFile(name + ".tmp1.bin", data);
				} catch (Exception e) {
					System.out.println("Failed to decode .mod file: "
							+ e.toString());
				}
			}
		}
		if (fileExists(name + ".tmp1.bin")) {
			if (verifySignature()) {
				try {
					byte[] compressed = readFile(name + ".tmp1.bin");
					Inflater zip = new Inflater();
					zip.setInput(compressed, 4, compressed.length - 0x108);
					int len = ((compressed[0] << 0) & 0x000000FF)
							| ((compressed[1] << 8) & 0x0000FF00)
							| ((compressed[2] << 16) & 0x00FF0000)
							| ((compressed[3] << 24) & 0xFF000000);
					byte[] uncompressed = new byte[len];
					zip.inflate(uncompressed);
					saveFile(name + ".tmp2.bin", uncompressed);
				} catch (Exception e) {
					System.out.println("Failed to inflate .tmp1.bin file: "
							+ e.toString());
				}
			}
		}

		if (fileExists(name + ".tmp2.bin")) {
			try {
				byte[] raw = readFile(name + ".tmp2.bin");
				byte[] preped = prepareModule(raw, 0x40000000);
				saveFile(name + ".bin", preped);
			} catch (Exception e) {
				System.out.println("Failed to prepare .tmp2.bin file: "
						+ e.toString());
			}
		}
	}

	private byte[] prepareModule(byte[] original, int base_address) {
		IntFromByteArray ifba = IntFromByteArray.LITTLEENDIAN;
		int counter;

		int length = ifba.getInteger(original, 0);
		byte[] module = new byte[length];

		System.out.println("Allocated " + length + " (0x"
				+ PadString.padHex(length, 4) + ") bytes for new module.\n");

		/* Copy 40 bytes from the original module to the new one. */
		System.arraycopy(original, 0, module, 0, 40);

		int source_location = 0x28 + (ifba.getInteger(module, 0x24) * 12);
		int destination_location = ifba.getInteger(original, 0x28);
		int limit = ifba.getInteger(original, 0);

		boolean skip = false;

		System.out.println("Copying code sections to module.");
		while (destination_location < limit) {
			int count = ((original[source_location] & 0x0FF) << 0)
					| ((original[source_location + 1] & 0x0FF) << 8);

			source_location += 2;

			if (!skip) {
				System.arraycopy(original, source_location, module,
						destination_location, count);
				source_location += count;
			}
			skip = !skip;
			destination_location += count;
		}

		System.out.println("Adjusting references to global variables...");
		source_location = ifba.getInteger(original, 8);
		destination_location = 0;

		counter = 0;
		while (counter < ifba.getInteger(module, 0x0c)) {
			if (module[source_location] < 0) {
				/* This code is never used, so I am not 100% sure that it works. */
				destination_location = ((module[source_location + 0] & 0x07F) << 24)
						| ((module[source_location + 1] & 0x0FF) << 16)
						| ((module[source_location + 2] & 0x0FF) << 8)
						| ((module[source_location + 3] & 0x0FF) << 0);
				source_location += 4;
			} else {
				destination_location = destination_location
						+ (module[source_location + 1] & 0x0FF)
						+ (module[source_location] << 8);
				source_location += 2;
			}
			// System.out.println("Offset 0x" +
			// PadString.padHex(destination_location, 4) +
			// " (was 0x" + PadString.padHex(ifba.getInteger(module,
			// destination_location), 8) + ")");
			ifba.insertInteger(module, destination_location, ifba.getInteger(
					module, destination_location)
					+ base_address);
			counter++;
		}

		System.out.println("Updating API library references...");
		counter = 0;
		limit = ifba.getInteger(module, 0x20);
		String library;

		for (counter = 0; counter < limit; counter++) {
			int proc_start = ifba.getInteger(module, 0x1c) + (counter * 8);
			library = getNTString(module, ifba.getInteger(module, proc_start));
			int proc_offset = ifba.getInteger(module, proc_start + 4);

			while (ifba.getInteger(module, proc_offset) != 0) {
				int proc = ifba.getInteger(module, proc_offset);
				int addr = -1 /* Modules.ERROR */;

				if (proc > 0) {
					String strProc = getNTString(module, proc);
					addr = -1; /* Modules.get(library, strProc); */

					if (addr != -1 /* Modules.ERROR */)
						System.out.println("Module " + library + "!" + strProc
								+ " found @ 0x" + PadString.padHex(addr, 8));
				} else {
					proc = proc & 0x7FFFFFFF;
					System.out.println("Proc: ord(0x"
							+ PadString.padHex(proc, 8) + ")");
				}
				ifba.insertInteger(module, proc_offset, addr); /*
																 * TODO: Fix
																 * this.
																 */
				/*
				 * Note: real code increments [ebx+8] here, which is used for
				 * unloading the libraries.
				 */

				proc_offset += 4;
			}
		}

		return module;
	}

	public void reset() {
		deleteFile(name + ".mod");
		currentLength = 0;
	}

	public boolean md5verify() {
		if (!fileExists(name + ".mod"))
			return false;
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			byte[] data = readFile(name + ".mod");
			byte[] results = md5.digest(data);
			for (int x = 0; x < 0x10; x++)
				if (results[x] != md5Hash[x])
					return false;
			return true;
		} catch (Exception e) {
			System.out.println("md5Verify failed: " + e.toString());
		}
		return false;
	}

	public boolean verifySignature() {
		try {
			byte[] data = readFile(name + ".tmp1.bin");
			if (data == null)
				return false;

			if (data[data.length - 0x104] != 'N'
					|| data[data.length - 0x103] != 'G'
					|| data[data.length - 0x102] != 'I'
					|| data[data.length - 0x101] != 'S')
				return false;

			byte[] signature = new byte[0x100];
			byte[] module = new byte[data.length - 0x104];
			System.arraycopy(data, data.length - 0x100, signature, 0, 0x100);
			System.arraycopy(data, 0, module, 0, data.length - 0x104);

			BigIntegerEx power = new BigIntegerEx(BigIntegerEx.LITTLE_ENDIAN,
					new byte[] { 0x01, 0x00, 0x01, 0x00 });
			BigIntegerEx mod = new BigIntegerEx(BigIntegerEx.LITTLE_ENDIAN,
					new byte[] { (byte) 0x6B, (byte) 0xCE, (byte) 0xF5,
							(byte) 0x2D, (byte) 0x2A, (byte) 0x7D, (byte) 0x7A,
							(byte) 0x67, (byte) 0x21, (byte) 0x21, (byte) 0x84,
							(byte) 0xC9, (byte) 0xBC, (byte) 0x25, (byte) 0xC7,
							(byte) 0xBC, (byte) 0xDF, (byte) 0x3D, (byte) 0x8F,
							(byte) 0xD9, (byte) 0x47, (byte) 0xBC, (byte) 0x45,
							(byte) 0x48, (byte) 0x8B, (byte) 0x22, (byte) 0x85,
							(byte) 0x3B, (byte) 0xC5, (byte) 0xC1, (byte) 0xF4,
							(byte) 0xF5, (byte) 0x3C, (byte) 0x0C, (byte) 0x49,
							(byte) 0xBB, (byte) 0x56, (byte) 0xE0, (byte) 0x3D,
							(byte) 0xBC, (byte) 0xA2, (byte) 0xD2, (byte) 0x35,
							(byte) 0xC1, (byte) 0xF0, (byte) 0x74, (byte) 0x2E,
							(byte) 0x15, (byte) 0x5A, (byte) 0x06, (byte) 0x8A,
							(byte) 0x68, (byte) 0x01, (byte) 0x9E, (byte) 0x60,
							(byte) 0x17, (byte) 0x70, (byte) 0x8B, (byte) 0xBD,
							(byte) 0xF8, (byte) 0xD5, (byte) 0xF9, (byte) 0x3A,
							(byte) 0xD3, (byte) 0x25, (byte) 0xB2, (byte) 0x66,
							(byte) 0x92, (byte) 0xBA, (byte) 0x43, (byte) 0x8A,
							(byte) 0x81, (byte) 0x52, (byte) 0x0F, (byte) 0x64,
							(byte) 0x98, (byte) 0xFF, (byte) 0x60, (byte) 0x37,
							(byte) 0xAF, (byte) 0xB4, (byte) 0x11, (byte) 0x8C,
							(byte) 0xF9, (byte) 0x2E, (byte) 0xC5, (byte) 0xEE,
							(byte) 0xCA, (byte) 0xB4, (byte) 0x41, (byte) 0x60,
							(byte) 0x3C, (byte) 0x7D, (byte) 0x02, (byte) 0xAF,
							(byte) 0xA1, (byte) 0x2B, (byte) 0x9B, (byte) 0x22,
							(byte) 0x4B, (byte) 0x3B, (byte) 0xFC, (byte) 0xD2,
							(byte) 0x5D, (byte) 0x73, (byte) 0xE9, (byte) 0x29,
							(byte) 0x34, (byte) 0x91, (byte) 0x85, (byte) 0x93,
							(byte) 0x4C, (byte) 0xBE, (byte) 0xBE, (byte) 0x73,
							(byte) 0xA9, (byte) 0xD2, (byte) 0x3B, (byte) 0x27,
							(byte) 0x7A, (byte) 0x47, (byte) 0x76, (byte) 0xEC,
							(byte) 0xB0, (byte) 0x28, (byte) 0xC9, (byte) 0xC1,
							(byte) 0xDA, (byte) 0xEE, (byte) 0xAA, (byte) 0xB3,
							(byte) 0x96, (byte) 0x9C, (byte) 0x1E, (byte) 0xF5,
							(byte) 0x6B, (byte) 0xF6, (byte) 0x64, (byte) 0xD8,
							(byte) 0x94, (byte) 0x2E, (byte) 0xF1, (byte) 0xF7,
							(byte) 0x14, (byte) 0x5F, (byte) 0xA0, (byte) 0xF1,
							(byte) 0xA3, (byte) 0xB9, (byte) 0xB1, (byte) 0xAA,
							(byte) 0x58, (byte) 0x97, (byte) 0xDC, (byte) 0x09,
							(byte) 0x17, (byte) 0x0C, (byte) 0x04, (byte) 0xD3,
							(byte) 0x8E, (byte) 0x02, (byte) 0x2C, (byte) 0x83,
							(byte) 0x8A, (byte) 0xD6, (byte) 0xAF, (byte) 0x7C,
							(byte) 0xFE, (byte) 0x83, (byte) 0x33, (byte) 0xC6,
							(byte) 0xA8, (byte) 0xC3, (byte) 0x84, (byte) 0xEF,
							(byte) 0x29, (byte) 0x06, (byte) 0xA9, (byte) 0xB7,
							(byte) 0x2D, (byte) 0x06, (byte) 0x0B, (byte) 0x0D,
							(byte) 0x6F, (byte) 0x70, (byte) 0x9E, (byte) 0x34,
							(byte) 0xA6, (byte) 0xC7, (byte) 0x31, (byte) 0xBE,
							(byte) 0x56, (byte) 0xDE, (byte) 0xDD, (byte) 0x02,
							(byte) 0x92, (byte) 0xF8, (byte) 0xA0, (byte) 0x58,
							(byte) 0x0B, (byte) 0xFC, (byte) 0xFA, (byte) 0xBA,
							(byte) 0x49, (byte) 0xB4, (byte) 0x48, (byte) 0xDB,
							(byte) 0xEC, (byte) 0x25, (byte) 0xF3, (byte) 0x18,
							(byte) 0x8F, (byte) 0x2D, (byte) 0xB3, (byte) 0xC0,
							(byte) 0xB8, (byte) 0xDD, (byte) 0xBC, (byte) 0xD6,
							(byte) 0xAA, (byte) 0xA6, (byte) 0xDB, (byte) 0x6F,
							(byte) 0x7D, (byte) 0x7D, (byte) 0x25, (byte) 0xA6,
							(byte) 0xCD, (byte) 0x39, (byte) 0x6D, (byte) 0xDA,
							(byte) 0x76, (byte) 0x0C, (byte) 0x79, (byte) 0xBF,
							(byte) 0x48, (byte) 0x25, (byte) 0xFC, (byte) 0x2D,
							(byte) 0xC5, (byte) 0xFA, (byte) 0x53, (byte) 0x9B,
							(byte) 0x4D, (byte) 0x60, (byte) 0xF4, (byte) 0xEF,
							(byte) 0xC7, (byte) 0xEA, (byte) 0xAC, (byte) 0xA1,
							(byte) 0x7B, (byte) 0x03, (byte) 0xF4, (byte) 0xAF,
							(byte) 0xC7 });

			byte[] result = new BigIntegerEx(BigIntegerEx.LITTLE_ENDIAN,
					signature).modPow(power, mod).toByteArray();

			byte[] digest;
			byte[] properResult = new byte[0x100];

			/* Fill the proper result with 0xBB */
			for (int i = 0; i < properResult.length; i++)
				properResult[i] = (byte) 0xBB;

			/* Do a SHA1 of the data and the string (for some reason). */
			MessageDigest md = MessageDigest.getInstance("SHA1");
			md.update(module);
			md.update("MAIEV.MOD".getBytes());
			digest = md.digest();

			/* Copy the digest over the proper result. */
			System.arraycopy(digest, 0, properResult, 0, digest.length);

			/* Finally, check the array against the signature. */
			for (int i = 0; i < result.length; i++)
				if (result[i] != properResult[i])
					return false;

			return true;
		} catch (Exception e) {
			System.out.println("Failed to verify signature: " + e.toString());
		}
		return false;
	}

	public void savePart(byte[] data, int length) {
		try {
			checkWardenFolder();
			FileOutputStream out = new FileOutputStream("warden/" + name + ".mod",
					(currentLength == 0 ? false : true));
			out.write(data);
			out.close();
		} catch (Exception e) {
			System.out
					.println("Failed to save module segment: " + e.toString());
		}
		currentLength += length;
	}

	private void checkWardenFolder() {
		File folder = new File("warden");
		if(!folder.exists())
			folder.mkdir();
		if(!folder.isDirectory())
			throw new IllegalStateException("Warden must be a folder");
	}

	public boolean downloadComplete() {
		return currentLength == compressedSize;
	}

	public boolean alreadyExists() {
		if (!md5verify())
			return false;
		return true;
	}

	private boolean fileExists(String file) {
		file = "warden/" + file;
		return (new File(file)).exists();
	}

	private void deleteFile(String file) {
		try {
			file = "warden/" + file;
			File f = new File(file);
			if (f.exists())
				f.delete();
		} catch (Exception e) {
			System.out.println("Failed to delete file: " + file + ": "
					+ e.toString());
		}
	}

	public byte[] readFile(String path) throws IOException {
		path = "warden/" + path;
		File file = new File(path);
		if (!file.exists())
			return null;
		byte[] ret = new byte[(int) file.length()];
		InputStream in = new FileInputStream(file);
		in.read(ret);
		in.close();
		return ret;
	}

	public void saveFile(String file, byte[] data) {
		try {
			checkWardenFolder();
			file = "warden/" + file;
			FileOutputStream out = new FileOutputStream(file);
			out.write(data);
			out.close();
		} catch (Exception e) {
			System.out.println("Failed to save file: " + file + ": "
					+ e.toString());
		}
	}

	private String getNTString(byte[] data, int offset) {
		StringBuffer s = new StringBuffer();
		while (data[offset] != (byte) 0x00) {
			s.append(data[offset]);
			offset++;
		}

		return s.toString();
	}

	public int getSize() {
		return compressedSize;
	}

	public String getName() {
		return name;
	}

	public String getSeed() {
		StringBuffer s = new StringBuffer();

		for(byte x : decryptionSeed)
			s.append(PadString.padHex(x, 2));

		return s.toString();
	}
}