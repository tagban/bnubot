package util;


import java.net.*;
import java.io.*;
public class BNFTP {
	private Socket bnSCK = null;
	private OutputStream out = null;
	private InputStream in = null;

	public static void main(String[] args) {
		//BNFTP ftp = new BNFTP("uswest.battle.net", 6112, "D2DV_IX86_111A_111B.mpq");
	}

	public String BNFTP(String bnServer, int bnPort, String fileName){
		try {
			bnSCK = new Socket(bnServer, bnPort);

			out = bnSCK.getOutputStream();
			in = bnSCK.getInputStream();


			out.write((byte)0x02);

			Buffer pRequest = new Buffer();
			pRequest.add((short)(fileName.length() + 0x21));
			pRequest.add((short)0x0100);    //Version
			pRequest.add(0x49583836);       //IX86
			pRequest.add(0x44324456);       //D2DV
			pRequest.add(0x00);             //Banner ID
			pRequest.add(0x00);             //Banner Ext
			pRequest.add(0x00);             //FileTime HighDword
			pRequest.add(0x00);             //FileTime LowDword
			pRequest.add(0x00);             //Starting
			pRequest.addNTString(fileName); //File name
			Out.println("BNFTP", "Sending request for " + fileName);
			out.write(pRequest.getBuffer());

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

                Buffer header = new Buffer();

                int bytesRead = 2;
                while (bytesRead < headlen){
	            	i = in.read();
	                if (i == -1)
	                    throw new IOException("Connection terminated. " + bytesRead);
	                header.add((byte) i);
	                bytesRead++;
                }

                header.removeWord();
                int fileSize = header.removeDWord();
                int bannerID = header.removeDWord();
                int bannerExt = header.removeDWord();
                long filetime = header.removeLong();
                String sName = header.removeNTString();

                Out.println("BNFTP", "Recived header: " );
                Out.println("BNFTP", "FileSize: " + fileSize);
			    FileOutputStream file = new FileOutputStream(fileName);

				bytesRead = 0;
				while (bytesRead < fileSize) {
					i = in.read();
					if (i == -1)
						throw new IOException("Failed to recive all file data. Last offset: " + bytesRead);
					file.write((byte)i);
					bytesRead++;
				}
				file.flush();
				file.close();
				new File(fileName).setLastModified(fileTimeToMillis(filetime));
				Out.println("BNFTP", "Recived file: " + fileName + " Size: " + fileSize);
				return "Recived file: " + fileName + " Size: " + fileSize;
			}catch (IOException e) {
				Out.println("BNFTP", "IOError: " + e.toString());
				return ("IOError: " + e.toString());
			}

		}catch (UnknownHostException e) {
			Out.println("BNFTP", "Could not find host: " + bnServer + ":" + bnPort);
			return ("Could not find host: " + bnServer + ":" + bnPort);
		}catch (IOException e){
			Out.println("BNFTP", "IOExcaption: " + e.toString());
			return ("IOExcaption: " + e.toString());
		}
	}
    public static long fileTimeToMillis(long fileTime){
       return (fileTime / 10000L) - 11644473600000L;
    }
}
