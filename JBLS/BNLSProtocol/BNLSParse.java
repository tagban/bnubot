/*
 * Created on Sep 24, 2004
 *
 * This class parses all the data from BNLS Input
 */
/*
 * This Class Does all the parsing of the BNLS Packets.  Deciding what to
 * take out, what to return, etc.  Calls on HashMain for hashing Functions.
 *
 * Also stores individual connection specific variables
 * @author The-FooL
 */

package BNLSProtocol;

import java.util.Hashtable;
import java.util.Random;
import java.io.IOException;

import util.Buffer;
import util.Constants;
import util.Out;
import Hashing.BrokenSHA1;
import Hashing.DoubleHash;
import Hashing.HashException;
import Hashing.HashMain;
import Hashing.SRP;

public class BNLSParse
{

    // The Following are Variables that are unique to this specific connection

    /** Whether or not the client is authorized to request hash info */
    private boolean authorized = (!Constants.requireAuthorization);
    private String BNLSUsername, BNLSPassword;// Username/Password of the current client

    /** used to check the status of the BNLS Account/Password */
    private int BNLSServerCode;
    private int nlsRevision = 1;// NLS Revision number that wants to be used
    private SRP mySRP;          // Stored SRP...Needed for when we go from 0x02 to 0x03
    private SRP myNewSRP;       // For password changes

    /** salt and B are stored for the server's proof */
    private byte []salt = null;
    private byte []B = null;
    private String oldPass = null;
    private SRP []reservedSRPs = null;
    private int SRPs = 0;

	private static final byte BNLS_NULL                 = 0x00; //Fully Supported
    private static final byte BNLS_CDKEY                = 0x01; //Fully Supported
    private static final byte BNLS_LOGONCHALLENGE       = 0x02; //Fully Supported
    private static final byte BNLS_LOGONPROOF           = 0x03; //Fully Supported
    private static final byte BNLS_CREATEACCOUNT        = 0x04; //Fully Supported
    private static final byte BNLS_CHANGECHALLENGE      = 0x05; //Fully Supported
    private static final byte BNLS_CHANGEPROOF          = 0x06; //Fully Supported
    private static final byte BNLS_UPGRADECHALLENGE     = 0x07; //Fully Supported
    private static final byte BNLS_UPGRADEPROOF         = 0x08; //fully Supported
    private static final byte BNLS_VERSIONCHECK         = 0x09; //Fully Supported
    private static final byte BNLS_CONFIRMLOGON         = 0x0a; //Fully Supported
    private static final byte BNLS_HASHDATA             = 0x0b; //Fully Supported
    private static final byte BNLS_CDKEY_EX             = 0x0c; //Fully Supported
    private static final byte BNLS_CHOOSENLSREVISION    = 0x0d; //Fully Supported
    private static final byte BNLS_AUTHORIZE            = 0x0e; //Fully Supported
    private static final byte BNLS_AUTHORIZEPROOF       = 0x0f; //Fully Supported
    private static final byte BNLS_REQUESTVERSIONBYTE   = 0x10; //Fully Supported
    private static final byte BNLS_VERIFYSERVER         = 0x11; //Fully Supported
    private static final byte BNLS_RESERVESERVERSLOTS   = 0x12; //Fully Supported
    private static final byte BNLS_SERVERLOGONCHALLENGE = 0x13; //Fully Supported
    private static final byte BNLS_SERVERLOGONPROOF     = 0x14; //Fully Supported
    private static final byte BNLS_RESERVED0            = 0x15;
    private static final byte BNLS_RESERVED1            = 0x16;
    private static final byte BNLS_RESERVED2            = 0x17;
    private static final byte BNLS_VERSIONCHECKEX       = 0x18; //Fully Supported
    private static final byte BNLS_RESERVED3            = 0x19;
    private static final byte BNLS_VERSIONCHECKEX2      = 0x1A; //Supported {Lockdown Needs to be reversed}

    private static final byte PRODUCT_STARCRAFT         = 0x01; //Fully supported
    private static final byte PRODUCT_BROODWAR          = 0x02; //Fully Supported
    private static final byte PRODUCT_WAR2BNE           = 0x03; //Fully Supported
    private static final byte PRODUCT_DIABLO2           = 0x04; //Fully Supported
    private static final byte PRODUCT_LORDOFDESTRUCTION = 0x05; //Fully Supported
    private static final byte PRODUCT_JAPANSTARCRAFT    = 0x06; //Fully Supported
    private static final byte PRODUCT_WARCRAFT3         = 0x07; //Fully Supported
    private static final byte PRODUCT_THEFROZENTHRONE   = 0x08; //Fully Supported
    private static final byte PRODUCT_DIABLO            = 0x09; //Fully Supported
    private static final byte PRODUCT_DIABLOSHAREWARE   = 0x0A; //Fully Supported
    private static final byte PRODUCT_STARCRAFTSHAREWARE= 0x0B; //Fully Supported


    public static Hashtable<String, Integer> botIds;

    public OutPacketBuffer parseInput(byte packetID, short pLength, String data) throws InvalidPacketException, BNLSException
    {
        return parseInput(new InPacketBuffer(packetID, pLength, data));
    }

