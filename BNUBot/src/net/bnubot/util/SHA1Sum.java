/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

import net.bnubot.logging.Out;

/**
 * @author scotta
 */
public class SHA1Sum {
	private final byte[] sha1sum;
	private String display = null;

	public SHA1Sum(String hexStr) throws Exception {
		if(!hexStr.matches("[0-9a-fA-F]{40}"))
			throw new Exception("Invalid format: " + hexStr);
		display = hexStr.toLowerCase();
		sha1sum = new byte[20];
		for(int i = 0; i < 20; i++) {
			int pos = i << 1;
			sha1sum[i] = (byte) Integer.parseInt(hexStr.substring(pos, pos+2), 16);
		}
	}

	public SHA1Sum(byte[] bytes) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA1");
		digest.update(bytes, 0, bytes.length);
		sha1sum = digest.digest();
	}

	public SHA1Sum(File f) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA1");
		try (InputStream is = new FileInputStream(f)) {
			byte[] buffer = new byte[8192];
			do {
				int read = is.read(buffer);
				if(read <= 0)
					break;
				digest.update(buffer, 0, read);
			} while(true);
		}
		sha1sum = digest.digest();

		if(Out.isDebug(SHA1Sum.class))
			Out.debugAlways(getClass(), f.getName() + ": " + toString());
	}

	private static String hexChr(int b) {
		return Integer.toHexString(b & 0xF);
	}

	private static String toHex(int b) {
		return hexChr((b & 0xF0) >> 4) + hexChr(b & 0x0F);
	}

	@Override
	public String toString() {
		if(display == null) {
			display = "";
			for(byte b : sha1sum)
				display += toHex(b);
		}
		return display;
	}

	public byte[] getSum() {
		return sha1sum;
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof SHA1Sum))
			return false;

		byte[] obj_sha1sum = ((SHA1Sum)obj).sha1sum;
		if(sha1sum.length != obj_sha1sum.length)
			return false;

		for(int i = 0; i < sha1sum.length; i++)
			if(sha1sum[i] != obj_sha1sum[i])
				return false;

		return true;
	}
}
