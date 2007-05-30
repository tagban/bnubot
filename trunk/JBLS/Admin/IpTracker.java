/*
 * Created on Dec 18, 2004
 *
 */
package Admin;

/**
 * @author FooL
 * This is the IpTracker Class
 * It stores all necessary information for a single IPAddress that we keep
 * track of
 */
public class IpTracker {
	
	//Constants for the status variable
	public static final int STATUSNORMAL=0;
	public static final int STATUSAUTHORIZED=1;//this ip is always authorized(unrestricted)
	public static final int STATUSRESTRICTED=2;//allows limited access(high IPBanning restrictions - low tolerance threshold
	public static final int STATUSBANNED=3;//this ip is banned from our server
	
	
	private int connectionCount=0;//number of times this IP has connected
	private int status=0;//What terms we are on with this IPAddress
	
	/**
	 * 
	 * @param c Connection Count
	 * @param s Status
	 */
	public IpTracker(int c, int s){
		connectionCount=c;
		status=s;
	}
	
	/**
	 * Called When a connection is made
	 *
	 */
	public boolean connect(){
		
		connectionCount++;
		return true;
	}
	
	public int getConnectionCount(){
		return connectionCount;
	}
	
	public int getStatus(){
		return status;
	}
	
	

}
