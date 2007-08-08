/*
 * Created on Sep 29, 2004
 *
 *
 */
package Hashing;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import util.Buffer;
import util.Constants;
import util.Out;
import util.PE;
import util.PadString;

/**
 *
 * @author The-FooL
 *
 * This is the main Class that provides hashing functions,
 * CheckRevision, Exe Info, etc provided by BNLS.
 *
 * Static Methods Allow it to be accessible by any thread
 *
 * @throws HashException - If error caused by Hashing or retrieval (bad key, etc)
 *
 */
public class HashMain {
	public static final byte PLATFORM_INTEL   = 0x01;
    public static final byte PLATFORM_POWERPC = 0x02;
    public static final byte PLATFORM_MACOSX  = 0x03;

	//Only compilied once(as needed), then stored
    private static String ExeInfo[]    = new String[0x0B];
	private static int    ExeVer[]     = new int[0x0B];
	public  static int    CRevChecks[] = new int[0x0B];

	public static int WAR3KeysHashed=0;
	public static int STARKeysHashed=0;
	public static int D2DVKeysHashed=0;


	/** Picks appropriate hashing method based on length, and
	 * hashes the CD-Key.
	 *
	 * @param clientToken ClientToken used in hash specified by Client or JBLS
	 * @param serverToken ServerToken used in hash specified by BNET server
	 * @return HashedKey(in a Buffer) - 9 DWORDS
	 * @throws HashException If Invalid Key
	 *
	 * */
	public static Buffer hashKey(int clientToken, int serverToken, String key) throws HashException{
		switch(key.length()){
			case 13://STAR/SEXP
				if(Constants.displayParseInfo)
					Out.println("Hash",">>> STAR Key Hashed");
					STARKeysHashed++;
				return hashSCKey(clientToken, serverToken, key);
			case 16://WAR2/D2DV/D2XP
				if(Constants.displayParseInfo)
					Out.println("Hash",">>> WAR2/D2 Key Hashed");
					D2DVKeysHashed++;
				return hashD2Key(clientToken, serverToken, key);
			case 26://WAR3/W3XP
				if(Constants.displayParseInfo)
					Out.println("Hash",">>> WAR3/FT Key Hashed");
					WAR3KeysHashed++;
				return hashWAR3Key(clientToken, serverToken, key);
		}//end of switch
		throw new HashException("Invalid Key Length");
	}//end of hashKey Method

	private static Buffer hashD2Key(int clientToken, int serverToken, String key) throws HashException{
		D2KeyDecode d2=new D2KeyDecode(key);
		Buffer ret = new Buffer();
        ret.addDWord(key.length());
        ret.addDWord(d2.getProduct());
        ret.addDWord(d2.getVal1());
        ret.addDWord(0);
        int hashedKey[]=d2.getKeyHash(clientToken, serverToken);
        for(int i = 0; i < 5; i++)
            ret.addDWord(hashedKey[i]);
        return ret;
	}

	private static Buffer hashSCKey(int clientToken, int serverToken, String key) throws HashException{

		SCKeyDecode sc=new SCKeyDecode(key);
        Buffer ret = new Buffer();
        ret.addDWord(key.length());
        ret.addDWord(sc.getProduct());
        ret.addDWord(sc.getVal1());
        ret.addDWord(0);
        int hashedKey[]=sc.getKeyHash(clientToken, serverToken);
        for(int i = 0; i < 5; i++)
            ret.addDWord(hashedKey[i]);
        return ret;
	}

	private static Buffer hashWAR3Key(int clientToken, int serverToken, String key){
		War3Decode w3=new War3Decode(key);
		Buffer ret = new Buffer();
        ret.addDWord(key.length());
        ret.addDWord(w3.getProduct());
        ret.addDWord(w3.getVal1());
        ret.addDWord(0);
        int hashedKey[]=w3.getKeyHash(clientToken, serverToken);
        for(int i = 0; i < 5; i++)
            ret.addDWord(hashedKey[i]);
        return ret;
	}

	/** Compiles BNET Exe Info of a given game set
	 * @return Exe Info String
	 * @param files - file list
	 * */
    private static String getExeInfo(String[] files){

        File f = new File(files[0]);
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(f.lastModified()));

        StringBuffer exeInfo = new StringBuffer();
        exeInfo.append(f.getName()).append(" ");
        exeInfo.append(PadString.padNumber(c.get(Calendar.MONTH), 2)).append("/");
        exeInfo.append(PadString.padNumber(c.get(Calendar.DAY_OF_MONTH), 2)).append("/");
        exeInfo.append(PadString.padNumber((c.get(Calendar.YEAR) % 100), 2)).append(" ");
        exeInfo.append(PadString.padNumber(c.get(Calendar.HOUR_OF_DAY), 2)).append(":");
        exeInfo.append(PadString.padNumber(c.get(Calendar.MINUTE), 2)).append(":");
        exeInfo.append(PadString.padNumber(c.get(Calendar.SECOND), 2)).append(" ");
        exeInfo.append(f.length());

