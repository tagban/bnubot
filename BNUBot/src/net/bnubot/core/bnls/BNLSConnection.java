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

/**
 * @author scotta
 */
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
	public void initialize() throws IOException {
		// Connect to BNLS
		setBNLSConnected(true);

		// Send BNLS_AUTHORIZE
		try (BNLSPacket loginPacket = new BNLSPacket(BNLSPacketId.BNLS_AUTHORIZE)) {
			loginPacket.writeNTString("bnu2");
			loginPacket.sendPacket(bnlsOutputStream);
		}

		// Recieve BNLS_AUTHORIZE
		BNetInputStream is = readPacket(BNLSPacketId.BNLS_AUTHORIZE);
		int serverCode = is.readDWord();

		// Calculate checksum
		int checksum = (int) (org.jbls.BNLSProtocol.BNLSlist.BNLSChecksum(
				"bot", serverCode) & 0xFFFFFFFF);

		// Send BNLS_AUTHORIZEPROOF
		try (BNLSPacket loginPacket = new BNLSPacket(BNLSPacketId.BNLS_AUTHORIZEPROOF)) {
			loginPacket.writeDWord(checksum);
			loginPacket.sendPacket(bnlsOutputStream);
		}

		// Recieve BNLS_AUTHORIZEPROOF
		is = readPacket(BNLSPacketId.BNLS_AUTHORIZEPROOF);
		int statusCode = is.readDWord();
		if (statusCode != 0)
			Out.error(getClass(), "Login to BNLS failed; logged in anonymously");
	}

	public int getVerByte(ProductIDs product) throws IOException {
		// Ask BNLS for the verbyte
		try (BNLSPacket vbPacket = new BNLSPacket(BNLSPacketId.BNLS_REQUESTVERSIONBYTE)) {
			vbPacket.writeDWord(product.getBnls());
			vbPacket.sendPacket(bnlsOutputStream);
			vbPacket.close();
		}

		BNetInputStream vbInputStream = readPacket(BNLSPacketId.BNLS_REQUESTVERSIONBYTE);
		int vbProduct = vbInputStream.readDWord();
		if (vbProduct == 0)
			throw new IOException("BNLS_REQUESTVERSIONBYTE failed.");
		if(vbProduct != product.getBnls())
			throw new IOException("BNLS_REQUESTVERSIONBYTE returned the wrong product [0x" + Integer.toHexString(vbProduct) + "]");
		return vbInputStream.readWord();
	}

	public VersionCheckResult sendVersionCheckEx2(Task task, ProductIDs productID, long MPQFileTime, String MPQFileName, byte[] ValueStr) throws IOException, InterruptedException {
		try (BNLSPacket bnlsOut = new BNLSPacket(BNLSPacketId.BNLS_VERSIONCHECKEX2)) {
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
			if(bpr.packetId != BNLSPacketId.BNLS_VERSIONCHECKEX2)
				throw new IOException("Recieved the wrong packet (" + bpr.packetId.name() + ", but expected BNLS_VERSIONCHECKEX2)");
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

	public void keepAlive() throws IOException {
		try (BNLSPacket p = new BNLSPacket(BNLSPacketId.BNLS_NULL)) {
			p.sendPacket(bnlsOutputStream);
		}
	}

	private BNetInputStream readPacket(BNLSPacketId expected) throws IOException {
		BNLSPacketReader pr = new BNLSPacketReader(bnlsInputStream);
		if(pr.packetId != expected)
			throw new IOException("Recieved the wrong packet (" + pr.packetId.name() + ", but expected " + expected.name() + ")");
		return pr.getInputStream();
	}

	public BNetInputStream sendWarden0(int cookie, int client, byte[] seed) throws IOException {
		try (BNLSPacket p = new BNLSPacket(BNLSPacketId.BNLS_WARDEN)) {
			p.writeByte(0);
			p.writeDWord(cookie);
			p.writeDWord(client);
			p.writeWord(seed.length);
			p.write(seed);
			p.writeNTString("");
			p.writeWord(0);
			p.sendPacket(bnlsOutputStream);
		}

		return readPacket(BNLSPacketId.BNLS_WARDEN);
	}

	public BNetInputStream sendWarden1(int cookie, byte[] payload) throws IOException {
		try (BNLSPacket p = new BNLSPacket(BNLSPacketId.BNLS_WARDEN)) {
			p.writeByte(1);
			p.writeDWord(cookie);
			p.writeWord(payload.length);
			p.write(payload);
			p.sendPacket(bnlsOutputStream);
		}

		return readPacket(BNLSPacketId.BNLS_WARDEN);
	}
}
