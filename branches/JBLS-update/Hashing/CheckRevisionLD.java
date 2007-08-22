package Hashing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import util.Buffer;
import util.Constants;
import util.Out;

/** This takes care of the CheckRevision() for the main game files of any program.
 * This is done to prevent tampering and to make sure the version is correct.
 * <P>
 * This function is generally slow because it has to read through the entire
 * files.  The majority of the time is spent in i/o, but I've tried to optimize
 * this as much as possible.
 * @author iago
 */
public class CheckRevisionLD{
    
    private static Hashtable<String, Buffer> crCache = new Hashtable<String, Buffer>();
    private static int crCacheHits = 0;
    private static int crCacheMisses = 0;

    public static void loadCache() {
    	for(int x = 0; x < Constants.IX86files.length; x++){
    	  try{
        	java.io.File file = new java.io.File(Constants.IX86files[x][0] + Constants.IX86files[x][1] + ".ld");
			util.Buffer buff = new util.Buffer();
    		int length = (int) file.length();
    		byte []ret = new byte[length];

    		java.io.InputStream in = new java.io.FileInputStream(file);
    		in.read(ret);
    		buff.add(ret);
    		in.close();
    		while(buff.size()>7){
	    		int mpq = buff.removeByte();
    			String value = buff.removeNTString();
    			int checksum = buff.removeDWord();
    			String exe = buff.removeNTString();
    			Buffer result = new Buffer();
    			result.addDWord(checksum);
    			result.addNTString(exe);
    		    crCache.put(mpq + value + Constants.IX86files[x][0] + Constants.IX86files[x][1], result);
    		}
    	  }catch(java.io.FileNotFoundException fnfex){
    	  }catch(java.io.IOException e){}
    	}
    	Out.println("LDCrev", "Cache Loaded");
    	

    }
    public static int checkRevision(String versionString, String[] files, int mpqNum){ 
    	Buffer cacheHit = crCache.get(mpqNum + versionString + files[0]);
        if(cacheHit != null){
        	crCacheHits++;
            Out.println("CREVLD", "CheckRevision cache hit: " + crCacheHits + "/" + crCacheMisses);
            Buffer newRet = new Buffer(cacheHit.getBuffer());
            return newRet.removeDWord();
        }
        crCacheMisses++;
        Out.println("CREVLD", "CheckRevision cache miss: " + crCacheHits + "/" + crCacheMisses);
    	return 0;
    }
    public static String getExe(String versionString, String[] files, int mpqNum){ 
    	Buffer cacheHit = (Buffer) crCache.get(mpqNum + versionString + files[0]);
        if(cacheHit != null){
            Buffer newRet = new Buffer(cacheHit.getBuffer());
            newRet.removeDWord();
            return newRet.removeNTString();
        }
    	return null;
    }

    public static byte []readFile(File file) throws IOException{
    	int length = (int) file.length();
    	byte []ret = new byte[(length % 1024) == 0 ? length : (length / 1024 * 1024)];

    	InputStream in = new FileInputStream(file);
    	in.read(ret);
    	in.close();

    	return ret;
    }
}