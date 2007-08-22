package Hashing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.StringTokenizer;
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
public class CheckRevision
{
    /** These are the hashcodes for the various .mpq files. */
    private static final int hashcodes[] = { 0xE7F4CB62, 0xF6A14FFC, 0xAA5504AF, 0x871FCDC2, 0x11BF6A18, 0xC57292E6, 0x7927D27E, 0x2FEC8733 };

    /** Stores some past results */
    private static Hashtable<String, Integer> crCache = new Hashtable<String, Integer>();
    private static int crCacheHits = 0;
    private static int crCacheMisses = 0;

    /** Does the actual version check.
     * @param versionString The version string.  This is recieved from Battle.net in 0x50 (SID_AUTH_INFO) and
     * looks something like "A=5 B=10 C=15 4 A=A+S B=B-A A=S+B C=C-B".
     * @param files The array of files we're checking.  Generally the main game files, like
     * Starcraft.exe, Storm.dll, and Battle.snp.
     * @param mpqNum The number of the mpq file, from 1..7.
     * @throws FileNotFoundException If the datafiles aren't found.
     * @throws IOException If there is an error reading from one of the datafiles.
     * @return The 32-bit CheckRevision hash.
     */
    public static void main(String[] args){
    	try{
    	String dir = "IX86";
    	String vs[] = {
          "C=502518104 A=3046850123 B=2644909248 4 A=A-S B=B^C C=C^A A=A^B"//,
          //"A=3890603401 C=2543385810 B=2612801343 4 A=A^S B=B+C C=C+A A=A+B",
          //"A=1249383011 B=2387119329 C=319668889 4 A=A+S B=B^C C=C^A A=A^B",
          //"B=1324766345 A=17937714 C=3208736877 4 A=A-S B=B+C C=C+A A=A+B",
          //"A=284179296 C=1987738994 B=999863123 4 A=A-S B=B-C C=C+A A=A^B"
        };
        String[][] files = {
          //{dir+"/WAR3/war3.exe",            dir+"/WAR3/storm.dll",    dir+"/WAR3/game.dll"},
	      //{dir+"/W2BN/Warcraft II BNE.exe", dir+"/W2BN/storm.dll",    dir+"/W2BN/battle.snp"},
        	{dir+"/STAR/starcraft.exe",       dir+"/STAR/storm.dll",    dir+"/STAR/battle.snp"}
	      //{dir+"/D2DV/game.exe",            dir+"/D2DV/Bnclient.dll", dir+"/D2DV/D2Client.dll"},
	      //{dir+"/D2XP/game.exe",            dir+"/D2XP/Bnclient.dll", dir+"/D2XP/D2Client.dll"},
	      //{dir+"/JSTR/starcraftj.exe",      dir+"/JSTR/storm.dll",    dir+"/JSTR/battle.snp"}
        };
        for (int x = 0; x < vs.length; x++){
        	System.out.println("Value String: " + vs[x]);
        	for (int y = 0; y < 8; y++){
        		System.out.println(y+": " +
        		  //" WAR3: " + Long.toHexString((int)checkRevision(vs[x], files[0], y)) +
        		  //" W2BN: " + Long.toHexString((int)checkRevision(vs[x], files[1], y)) +
        		  " STAR: " + Long.toHexString((int)checkRevision(vs[x], files[0], y))
        		  //" D2DV: " + Long.toHexString((int)checkRevision(vs[x], files[3], y)) +
        		  //" D2XP: " + Long.toHexString((int)checkRevision(vs[x], files[4], y)) +
        		  //" JSTR: " + Long.toHexString((int)checkRevision(vs[x], files[5], y))
        		);
        	}
        }
    	}catch(Exception e){System.out.println(e.toString());}
    }
    public static int checkRevision(String versionString, String[] files, int mpqNum) throws FileNotFoundException, IOException
    {
        Integer cacheHit = (Integer) crCache.get(versionString + mpqNum + files[0]);
        if(cacheHit != null){
            Out.println("CREV", "CheckRevision cache hit: " + crCacheHits + " hits, " + crCacheMisses + " misses.");
            crCacheHits++;
            return cacheHit.intValue();
        }

        crCacheMisses++;
        if(Constants.debugInfo)
		Out.println("CREV", "CheckRevision cache miss: " + crCacheHits + " hits, " + crCacheMisses + " misses.");

        // Break this apart at the spaces
        StringTokenizer tok = new StringTokenizer(versionString, " ");

        // Get the values for a, b, and c
        long a=0,b=0,c=0;

        for(int x = 0; x<3; x++){
          String seed = tok.nextToken();
          if(seed.toLowerCase().startsWith("a=") == true) a = Long.parseLong(seed.substring(2));
          if(seed.toLowerCase().startsWith("b=") == true) b = Long.parseLong(seed.substring(2));
          if(seed.toLowerCase().startsWith("c=") == true) c = Long.parseLong(seed.substring(2));
        }
        tok.nextToken();
        if (a == 0 || b == 0 || c == 0) return 0;
        String formula;

        formula = tok.nextToken();
        if(formula.matches("A=A.S") == false) return checkRevisionSlow(versionString, files, mpqNum);
        char op1 = formula.charAt(3);

        formula = tok.nextToken();
        if(formula.matches("B=B.C") == false) return checkRevisionSlow(versionString, files, mpqNum);
        char op2 = formula.charAt(3);

        formula = tok.nextToken();
        if(formula.matches("C=C.A") == false) return checkRevisionSlow(versionString, files, mpqNum);
        char op3 = formula.charAt(3);

        formula = tok.nextToken();
        if(formula.matches("A=A.B") == false) return checkRevisionSlow(versionString, files, mpqNum);
        char op4 = formula.charAt(3);


        // Now we actually do the hashing for each file
        // Start by hashing A by the hashcode
        a ^= hashcodes[mpqNum];

        for(int i = 0; i < files.length; i++)
        {
            File currentFile = new File(files[i]);

            byte []data = CheckRevision.readFile(currentFile);
            //System.out.println("File length: " + currentFile.length());
            //System.out.println("Padded length: " + data.length);

            for(int j = 0; j < data.length; j += 4)
            {
                int s = 0;
                s |= ((data[j+0] << 0) & 0x000000ff);
                s |= ((data[j+1] << 8) & 0x0000ff00);
                s |= ((data[j+2] << 16) & 0x00ff0000);
                s |= ((data[j+3] << 24) & 0xff000000);

				switch (op1) {
					case '^': a ^= s; break;
					case '+': a += s; break;
					case '-': a -= s; break;
					case '*': a *= s; break;
					case '/': a /= s; break;
				}
				switch (op2) {
					case '^': b ^= c; break;
					case '+': b += c; break;
					case '-': b -= c; break;
					case '*': b *= c; break;
					case '/': b /= c; break;
				}
				switch (op3) {
					case '^': c ^= a; break;
					case '+': c += a; break;
					case '-': c -= a; break;
					case '*': c *= a; break;
					case '/': c /= a; break;
				}
				switch (op4) {
					case '^': a ^= b; break;
					case '+': a += b; break;
					case '-': a -= b; break;
					case '*': a *= b; break;
					case '/': a /= b; break;
				}
            }

            //System.err.format("After %s:\n", currentFile);
            //System.err.format("A = %08x           B = %08x          C = %08x\n", a, b, c);
        }

        crCache.put(versionString + mpqNum + files[0], new Integer((int)c));

        return (int)c;
    }


