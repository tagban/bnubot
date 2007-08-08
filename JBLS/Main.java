/*
 * Created on Sep 19, 2004
 *
 * BNLS Server Main
 *
 */

/**
 * Project Name:  JBLS ( Java Bot Login Server), or however you want to make the acronym
 *
 * >>>>>  http://www.quikness.com/
 * >>>>>  http://www.JBLS.org/
 *
 * @author The-FooL (fool@the-balance.com)
 * @author Hdx      (HdxBmx27@gmail.com)
 *   Other Contributors:
 *
 *
 *
 * Program Emulates a BNLS Server, allowing users to run their own server
 * and connect bots that use the BNLS protocol to it.
 *
 *   I am not the best at Programming, but (most of)what is in here works.
 *   Please feel free to modify/change any parts you like, and submit them back to me.
 *   This project is now open source, hopefully few scams will arise from this.
 *
 *   I also take no responsibility for your use of this program.
 *
 * @version
 *
 *  V2 - Constants are no longer labeled as final so that the
 *       Constants.class file can be replaced when updates are needed
 *
 *  VCurrent - CVS Version to be worked on
 * 		*The-FooL:
 * 		-Fixed the issue that required double connections in order to get
 *      	JBLS to work.
 * 		-Converted All System.out statements to the static Out Class
 * 		-Set up Admin Server Thread, and connection sub-threads
 * 		-Created settings class, and IP HashTable/Auth classes
 */
import util.Constants;
import util.Controller;
import util.Out;
import util.cSettings;
import Admin.AdminList;
import Admin.AdminServer;
import BNLSProtocol.BNLSServer;
import BNLSProtocol.BNLSlist;
import HTTP.HTTPServer;

public class Main {

	public static AdminServer admin;
	/** Main Method - Starting point for program */
	public static void main(String[] args) throws Exception{
		Out.setDefaultOutputStream();
        cSettings.LoadSettings();
		cSettings.SaveSettings();
		AdminList.LoadUsers();
		BNLSlist.LoadUsers();

		Out.println("Main", "Java Battle.Net Login Sever - (JBLS)  http://www.JBLS.org/");
		Out.println("Main", "Build: " + Constants.build);

		Out.println("Main", "JBLS Started");

		//Load Administration shit
		if(Constants.RunAdmin){
		  Out.println("Main","Loading Admin Settings");
		  Controller.aServer=new AdminServer();
		  Controller.aServer.start();//start admin thread
		}

		//Start BNLS Server
		Out.println("Main","Loading JBLS Server");
		Controller.jServer=new BNLSServer();
		Controller.jServer.start();

		//Start HTTP Server
		if(Constants.RunHTTP){
		  Out.println("Main","Loading HTTP Server");
		  Controller.hServer=new HTTPServer();
		  Controller.hServer.start();
		}
		Hashing.CheckRevisionLD.loadCache();
	}
}//end main class