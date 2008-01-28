/**
 * This file is distributed under the Mozilla Public License Version 1.1
 * $Id$
 */

package net.bnubot.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ITunesController {
	private static File script = new File("apple.scpt");
	
	public static String getCurrentlyPlaying() throws Exception {
		if(!System.getProperty("os.name").startsWith("Mac OS X"))
			throw new IllegalStateException("This utility only works on Mac OS X");
		
		String data = "set sep to \"\\n\"\n"
			+ "tell application \"iTunes\"\n"
			+ "if player state is playing or player state is paused then\n"
			+ "set r to current track\n"
			+ "set myvalue to (artist of r & sep)\n"
			+ "set myvalue to (myvalue & album of r & sep)\n"
			+ "set myvalue to (myvalue & name of r & sep)\n"
			+ "set myvalue to (myvalue & bit rate of r & sep)\n"
			+ "set myvalue to (myvalue & duration of r)\n"
			+ "get myvalue\n"
			+ "else\n"
			+ "get player state\n"
			+ "end if\n"
			+ "end tell";
		data = run(data);
		String[] result = data.split("\n");
		if(result.length != 5)
			throw new IllegalStateException(data);
		
		String artist = result[0];
		//String album = result[1];
		String name = result[2];
		//int bitrate = Integer.parseInt(result[3]);
		//float duration = Float.parseFloat(result[4]);
		return artist + " - " + name;
	}
	
	private static String run(String data) throws Exception {
		synchronized(script) {
			OutputStream fos = new FileOutputStream(script);
			fos.write(data.getBytes());
			fos.close();
			
			List<String> cmd = new ArrayList<String>();
			cmd.add("/usr/bin/osascript");
			cmd.add(script.getPath());
			Process result = Runtime.getRuntime().exec(cmd.toArray(new String[0]));

			String line;
			StringBuffer output = new StringBuffer();
			
			if(result.waitFor() != 0) {
				BufferedReader err = new BufferedReader(new InputStreamReader(result.getErrorStream()));
				while ((line = err.readLine()) != null)
					output.append(line + "\n");
				
				script.delete();
				throw new IllegalStateException(output.toString().trim());
			}
			
			BufferedReader err = new BufferedReader(new InputStreamReader(result.getInputStream()));
			while ((line = err.readLine()) != null)
				output.append(line + "\n");
			
			script.delete();
			return output.toString();
		}
	}
}
