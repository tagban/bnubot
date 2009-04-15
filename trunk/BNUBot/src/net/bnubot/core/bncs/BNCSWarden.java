/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.core.bncs;

import java.io.IOException;
import java.io.OutputStream;

import net.bnubot.util.Out;

import org.jbls.Warden.SimpleCrypto;
import org.jbls.Warden.WardenModule;
import org.jbls.Warden.WardenRandom;
import org.jbls.util.Buffer;
import org.jbls.util.PadString;

/**
 * This is ported from Hdx's SOCK4Connection proxy
 * @author scotta
 */
public class BNCSWarden {
	private static final String SC_WARDEN = "Starcraft.exe";

	private final BNCSConnection c;
	private final SimpleCrypto incoming;
	private final SimpleCrypto outgoing;
	private WardenModule warden_module;
	private String game_exe;

	public BNCSWarden(BNCSConnection c, byte[] seed) {
		this.c = c;
		Out.debug(getClass(), "Generating Warden Cryptos");
		WardenRandom rand = new WardenRandom(seed);
		this.outgoing = new SimpleCrypto(rand.getBytes(0x10));
		this.incoming = new SimpleCrypto(rand.getBytes(0x10));

		switch(c.getProductID()) {
		case STAR:
		case SEXP:
			game_exe = SC_WARDEN;
			break;
		default:
			throw new IllegalStateException("Not sure how to process warden for " + c.getProductID().toString());
		}
	}

	public void processWardenPacket(byte[] payload, OutputStream os)
			throws IOException {
		BNCSPacket out = new BNCSPacket(c, BNCSPacketId.SID_WARDEN);

		//in.readDWord();
		Buffer warden = new Buffer(incoming.do_crypt(payload));
		byte opcode = warden.removeByte();
		switch (opcode) {
		case 0x00: // Startup

			byte[] md5 = warden.removeBytes(0x10);
			byte[] decryption = warden.removeBytes(0x10);
			int length = warden.removeDWord();

			warden_module = new WardenModule(length, md5, decryption, game_exe);

			Out.debug(getClass(), "Received warden module info: ");
			Out.debug(getClass(), "  Name:           " + warden_module.getName() + ".mod");
			Out.debug(getClass(), "  Decryption Key: " + warden_module.getSeed());
			Out.debug(getClass(), "  Length:         " + warden_module.getSize());

			if (warden_module.alreadyExists()) {
				Out.debug(getClass(), "Module already exists");
				out.write(outgoing.do_crypt((byte) 1));
			} else {
				Out.debug(getClass(), "Downloading module...");
				out.write(outgoing.do_crypt((byte) 0));
			}
			out.sendPacket(os);
			break;

		case 0x01:
			length = warden.removeWord();
			byte[] data = warden.removeBytes(length);
			warden_module.savePart(data, length);

			if (warden_module.downloadComplete()) {
				if (warden_module.alreadyExists()) {
					Out.debug(getClass(), "Download successful");
					out.write(outgoing.do_crypt((byte) 1));
					// warden_module.setup();
				} else {
					Out.error(getClass(), "Downloading failed");
					out.write(outgoing.do_crypt((byte) 0));
					warden_module.reset();
				}
				out.sendPacket(os);
			}
			break;

		case 0x02:
			Buffer modRet = warden_module.handleRequest(warden);

			data = modRet.removeBytes(0x20);
			int checksum = modRet.removeDWord();
			switch (checksum) {
			case 123:
				checksum = 0x193E73E8;
				break;
			case 132:
				checksum = 0x2183172A;
				break;
			case 213:
				checksum = 0xD6557DEF;
				break;
			case 231:
				checksum = 0xCA841860;
				break;
			case 312:
				checksum = 0xC04CF757;
				break;
			case 321:
				checksum = 0x9F2AD2C3;
				break;
			}
			/*
			 * 0x00497FB0, 0x0049C33D, 0x004A2FF7 = 0x193E73E8 0x00497FB0,
			 * 0x004A2FF7, 0x0049C33D = 0x2183172A
			 *
			 * 0x0049C33D, 0x00497FB0, 0x004A2FF7 = 0xD6557DEF 0x0049C33D,
			 * 0x004A2FF7, 0x00497FB0 = 0xCA841860
			 *
			 * 0x004A2FF7, 0x0049C33D, 0x00497FB0 = 0x9F2AD2C3 0x004A2FF7,
			 * 0x00497FB0, 0x0049C33D = 0xC04CF757
			 */

			Buffer response = new Buffer();
			response.addByte((byte) 0x02);
			response.addWord((short) data.length);
			response.addDWord(checksum);
			response.addBytes(data);

			out.write(outgoing.do_crypt(response.getBuffer()));
			out.sendPacket(os);
			break;

		default:
			Out.error(getClass(), "Unknown Warden opcode: 0x"
					+ PadString.padHex(opcode, 2));
		}
	}
}
