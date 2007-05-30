// Created on Dec 18, 2004

package Admin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Timer;
import java.util.TimerTask;

import util.Constants;
import util.Out;
import BNLSProtocol.BNLSConnectionThread;
import BNLSProtocol.BNLSParse;
import Hashing.HashMain;

/**
 * @author The-FooL
 * Admin Settings Class
 *
 * Stores All Settings Related to the Adminstration of the JBLS
 *
 */

public class AdminSettings {

	// Constructor
	// Initializes all Vars, etc
	// private boolean trackStatistics=true;

	//Timer Vars
	private Timer timer;

	/**
	 * Constructs class at sets all defaults for the program
	 *
	 */
	public AdminSettings(){

	}

	/**Loads all the settings
	 *@return Whether all settings were loaded successfully
	 */
	public boolean load()
	{
		IpAuth.initializeAuth();

		if(Constants.trackStatistics){//enable the timer to automattically record the statistics every hour
			if(timer!=null)//cancel it if its already running
				timer.cancel();
			timer=new Timer();
			timer.schedule(new StatisticsTask(), 3600000,3600000);//1 hours
		}


		return true;
	}

	//Save the settings to the file
	//@return Whether save was successfull
	public boolean save()
	{
		boolean success=true;
		success=IpAuth.saveIpList();
		return success;
	}

    public String getStats(){
      return getStats(true);
    }

	public String getStats(boolean time){
		String ts = "";
		if (time == true)
		  ts = Out.getTimestamp() + " ";

		StringBuffer toWrite=new StringBuffer();
		toWrite.append(ts+"Connections: "+BNLSConnectionThread.connectionCount+"\r\n");
		toWrite.append(ts+"Admin Connections: "+AdminConnectionThread.connectionCount+"\r\n");
		toWrite.append(ts+"STAR KEYS: "+HashMain.STARKeysHashed+"\r\n");
		toWrite.append(ts+"D2/WAR2 KEYS: "+HashMain.D2DVKeysHashed+"\r\n");
		toWrite.append(ts+"WAR3 KEYS: "+HashMain.WAR3KeysHashed+"\r\n");
		for(int x = 0; x < Constants.prods.length; x++)
			toWrite.append(ts + Constants.prods[x] + " CHECKS: "+HashMain.CRevChecks[x]+"\r\n");
		if(BNLSParse.botIds!=null)
			toWrite.append(ts+"BOT IDs: "+BNLSParse.botIds.toString()+"\r\n");
		return toWrite.toString();
	}

	/**
	 *
	 * @author FooL
	 * Statistics Sub-Timer Class.  Stores all the statistics to a file
	 * When it is run
	 */
	private class StatisticsTask extends TimerTask{

		public StatisticsTask(){
			Out.debug("Statistics","Task Created");
		}

		public void run(){//run
			saveStats();
		}

		private void saveStats(){
			File fil=new File("stats.txt");

			try{
				//make file if it doesn't exist
				if(fil.createNewFile())
					Out.println("Statistics","New Statistics File Created('stats.txt')");
				PrintWriter pr=new PrintWriter(new FileOutputStream(fil,true));
				String toWrite=getStats();
				//reset it all

				BNLSConnectionThread.connectionCount=0;
				AdminConnectionThread.connectionCount=0;
				HashMain.STARKeysHashed=0;
				HashMain.D2DVKeysHashed=0;
				HashMain.WAR3KeysHashed=0;
				for(int x = 0; x < 0x0B; x++)
					HashMain.CRevChecks[x] = 0;
				BNLSParse.botIds=null;

				pr.println(toWrite);
				pr.close();
				Out.println("Statistics","Statistics Saved to File");



			}catch(IOException e){
				Out.error("Statistics","Error Saving Statistics: "+e.toString());
			}//end try-catch
		}//end save stats
	}//end statisticstask subclass


}//end class
