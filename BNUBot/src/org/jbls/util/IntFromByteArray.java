/**
 * $Id$
 */
/*
 * IntFromByteArray.java
 *
 * Created on May 21, 2004, 12:35 PM
 */

package org.jbls.util;

import java.security.InvalidParameterException;

/**
 * This is a class to take care of inserting or getting the value of an int in
 * an array of bytes.
 *
 * @author iago
 */
public class IntFromByteArray {
	private final boolean littleEndian;

	public static final IntFromByteArray LITTLEENDIAN = new IntFromByteArray(
			true);
	public static final IntFromByteArray BIGENDIAN = new IntFromByteArray(false);

	public IntFromByteArray(boolean littleEndian) {
		this.littleEndian = littleEndian;
	}

	public int getInteger(byte[] array, int location) {
		if ((location + 3) >= array.length)
			throw new ArrayIndexOutOfBoundsException("location = " + location
					+ ", number of bytes = " + array.length
					+ " (note: 4 available bytes are needed)");

		int retVal = 0;

		// reverse the byte to simulate little endian
		if (littleEndian) {
			retVal = retVal | ((array[location++] << 0) & 0x000000FF);
			retVal = retVal | ((array[location++] << 8) & 0x0000FF00);
			retVal = retVal | ((array[location++] << 16) & 0x00FF0000);
			retVal = retVal | ((array[location++] << 24) & 0xFF000000);
		} else {
			retVal = retVal | ((array[location++] << 24) & 0xFF000000);
			retVal = retVal | ((array[location++] << 16) & 0x00FF0000);
			retVal = retVal | ((array[location++] << 8) & 0x0000FF00);
			retVal = retVal | ((array[location++] << 0) & 0x000000FF);
		}

		return retVal;
	}

	/**
	 * This function is used to insert the byte into a specified spot in an int
	 * array. This is used to simulate pointers used in C++. Note that this
	 * works in little endian only.
	 *
	 * @param array
	 *            The buffer to insert the int into.
	 * @param b
	 *            The byte we're inserting.
	 * @param location
	 *            The location (which byte) we're inserting it into.
	 * @return The new array - this is returned for convenience only.
	 */
	public byte[] insertInteger(byte[] array, int location, int b) {
		if (location + 3 >= array.length)
			throw new ArrayIndexOutOfBoundsException("location = " + location
					+ ", length = " + array.length
					+ " - note that we need 4 bytes to insert an int");

		if (littleEndian) {
			array[location++] = (byte) ((b & 0x000000FF) >> 0);
			array[location++] = (byte) ((b & 0x0000FF00) >> 8);
			array[location++] = (byte) ((b & 0x00FF0000) >> 16);
			array[location++] = (byte) ((b & 0xFF000000) >> 24);
		} else {
			array[location++] = (byte) ((b & 0xFF000000) >> 24);
			array[location++] = (byte) ((b & 0x00FF0000) >> 16);
			array[location++] = (byte) ((b & 0x0000FF00) >> 8);
			array[location++] = (byte) ((b & 0x000000FF) >> 0);
		}

		return array;
	}

	/**
	 * @param array the input <code>byte[]</code>
	 * @return the transformed array as an <code>int[]</code>
	 * @throws InvalidParameterException if array is not a multiple of 4
	 */
	public int[] getIntArray(byte[] array) throws InvalidParameterException {
		if(array.length % 4 != 0)
			throw new InvalidParameterException("array.length must be a multiple of 4");
		int[] newArray = new int[array.length / 4];

		int pos = 0;
		for (int i = 0; i < newArray.length; i++) {
			if (littleEndian) {
				newArray[i] |= ((array[pos++] << 0) & 0x000000FF);
				newArray[i] |= ((array[pos++] << 8) & 0x0000FF00);
				newArray[i] |= ((array[pos++] << 16) & 0x00FF0000);
				newArray[i] |= ((array[pos++] << 24) & 0xFF000000);
			} else {
				newArray[i] |= array[pos++] << 24;
				newArray[i] |= array[pos++] << 16;
				newArray[i] |= array[pos++] << 8;
				newArray[i] |= array[pos++] << 0;
			}
		}

		return newArray;
	}

}
