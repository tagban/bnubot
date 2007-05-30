/*
 * Created on Dec 18, 2004
 */
package Admin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import util.Constants;

import util.Out;
/**
 * @author The-FooL
 * IpAuth Class
 *
 * This Class Stores the IP Authorization/Banning list
 * It Keeps Track of Who Connects and when, and performs any necessary
 * IPBanning
 *
 * This Class and all members are static, in order to be accessible
 * to all connection threads
 *
 */
public class IpAuth {

	/* Constants for the IP Authorization Status*/

	public static final int IPNORESTRICTIONS=0;//All connections are completly unrestricted
	public static final int IPBANNING=1;       //Enables IPBanning of IPs
	public static final int IPRESTRICTED=2;    //Only allows Authorized IPs to connect
	public static final int IPLOCALONLY=3;     //Only allows Local IPs to connect (prefix 10, 192, 127


	public static boolean bAddNewIPs=false; //Create a new IP tracer for new IP addresses.

	private static Hashtable<String, Object> IpList;

	/** Constructs the Class and Initializes the IPList*/
	public static void initializeAuth(){
		IpList=readIPList();
		if(IpList==null){
			Out.println("IP Auth","No IP List Loaded");
			IpList=new Hashtable<String, Object>();
		}else{
			Out.println("IP Auth","IP List loaded from 'ips.txt'");
			Out.println("IP Auth","IPs Listed:"+IpList.size());
		}
	}

	public static String getIpList(){
		StringBuffer ret=new StringBuffer();
		Enumeration en=IpList.keys();
		while(en.hasMoreElements()){
			String tempIp=(String)en.nextElement();
			IpTracker tempTrack=(IpTracker)IpList.get(tempIp);
			ret.append(tempIp+" Status:"+tempTrack.getStatus()+" Connections:"+tempTrack.getConnectionCount()+"\n\r");			Out.println("IpSAVE",tempIp+" "+tempTrack.getStatus()+" "+tempTrack.getConnectionCount()+"\r\n");
		}
		return ret.toString();
	}

	/******************************************************
	 *@param IP - IP address to be checked, as a string   *
	 *@return boolean whether the IP is allowed to connect*
	 *@SuppressWarnings("fallthrough")                    *
	 ******************************************************/
	public static boolean checkAuth(String IP)
	{

		int thisIpStatus=IpTracker.STATUSNORMAL;//default status if they arn't found
		IpTracker thisIp=null;//tracker object for this connecting IP Address
		boolean allow=false;//whether we successfully passed this object

		if(IpList!=null){
			thisIp=(IpTracker)IpList.get(IP);//get the tracker for this one
			if(thisIp!=null){
				thisIpStatus=thisIp.getStatus();
				thisIp.connect();
			}else{
				//create a new tracker object for this IP
				if(bAddNewIPs) {
			      thisIp = new IpTracker(0,IpTracker.STATUSNORMAL);
				  IpList.put(IP,thisIp);
				  thisIp.connect();
				}

			}
		}//end outter if block

		switch(Constants.ipAuthStatus) //make choice based on our status
		{
			// Everything passes
			case IPNORESTRICTIONS:
				allow=true;
				break;

			// Check for banned IPs
			case IPBANNING:
				if(thisIpStatus!=IpTracker.STATUSBANNED)
					allow=true;
				break;

			case IPRESTRICTED:
				allow = (thisIpStatus==IpTracker.STATUSAUTHORIZED) ? true : false;
				String comp=IP.substring(0,3);
				if(comp.equals("127")||comp.equals("192")||comp.equals("10."))
					allow=true;
                break;

			case IPLOCALONLY:
				comp=IP.substring(0,3);//get first 3 chars of the IP
				if(comp.equals("127")||comp.equals("192")||comp.equals("10.")){//if its a local IP
					allow=true;
				}
		}// End Switch Statement

		if(allow && thisIp != null)
			thisIp.connect();

		return allow;
	}

	/**
	 * Sets an IPAddress to be changed/added
	 * @param ip IP Address to be changed
	 * @param status Status the IP should be set to
	 */
	public static void setIP(String ip, int status){
		IpTracker thisIp=(IpTracker)IpList.get(ip);
		if(thisIp==null)
			thisIp=new IpTracker(0,0);
		IpList.put(ip, new IpTracker(thisIp.getConnectionCount(),status));
		saveIpList();
	}


	private static Hashtable<String, Object> readIPList()
	{
		try
		{
			new File("ips.txt").createNewFile();
			BufferedReader in=new BufferedReader(new FileReader("ips.txt"));
			Hashtable<String, Object> ret=new Hashtable<String, Object>();

			String lin, ip; // temp reading vars
			int c, s; // C- connection count S- Status (banned/authorized/normal
			while((lin = in.readLine()) != null) // As long as data to read
			{
				StringTokenizer st=new StringTokenizer(lin, " ");
				ip=st.nextToken();
				c=0;s=0;
				try
				{
					s=Integer.parseInt(st.nextToken());
					c=Integer.parseInt(st.nextToken());
				}catch(NoSuchElementException e){
					Out.error("IP List","'"+lin+"' Params Not Fully Specified");
				}
				if(ip.length()<7)
					Out.error("IP List","'"+lin+"' Invalid IP Address");
				else
					ret.put(ip,new IpTracker(c,s));
			}//end while loop
			in.close();
			return ret;
		}catch(FileNotFoundException e){
			Out.error("IP Auth","No IP list found (ips.txt)");
		}catch(IOException e){
			Out.error("IP Auth","Error reading IP list: "+e.toString());
		}

		return null;
	} // End get IP List class

	/**
	 * Saves the ipList to Ips.txt
	 * @return Whether the save was sucessful
	 */
	public static boolean saveIpList(){
		if(IpList==null){
			Out.error("IP Auth","Error Saving IP List - No IP List in Memory");
			return false;
		}

		File fil=new File("ips.txt");
		try{
			//make file if it doesn't exist
			if(fil.createNewFile())
				Out.info("IP Auth","New IP File Created('ips.txt')");
			PrintWriter pr=new PrintWriter(new FileOutputStream(fil,false));
			//System.out.println(IpList.toString());
			Enumeration en=IpList.keys();
			while(en.hasMoreElements()){
				String tempIp=(String)en.nextElement();
				IpTracker tempTrack=(IpTracker)IpList.get(tempIp);
				pr.println(tempIp+" "+tempTrack.getStatus()+" "+tempTrack.getConnectionCount());
				Out.debug("IpSAVE",tempIp+" "+tempTrack.getStatus()+" "+tempTrack.getConnectionCount());
			}
			pr.close();
		}catch(IOException e){
			Out.error("IP Auth","Error Saving IP List: "+e.toString());
			return false;
		}
		return true;
	}//end saveIpList

	/**
	 * Setter for IpAuthStatus to make sure a correct value is used
	 * @param s status to bet set to, see this class's constants
	 */
	public static void setIpAuthStatus(int s){
		if(s<0)
			s=0;
		if(s>3)
			s=3;
		Constants.ipAuthStatus=s;
	}//end setipauthstatus

}
