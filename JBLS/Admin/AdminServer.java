// Created on Feb 20, 2005

package Admin;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.BindException;
import util.Out;
import util.Controller;
import util.Constants;
/**
 * @author FooL
 *
 * This class listens for incoming admin connections and controls all
 * admin settings.
 *
 */

public class AdminServer extends Thread{

	public AdminSettings settings;

	// Port to listen on
	// private static final int AdminPort = 9360;

	 //static BufferedReader in = new BufferedReader(
     //       new InputStreamReader(
     //       Channels.newInputStream(
     //      (new FileInputStream(FileDescriptor.in)).getChannel())));

	//Socket
	private ServerSocket server=null;
	private boolean listening=false;

	public AdminServer()
	{
		Out.println("Admin","Server thread created.");
		settings = new AdminSettings();
		settings.load();
	}

	/**Starts the Server Listening process(infinatly blocking loop)*/
	public void run() {
		listening=true;
		try
		{
		    server = new ServerSocket(Constants.AdminPort);
		    Out.println("Admin", "Listening on port " + Constants.AdminPort);
		}catch (BindException e) {
			Out.error("Admin", "Could not bind port " + Constants.AdminPort + ". Admin server is disabled.");
			return;
		}
		catch (IOException e)
		{
		    Out.error("Admin", "Could not create socket (Port:" + Constants.AdminPort + ")  - Shutting down Admin Server. JBLS will remain running. "+e.toString());
		    // Out.error("Admin", e.toString());
		    // No need to display error message when its already displayed. -Joe
		    Controller.disableAdminServer();
			return;
		}

		while (listening)
		{
			try
			{
				Out.debug("Admin", "Listening for new connection...");
				Socket inSocket=server.accept();//block until a connection is made

				new AdminConnectionThread(inSocket).start();
			}
			catch (IOException e)
			{
				Out.error("Admin", "Could not Accept Connection " + e.toString());
				listening=false;
			}
		}
		Out.info("Admin","Server Thread Terminated");
	}//end run method

	/** Saves Everything necessary from the settings*/
	public void save()
	{
		if(settings != null)
		{
			if(settings.save())
			{
				Out.println("Admin", "Settings saved successfully.");
			}
			else
			{
				Out.error("Admin", "Failed to save settings.");
			} // End of inner if.
		} // End of outter if
	} // End of save method

	public void closeSocket(){
		listening=false;
		try{
			if(!server.isClosed())
				server.close();
		}catch(IOException e){
			Out.error("Admin","Error Closing Socket: "+e.toString());
		}
	}

}//end class
