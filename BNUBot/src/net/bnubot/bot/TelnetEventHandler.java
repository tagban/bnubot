/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import net.bnubot.core.Connection;
import net.bnubot.core.EventHandler;
import net.bnubot.core.Profile;
import net.bnubot.settings.ConnectionSettings;
import net.bnubot.util.BNetUser;
import net.bnubot.util.Out;
import net.bnubot.util.crypto.HexDump;

/**
 * @author scotta
 */
public class TelnetEventHandler extends EventHandler implements Runnable {
	private static boolean initialized = false;
	private List<ChatConnection> connections = new ArrayList<ChatConnection>();

	public TelnetEventHandler(Profile profile) {
		super(profile);
		if(initialized)
			throw new IllegalStateException("You may only use the TelnetEventHandler once!");
		initialized = true;

		new Thread(this).start();
	}

	public void run() {
		try {
			ServerSocket ss = new ServerSocket(6112);
			while(true) {
				ChatConnection cc = new ChatConnection(ss.accept());
				connections.add(cc);
				cc.start();
			}
		} catch(Exception e) {
			Out.exception(e);
		}
	}

	private class ChatConnection extends Thread {
		boolean connected = false;
		private Socket socket;
		private BufferedWriter bw = null;

		public ChatConnection(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			Connection pri = null;
			do {
				try {
					sleep(1000);
				} catch (InterruptedException e) {}
				if(profile != null)
					pri = profile.getPrimaryConnection();
				if((pri != null) && (pri.getMyUser() == null))
					pri = null;
			} while(pri == null);

			try {
				InputStream is = socket.getInputStream();
				OutputStream os = socket.getOutputStream();
				bw = new BufferedWriter(new OutputStreamWriter(os));
				BufferedReader br = new BufferedReader(new InputStreamReader(is));

				while((is.available() == 0) && socket.isConnected())
					sleep(500);

				do {
					int b = is.read();
					while(b == 255) {
						sleep(500);
						b = is.read();
					}
					if((b != 'c') && (b != 3))
						break;

					Out.debug(TelnetEventHandler.class, "Connection established from " + socket.getRemoteSocketAddress().toString());

					sendInternal("Username: ");
					String username = br.readLine();
					while(username.charAt(0) < 0x20)
						username = username.substring(1);

					ConnectionSettings cs = pri.getConnectionSettings();
					if(!username.equalsIgnoreCase(cs.username)) {
						sendInternal("1019 Error \"Invalid username\"");
						break;
					}

					sendInternal("Password: ");
					String password = br.readLine();

					if(!password.equalsIgnoreCase(cs.password)) {
						sendInternal("1019 Error \"Invalid password\"");
						break;
					}

					connected = true;
				} while(false);

				if(connected) {
					Out.debug(TelnetEventHandler.class, "Login accepted from " + socket.getRemoteSocketAddress().toString());
					send("2010 NAME " + pri.getMyUser().getShortLogonName());
					send("1007 CHANNEL \"" + pri.getChannel() + "\"");
					for(BNetUser user : pri.getUsers())
						send("1001 USER " + user.getShortLogonName() + " " + HexDump.toHexWord(user.getFlags()) + " [" + user.getStatString().getProduct().name() + "] " + user.getPing().toString());
				} else
					send("Login failed");

				try {
					while(connected) {
						if(!socket.isConnected())
							break;
						pri.sendChatInternal(br.readLine());
					}
				} catch(SocketException e) {
					Out.debug(TelnetEventHandler.class, socket.getRemoteSocketAddress().toString() + " " + e.getMessage());
				}
			} catch(Exception e) {
				Out.exception(e);
			}
			Out.debug(TelnetEventHandler.class, "Connection closed from " + socket.getRemoteSocketAddress().toString());

			try {
				socket.close();
			} catch (Exception e) {}
			connections.remove(this);
		}

		private void sendInternal(String text) {
			try {
				bw.write(text);
				bw.write("\r\n");
				bw.flush();
			} catch(Exception e) {
				connected = false;
			}
		}

		public void send(String text) {
			if(connected)
				sendInternal(text);
		}
	}

	private String quoteText(String text) {
		return "\"" + text + "\"";
	}

	private void dispatch(String data) {
		synchronized(connections) {
			for(ChatConnection cc : connections)
				cc.send(data);
		}
	}

	@Override
	public void channelUser(Connection source, BNetUser user) {
		dispatch("1001 USER " + user.getShortLogonName() + " " + HexDump.toHexWord(user.getFlags()) + " [" + user.getStatString().getProduct().name() + "] " + user.getPing().toString());
	}

	@Override
	public void channelJoin(Connection source, BNetUser user) {
		dispatch("1002 JOIN " + user.getShortLogonName() + " " + HexDump.toHexWord(user.getFlags()) + " [" + user.getStatString().getProduct().name() + "] " + user.getPing().toString());
	}

	@Override
	public void channelLeave(Connection source, BNetUser user) {
		dispatch("1003 LEAVE " + user.getShortLogonName());
	}

	@Override
	public void whisperRecieved(Connection source, BNetUser user, String text) {
		dispatch("1004 WHISPERRECIEVED " + user.getShortLogonName() + " " + HexDump.toHexWord(user.getFlags()) + " " + quoteText(text));
	}

	@Override
	public void recieveChat(Connection source, BNetUser user, String text) {
		if(source.getProfile().isOneOfMyUsers(user))
			return;
		dispatch("1005 TALK " + user.getShortLogonName() + " " + HexDump.toHexWord(user.getFlags()) + " " + quoteText(text));
	}

	@Override
	public void joinedChannel(Connection source, String channel) {
		dispatch("1007 CHANNEL " + quoteText(channel));
	}

	@Override
	public void whisperSent(Connection source, BNetUser user, String text) {
		dispatch("1010 WHISPERSENT " + user.getShortLogonName() + " " + HexDump.toHexWord(user.getFlags()) + " " + quoteText(text));
	}

	@Override
	public void recieveInfo(Connection source, String text) {
		dispatch("1018 INFO \"" + text + "\"");
	}

	@Override
	public void recieveServerInfo(Connection source, String text) {
		recieveInfo(source, text);
	}

	@Override
	public void recieveError(Connection source, String text) {
		dispatch("1019 Error \"" + text + "\"");
	}

	@Override
	public void recieveServerError(Connection source, String text) {
		recieveError(source, text);
	}

	@Override
	public void recieveEmote(Connection source, BNetUser user, String text) {
		dispatch("1023 EMOTE " + user.getShortLogonName() + " " + HexDump.toHexWord(user.getFlags()) + " " + quoteText(text));
	}
}
