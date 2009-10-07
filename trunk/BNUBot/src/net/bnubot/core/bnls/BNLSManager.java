/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.bnls;

import java.io.IOException;

import net.bnubot.core.bncs.ProductIDs;
import net.bnubot.logging.Out;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.task.Task;

/**
 * @author scotta
 */
public class BNLSManager {
	private static String[] warden_servers = new String[] {
		"instinct121.no-ip.org:6111",
		"senkin.myvnc.com:5007",
		"80.86.83.93:9367",
		"bnls.ghostbot.net:9367",
	};

	private static BNLSConnection bnls = null;
	private static BNLSConnection bnls_warden = null;

	public static BNLSConnection getConnection() throws IOException {
		if(bnls != null)
			try {
				bnls.keepAlive();
			} catch(Exception e) {
				bnls = null;
			}

		if(bnls == null) {
			bnls = new BNLSConnection();
			bnls.initialize();
		}
		return bnls;
	}

	public static BNLSConnection getWardenConnection() throws IOException {
		if(bnls_warden != null)
			try {
				bnls_warden.keepAlive();
			} catch(Exception e) {
				bnls_warden = null;
			}

		if(bnls_warden == null)
			init_warden();
		return bnls_warden;
	}

	private static void init_warden() throws IOException {
		// Push the original BNLS settings
		String s = GlobalSettings.bnlsServer;
		int p = GlobalSettings.bnlsPort;

		int tries = 0;
		while(bnls_warden == null) {
			if(tries++ > 5)
				throw new IOException("Tried 5 times; couldn't connect to a BNLS warden server");

			pickWardenServer();
			try {
				bnls_warden = new BNLSConnection();
				bnls_warden.initialize();
			} catch(Exception e) {
				bnls_warden = null;
			}
		}

		// Pop the original BNLS settings
		GlobalSettings.bnlsServer = s;
		GlobalSettings.bnlsPort = p;
	}

	private static void pickWardenServer() {
		int n = (int) (Math.random() * warden_servers.length);
		Out.debug(BNLSManager.class, "Trying warden server " + warden_servers[n]);
		String[] sever_port = warden_servers[n].split(":");
		GlobalSettings.bnlsServer = sever_port[0];
		GlobalSettings.bnlsPort = Integer.parseInt(sever_port[1]);
	}

	public static int getVerByte(ProductIDs product) throws IOException {
		return getConnection().getVerByte(product);
	}

	public static VersionCheckResult sendVersionCheckEx2(Task task, ProductIDs productID, long mpqFileTime, String mpqFileName, byte[] valueStr) throws IOException, InterruptedException {
		return getConnection().sendVersionCheckEx2(task, productID, mpqFileTime, mpqFileName, valueStr);
	}
}
