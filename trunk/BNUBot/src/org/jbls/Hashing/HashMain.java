/**
 * $Id$
 */
/*
 * Created on Sep 29, 2004
 *
 *
 */
package org.jbls.Hashing;

import org.jbls.util.Buffer;
import org.jbls.util.Constants;

/**
 *
 * @author The-FooL
 *
 * This is the main Class that provides hashing functions, CheckRevision, Exe
 * Info, etc provided by BNLS.
 *
 * Static Methods Allow it to be accessible by any thread
 */
public class HashMain {
	public static final byte PLATFORM_INTEL = 0x01;
	public static final byte PLATFORM_POWERPC = 0x02;
	public static final byte PLATFORM_MACOSX = 0x03;

	public static int WAR3KeysHashed = 0;
	public static int STARKeysHashed = 0;
	public static int D2DVKeysHashed = 0;

	/**
	 * Picks appropriate hashing method based on length, and hashes the CD-Key.
	 *
	 * @param clientToken
	 *            ClientToken used in hash specified by Client or JBLS
	 * @param serverToken
	 *            ServerToken used in hash specified by BNET server
	 * @param key CDKey to hash
	 * @return HashedKey(in a Buffer) - 9 DWORDS
	 * @throws HashException
	 *             If Invalid Key
	 *
	 */
	public static Buffer hashKey(int clientToken, int serverToken, String key)
			throws HashException {
		switch (key.length()) {
		case 13:// STAR/SEXP
			return hashSCKey(clientToken, serverToken, key);
		case 16:// WAR2/D2DV/D2XP
			return hashD2Key(clientToken, serverToken, key);
		case 26:// WAR3/W3XP
			return hashWAR3Key(clientToken, serverToken, key);
		}// end of switch
		throw new HashException("Invalid Key Length");
	}// end of hashKey Method

	private static Buffer hashD2Key(int clientToken, int serverToken, String key)
			throws HashException {
		D2KeyDecode d2 = new D2KeyDecode(key);
		Buffer ret = new Buffer();
		ret.addDWord(key.length());
		ret.addDWord(d2.getProduct());
		ret.addDWord(d2.getVal1());
		ret.addDWord(0);
		int hashedKey[] = d2.getKeyHash(clientToken, serverToken);
		for (int i = 0; i < 5; i++)
			ret.addDWord(hashedKey[i]);
		return ret;
	}

	/**
	 * OLS-style key hash
	 * @param clientToken
	 *            ClientToken used in hash specified by Client or JBLS
	 * @param serverToken
	 *            ServerToken used in hash specified by BNET server
	 * @param key CDKey to hash
	 * @return HashedKey(in a Buffer) - 9 DWORDS
	 * @throws HashException
	 *             If Invalid Key
	 */
	public static Buffer hashW2Key(int clientToken, int serverToken, String key)
			throws HashException {
		D2KeyDecode d2 = new D2KeyDecode(key);
		Buffer ret = new Buffer();
		ret.addDWord(key.length());
		ret.addDWord(d2.getProduct());
		ret.addDWord(d2.getVal1());
		ret.addDWord(serverToken);
		ret.addDWord(clientToken);
		int hashedKey[] = d2.getOldKeyHash(clientToken, serverToken);
		for (int i = 0; i < 5; i++)
			ret.addDWord(hashedKey[i]);
		return ret;
	}

	private static Buffer hashSCKey(int clientToken, int serverToken, String key)
			throws HashException {

		SCKeyDecode sc = new SCKeyDecode(key);
		Buffer ret = new Buffer();
		ret.addDWord(key.length());
		ret.addDWord(sc.getProduct());
		ret.addDWord(sc.getVal1());
		ret.addDWord(0);
		int hashedKey[] = sc.getKeyHash(clientToken, serverToken);
		for (int i = 0; i < 5; i++)
			ret.addDWord(hashedKey[i]);
		return ret;
	}

	private static Buffer hashWAR3Key(int clientToken, int serverToken,
			String key) {
		War3Decode w3 = new War3Decode(key);
		Buffer ret = new Buffer();
		ret.addDWord(key.length());
		ret.addDWord(w3.getProduct());
		ret.addDWord(w3.getVal1());
		ret.addDWord(0);
		int hashedKey[] = w3.getKeyHash(clientToken, serverToken);
		for (int i = 0; i < 5; i++)
			ret.addDWord(hashedKey[i]);
		return ret;
	}

	public static int getVerByte(int prod) {
		if (prod <= 0)
			return 0;
		if (prod > Constants.prods.length + 1)
			return 0;
		return Constants.IX86verbytes[prod - 1];
	}
}