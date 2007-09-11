/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.util;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * http://www.x86labs.org/forum/index.php/topic,10225.0.html
 * 
 * @author Chavo
 */
public class MirrorSelector {
	private static int MAX_TIME = 1000; // timeout time in ms

	public static InetAddress selectMirror(InetAddress[] mirrors, int port) {
		Socket s = new Socket();
		long bestTime = MAX_TIME;
		InetAddress bestHost = null;
		long start;

		for (InetAddress mirror : mirrors) {
			long time;
			try {
				s = new Socket();
				start = System.currentTimeMillis();
				s.connect(new InetSocketAddress(mirror, port), MAX_TIME);
				time = System.currentTimeMillis() - start;
				s.close();
				Out.debug(MirrorSelector.class, "Connecting to " + mirror.getHostAddress() + " took " + time + "ms");

				if (time < bestTime) {
					bestTime = time;
					bestHost = mirror;
				}
			} catch (SocketTimeoutException ste) {
				Out.error(MirrorSelector.class, "Address " + mirror + " timed out");
			} catch (ConnectException ce) {
				Out.error(MirrorSelector.class, "Connect Exception: " + ce.getMessage() + " for " + mirror);
				Out.exception(ce);
			} catch (SocketException se) {
				Out.error(MirrorSelector.class, "Unable to connect to " + mirror + ", there may be a problem with your network connection.");
				Out.exception(se);
			} catch (IOException ioe) {
				Out.error(MirrorSelector.class, "Error connecting to " + mirror);
				Out.exception(ioe);
			}
		}

		if(bestHost == null) {
			bestHost = mirrors[(int)(Math.random() * mirrors.length)];
			Out.info(MirrorSelector.class, "There was no clear winner; randomly choosing " + bestHost.getHostAddress());
		}
		return bestHost;
	}

	public static InetAddress getClosestMirror(String hostname, int port) 
			throws UnknownHostException {
		InetAddress hosts[] = InetAddress.getAllByName(hostname);
		if (hosts.length == 1) {
			Out.debug(MirrorSelector.class, "There was only one host: " + hosts[0]);
			return hosts[0];
		}
		Out.info(MirrorSelector.class, "Searching for fastest of " + hosts.length + " hosts for " + hostname);
		return selectMirror(hosts, port);
	}
}