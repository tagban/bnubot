/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.util.mpq;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * http://www.zezula.net/en/mpq/techinfo.html
 * @author scotta
 */
public class MPQUtils {
	private static int[] crypt_table = null;
	private static void build_crypt_table() {
		if(crypt_table != null)
			return;

		crypt_table = new int[0x500];

		int r = 0x100001;
		for(int i=0;i<0x100;i++) {
			for(int j=0;j<5;j++) {
				r = (r*125+3) % 0x002AAAAB;
				int s1 = (r&0xFFFF) << 0x10;
				r = (r*125+3) % 0x002AAAAB;
				s1 |= (r&0xFFFF);
				crypt_table[i|(j<<8)]=s1;
			}
		}
	}

	public static int crc(String str, int hash_type) {
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

	public static void decrypt(int[] data, int key) {
		build_crypt_table();

		int seed = 0xEEEEEEEE;
		for(int i = 0; i < data.length; i++) {
			seed += crypt_table[0x400 + (key & 0xFF)];
			int ch = data[i];
			ch ^= (key + seed);
			data[i] = ch;

			key = ((~key << 0x15) + 0x11111111) | (key >>> 0x0B);
			seed = ch + seed + (seed << 5) + 3;
		}
	}

	public static void decrypt(byte[] data, int key) {
		build_crypt_table();

		ByteBuffer buf = ByteBuffer.wrap(data, 0, data.length);
		buf.order(ByteOrder.LITTLE_ENDIAN);

		int seed = 0xeeeeeeee;
		for(int i = 0; i < data.length>>>2; i++) {
			seed += crypt_table[0x400 + (key & 0xFF)];
			int ch = buf.getInt(i<<2);
			ch ^= (key + seed);
			buf.putInt(i<<2, ch);

			key = ((~key << 0x15) + 0x11111111) | (key >>> 0x0B);
			seed = ch + seed + (seed << 5) + 3;
		}
	}

	public static byte[] unpack(byte[] data, int out_size) throws IOException {
		int method = data[0];

		if(method == 2) {
			Inflater i = new Inflater();
			i.setInput(data, 1, data.length-1);

			byte[] out = new byte[out_size];
			try {
				int j = i.inflate(out);
				if(j != out_size)
					throw new IOException("Invalid out size " + j + " expecting " + out_size);
				if(!i.finished())
					throw new IOException("Unfinished");
			} catch (DataFormatException e) {
				throw new IOException(e.getMessage());
			}
			return out;
		}

		if((method & 0x08) != 0)
			data = Explode.explode(data, 1, data.length - 1, out_size);
		if((method & 0x01) != 0)
			throw new IllegalStateException("ExtWavUnp1");
		if((method & 0x40) != 0)
			throw new IllegalStateException("ExtWavUnp2");
		if((method & 0x80) != 0)
			throw new IllegalStateException("ExtWavUnp3");

		return data;
	}
}
