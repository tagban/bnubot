/**
 * This file is distributed under the GPL
 * $Id$
 */

package org.jbls.Warden;

import org.jbls.util.ByteFromIntArray;

/**
 * @author iago
 */
public class WardenRandom {
	int position = 0;
	byte[] random_data;
	byte[] randomSource1;
	byte[] randomSource2;

	public WardenRandom(byte[] seed) {
		int length1 = seed.length >>> 1;
		int length2 = seed.length - length1;

		byte[] seed1 = new byte[length1];
		byte[] seed2 = new byte[length2];

		for (int i = 0; i < length1; i++)
			seed1[i] = seed[i];
		for (int i = 0; i < length2; i++)
			seed2[i] = seed[i + length1];

		random_data = new byte[0x14];

		randomSource1 = ByteFromIntArray.LITTLEENDIAN.getByteArray(WardenSHA1
				.hash(seed1));
		randomSource2 = ByteFromIntArray.LITTLEENDIAN.getByteArray(WardenSHA1
				.hash(seed2));

		this.update();
		position = 0;
	}

	private void update() {
		WardenSHA1 sha1 = new WardenSHA1();
		sha1.update(this.randomSource1);
		sha1.update(this.random_data);
		sha1.update(this.randomSource2);
		this.random_data = ByteFromIntArray.LITTLEENDIAN.getByteArray(sha1
				.digest());
	}

	public byte getByte() {
		int i = this.position;
		byte value = this.random_data[i];

		i++;
		if (i >= 0x14) {
			i = 0;
			this.update();
		}
		this.position = i;

		return value;
	}

	public byte[] getBytes(int bytes) {
		byte[] buffer = new byte[bytes];

		for (int i = 0; i < bytes; i++)
			buffer[i] = this.getByte();

		return buffer;
	}
}