package core;

import java.io.Serializable;

public class ConnectionSettings implements Serializable {
	private static final long serialVersionUID = 0L;
	
	public ConnectionSettings() {
		
	}
	
	public String server;
	public int port;
	public String username;
	public String password;
	public String cdkey;
	public boolean autoconnect;
}