        return exeInfo.toString();
    }

    public static int getVerByte(int prod){
    	if(prod <= 0) return 0;
    	if(prod > Constants.prods.length + 1) return 0;
    	if (Constants.displayParseInfo) Out.info("JBLS", ">>> [" + Constants.prods[prod-1] + "] Verbyte");
    	return Constants.IX86verbytes[prod-1];
    }

    public static String getExeInfo(int prod){
    	if (ExeInfo[prod] != null) return ExeInfo[prod];
    	String[] files = getFiles(prod, PLATFORM_INTEL);
    	if(files == null) return null;
        ExeInfo[prod] = getExeInfo(files);
        return ExeInfo[prod];
    }

    public static int getExeVer(int prod){
    	if (ExeVer[prod] != 0) return ExeVer[prod];
    	String[] files = getFiles(prod, PLATFORM_INTEL);
    	if(files == null) return 0;
    	try{
	    	if(prod == Constants.PRODUCT_WARCRAFT3 || prod == Constants.PRODUCT_THEFROZENTHRONE){
		    	ExeVer[prod] = PE.getVersion(files[0], true);
				ExeVer[prod] = (ExeVer[prod] & 0xFF000000) >>> 24 |
						 	   (ExeVer[prod] & 0x00FF0000) >> 8 |
						       (ExeVer[prod] & 0x0000FF00) << 8 |
						       (ExeVer[prod] & 0x000000FF) << 24;
	    	}else{
	    		ExeVer[prod] = PE.getVersion(files[0], false);
	    	}
        	return ExeVer[prod];
		}catch(FileNotFoundException e){
			Out.error("HashMain", "Hash Exception(Exe version): \n\r" +
			          "[getExeVer] File Not Found/Accessible (" + Constants.prods[prod-1] + ") (" + files[0] + ")");
		}catch(IOException e){
			Out.error("HashMain", "Hash Exception(Exe Version): [getExeVer] IOException (" + Constants.prods[prod-1] + ")");
		}
		return 0;
    }

    public static int getChecksum(int prod, String formula, int dll){
		return getChecksum(prod, formula, dll, PLATFORM_INTEL, 2);
    }
    public static int getChecksum(int prod, String formula, int dll, int platform, int ver){
    	String[] files = null;
    	try{
        	files = getFiles(prod, platform);
		    if (files == null) return 0;
            CRevChecks[prod-1]++;
            if (Constants.displayParseInfo) Out.info("HashMain", ">>> [" + Constants.prods[prod-1] + "] Version Check V"+ver);
            switch(ver){
            	case 1: return CheckRevisionV1.checkRevision(formula, files, dll);
            	case 2:	return CheckRevision.checkRevision(formula, files, dll);
            	default:return CheckRevision.checkRevision(formula, files, dll);
            }
		}catch(FileNotFoundException e){
			Out.error("HashMain", "Hash Exception(version check): \n\r" +
			          "[CheckRevision] Files Not Found/Accessible (" + Constants.prods[prod-1] + ") (" + files[0] + ", " + files[1] + ", " + files[2] + ")");
		}catch(IOException e){
			Out.error("HashMain", "Hash Exception(version check): [CheckRevision] IOException (" + Constants.prods[prod-1] + ")");
		}
		return 0;
    }

    public static int getChecksum(int prod, String formula, String dll, long filetime){
      if(dll.matches("ver-IX86-[0-7].mpq") == true) return getChecksum(prod, formula, dll.charAt(9) - 0x30, PLATFORM_INTEL, 2);
      if(dll.matches("ver-XMAC-[0-7].mpq") == true) return getChecksum(prod, formula, dll.charAt(9) - 0x30, PLATFORM_MACOSX, 2);
      if(dll.matches("ver-PMAC-[0-7].mpq") == true) return getChecksum(prod, formula, dll.charAt(9) - 0x30, PLATFORM_POWERPC, 2);

      if(dll.matches("IX86ver-[0-7].mpq") == true) return getChecksum(prod, formula, dll.charAt(7) - 0x30, PLATFORM_INTEL, 1);
      if(dll.matches("XMACver-[0-7].mpq") == true) return getChecksum(prod, formula, dll.charAt(7) - 0x30, PLATFORM_MACOSX, 1);
      if(dll.matches("PMACver-[0-7].mpq") == true) return getChecksum(prod, formula, dll.charAt(7) - 0x30, PLATFORM_POWERPC, 1);

      Out.info("CHSUM", "Unknown archive: " + dll + ", Filetime: 0x" + Long.toHexString(filetime));
      return 0;
    }

    public static String[] getFiles(int prod, int plat){
      String[] ret = {"", "", ""};
      if(prod < 0) return null;
      if(prod > Constants.prods.length + 1) return null;
      
      switch(plat){
        case PLATFORM_INTEL:
          ret[0] = Constants.IX86files[prod-1][0] + Constants.IX86files[prod-1][1];
          ret[1] = Constants.IX86files[prod-1][0] + Constants.IX86files[prod-1][2];
          ret[2] = Constants.IX86files[prod-1][0] + Constants.IX86files[prod-1][3];          
          break;
    	/*case PLATFORM_POWERPC:
          break;
    	case PLATFORM_MACOSX:
    	  break;*/
        default: ret = null;
      }
      return ret;
    }
}