    /**
     * @author The-FooL
     * @param in -
     *            PacketBuffer to be Parsed
     * @throws InvalidPacketException -
     *             when packet is malformed/incorrect
     * @throws BNLSException -
     *             when a connection-fatal BNLS problem has occured
     * @return OutPacketBuffer - Buffer representing a packet to be sent in
     *         response
     *
     */
     public OutPacketBuffer parseInput(InPacketBuffer in) throws InvalidPacketException, BNLSException
    {

        int packetID = in.getPacketID();

        /*
         * Check For Authorization. If not authorized, they can only send
         * 0x0E(BNLS Account) or 0x0F(BNLS Pass Authorization)
         */
        if (!authorized && (packetID != 0x00 && packetID != 0x0e && packetID != 0x0F))
            throw new BNLSException("Attempted to Access Info When Not Authorized");
        try
        {
            switch (packetID)
            {// parse the individual packets
                case BNLS_NULL:
                    /*
                     * BNLS_NULL (0x00)
                     * ----------------
                     * This message is empty and may be used to keep the
                     * connection alive. The client is not required to
                     * send this. There is no response from the server.
                     */
                    return null;
                case BNLS_CDKEY:
                    /*
                     * BNLS_CDKEY (0x01)
                     * -----------------
                     * This message will encrypt your CD-key,
                     * and will reply with the properly encoded CD-key
                     * as it is supposed to be sent in the message
                     * SID_AUTH_CHECK (0x51).
                     * It now works with CD-keys of all products.
                     * (DWORD) Session key from Battle.net.
                     *     This is the second DWORD in SID_AUTH_INFO (0x50).
                     * (String) CD-key.
                     *    No dashes or spaces.
                     * Response:
                     * ---------
                     * (BOOL) Success (TRUE if successful, FALSE otherwise).
                     *    If this is FALSE, there is no more data in this message.
                     * (DWORD) Client session key.
                     * (9 DWORDs) CD-key data.
                     */

                    int clientKey = Math.abs(new Random().nextInt());// Randomly generate client key
                    int sessionKey = in.removeDWord();// Get Server Key from packet
                    Buffer keyHashBuf = new Buffer();
                    String key = in.removeNTString();
                    OutPacketBuffer pBNLS_CDKEY = new OutPacketBuffer(BNLS_CDKEY);

                    try{
                        keyHashBuf = HashMain.hashKey(clientKey, sessionKey, key);
                    }catch (HashException e){
                        Out.error("JBLS Parse", "0x01 Key Hash Failed,");
                        pBNLS_CDKEY.addDWord(0);// failed hash
                        return pBNLS_CDKEY;
                    }

                    pBNLS_CDKEY.addDWord(1);// successful hash
                    pBNLS_CDKEY.addDWord(clientKey);
                    pBNLS_CDKEY.addBuffer(keyHashBuf);

                    return pBNLS_CDKEY;// return packet

                case BNLS_LOGONCHALLENGE:
                    /*
                     * BNLS_LOGONCHALLENGE (0x02)
                     * --------------------------
                     * This message will give you data you need for
                     * SID_AUTH_ACCOUNTLOGON (0x53).
                     * You must send this before you can send BNLS_LOGONPROOF (0x03).
                     * (String) Account name.
                     * (String) Account password.
                     * Response:
                     * ---------
                     * (8 DWORDs) Data for SID_AUTH_ACCOUNTLOGON (0x53).
                     */

                    String accName = in.removeNTString();// Get Username/Pass from packet
                    String accPass = in.removeNTString();

                    if (accName == null || accPass == null)// Check for valid strings
                        return null;

                    mySRP = new SRP(accName, accPass);// Create New SRP to handle this
					mySRP.set_NLS(nlsRevision);

                    OutPacketBuffer pBNLS_LOGONCHALLENGE = new OutPacketBuffer(BNLS_LOGONCHALLENGE);

                    pBNLS_LOGONCHALLENGE.add(mySRP.get_A());

                    if (Constants.displayParseInfo)
                        Out.info("JBLS Parse", ">>> \"" + accName + "\" WAR3 Account Login ");

                    return pBNLS_LOGONCHALLENGE;

                case BNLS_LOGONPROOF:

                    /*
                     * BNLS_LOGONPROOF (0x03)
                     * ----------------------
                     * This message will parse data from SID_AUTH_ACCOUNTLOGON (0x53)
                     * and will reply with data to send in SID_AUTH_ACCOUNTLOGONPROOF (0x54).
                     * You must send BNLS_LOGONCHALLENGE (0x02) before you can send this.
                     * This message cannot be used simultaneously with
                     * BNLS_CHANGEPROOF (0x06) or BNLS_UPGRADEPROOF (0x08).
                     * (16 DWORDs) Data from SID_AUTH_ACCOUNTLOGON (0x53).
                     * Response:
                     * ---------
                     * (5 DWORDs) Data for SID_AUTH_ACCOUNTLOGONPROOF (0x54).
                     */

                    if (mySRP == null)
                        throw new BNLSException("Must send BNLS_LOGONCHALLENGE Before BNLS_LOGONPROOF");// We need the SRP class already made with username/pass

                    OutPacketBuffer pBNLS_LOGONPROOF = new OutPacketBuffer(BNLS_LOGONPROOF);

                    salt = in.removeBytes(SRP.BIGINT_SIZE);// Data sent by bnet required for AccountLogonProof
                    B = in.removeBytes(SRP.BIGINT_SIZE);// <3 iago's SRP class (NOTE: if anybody capitalizes "Iago" again I'll eat their balls)

					mySRP.set_NLS(nlsRevision);
                    pBNLS_LOGONPROOF.add(mySRP.getM1(salt, B));// add Buffer to packet, return packet

                    return pBNLS_LOGONPROOF;

                case BNLS_CREATEACCOUNT:
                    /*
                     * BNLS_CREATEACCOUNT (0x04)
                     * -------------------------
                     * This message will give you data you need for
                     * SID_AUTH_ACCOUNTCREATE (0x52).
                     * (String) Account name.
                     * (String) Account password.
                     * Response:
                     * ---------
                     * (16 DWORDs) Data for SID_AUTH_ACCOUNTCREATE (0x52).
                     */

                    String aName = in.removeNTString();
                    String aPass = in.removeNTString();

                    mySRP = new SRP(aName, aPass);// Create new SRP class for the account create
					mySRP.set_NLS(nlsRevision);
                    OutPacketBuffer pBNLS_CREATEACCOUNT = new OutPacketBuffer(BNLS_CREATEACCOUNT);

                    // We need a random variable for salt, and guess what A is?
                    salt = mySRP.get_A();

                    pBNLS_CREATEACCOUNT.add(salt);
                    pBNLS_CREATEACCOUNT.add(mySRP.get_v(salt).toByteArray());

                    return pBNLS_CREATEACCOUNT;

                case BNLS_CHANGECHALLENGE:
                    /*
                     * BNLS_CHANGECHALLENGE (0x05)
                     * ---------------------------
                     * This message will give you data you need for
                     * SID_AUTH_ACCOUNTCHANGE (0x55).
                     * This message is used to change the password
                     * of an existing account. You must send
                     * this before you can send BNLS_CHANGEPROOF (0x06).
                     * (String) Account name.
                     * (String) Account old password.
                     * (String) Account new password.
                     * Response:
                     * ---------
                     * (8 DWORDs) Data for SID_AUTH_ACCOUNTCHANGE (0x55).
                     */

                    // get Name/Passes from packet
                    accName = in.removeNTString();
                    oldPass = in.removeNTString();
                    accPass = in.removeNTString();

                    mySRP = new SRP(accName, oldPass);
                    myNewSRP = new SRP(accName, accPass);
                    mySRP.set_NLS(nlsRevision);

                    OutPacketBuffer pBNLS_CHANGECHALLENGE = new OutPacketBuffer(BNLS_CHANGECHALLENGE);
                    pBNLS_CHANGECHALLENGE.add(mySRP.get_A());

                    return pBNLS_CHANGECHALLENGE;

                case BNLS_CHANGEPROOF:
                    /*
                     * BNLS_CHANGEPROOF (0x06)
                     * -----------------------
                     * This message will parse data from SID_AUTH_ACCOUNTCHANGE
                     * (0x55) and will reply with data to send in
                     * SID_AUTH_ACCOUNTCHANGEPROOF (0x56).
                     * You must send BNLS_CHANGECHALLENGE (0x05) before you can send this.
                     * This message cannot be used simultaneously with
                     * BNLS_LOGONPROOF (0x03) or BNLS_UPGRADEPROOF (0x08).
                     *
                     * (16 DWORDs) Data from SID_AUTH_ACCOUNTCHANGE (0x55).
                     * Response:
                     * ---------
                     * (21 DWORDs) Data for SID_AUTH_ACCOUNTCHANGEPROOF (0x56).
                     */

                    OutPacketBuffer pBNLS_CHANGEPROOF = new OutPacketBuffer(BNLS_CHANGEPROOF);

                    salt = in.removeBytes(SRP.BIGINT_SIZE);
                    B = in.removeBytes(SRP.BIGINT_SIZE);

					mySRP.set_NLS(nlsRevision);
                    pBNLS_CHANGEPROOF.add(mySRP.getM1(salt, B));
                    pBNLS_CHANGEPROOF.add(salt);
                    pBNLS_CHANGEPROOF.add(myNewSRP.get_v(salt).toByteArray());

                    return pBNLS_CHANGEPROOF;

                case BNLS_UPGRADECHALLENGE:
                    /*BNLS_UPGRADECHALLENGE (0x07)
                     * ----------------------------
                     * This message will give you data you need for
                     * SID_AUTH_ACCOUNTUPGRADE (0x57). This message is used to
                     * upgrade an existing account from Old Logon System to New
                     * Logon System.
                     * You must send this before you can send BNLS_UPGRADEPROOF (0x08).
                     * Important:
                     *    You must send BNLS_LOGONCHALLENGE (0x02) or BNLS_CHANGECHALLENGE (0x05)
                     *    before sending this. Otherwise, the results are meaningless.
                     * Note: Since Old Logon System and New Logon
                     * System are incompatible, you can change the password and
                     * upgrade the account at the same time. This is not
                     * required - the old password and the new password may be
                     * identical for this message.
                     * (String) Account name.
                     * (String) Account old password.
                     * (String) Account new password.
                     *     (May be identical to old password but still must be provided.)
                     * Response:
                     * ---------
                     * (BOOL) Success code.
                     *     If this is TRUE, you may send SID_AUTH_ACCOUNTUPGRADE (0x57).
                     * Currently, no error conditions are defined, so this is always TRUE.
                     */
                     accName = in.removeNTString();
                     oldPass = in.removeNTString();
                     accPass = in.removeNTString();
                     mySRP = new SRP(accName, accPass);
                     mySRP.set_NLS(nlsRevision);

                     OutPacketBuffer pBNLS_UPGRADECHALLENGE = new OutPacketBuffer(BNLS_UPGRADECHALLENGE);
                     pBNLS_UPGRADECHALLENGE.addDWord(true);
                     return pBNLS_UPGRADECHALLENGE;

                case BNLS_UPGRADEPROOF:
                    /* BNLS_UPGRADEPROOF (0x08)
                     * ------------------------
                     * This message will parse data from SID_AUTH_ACCOUNTUPGRADE
                     * (0x57) and will reply with data to send in
                     * SID_AUTH_ACCOUNTUPGRADEPROOF (0x58).
                     * You must send BNLS_UPGRADECHALLENGE (0x07) before you can
                     * send this.
                     * This message cannot be used simultaneously with
                     * BNLS_LOGONPROOF (0x03) or BNLS_CHANGEPROOF (0x06).
                     *
                     * (DWORD) Session key from SID_AUTH_ACCOUNTUPGRADE (0x57).
                     * Response:
                     * ---------
                     * (22 DWORDs) Data for SID_AUTH_ACCOUNTUPGRADEPROOF (0x58).
                     *
                     * The 22 DWORD responsein comprised of the following:
					 *	(DWORD) Client Token
					 *  (DWORD[5]) Double XSHA-1 password hash (Old password)
					 *  (DWORD[8]) Salt
					 *  (DWORD[8]) Password verifyer
                     */
                     int serverKey = in.removeDWord();
                     if (mySRP == null) //They havent sent 0x07
                     	return null;

                     int clientToken = Math.abs(new Random().nextInt());
                     int[] oldHash = DoubleHash.doubleHash(oldPass, serverKey, clientToken);
                     mySRP.set_NLS(nlsRevision);

                     salt = mySRP.get_A();

                     OutPacketBuffer pBNLS_UPGRADEPROOF = new OutPacketBuffer(BNLS_UPGRADEPROOF);
                     pBNLS_UPGRADEPROOF.addDWord(clientToken);
                     for (int Y = 0; Y < 5; Y++)
                     	pBNLS_UPGRADEPROOF.addDWord(oldHash[Y]);
                     pBNLS_UPGRADEPROOF.add(salt);
                     pBNLS_UPGRADEPROOF.add(mySRP.get_v(salt).toByteArray());

                     return pBNLS_UPGRADEPROOF;


                case BNLS_VERSIONCHECK:
                    /*
                     * BNLS_VERSIONCHECK (0x09)
                     *------------------------
                     * This message will request a fast version check.
                     * Now works with all products.
                     * (DWORD) Product ID.
                     * (DWORD) Version DLL digit in the range 0-7.
                     *          (For example, for IX86Ver1.mpq this is 1)
                     * (String) Checksum formula.
                     * Valid product IDs are:
                     * #define PRODUCT_STARCRAFT (0x01)
                     * #define PRODUCT_BROODWAR (0x02)
                     * #define PRODUCT_WAR2BNE (0x03)
                     * #define PRODUCT_DIABLO2 (0x04)
                     * #define PRODUCT_LORDOFDESTRUCTION (0x05)
                     * #define PRODUCT_JAPANSTARCRAFT (0x06)
                     * #define PRODUCT_WARCRAFT3 (0x07)
                     * #define PRODUCT_THEFROZENTHRONE (0x08)
                     * Response:
                     * ---------
                     * (BOOL) Success (TRUE if successful, FALSE otherwise).
                     * If this is FALSE, there is no more data in this message.
                     * (DWORD) Version.
                     * (DWORD) Checksum.
                     * (String) Version check stat string.
                     */

                    int productID = in.removeDWord();
                    int MPQName = in.removeDWord();
                    String ChecksumFormula = in.removeNTString();
                    boolean success = true;
                    int checksum = 0; 
                    int versionHash = 0;
                    String exeInfo = null;
                    if(productID == 1 || productID == 2 || productID == 3){
                    	String files[] = HashMain.getFiles(productID, HashMain.PLATFORM_INTEL);
                    	checksum = Hashing.CheckRevisionLD.checkRevision(ChecksumFormula, files, MPQName);
                    	exeInfo = Hashing.CheckRevisionLD.getExe(ChecksumFormula, files, MPQName);
                    	versionHash = HashMain.getExeVer(productID);
                	}else{
                    	exeInfo  = HashMain.getExeInfo(productID);
                    	checksum    = HashMain.getChecksum(productID, ChecksumFormula, MPQName);
                    	versionHash = HashMain.getExeVer(productID);
                    }
                    if (checksum == 0 || exeInfo == null) success = false;
                    OutPacketBuffer vCheck = new OutPacketBuffer(BNLS_VERSIONCHECK);
                    if (!success){
                       	vCheck.addDWord(0);// not successful
                       	return vCheck;
                    }
                   	vCheck.addDWord(1);// successful
                   	vCheck.addDWord(versionHash);// versionhash
                   	vCheck.addDWord(checksum);// checksum
                   	vCheck.addNTString(exeInfo);// exe Info(stat string)
                   	return vCheck;
 
                case 0x0a:
                    /*
                     * BNLS_CONFIRMLOGON (0x0a)
                     * ------------------------
                     * This message will confirm that the server really knows your
                     * password. May be used after "proof" messages:
                     * BNLS_LOGONPROOF (0x03), BNLS_CHANGEPROOF (0x06),
                     * BNLS_UPGRADEPROOF (0x08).
                     * (5 DWORDs) Password proof from Battle.net.
                     * Response:
                     * ---------
                     * (BOOL) TRUE if the server knows your password, FALSE otherwise.
                     *   If this is FALSE, the Battle.net connection should be closed by the client.
                     */

                    if(salt == null || B == null)
                        throw new BNLSException("Error: BNLS_LOGONPROOF has to be sent before BNLS_CONFIRMLOGON");

					mySRP.set_NLS(nlsRevision);
                    byte []givenProof = in.removeBytes(SRP.SHA_DIGESTSIZE);
                    byte []proof = mySRP.getM2(salt, B);

                    boolean good = true;
                    for(int i = 0; i < proof.length && good; i++)
                        if(proof[i] != givenProof[i])
                            good = false;

                    OutPacketBuffer vConfirm = new OutPacketBuffer((byte) 0x0A);
                    vConfirm.add(good ? 1 : 0);

                    return vConfirm;

                case 0x0b:
                    /*
                     * BNLS_HASHDATA (0x0b)
                     * --------------------
                     * This message will calculate the hash of the given data.
                     * The hashing algorithm used is the Battle.net standard
                     * hashing algorithm also known as "broken SHA-1".
                     * (DWORD) The size of the data to be hashed.
                     *         Note: This is no longer restricted to 64 bytes.
                     * (DWORD) Flags.
                     * (VOID)  Data to be hashed.
                     * (Optional DWORD) Client key.
                     *      Present only if HASHDATA_FLAG_DOUBLEHASH (0x02) is specified.
                     * (Optional DWORD) Server key.
                     *      Present only if HASHDATA_FLAG_DOUBLEHASH (0x02) is specified.
                     * (Optional DWORD) Cookie.
                     *      Present only if HASHDATA_FLAG_COOKIE (0x04) is specified.
                     *
                     * The flags may be zero, or any bitwise combination of the defined
                     * flags. Currently, the following flags are defined:
                     *    #define HASHDATA_FLAG_UNUSED (0x01)
                     *    #define HASHDATA_FLAG_DOUBLEHASH (0x02)
                     *    #define HASHDATA_FLAG_COOKIE (0x04)
                     *
                     * HASHDATA_FLAG_UNUSED (0x01):
                     *    This flag has no effect.
                     * HASHDATA_FLAG_DOUBLEHASH (0x02):
                     *    If this flag is present, the server will calculate a
                     *    double hash. First it will calculate the hash of the
                     *    data. Then it will prepend the client key and the server
                     *    key to the resulting hash, and calculate the hash of the
                     *    result. If this flag is present, the client key and
                     *    server key DWORDs must be specified in the request after
                     *    the data. This may be used to calculate password hashes
                     *    for the "Old Logon System".
                     * HASHDATA_FLAG_COOKIE (0x04):
                     *    If this flag is present, a cookie DWORD is specified in
                     *    the request. This is an application-defined value that is
                     *    echoed back to the client in the response.
                     * Response:
                     * ---------
                     * (5 DWORDs) The data hash.
                     * (Optional DWORD) Cookie.
                     *   Same as the cookie from the request. Present only
                     *   if HASHDATA_FLAG_COOKIE (0x04) is specified.
                     */
                    int hashLength = in.removeDWord();
                    int hashFlags = in.removeDWord();

                    int[] hashData;
                    Buffer hashPass = new Buffer();
                    for(int x = 0; x < hashLength; x++)
                      hashPass.add(in.removeByte());
                    //String hashPass = in.removeString(hashLength);
                    if ((hashFlags & 0x02) != 0)
                    {
                        int hashCToken = in.removeDWord();
                        int hashSToken = in.removeDWord();

                        // Do the password doublehash
                        //Out.println("JBLS Parse", ">>>> Double Hash:" + hashPass);
                        hashData = DoubleHash.doubleHash(new String(hashPass.getBuffer()), hashCToken, hashSToken);
                    }
                    else
                    {
                        //Out.println("JBLS Parse", ">>>> Standard Hash:" + hashPass);
                        hashData = BrokenSHA1.calcHashBuffer(hashPass.getBuffer());
                    }
                    OutPacketBuffer p0b = new OutPacketBuffer(0x0b);
                    for (int x = 0; x < 5; x++)
                        p0b.add(hashData[x]);

                    if ((hashFlags & 0x04) != 0)// whether or not to return the cookie(or if it exists)
                        p0b.add(in.removeDWord());

                    return p0b;
                case 0x0c:
                    /*
                     * BNLS_CDKEY_EX (0x0c)
                     * --------------------
                     * This message will encrypt your CD-key or CD-keys using
                     * the given flags.
                     * (DWORD) Cookie.
                     * (BYTE) Amount of CD-keys to encrypt. Must be between 1 and 32.
                     * (DWORD) Flags.
                     * (DWORD or DWORDs) Server session key(s), depending on the flags.
                     * (Optional DWORD or DWORDs) Client session key(s), depending on the flags.
                     * (String or strings) CD-keys. No dashes or spaces.
                     *
                     * The client can use multiple types of CD-keys in the same
                     * packet. The flags may be zero, or any bitwise combination
                     * of the defined flags.
                     * Currently, the following flags are defined:
                     * #define CDKEY_SAME_SESSION_KEY (0x01)
                     * #define CDKEY_GIVEN_SESSION_KEY (0x02)
                     * #define CDKEY_MULTI_SERVER_SESSION_KEYS (0x04)
                     * #define CDKEY_OLD_STYLE_RESPONSES (0x08)
                     *
                     * CDKEY_SAME_SESSION_KEY (0x01):
                     *    This flag specifies that all the returned CD-keys
                     *    will use the same client session key. When used in
                     *    combination with CDKEY_GIVEN_SESSION_KEY (0x02), a
                     *    single client session key is specified immediately
                     *    after the server session key(s). When used without
                     *    CDKEY_GIVEN_SESSION_KEY (0x02), a client session key
                     *    isn't sent in the request, and the server will create
                     *    one. When not used, each CD-key gets its own client
                     *    session key. This flag has no effect if the amount of
                     *    CD-keys to encrypt is 1.
                     * CDKEY_GIVEN_SESSION_KEY (0x02):
                     *    This flag specifies that the client session keys to be
                     *    used are specified in the request. When used in
                     *    combination with CDKEY_SAME_SESSION_KEY (0x01), a single
                     *    client session key is specified immediately after the
                     *    server session key(s). When used without
                     *    CDKEY_SAME_SESSION_KEY (0x01), an array of client session
                     *    keys (as many as the amount of CD-keys) is specified.
                     *    When not used, client session keys aren't included in the
                     *    request.
                     * CDKEY_MULTI_SERVER_SESSION_KEYS (0x04):
                     *    This flag specifies that each CD-key has its own server
                     *    session key. When specified, an array of server session
                     *    keys (as many as the amount of CD-keys) is specified.
                     *    When not specified, a single server session key is
                     *    specified. This flag has no effect if the amount of
                     *    CD-keys to encrypt is 1.
                     * CDKEY_OLD_STYLE_RESPONSES (0x08):
                     *    Specifies that the response to this packet is a
                     *    number of BNLS_CDKEY (0x01) responses, instead of a
                     *    BNLS_CDKEY_EX (0x0c) response. The responses are
                     *    guaranteed to be in the order of the CD-keys' appearance
                     *    in the request. Note that when this flag is specified,
                     *    the Cookie cannot be echoed. (It must still be included
                     *    in the request.)
                     * Note:
                     *    When using Lord of Destruction, two CD-keys are encrypted,
                     *    and they must share the same client session key. There are
                     *    several ways to do this:
                     *    One way is to provide both CD-keys in BNLS_CDKEY_EX
                     *    (0x0c) using the flag CDKEY_SAME_SESSION_KEY (0x01).
                     *    Another way is to use BNLS_CDKEY (0x01) to encrypt the
                     *    first CD-key, then use BNLS_CDKEY_EX (0x0c) using the
                     *    flag CDKEY_GIVEN_SESSION_KEY (0x02) to encrypt the second
                     *    CD-key with the same client session key.
                     * Response:
                     * ---------
                     * When the flags don't contain CDKEY_OLD_STYLE_RESPONSES (0x08),
                     * the response is a BNLS_CDKEY_EX (0x0c) message:
                     * (DWORD) Cookie. Same as the value sent to the server in the request.
                     * (BYTE) Amount of CD-keys that were requested.
                     * (BYTE) Amount of CD-keys that were successfully encrypted.
                     * (DWORD) Bit mask for the success code of each CD-key.
                     *         Each bit of the 32 bits in this DWORD is 1 for success
                     *         or 0 for failure. The least significant bit specifies
                     *         the success code of the first CD-key provided. Bits
                     *         that exceed the amount of CD-keys provided are set to
                     *         0.
                     * The following fields repeat for each successful
                     * CD-key (they do not exist for failed CD-keys):
                     * (DWORD) Client session key.
                     * (9 DWORDs) CD-key data.
                     */

                    int cookie = in.removeDWord();// Cookie to Echo Back
                    byte numKeys = in.removeByte();// Number of Keys to Parse
                    int[] serverKeys,
                    clientKeys;
                    boolean sameServerKey,
                    sameClientKey;// Whether to use the same server/client key
                    if (numKeys <= 0)
                        throw new InvalidPacketException("No keys specified in 0x0C");
                    int flags = in.removeDWord();
                    if ((flags & 0x04) != 0)
                    {// Multiple Server session keys
                        serverKeys = new int[numKeys];
                        for (int x = 0; x < numKeys; x++)
                            serverKeys[x] = in.removeDWord();
                        sameServerKey = false;
                    }
                    else
                    {// only 1 server session key
                        serverKeys = new int[1];
                        serverKeys[0] = in.removeDWord();
                        sameServerKey = true;
                    }
                    if ((flags & 0x01) != 0)
                    {// 1 client key
                        clientKeys = new int[1];
                        if ((flags & 0x02) != 0)
                        {// given key
                            clientKeys[0] = in.removeDWord();
                        }
                        else
                        {
                            clientKeys[0] = Math.abs(new Random().nextInt());
                        }
                        sameClientKey = true;
                    }
                    else
                    {// multiple client keys
                        clientKeys = new int[numKeys];
                        if ((flags & 0x02) != 0)
                        {// given key\
                            for (int x = 0; x < numKeys; x++)
                                clientKeys[x] = in.removeDWord();
                        }
                        else
                        {
                            Random r = new Random();
                            for (int x = 0; x < numKeys; x++)
                                clientKeys[x] = Math.abs(r.nextInt());
                        }
                        sameClientKey = false;
                    }

                    OutPacketBuffer p0c = new OutPacketBuffer(0x0c);
                    Buffer[] hashedKey = new Buffer[numKeys];

                    // int cClientToken, cServerToken;
                    int cClientToken = clientKeys[0];
                    int cServerToken = serverKeys[0];

                    byte successKeys = numKeys;
                    int successBitMask = 0;
                    for (int x = 0; x < numKeys; x++)
                    {// do all the keys
                        String keyToHash = in.removeNTString();
                        if (!sameClientKey)// Change The Server/Client Key, If
                                            // necessary.
                            cClientToken = clientKeys[x];
                        if (!sameServerKey)
                            cServerToken = serverKeys[x];
                        try
                        {
                            /** Actually Hash the key */

                            // Call on HashMain, place into array
                            hashedKey[x] = HashMain.hashKey(cClientToken, cServerToken, keyToHash);

                            // Change the success BitMask(is this valid?)
                            successBitMask = (successBitMask | (int) Math.pow(2.0, x));
                        }
                        catch (HashException e)
                        {
                            System.out.println("[0x0C] Invalid Key");
                            successKeys--;
                        }
                    }// end of Key Hashing For Loop

                    // Place Data into Pakcet
                    p0c.addDWord(cookie);// Cookie of Request
                    p0c.addByte(numKeys);// Amount of CD-KEYS Requested
                    p0c.addByte(successKeys);
                    p0c.addDWord(successBitMask);
                    for (int x = 0; x < numKeys; x++)
                    {// Place Each Key with its client key into the pakcet
                        if (hashedKey[x] != null)
                        {
                            if (sameClientKey)
                            {
                                p0c.addDWord(clientKeys[0]);
                            }
                            else
                            {
                                p0c.addDWord(clientKeys[x]);
                            }
                            p0c.addBuffer(hashedKey[x]);
                        }
                    }
                    return p0c;// return Multiple Hashed Key Packet
                case 0x0d:
                    /*
                     * BNLS_CHOOSENLSREVISION (0x0d)
                     * -------------------------------
                     * This message instructs the server which revision of NLS
                     * you want to use.
                     * (DWORD) NLS revision number.
                     *         The NLS revision number is given by Battle.net in
                     *         SID_AUTH_INFO (0x50).
                     * Response:
                     * ---------
                     * (BOOL) Success code.
                     *        If this is TRUE, the revision number was recognized
                     *        by the server and will be used. If this is
                     *        FALSE, the revision number was rejected by the server
                     *        and this request is ignored.
                     * NOTE: The default revision number is 1. Therefore, if Battle.net
                     * reports a revision number of 1, this message may be omitted.
                     */
                    nlsRevision = in.removeDWord();
                    OutPacketBuffer p0d = new OutPacketBuffer(0x0d);
                    if (nlsRevision < 0 || nlsRevision > 2)
                    {
                        p0d.addDWord(0);// unsuccessful, unkown NLS type
                        nlsRevision = 1;
                    }else{
                        p0d.addDWord(1);// successful
                    }
                    return p0d;
                case 0x0e:
                    /*
                     * BNLS_AUTHORIZE (0x0e)
                     * ---------------------
                     *
                     * NOTE: You no longer have to send this. This message logs
                     * on to the BNLS server.
                     *
                     * (String) Bot ID.
                     *
                     * Note: The bot ID is not case sensitive, and is limited to
                     * 31 characters. This message must be sent before sending
                     * any other message. To get a bot ID and password, ask Yoni
                     * or Skywing.
                     * Response:
                     * ---------
                     * The following response is always sent:
                     * (DWORD) Server code. The client will
                     * calculate the checksum of the auth password and the
                     * server code using the BNLS Checksum Algorithm, described
                     * in the appendix at the bottom of this document. The
                     * result is sent in BNLS_AUTHORIZEPROOF (0x0f).
                     *
                     * If the bot ID sent in BNLS_AUTHORIZE (0x0e) did not
                     * exist, then this message is still sent, as backwards
                     * compatibility with the previous version of BNLS, which
                     * required authorization.
                     */

                    String inUsername = in.removeNTString();
                    if (Constants.displayParseInfo)
                        Out.info("JBLS", ">>> BNLS Bot ID: " + inUsername);

                    // Add it to the bot ID List
                    if (botIds == null){
                        botIds = new Hashtable<String, Integer>(5);
                    }
                    Integer i = (Integer) botIds.get(inUsername);
                    if (i == null)
                        i = new Integer(0);
                    i = new Integer(i.intValue() + 1);
                    botIds.put(inUsername, i);

                    OutPacketBuffer p0x0e = new OutPacketBuffer(0x0e);

                    BNLSServerCode = Math.abs(new Random().nextInt());
                    if (Constants.requireAuthorization)
                    {
                    	BNLSPassword = BNLSlist.GetPassword(inUsername);
                        if (BNLSPassword == null)
                        	BNLSUsername = null;
                        else
                            BNLSUsername = inUsername;

                        //throw new BNLSException("Authorization System not setup");// for now, until I set up the system
                    }

                    p0x0e.addDWord(BNLSServerCode);// return the server code to be checked with
                    return p0x0e;

                case 0x0f:
                    /*
                     * BNLS_AUTHORIZEPROOF (0x0f)
                     * --------------------------
                     * This is sent to the server when receiving the status code
                     * in BNLS_AUTHORIZE (0x0e).
                     * (DWORD) Checksum.
                     * Response:
                     * ---------
                     * If the client sent a valid account name, but a wrong password
                     * checksum, then BNLS disconnects the client.
                     * If the client sent an invalid account name, or a valid account
                     * name with a correct password checksum, the following response
                     * is sent:
                     * (DWORD) Status code.
                     *     The following status codes are defined:
                     *     #define STATUS_AUTHORIZED (0x00)
                     *     #define STATUS_UNAUTHORIZED (0x01)
                     * STATUS_AUTHORIZED (0x00) means the login was performed as a
                     * registered account. STATUS_UNAUTHORIZED (0x01) means an
                     * anonymous login was performed.
                     */

                    int retChecksum = in.removeDWord();// Returned Checksum, to be compared
                    OutPacketBuffer p0f = new OutPacketBuffer(0x0f);
                    if ((BNLSPassword != null) && (BNLSlist.BNLSChecksum(BNLSPassword, BNLSServerCode) == retChecksum))
                    {
                        p0f.addDWord(0x00);//Reged acct.
                        authorized = true;// Set Authorized Status
                        if (Constants.displayParseInfo)
                            Out.info("JBLS", ">>> BNLS Password Verified");
                    }else{// password was not authorized
                        if (authorized || (!Constants.requireAuthorization))
                        {
                            p0f.addDWord(0x00);//anonymous login
                            if (Constants.displayParseInfo)
                                Out.info("JBLS", ">>> BNLS ID Anonymous Login");
                        }else{
                            throw new BNLSException("Incorrect Pass >>> Close Connection ");
                        }
                    }
                    return p0f;
                case BNLS_REQUESTVERSIONBYTE:
                    /*
                     * BNLS_REQUESTVERSIONBYTE (0x10)
                     * ------------------------------
                     * This message requests the latest version byte for a given
                     * product. The version byte is sent to Battle.net in
                     * SID_AUTH_INFO (0x50).
                     * (DWORD) Product ID.*
                     *   Valid product IDs are:
                     *   #define PRODUCT_STARCRAFT (0x01)
                     *   #define PRODUCT_BROODWAR (0x02)
                     *   #define PRODUCT_WAR2BNE (0x03)
                     *   #define PRODUCT_DIABLO2 (0x04)
                     *   #define PRODUCT_LORDOFDESTRUCTION (0x05)
                     *   #define PRODUCT_JAPANSTARCRAFT (0x06)
                     *   #define PRODUCT_WARCRAFT3 (0x07)
                     *   #define PRODUCT_THEFROZENTHRONE (0x08)
                     *   #define PRODUCT_DIABLO (0x09)
                     *   #define PRODUCT_DIABLOSHAREWARE (0x0A)
                     *   #define PRODUCT_STARCRAFTSHAREWARE (0x0B)
                     * Response:
                     * ---------
                     * (DWORD) On failure (invalid product ID), this is 0.
                     *         On success, this is equal to the requested product ID.
                     * (DWORD) Latest version byte for specified product. If the
                     *         previous DWORD is 0, this DWORD is not included in
                     *         the message.
                     */

                    int prod = in.removeDWord();
                    OutPacketBuffer pBNLS_REQUESTVERSIONBYTE = new OutPacketBuffer(BNLS_REQUESTVERSIONBYTE);
                    int vByte = HashMain.getVerByte(prod);
                    if (vByte == 0){
                    	pBNLS_REQUESTVERSIONBYTE.addDWord(0);
                    }else{
                    	pBNLS_REQUESTVERSIONBYTE.addDWord(prod);// product ID
                    	pBNLS_REQUESTVERSIONBYTE.addDWord(vByte);// versionbyte
                    }
                    return pBNLS_REQUESTVERSIONBYTE;// send back packet

                case 0x11:
                   /*
                    * BNLS_VERIFYSERVER (0x11) ------------------------
                    *
                    * This messages verifies a server's signature, which is
                    * based on the server's IP. The signature is optional
                    * (currently sent only with Warcraft 3), and is sent in
                    * SID_AUTH_INFO (0x50).
                    *
                    * (DWORD) Server's IP. (128 bytes) Signature.
                    *
                    * Response: ---------
                    *
                    * (BOOL) Success. (If this is TRUE, the signature matches
                    * the server's IP - if this is FALSE, it does not.)
                    */

                    byte []serverIp = in.removeBytes(4);
                    byte []serverSig = in.removeBytes(128);

                    boolean result = SRP.checkServerSignature(serverSig, serverIp);
                    OutPacketBuffer verifyServer = new OutPacketBuffer((byte) 0x11);
                    verifyServer.add(result ? 1 : 0);

                    return verifyServer;

                case 0x12:
                    /* BNLS_RESERVESERVERSLOTS (0x12)
                     * ------------------------------
                     *
                     * This message reserves a number of slots for concurrent
                     * NLS checking operations. No other NLS checking messages
                     * can be sent before this message has been sent. This
                     * message cannot be sent more than once per connection.
                     *
                     * (DWORD) Number of slots to reserve.
                     *
                     * BNLS may limit the number of slots to a reasonable value.
                     *
                     * Response: ---------
                     *
                     * (DWORD) Number of slots reserved.
                     *
                     * This may be equal to the number of slots requested,
                     * although it does not necessarily have to be the same
                     * value. Valid slot indicies are in the range of [0, Number
                     * of slots reserved - 1]. Each slot stores state
                     * information about a NLS checking operation. A logon
                     * checking session must be finished on the same slot on
                     * which it was started. If a logon checking session is
                     * abandoned before it is completed, no special action is
                     * required. Starting a new logon checking session on a slot
                     * overwrites all previous state information. A logon
                     * checking session cannot be resumed if the connection to
                     * BNLS is interrupted before it is completed.
                     */
                     int askedFor = in.removeDWord();
                     if (askedFor < 0)
                     	askedFor = 1;
                     else if(askedFor > 32)
                     	askedFor = 32;

                     reservedSRPs = new SRP[askedFor];
                     OutPacketBuffer p12 = new OutPacketBuffer(0x12);
                     p12.addDWord(askedFor);
                     return p12;

                case 0x13:
				    /* BNLS_SERVERLOGONCHALLENGE (0x13)
                     * --------------------------------
                     *
                     * This message initializes a new logon checking session and
                     * calculates the values needed for the server's reply to
                     * SID_AUTH_ACCOUNTLOGON (0x53). BNLS_RESERVESERVERSLOTS
                     * (0x12) must be sent before this message to reserve slots
                     * for logon checking sessions.
                     *
                     * (DWORD) Slot index.
                     * (DWORD) NLS revision number.
                     * (16 DWORDs) Data from account database.
                     *    -8DWORDs Salt
                     *    -8DWORDs 'v'
                     * (8 DWORDs) Data from the client's SID_AUTH_ACCOUNTLOGON (0x53) request.
                     *
                     * Both the slot indicies and the NLS revision number follow
                     * their respective conventions introduced earlier in this
                     * document. The account database data is first received
                     * from the client's SID_AUTH_ACCOUNTCREATE (0x04) message.
                     * This information must be stored by the server's account
                     * database for logon checking. If the account database data
                     * is invalid, then the logon checking session will not
                     * succeed. This message initializes a slot with all the
                     * information required for it to operate, including the NLS
                     * revision. Although BNLS supports switching the NLS
                     * revision of a given slot, it can respond to requests
                     * slightly faster if the same NLS revision is used for the
                     * same slots in a given connection.
                     *
                     * Response: ---------
                     *
                     * (DWORD) Slot index.
                     * (16 DWORDs) Data for the server's SID_AUTH_ACCOUNTLOGON (0x53) response.
                     *
                     * The slot index is returned since individual operations
                     * may be returned in a different order than they are
                     * requested. This message can also be used to calculate the
                     * server's SID_AUTH_ACCOUNTCHANGE (0x55) response. Simply
                     * substitute the SID_AUTH_ACCOUNTLOGON (0x53) data with the
                     * SID_AUTH_ACCOUNTCHANGE (0x55) data.
                     */
                     int slotID = in.removeDWord();
                     int NLS = in.removeDWord();

                     if (SRPs < slotID)
                       return null;
                     salt = in.removeBytes(SRP.BIGINT_SIZE);
                     byte[] v = in.removeBytes(SRP.BIGINT_SIZE);
                     reservedSRPs[slotID] = new SRP(in.removeBytes(SRP.BIGINT_SIZE));
                     reservedSRPs[slotID].set_NLS(NLS);

                     OutPacketBuffer p13 = new OutPacketBuffer(0x13);
                     p13.addDWord(slotID);
                     for (int Y = 0; Y < 5; Y++)
                     	p13.addDWord(salt[Y]);
                     B = reservedSRPs[slotID].get_B(v);
                     reservedSRPs[slotID].set_B(B);
                     for (int Y = 0; Y < 5; Y++)
                     	p13.addDWord(B[Y]);
                     return p13;

                case 0x14:
                    /* BNLS_SERVERLOGONPROOF (0x14)
                     * ----------------------------
                     *
                     * This message performs two operations. First, it checks if
                     * the client's logon was successful. Second, it calculates
                     * the data for the server's reply to
                     * SID_AUTH_ACCOUNTLOGONPROOF (0x54). If this data is not
                     * correct, then the client will not accept the logon
                     * attempt as valid.
                     *
                     * (DWORD) Slot index.
                     * (5 DWORDs) Data from the client's SID_AUTH_ACCOUNTLOGONPROOF (0x54) request.
                     * (STRING) The client's account name.
                     *
                     * Response: ---------
                     *
                     * (DWORD) Slot index.
                     * (BOOL) Success.
                     *       (If this is TRUE, then the client's logon information
                     *        was valid. Otherwise, if it is FALSE, then the client's
                     *        logon information was invalid, and the logon request must
                     *        be denied.)
                     * (5 DWORDs) Data for the server's SID_AUTH_ACCOUNTLOGONPROOF (0x54) response.
                     *
                     * After this message is received, the logon checking
                     * sequence for a particular logon session is complete. This
                     * message can also be used to calculate the server's
                     * SID_AUTH_ACCOUNTCHANGEPROOF (0x56) response, and check
                     * the client's change password request. Simply substitute
                     * the SID_AUTH_ACCOUNTLOGONPROOF (0x54) data with the
                     * SID_AUTH_ACCOUNTCHANGEPROOF (0x56) data
                     */
                        slotID = in.removeDWord();
                     	byte[] M1 = in.removeBytes(SRP.SHA_DIGESTSIZE);

                     	if (SRPs < slotID)
                     		return null;

                     	byte[] M2 = reservedSRPs[slotID].getM2(reservedSRPs[slotID].get_A(), reservedSRPs[slotID].get_B());

                     	OutPacketBuffer p14 = new OutPacketBuffer(0x14);
                     	p14.addDWord(slotID);
                     	if (equal(M1, M2))
                     		p14.addDWord(0x01);
                     	else
                     		p14.addDWord(0x00);
                     	for(int Y = 0; Y < 5; Y++)
                     		p14.addDWord(M1[Y]);

                     	return p14;


                case BNLS_VERSIONCHECKEX:
                	/* BNLS_VERSIONCHECKEX (0x18)
					 * --------------------------
					 * This message performs two operations.
					 * First, will request a fast version check.
					 *   Now works with all products.
					 * Second, it will request the current version code
					 *   for the given product (eliminating the need for
					 *   BNLS_REQUESTVERSIONBYTE).
                     *
					 * (DWORD) Product ID.*
                     * (DWORD) Version DLL digit in the range 0-7.
                     *        (For example, for IX86Ver1.mpq this is 1)
					 * (DWORD) Flags.**
					 * (DWORD) Cookie.
					 * (String) Checksum formula.
					 *	** The flags field is currently reserved and must
					 *	   be set to zero or you will be disconnected.
					 *
					 *	Response:
					 *	---------
					 *	(BOOL) Success (TRUE if successful, FALSE otherwise).
					 *	       If this is FALSE, the next DWORD is the provided
					 *	       cookie, following which the message ends.
					 *	(DWORD) EXE Version.
					 *	(DWORD) Checksum.
					 *	(String) EXE Info.
					 *	(DWORD) Cookie.
					 *	(DWORD) VerByte.
					 */
                     int ProdID = in.removeDWord();
                     int DLLDig = in.removeDWord();
                     int Flags  = in.removeDWord();
                     int Cookie = in.removeDWord();
                     String Formula = in.removeNTString();
                     boolean successEX = true;
                     String exeInfoEX  = HashMain.getExeInfo(ProdID);
                     int checksumEX    = HashMain.getChecksum(ProdID, Formula, DLLDig);
                     int versionHashEX = HashMain.getExeVer(ProdID);
                     int versionByteEX = HashMain.getVerByte(ProdID);

                     if (checksumEX == 0 || exeInfoEX == null || versionByteEX == 0) {
                     	successEX = false;
                     }

                     OutPacketBuffer pVerCheckEX = new OutPacketBuffer(BNLS_VERSIONCHECKEX);
                     if (!successEX) {
                     	pVerCheckEX.addDWord(successEX);
                     	pVerCheckEX.addDWord(Cookie);
                     	return pVerCheckEX;
                     }

                     pVerCheckEX.addDWord(successEX);
                     pVerCheckEX.addDWord(versionHashEX);
                     pVerCheckEX.addDWord(checksumEX);
                     pVerCheckEX.addNTString(exeInfoEX);
                     pVerCheckEX.addDWord(Cookie);
                     pVerCheckEX.addDWord(versionByteEX);
                     return pVerCheckEX;

                case BNLS_VERSIONCHECKEX2:
                    /* BNLS_VERSIONCHECKEX2 (0x1A)
                     * ---------------------------
                     * This message performs two operations.
                     * First, will request a fast version check. Now works with all products.
                     * Second, it will request the current version code for the given product
                     * (eliminating the need for BNLS_REQUESTVERSIONBYTE).
                     * This message does not require the client to perform any parsing on the version
                     * check MPQ filenames. Instead, the full file name and timestamp are sent to the server.
                     * (DWORD) Product ID.*
                     * (DWORD) Flags.**
                     * (DWORD) Cookie.
                     * (ULONGLONG) Timestamp for version check archive.
                     * (String) Version check archive filename.
                     * (String) Checksum formula.
                     * ** The flags field is currently reserved and must be set to zero or you will be disconnected.
                     * Response:
                     * ---------
                     * (BOOL) Success (TRUE if successful, FALSE otherwise).
                     *        If this is FALSE, the next DWORD is the provided cookie, following which the message ends.
                     * (DWORD) Version.
                     * (DWORD) Checksum.
                     * (String) Version check stat string.
                     * (DWORD) Cookie.
                     * (DWORD) VerByte
                     */

                     ProdID = in.removeDWord();
                     Flags  = in.removeDWord();
                     Cookie = in.removeDWord();
                     long fileTime = in.removeLong();
                     String archive = in.removeNTString();
                     Formula = in.removeNTString();
                     //fileTime = util.BNFTP.fileTimeToMillis(fileTime);
                     
                     checksumEX    = HashMain.getChecksum(ProdID, Formula, archive, fileTime);
					 if(checksumEX != 0){
                       exeInfoEX     = HashMain.getExeInfo(ProdID);
                       versionHashEX = HashMain.getExeVer(ProdID);
                       versionByteEX = HashMain.getVerByte(ProdID);

                       OutPacketBuffer pVerCheckEX2 = new OutPacketBuffer(BNLS_VERSIONCHECKEX2);
                       if (checksumEX == 0 || exeInfoEX == null || versionHashEX == 0 || versionByteEX == 0) {
                     	  pVerCheckEX2.addDWord(0);
                     	  pVerCheckEX2.addDWord(Cookie);
                     	  return pVerCheckEX2;
                       }
                       pVerCheckEX2.addDWord(1);
                       pVerCheckEX2.addDWord(versionHashEX);
                       pVerCheckEX2.addDWord(checksumEX);
                       pVerCheckEX2.addNTString(exeInfoEX);
                       pVerCheckEX2.addDWord(0);
                       pVerCheckEX2.addDWord(versionByteEX);
                       return pVerCheckEX2;
                     }else{
                     	OutPacketBuffer ret = Hashing.CheckRevisionBNLS.checkRevision(Formula, ProdID, archive, fileTime);
                     	if(ret == null){
                          OutPacketBuffer pVerCheckEX22 = new OutPacketBuffer(BNLS_VERSIONCHECKEX2);
                     	  pVerCheckEX22.addDWord(0);
                     	  pVerCheckEX22.addDWord(Cookie);
                     	  return pVerCheckEX22;
                     	}else{
                     		return ret;
                     	}
                     }

                default:
                    Out.error("JBLS Parse", "Unhandled packet 0x" + ((packetID & 0xF0) >> 4) + "" + Integer.toString((packetID & 0x0F) >> 0, 16) );
                    return null;
            }// end of switch

        }catch (IndexOutOfBoundsException e){
            Out.error("JBLS Parse", "Index out of bounds Exception");
            throw new InvalidPacketException("Packet not Long Enough(Array out of Bounds)" + e.toString());
        }// end of tryCatch
    }// end of process input

    private boolean equal(byte[] a, byte[] b){
    	if (a.length != b.length)
    		return false;

    	for (int I = 0; I < a.length; I++)
    		if (a[I] != b[I])
    			return false;

    	return true;
    }
}// end of BNLS Parse class