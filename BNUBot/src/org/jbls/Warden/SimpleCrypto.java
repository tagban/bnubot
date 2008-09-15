/**
 * This file is distributed under the GPL
 * $Id$
 */

package org.jbls.Warden;

/**
 * Implements a very simple crypto system used by Warden. I'm told it's RC4, but
 * I haven't bothered confirming.
 * @author iago
 */
public class SimpleCrypto {
	private byte[] key;

	/**
	 * Generates the key based on "base"
	 * @param base the <code>byte[]</code> to generate the key from
	 */
	public SimpleCrypto(byte[] base) {
		char val = 0;
		int i;
		int position = 0;
		byte temp;

		key = new byte[0x102];

		for (i = 0; i < 0x100; i++)
			key[i] = (byte) i;
		key[0x100] = 0;
		key[0x101] = 0;

		for (i = 1; i <= 0x40; i++) {
			val += key[(i * 4) - 4] + base[position++ % base.length];
			temp = key[(i * 4) - 4];
			key[(i * 4) - 4] = key[val & 0x0FF];
			key[val & 0x0FF] = temp;

			val += key[(i * 4) - 3] + base[position++ % base.length];
			temp = key[(i * 4) - 3];
			key[(i * 4) - 3] = key[val & 0x0FF];
			key[val & 0x0FF] = temp;

			val += key[(i * 4) - 2] + base[position++ % base.length];
			temp = key[(i * 4) - 2];
			key[(i * 4) - 2] = key[val & 0x0FF];
			key[val & 0x0FF] = temp;

			val += key[(i * 4) - 1] + base[position++ % base.length];
			temp = key[(i * 4) - 1];
			key[(i * 4) - 1] = key[val & 0x0FF];
			key[val & 0x0FF] = temp;
		}
	}

	public byte[] do_crypt(byte data) {
		return do_crypt(new byte[] { data });
	}

	/**
	 * Encrypts or decrypts.
	 * @param data the input <code>byte[]</code>
	 * @return the output <code>byte[]</code>
	 */
	public byte[] do_crypt(byte[] data) {
		int i;
		byte temp;

		for (i = 0; i < data.length; i++) {
			key[0x100]++;
			key[0x101] += key[key[0x100] & 0x0FF];
			temp = key[key[0x101] & 0x0FF];
			key[key[0x101] & 0x0FF] = key[key[0x100] & 0x0FF];
			key[key[0x100] & 0x0FF] = temp;

			data[i] = (byte) (data[i] ^ key[(key[key[0x101] & 0x0FF] + key[key[0x100] & 0x0FF]) & 0x0FF]);
		}

		return data;
	}

	/**
	 * More for debugging than anything.
	 * @return the <code>byte[]</code> key
	 */
	public byte[] getKey() {
		return key;
	}
}