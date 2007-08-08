package Admin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import util.Out;

public class AdminList {

	private static int numOfAdmins;
	private static String[] AdminNames;
	private static String[] AdminPasses;
	
  static public void LoadUsers () {
  	StringBuffer contents = new StringBuffer();
  	BufferedReader input = null;
    File AdminFile = new File("Admins.txt");
  	String line = null;
  	try {
    		if (!AdminFile.exists()) AdminFile.createNewFile();
  			input = new BufferedReader( new FileReader(AdminFile) );
  			numOfAdmins = 0;
  			while (( line = input.readLine()) != null) {
  					numOfAdmins += 1;
			}	
			Out.println("Admin", "Loaded "+Integer.toString(numOfAdmins)+" admin accounts.");			
  		} catch (FileNotFoundException ex) {
  			Out.println( "Admin", "Admins file not found." );
  			return;
  		} catch (IOException ex) {
      		Out.println( "Admin", "Error reading from file: "+ex.toString());
    	} finally {
  			try {
  					if (input != null) { input.close();	}
  				} catch (IOException ex) {
      				Out.println( "Admin", "Error closing file: "+ex.toString());
  				}	
  		} 
  		
  		AdminNames = new String[numOfAdmins];
  		AdminPasses = new String[numOfAdmins];
  		
  		try {
  			input = new BufferedReader( new FileReader(AdminFile) );
  			line = null;
  			int X = -1;
  			while (( line = input.readLine()) != null) {
    			StringTokenizer st=new StringTokenizer(line, " ");
  					X++;
  					AdminNames[X] = st.nextToken().toLowerCase();
  					AdminPasses[X] = st.nextToken().toLowerCase();
				}				
  		} catch (FileNotFoundException ex) {
  			Out.println( "Admin", "Admins file not found." );
  		} catch (IOException ex) {
      		Out.println( "Admin", "Error reading from file: "+ex.toString());
    	} finally {
  			try {
  					if (input != null) { input.close();	}
  				} catch (IOException ex) {
      				Out.println( "Admin", "Error closing file: "+ex.toString());
  				}	
  		}
  	}
  	
  static public boolean CorrectLogin(String sUsername, String sPassword) {
  	for(int Y = 0; Y < numOfAdmins; Y++) {
  		if (AdminNames[Y].toLowerCase().equals(sUsername.toLowerCase())) {
  			if (AdminPasses[Y].toLowerCase().equals(sPassword.toLowerCase())) {
  				return true;
  			} else {
  				return false;
  			}
  		}
  	}
  	return false;
  }
}