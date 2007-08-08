package BNLSProtocol;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import util.Out;

public class BNLSlist {

	private static int numOfIDs;
	private static String[] IDNames;
	private static String[] IDPasses;

  	private static final int CRC32_POLYNOMIAL = 0xEDB88320;
    private static long CRC32Table[] = new long[256];
	private static boolean CRC32Initialized = false;	
  
  static public void LoadUsers () {
  	StringBuffer contents = new StringBuffer();
  	BufferedReader input = null;
    File IDFile = new File("BotIDs.txt");
  	String line = null;
  	try {
    		if (!IDFile.exists()) IDFile.createNewFile();
  			input = new BufferedReader( new FileReader(IDFile) );
  			numOfIDs = 0;
  			while (( line = input.readLine()) != null) {
  					numOfIDs += 1;
			}	
			Out.println("BotIDs", "Loaded "+Integer.toString(numOfIDs)+" BNLS Bot accounts.");			
  		} catch (FileNotFoundException ex) {
  			Out.println( "BotIDs", "BotIDs file not found." );
  			return;
  		} catch (IOException ex) {
      		Out.println( "BotIDs", "Error reading from file: "+ex.toString());
    	} finally {
  			try {
  					if (input != null) { input.close();	}
  				} catch (IOException ex) {
      				Out.println( "BotIDs", "Error closing file: "+ex.toString());
  				}	
  		} 
  		
  		IDNames = new String[numOfIDs];
  		IDPasses = new String[numOfIDs];
  		
  		try {
  			input = new BufferedReader( new FileReader(IDFile) );
  			line = null;
  			int X = -1;
  			while (( line = input.readLine()) != null) {
    			StringTokenizer st=new StringTokenizer(line, " ");
  					X++;
  					IDNames[X] = st.nextToken().toLowerCase();
  					IDPasses[X] = st.nextToken().toLowerCase();
				}				
  		} catch (FileNotFoundException ex) {
  			Out.println( "BotIDs", "BotIDs file not found." );
  		} catch (IOException ex) {
      		Out.println( "BotIDs", "Error reading from file: "+ex.toString());
    	} finally {
  			try {
  				if (input != null) { input.close();	}
  			} catch (IOException ex) {
      			Out.println( "BotIDs", "Error closing file: "+ex.toString());
  			}	
  		}
  	}
  	
  static public String GetPassword(String sID) {
  	for (int Y = 0; Y < numOfIDs; Y++) {
  		if (IDNames[Y].toLowerCase().equals(sID.toLowerCase())) {
  			return IDPasses[Y];
  		}
  	}
  	return null;
  }
  
  
  

	private static void InitCRC32()
	{
		if(CRC32Initialized)
			return;
		CRC32Initialized = true;
		for(int I = 0; I < 256; I++) {
			long K = I;
			
			for(long J = 0; J < 8; J++){
				long XorVal = (((K & 1) > 0)  ? CRC32_POLYNOMIAL : 0);
				
				if (K < 0)
					K = ((K & 0x7FFFFFFF) / 2) | 0x40000000;
				else
					K /= 2;
					
				K ^= XorVal;
			}
				  //K = (K >> 1) ^ (((K & 1) > 0)  ? CRC32_POLYNOMIAL : 0);
			CRC32Table[I] = K;
		}
	}

	private static long CRC32(char[] Data, long Size)
	{
		InitCRC32();
		long CRC = 0xffffffff;
		int tableIndex = 0;
		for (int X = 0; X < Size; X++){
			tableIndex = (int)((CRC & 0xff) ^ Data[X]);
			
			if (CRC < 0)
				CRC = ((CRC & 0x7FFFFFFF) / 0x100) | 0x800000;
			else
				CRC /= 0x100;
				
			CRC ^= CRC32Table[tableIndex];
			
		      //CRC = (CRC >> 8)    ^ CRC32Table[(int)((CRC & 0xff) ^ Data[X])];
		}
		return ~CRC;
	}

	private static char Hex(char Digit)
	{
		if(Digit < 10)
			return (char)(Digit + '0');
		else
			return (char)(Digit - 10 + 'A');
	}

	public static long BNLSChecksum(String Password, long ServerCode)
	{
		long Size = Password.length();
		char[] Data = new char[(int)(Size + 8)];
		for (int X = 0; X < Size; X++)
			Data[X] = Password.charAt(X);
		int I = 7;
		do {
			Data[(int)Size + I] = Hex((char)((char)ServerCode & 0xf));
			ServerCode >>= 4;
		}while((I-- > 0));
		long Checksum = CRC32(Data, Size + 8);
		return Checksum;
	}

  
}