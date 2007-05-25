package core.bnftp;

import java.io.*;
import java.net.Socket;

import core.*;

public class BNFTPConnection extends Connection {
	Socket s = null;
	DataInputStream dis = null;
	DataOutputStream dos = null;
	boolean connected = false;
	String fileName;
	
	public BNFTPConnection(ConnectionSettings cs, String fileName) {
		super(cs);
		this.fileName = fileName;
	}
	
	public void run() {
		System.out.println("BNFTPConnection running");
		
		Connect();
		
		System.out.println("BNFTPonnection terminated");
	}

	public void Connect() {
		try {
			s = new Socket(cs.server, cs.port);
			dis = new DataInputStream(s.getInputStream());
			dos = new DataOutputStream(s.getOutputStream());
			
			connected = true;
			
			// FTP
			dos.writeByte(0x02);
			dos.flush();
			
			BNetOutputStream p = new BNetOutputStream(dos);
			p.writeWord(32 + fileName.length() + 1);
			p.writeWord(0x100);		// Protocol version
			p.writeDWord("IX86");	// Platform ID
			p.writeDWord("STAR");	// Product ID
			p.writeDWord(0);		// Banners ID
			p.writeDWord(0);		// Banners File Extension
			p.writeDWord(0);		// File position
			p.writeQWord(0);		// Filetime
			p.writeNTString(fileName);
			dos.flush();
			
			while(s.isConnected()) {
				if(dis.available() > 0) {
					BNetInputStream is = new BNetInputStream(dis);
					int headerLength = is.readWord();
					int unknown = is.readWord();
					int fileSize = is.readDWord();
					int bannersID = is.readDWord();
					int bannersFileExt = is.readDWord();
					long fileTime = is.readQWord();
					fileName = is.readNTString();
					
					// the rest is the data
				}
				
				yield();
			}
			
			Disconnect();
			s.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void Disconnect() {
		connected = false;
	}
}
