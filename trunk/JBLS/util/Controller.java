/*
 * Created on Mar 4, 2005
 */
package util;
import Admin.AdminServer;
import BNLSProtocol.BNLSServer;
import BNLSProtocol.BNLSConnectionThread;
import HTTP.HTTPServer;

/**
 * @author FooL
 * This Class contains static references to all the main and important
 * classes.  It has full control of all the threads, and provides a way
 * for the admin, etc. classes to interact.
 */
public class Controller {
    public static HTTPServer hServer;
	public static BNLSServer jServer;
	public static AdminServer aServer;
	public static BNLSConnectionThread lLinkedHead = null;
	public static ThreadGroup jConnectionThreads=new ThreadGroup("JBLS");
	public static ThreadGroup aConnectionThreads=new ThreadGroup("Admin");

	/**
	 * Shuts down the program, terminating all threads
	 */
	public static void shutdown(){
		Out.println("Control","Shutting Down...");
		save();
		if(aServer!=null)//shut down sockets to exit the blocking threads
			aServer.closeSocket();
		if(jServer!=null){
            jServer.destroyAllConnections();
			jServer.closeSocket();
	    }
		aServer=null;
		jServer=null;
		ThreadGroup tg=Thread.currentThread().getThreadGroup();
		tg.list();
		//Thread[] en=new Thread[tg.activeCount()+1];
		/*tg.enumerate(en);
		for(int x=0;x<en.length;x++){
			if(en[x]!=null&&(!en[x].equals(Thread.currentThread())));
			System.out.println(en[x].getName()+en[x].toString());
			en[x].destroy();
			en[x].stop();
		}*/
		//System.gc();
		Out.println("Control","Terminated");
		System.exit(0);
	}

	/**
	 * Restarts the JBLS Server Thread Only
	 *
	 */
	public static void restartJBLS(){

		Out.println("Control","Restarting JBLS Server Thread");
		if(jServer!=null){
            jServer.destroyAllConnections();
			jServer.closeSocket();
	    }
		jServer=null;

		try{
			Thread.sleep(1000);
		}catch(InterruptedException e){

		}

		//Make The New Server Thread
		jServer=new BNLSServer();
		jServer.start();
	}

	public static void stopJBLS(){

		Out.println("Control","Stoping JBLS Server Thread");

		if(jServer!=null){
            jServer.destroyAllConnections();
			jServer.closeSocket();
		}
		jServer=null;

		try{
			Thread.sleep(1000);
		}catch(InterruptedException e){
		}
	}

	public static void startJBLS(){

		Out.println("Control","Starting JBLS Server Thread");
		if(jServer!=null)
			jServer.closeSocket();
		jServer=null;

		try{
			Thread.sleep(1000);
		}catch(InterruptedException e){

		}

		//Make The New Server Thread
		jServer=new BNLSServer();
		jServer.start();
	}

	/**
	 * Restarts all the server threads, reloads the settings
	 *
	 */
	public static void restartAll(){

		Out.println("Control","Restarting all Threads");
		save();

		if(aServer!=null)//shut down sockets to exit the blocking threads
			aServer.closeSocket();
		if(jServer!=null){
            jServer.destroyAllConnections();
			jServer.closeSocket();
		}
		aServer=null;
		jServer=null;

		jServer.destroyAllConnections();

		//Wait a second
		try{
			Thread.sleep(1000);
		}catch(InterruptedException e){

		}

		jServer=new BNLSServer();
		jServer.start();

		aServer=new AdminServer();
		aServer.start();




	}


	/**
	 * Saves All the Settings, etc.
	 *
	 */
	public static void save(){
		if(aServer!=null){
			aServer.save();
		}

	}
	/**
	 * Destroys the Admin Server
	 *
	 */
	public static void disableAdminServer(){
		if(aServer!=null)
			aServer.closeSocket();
		save();
		aServer=null;//remove reference to it
		Out.debug("Control","Admin Server Disabled");
		if(jServer==null){//jbls is also disabled
			Out.println("Control","Admin and JBLS Servers disabled, terminating program...");
			System.exit(-1);
		}
	}//end disableAdmin

	/**
	 * Destroys the JBLS Server
	 *
	 */
	public static void disableJBLSServer(){
		if(jServer!=null)
			jServer.closeSocket();
		jServer=null;
		Out.debug("Control","JBLS Server Disabled");
		if(aServer==null){//admin is also disabled
			Out.println("Control","Admin and JBLS Servers disabled, terminating program...");
			System.exit(-1);
		}
	}


}
