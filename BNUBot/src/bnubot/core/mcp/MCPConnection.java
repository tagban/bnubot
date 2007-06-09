package bnubot.core.mcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import Hashing.DoubleHash;
import Hashing.HashMain;
import bnubot.core.ConnectionSettings;

public class MCPConnection extends Thread {
	protected String server;
	protected int port;
	protected Socket s;
	protected String realm;
	protected int serverToken;
	protected DataInputStream dis = null;
	protected DataOutputStream dos = null;

	
	public MCPConnection(String server, int port, String realm, int serverToken) {
		this.server = server;
		this.port = port;
		this.realm = realm;
		this.serverToken = serverToken;
	}
	
	public void run() {
		try {
			s = new Socket(server, port);
			dis = new DataInputStream(s.getInputStream());
			dos = new DataOutputStream(s.getOutputStream());
			
			MCPPacket p;
			
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
