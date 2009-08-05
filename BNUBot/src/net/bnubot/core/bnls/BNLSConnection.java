/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.bnls;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import net.bnubot.core.bncs.ProductIDs;
import net.bnubot.logging.Out;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetInputStream;
import net.bnubot.util.MirrorSelector;
import net.bnubot.util.crypto.HexDump;
import net.bnubot.util.task.Task;

public class BNLSConnection {

	private Socket bnlsSocket = null;
	private InputStream bnlsInputStream = null;
	private OutputStream bnlsOutputStream = null;

	private void setBNLSConnected(boolean c) throws IOException {
		if(bnlsSocket != null) {
			bnlsSocket.close();
			bnlsSocket = null;
			bnlsInputStream = null;
			bnlsOutputStream = null;
		}

		if(c) {
			InetAddress address = MirrorSelector.getClosestMirror(GlobalSettings.bnlsServer, GlobalSettings.bnlsPort);
			Out.info(getClass(), "Connecting to " + address + ":" + GlobalSettings.bnlsPort + ".");
			bnlsSocket = new Socket(address, GlobalSettings.bnlsPort);
			bnlsSocket.setKeepAlive(true);
			bnlsInputStream = bnlsSocket.getInputStream();
			bnlsOutputStream = bnlsSocket.getOutputStream();
		}
	}


	/**
	 * Connect to BNLS and get verbyte
	 * @param connect
	 * @throws IOException
	 * @throws SocketException
	 */
	public void initialize(Task connect) throws IOException, SocketException {
		// Connect to BNLS
		connect.updateProgress("Connecting to BNLS");
		setBNLSConnected(true);

		// Log in to BNLS
		connect.updateProgress("Logging in to BNLS");

		// Send BNLS_AUTHORIZE
		BNLSPacket loginPacket = new BNLSPacket(BNLSPacketId.BNLS_AUTHORIZE);
		loginPacket.writeNTString("bnu2");
		loginPacket.sendPacket(bnlsOutputStream);

		// Recieve BNLS_AUTHORIZE
		BNetInputStream is = new BNLSPacketReader(bnlsInputStream)
				.getInputStream();
		int serverCode = is.readDWord();

		// Calculate checksum
		int checksum = (int) (org.jbls.BNLSProtocol.BNLSlist.BNLSChecksum(
				"bot", serverCode) & 0xFFFFFFFF);

		// Send BNLS_AUTHORIZEPROOF
		loginPacket = new BNLSPacket(BNLSPacketId.BNLS_AUTHORIZEPROOF);
		loginPacket.writeDWord(checksum);
		loginPacket.sendPacket(bnlsOutputStream);

		// Recieve BNLS_AUTHORIZEPROOF
		is = new BNLSPacketReader(bnlsInputStream).getInputStream();
		int statusCode = is.readDWord();
		if (statusCode != 0)
			Out.error(getClass(), "Login to BNLS failed; logged in anonymously");
	}

	public int getVerByte(ProductIDs product) throws IOException {
		// Ask BNLS for the verbyte
		BNLSPacket vbPacket = new BNLSPacket(BNLSPacketId.BNLS_REQUESTVERSIONBYTE);
		vbPacket.writeDWord(product.getBnls());
		vbPacket.sendPacket(bnlsOutputStream);

		BNetInputStream vbInputStream = new BNLSPacketReader(bnlsInputStream).getInputStream();
		int vbProduct = vbInputStream.readDWord();
		if (vbProduct == 0)
			throw new IOException("BNLS_REQUESTVERSIONBYTE failed.");
		return vbInputStream.readWord();
	}

	public VersionCheckResult sendVersionCheckEx2(Task task, ProductIDs productID, long MPQFileTime, String MPQFileName, byte[] ValueStr) throws IOException, InterruptedException {
		try {
			BNLSPacket bnlsOut = new BNLSPacket(BNLSPacketId.BNLS_VERSIONCHECKEX2);
			bnlsOut.writeDWord(productID.getBnls());
			bnlsOut.writeDWord(0); // Flags
			bnlsOut.writeDWord(0); // Cookie
			bnlsOut.writeQWord(MPQFileTime);
			bnlsOut.writeNTString(MPQFileName);
			bnlsOut.writeNTString(ValueStr);
			bnlsOut.sendPacket(bnlsOutputStream);

			long startTime = System.currentTimeMillis();
			task.setDeterminate(15000, "ms");
			while (bnlsInputStream.available() < 3) {
				Thread.sleep(50);

				long timeElapsed = System.currentTimeMillis() - startTime;
				if (timeElapsed > 15000) {
					Out.error(getClass(), "BNLS_VERSIONCHECKEX2 timed out");
					setBNLSConnected(false);
					return null;
				}

				task.setProgress((int) timeElapsed);
			}
			task.complete();

			BNLSPacketReader bpr = new BNLSPacketReader(bnlsInputStream);
			BNetInputStream bnlsIn = bpr.getInputStream();
			int success = bnlsIn.readDWord();
			if (success != 1) {
				Out.error(getClass(), "BNLS_VERSIONCHECKEX2 Failed\n"
						+ HexDump.hexDump(bpr.getData()));
				setBNLSConnected(false);
				return null;
			}

			VersionCheckResult vcr = new VersionCheckResult();
			vcr.exeVersion = bnlsIn.readDWord();
			vcr.exeHash = bnlsIn.readDWord();
			vcr.exeInfo = bnlsIn.readNTBytes();
			bnlsIn.readDWord(); // cookie
			bnlsIn.readDWord(); // verbyte
			assert (bnlsIn.available() == 0);

			Out.info(getClass(), "Recieved version check from BNLS.");
			setBNLSConnected(false);

			if((vcr.exeVersion == 0)
			|| (vcr.exeHash == 0)
			|| (vcr.exeInfo == null)
			|| (vcr.exeInfo.length == 0)) {
				Out.error(getClass(), "Recieved invalid CheckRevision data.");
				return null;
			}

			// Everything went well!
			return vcr;
		} catch (UnknownHostException e) {
			setBNLSConnected(false);
			Out.error(getClass(), "BNLS connection failed: " + e.getMessage());
			return null;
		}
	}
}
