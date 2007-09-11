/**
 * This file is distributed under the GPL 
 * $Id$
 */
package net.bnubot.core.bnls;

public class BNLSCommandIDs {
	public static final byte BNLS_NULL                 = 0x00; //Fully Supported
    public static final byte BNLS_CDKEY                = 0x01; //Fully Supported
    public static final byte BNLS_LOGONCHALLENGE       = 0x02; //Fully Supported
    public static final byte BNLS_LOGONPROOF           = 0x03; //Fully Supported
    public static final byte BNLS_CREATEACCOUNT        = 0x04; //Fully Supported
    public static final byte BNLS_CHANGECHALLENGE      = 0x05; //Fully Supported
    public static final byte BNLS_CHANGEPROOF          = 0x06; //Fully Supported
    public static final byte BNLS_UPGRADECHALLENGE     = 0x07; //Fully Supported
    public static final byte BNLS_UPGRADEPROOF         = 0x08; //fully Supported
    public static final byte BNLS_VERSIONCHECK         = 0x09; //Fully Supported
    public static final byte BNLS_CONFIRMLOGON         = 0x0a; //Fully Supported
    public static final byte BNLS_HASHDATA             = 0x0b; //Fully Supported
    public static final byte BNLS_CDKEY_EX             = 0x0c; //Fully Supported
    public static final byte BNLS_CHOOSENLSREVISION    = 0x0d; //Fully Supported
    public static final byte BNLS_AUTHORIZE            = 0x0e; //Fully Supported
    public static final byte BNLS_AUTHORIZEPROOF       = 0x0f; //Fully Supported
    public static final byte BNLS_REQUESTVERSIONBYTE   = 0x10; //Fully Supported
    public static final byte BNLS_VERIFYSERVER         = 0x11; //Fully Supported
    public static final byte BNLS_RESERVESERVERSLOTS   = 0x12; //Fully Supported
    public static final byte BNLS_SERVERLOGONCHALLENGE = 0x13; //Fully Supported
    public static final byte BNLS_SERVERLOGONPROOF     = 0x14; //Fully Supported
    public static final byte BNLS_RESERVED0            = 0x15;
    public static final byte BNLS_RESERVED1            = 0x16;
    public static final byte BNLS_RESERVED2            = 0x17;
    public static final byte BNLS_VERSIONCHECKEX       = 0x18; //Fully Supported
    public static final byte BNLS_RESERVED3            = 0x19;
    public static final byte BNLS_VERSIONCHECKEX2      = 0x1A; //Supported {Lockdown Needs to be reversed}
}