    /** This is an alternate implementation of CheckRevision.  It it slower (about 2.2 times slower), but it can handle
     * weird version strings that Battle.net would never send.  Battle.net's version strings are _always_ in the form:
     * A=x B=y C=z 4 A=A?S B=B?C C=C?A A=A?B:
     * C=1151438134 A=2788537374 B=2369803856 4 A=A-S B=B+C C=C^A A=A^B
     * A=1054538081 B=741521288 C=797042342 4 A=A^S B=B-C C=C^A A=A+B
     *
     * If, for some reason, the string in checkRevision() doesn't match up, this will run.
     *
     * @param versionString The version string.  This is recieved from Battle.net in 0x50 (SID_AUTH_INFO) and
     * looks something like "A=5 B=10 C=15 4 A=A+S B=B-A A=S+B C=C-B".
     * @param files The array of files we're checking.  Generally the main game files, like
     * Starcraft.exe, Storm.dll, and Battle.snp.
     * @param mpqNum The number of the mpq file, from 1..7.
     * @throws FileNotFoundException If the datafiles aren't found.
     * @throws IOException If there is an error reading from one of the datafiles.
     * @return The 32-bit CheckRevision hash.
     */
    private static int checkRevisionSlow(String versionString, String[] files, int mpqNum) throws FileNotFoundException, IOException
    {
        System.out.println("Warning: using checkRevisionSlow for version string: " + versionString);

        // First, parse the versionString to name=value pairs and put them
        // in the appropriate place
        int[] values = new int[4];

        int[] opValueDest = new int[4];
        int[] opValueSrc1 = new int[4];
        char[] operation = new char[4];
        int[] opValueSrc2 = new int[4];

        // Break this apart at the spaces
        StringTokenizer s = new StringTokenizer(versionString, " ");
        int currentFormula = 0;
        while(s.hasMoreTokens())
        {
            String thisToken = s.nextToken();
            // As long as there is an '=' in the string
            if(thisToken.indexOf('=') > 0)
            {
                // Break it apart at the '='
                StringTokenizer nameValue = new StringTokenizer(thisToken, "=");
                if(nameValue.countTokens() != 2)
                    return 0;

                int variable = getNum(nameValue.nextToken().charAt(0));

                String value = nameValue.nextToken();

                // If it starts with a number, assign that number to the appropriate variable
                if(Character.isDigit(value.charAt(0)))
                {
                    values[variable] = Integer.parseInt(value);
                }
                else
                {
                    opValueDest[currentFormula] = variable;

                    opValueSrc1[currentFormula] = getNum(value.charAt(0));
                    operation[currentFormula] = value.charAt(1);
                    opValueSrc2[currentFormula] = getNum(value.charAt(2));

                    currentFormula++;
                }
            }
        }

        // Now we actually do the hashing for each file
        // Start by hashing A by the hashcode
        values[0] ^= hashcodes[mpqNum];

        for(int i = 0; i < files.length; i++)
        {
            File currentFile = new File(files[i]);

            byte []data = readFile(currentFile);

            for(int j = 0; j < data.length; j += 4)
            {
                values[3] = 0;
                values[3] |= ((data[j+0] << 0) & 0x000000FF);
                values[3] |= ((data[j+1] << 8) & 0x0000ff00);
                values[3] |= ((data[j+2] << 16) & 0x00ff0000);
                values[3] |= ((data[j+3] << 24) & 0xff000000);

                for(int k = 0; k < currentFormula; k++)
                {
                    switch(operation[k])
                    {
                        case '+':
                            values[opValueDest[k]] = values[opValueSrc1[k]] + values[opValueSrc2[k]];
                            break;

                        case '-':
                            values[opValueDest[k]] = values[opValueSrc1[k]] - values[opValueSrc2[k]];
                            break;

                        case '^':
                            values[opValueDest[k]] = values[opValueSrc1[k]] ^ values[opValueSrc2[k]];
                    }
                }
             }
        }

        //crCache.put(versionString + mpqNum + files[0], new Integer(values[2]));

        return values[2];
    }

    /** Converts the parameter to which number in the array it is, based on A=0, B=1, C=2, S=3.
     * @param c The character letter.
     * @return The array number this is found at.
     */
    private static int getNum(char c)
    {
        c = Character.toUpperCase(c);
        if(c == 'S')
            return 3;

        return c - 'A';
    }

    public static byte []readFile(File file) throws IOException
    {
    	int length = (int) file.length();
    	byte []ret = new byte[(length % 1024) == 0 ? length : (length / 1024 * 1024) + 1024];

    	InputStream in = new FileInputStream(file);
    	in.read(ret);
    	in.close();

    	int value = 0xFF;
    	for(int i = (int) file.length(); i < ret.length; i++)
    		ret[i] = (byte) value--;

    	return ret;
    }
}