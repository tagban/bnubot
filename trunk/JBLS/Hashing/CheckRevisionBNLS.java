package Hashing;

import java.io.*;
import java.net.*;
import java.util.Hashtable;
import java.util.StringTokenizer;

import BNLSProtocol.OutPacketBuffer;
import util.Constants;
import util.Out;

/** 
 *This will request the CheckRevision data from another BNLS Server
 *Yes, this is 'eww' but I'm mainly doing it to add temporary lockdown support.
 *Feel free to hate me for it -Hdx
 */
public class CheckRevisionBNLS extends Thread{
    private static Hashtable<String, OutPacketBuffer> crCache = new Hashtable<String, OutPacketBuffer>();
    private static String bnServer = "logon.berzerkerjbls.com"; //"63.161.183.91";
    
    public static void main(String[] args){}
    
    
    public static OutPacketBuffer checkRevision(String versionString, int prod, String mpq, Long fileTime)
    {
	    Socket bnSCK = null;
	    OutputStream out = null;
	    InputStream in = null;
        OutPacketBuffer cacheHit = (OutPacketBuffer) crCache.get(versionString + mpq + prod + fileTime);
        if(cacheHit != null) return cacheHit;
            
        try {
			bnSCK = new Socket(bnServer, 9367);

			out = bnSCK.getOutputStream();
			in = bnSCK.getInputStream();

            OutPacketBuffer packet = new OutPacketBuffer(0x1A);
            packet.add(prod);
            packet.add((int)0);
            packet.add((int)0);
            packet.add(fileTime);
            packet.addNTString(mpq);
            packet.addNTString(versionString);
            
			out.write(packet.getBuffer());

			try {
				short headlen;
				int i;
				i = in.read();
				if (i == -1)
					throw new IOException("Connection terminated. 1");
				headlen = (short) ((i << 0) & 0x000000FF);

                i = in.read();
                if (i == -1)
                   	throw new IOException("Connection terminated. 2");
                headlen |= (short) ((i << 8) & 0x0000FF00);

                i = in.read();
                OutPacketBuffer header = new OutPacketBuffer(0x1A);
                int bytesRead = 3;
                while (bytesRead < headlen){
	            	i = in.read();
	                if (i == -1)
	                    throw new IOException("Connection terminated. " + bytesRead);
	                header.add((byte) i);
	                bytesRead++;
                }

				crCache.put(versionString + mpq + prod + fileTime, header);
				return header;
			}catch (IOException e) {
				Out.println("CRevBNLS", "IOError: " + e.toString());
				return null;
			}

		}catch (UnknownHostException e) {
			Out.println("CRevBNLS", "Could not find host: " + bnServer + ":9367");
			return null;
		}catch (IOException e){
			Out.println("CRevBNLS", "IOExcaption: " + e.toString());
			return null;
		}
    }    
    public static OutPacketBuffer checkRevision(String versionString, int prod, int mpq)
    {
	    Socket bnSCK = null;
	    OutputStream out = null;
	    InputStream in = null;
        OutPacketBuffer cacheHit = (OutPacketBuffer) crCache.get(versionString + mpq + prod);
        if(cacheHit != null) return cacheHit;
            
        try {
			bnSCK = new Socket(bnServer, 9367);

			out = bnSCK.getOutputStream();
			in = bnSCK.getInputStream();

            OutPacketBuffer packet = new OutPacketBuffer(0x09);
            packet.add(prod);
            packet.add(mpq);
            packet.addNTString(versionString);
            
			out.write(packet.getBuffer());

			try {
				short headlen;
				int i;
				i = in.read();
				if (i == -1)
					throw new IOException("Connection terminated. 1");
				headlen = (short) ((i << 0) & 0x000000FF);

                i = in.read();
                if (i == -1)
                   	throw new IOException("Connection terminated. 2");
                headlen |= (short) ((i << 8) & 0x0000FF00);

                i = in.read();
                OutPacketBuffer header = new OutPacketBuffer(0x09);
                int bytesRead = 3;
                while (bytesRead < headlen){
	            	i = in.read();
	                if (i == -1)
	                    throw new IOException("Connection terminated. " + bytesRead);
	                header.add((byte) i);
	                bytesRead++;
                }

				crCache.put(versionString + mpq + prod, header);
				return header;
			}catch (IOException e) {
				Out.println("CRevBNLS", "IOError: " + e.toString());
				return null;
			}

		}catch (UnknownHostException e) {
			Out.println("CRevBNLS", "Could not find host: " + bnServer + ":9367");
			return null;
		}catch (IOException e){
			Out.println("CRevBNLS", "IOExcaption: " + e.toString());
			return null;
		}
    }
}