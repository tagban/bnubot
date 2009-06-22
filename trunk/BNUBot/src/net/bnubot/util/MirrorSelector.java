/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import net.bnubot.logging.Out;
import net.bnubot.settings.GlobalSettings;

/**
 * A class for picking the fastest address
 * http://www.x86labs.org/forum/index.php/topic,10225.0.html
 * @author Chavo
 * @author scotta
 */
public class MirrorSelector {
	private static final int MAX_TIME = 400; // timeout time in ms
	private static final int IDEAL_LIMIT = 50;
	private static final int MAX_TIME_TOTAL = 3000;

	private static InetAddress selectMirror(InetAddress[] mirrors, int port) {
		Socket s = new Socket();
		long bestTime = MAX_TIME;
		InetAddress bestHost = null;
		long start = System.currentTimeMillis();

		for (InetAddress mirror : mirrors) {
			try {
				s = new Socket();
				long lap = System.currentTimeMillis();
				s.connect(new InetSocketAddress(mirror, port), MAX_TIME);
				long time = System.currentTimeMillis() - lap;
				s.close();
				if(Out.isDebug(MirrorSelector.class))
					Out.debugAlways(MirrorSelector.class, "Connecting to " + mirror.getHostAddress() + " took " + time + "ms");

				if(time <= IDEAL_LIMIT)
					return mirror;

				if (time < bestTime) {
					bestTime = time;
					bestHost = mirror;
				}
			} catch (SocketTimeoutException ste) {
				Out.error(MirrorSelector.class, "Address " + mirror + " timed out");
			} catch (ConnectException ce) {
				Out.error(MirrorSelector.class, "Connect Exception: " + ce.getMessage() + " for " + mirror);
			} catch (SocketException se) {
				Out.error(MirrorSelector.class, "Unable to connect to " + mirror + ", there may be a problem with your network connection.");
			} catch (IOException ioe) {
				Out.error(MirrorSelector.class, "Error connecting to " + mirror);
				Out.exception(ioe);
			}

			long elapsed = System.currentTimeMillis() - start;
			if(elapsed > MAX_TIME_TOTAL) {
				Out.info(MirrorSelector.class, "Mirror selection is taking too long; skipping.");
				break;
			}
		}

		if(bestHost == null) {
			bestHost = random(mirrors);
			Out.info(MirrorSelector.class, "There was no clear winner; randomly choosing " + bestHost.getHostAddress());
		}
		return bestHost;
	}

	private static InetAddress random(InetAddress[] hosts) {
		return hosts[(int)(Math.random() * hosts.length)];
	}

	public static InetAddress getClosestMirror(String hostname, int port)
			throws UnknownHostException {
		InetAddress hosts[] = InetAddress.getAllByName(hostname);

		if(true) {
			// Remove IPv6 addresses from the list
			int i = 0;
			for(InetAddress ia : hosts) {
				if(ia instanceof Inet4Address)
					i++;
			}
			if(i < hosts.length) {
				InetAddress[] newHosts = new InetAddress[i];
				i = 0;
				for(InetAddress ia : hosts) {
					if(ia instanceof Inet4Address)
						newHosts[i++] = ia;
				}
				hosts = newHosts;
			}
		}

		if (hosts.length == 1)
			return hosts[0];

		if(!GlobalSettings.enableMirrorSelector)
			return random(hosts);

		Out.info(MirrorSelector.class, "Searching for fastest of " + hosts.length + " hosts for " + hostname);
		return selectMirror(hosts, port);
	}
}