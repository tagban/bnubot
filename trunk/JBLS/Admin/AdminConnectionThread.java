/*
 * Created on Feb 20, 2005
 */
package Admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.lang.NullPointerException;

import util.Out;
import util.Constants;
/**
 * @author FooL
 * 
 * Individual Thread for Controlling each Admin Connection
 */
public class AdminConnectionThread extends Thread{
	/** Static Total # of Admin Connections*/
	public static int connectionCount=0;
	
	/** Thread's Socket*/
    private Socket socket = null;
    
    private PrintWriter out=null;
    //private OutputStream out=null;
    private BufferedReader in=null;
    //private BufferedReader inStream=null;
    
    /** Current Thread Count */
    private static int threadCount=0;
    
    /** Maximum Connection Threads Allowed */
    /** private final int maxThreads=5; */
    
    /** Thread ID of this instance */
    private int threadID;
    
    private AdminParse parse;;

    /**Creates the Interpreter Thread with a given socket*/
    public AdminConnectionThread(Socket cSocket) {
    	super("BNLSConnectionThread");
    	threadID=threadCount++;
    	connectionCount++;
    	socket = cSocket;
    	parse=new AdminParse();
    	setDaemon(true);//make this Thread Not Hold up the Program
    }
    
    public void run() {//run the connection thread
    	
    	//Check for too many thread instances(dont want to overload server)
	    if(threadCount>Constants.maxAdminThreads){
	    	Out.error("AdminThread","Max Threads Exceeded(ID: "+threadID+") Current Count:"+threadCount+" Max Threads: "+Constants.maxAdminThreads+".  Connection Closed.");
	    	threadCount--;
	    	return;
	    }
    	Out.println("AdminThread","Connection thread initialized(ID: "+threadID+") Remote IP:"+socket.getInetAddress().toString());
	    boolean parsing=true;
	    	
	    try{
	    	//Retrieve Input and output Streams
	    	out = new PrintWriter(socket.getOutputStream(),true);
		    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		    socket.setSoTimeout(300000);//5 min timeout(300 seconds)
		    socket.setKeepAlive(true);//keep connection alive
	    }catch(IOException e){
	    	Out.error("AdminThread(ID: "+threadID+")","Error Creating Streams");
	    	parsing=false;
	    }
		  
		
	    Out.debug("AdminThread(ID: "+threadID+")","Streams Created");
	    out.println("Welcome to Hdx's JBLS Admin server.");
	    out.println("Username:");
	    AdminParse.bAuthed=false;
	    AdminParse.sName=null;
	    AdminParse.sPass=null;
	    
	    while (parsing) {//as long as we are parsing, loop through inputs
	    	try{
	    		String inLine=in.readLine();
	    		if(inLine==null){
	    			parsing=false;
	    			break;
	    		}
		    	if (!inLine.equals("")) 
		    	  Out.info("AdminThread(ID: "+threadID+")","Data: "+inLine);
		    	String response;
		    	response = parse.parseAdminCommand(inLine);
		    	  
		    	if(response==null){
		    		//do nothing
		    	}else if(response.equals("close")){
		    		Out.println("AdminThread(ID: "+threadID+")","Closed by Parser");
		    		out.println("Terminating Admin Connection....");
		    		threadCount--;
		    		parse = null;
		    		socket.close();
		    	}else if(response.equals("Login failed. Connection closed.")) {
		    		Out.println("AdminThread(ID: "+threadID+")","Closed by Parser. Bad login.");
		    		out.println(response);
		    		out.println("Terminating Admin Connection....");
		    		threadCount--;
		    		parse = null;
		    		socket.close();		    	
		        }else if(response.equals("null")) {
		    		Out.println("AdminThread(ID: "+threadID+")","Connection lost.");
		    		threadCount--;
		    		parse = null;
		    		socket.close();
		    	}else{//send the response
		    		Out.debug("AdminThread(ID: "+threadID+")","Response: "+response);
		    		out.println(response);
		    	}
		    	
	    	}catch(InterruptedIOException e){
	    		Out.println("AdminThread(ID: "+threadID+")","Timeout");
	    		parsing=false;
	    		break;
	    	}catch(IOException e){
	    		Out.error("AdminThread(ID: "+threadID+")","IOException: "+e.toString());
	    		parsing=false;
	    	}//end catch block
	    }//end while loop
	    Out.debug("AdminThread(ID: "+threadID+")","Terminated");
	}//end run method

}//end class
