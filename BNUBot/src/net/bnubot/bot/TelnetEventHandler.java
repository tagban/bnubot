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
import net.bnubot.logging.Out;
import net.bnubot.settings.ConnectionSettings;
import net.bnubot.util.BNetUser;
import net.bnubot.util.UnloggedException;
import net.bnubot.util.crypto.HexDump;

/**
 * @author scotta
 */
public class TelnetEventHandler extends EventHandler implements Runnable {
	private static boolean initialized = false;
	private List<ChatConnection> connections = new ArrayList<ChatConnection>();

	private enum ChatEvent {
		USER(1001),
		JOIN(1002),
		LEAVE(1003),
		WHISPERRECIEVED(1004),
		TALK(1005),
		BROADCAST(1006),
		CHANNEL(1007),
		WHISPERSENT(1010),
		INFO(1018),
		ERROR(1019),
		EMOTE(1023),
		KEEPALIVE(2000),
		NAME(2010);

		public final int id;
		ChatEvent(int id) {
			this.id = id;
		}
	}

	public TelnetEventHandler(Profile profile) {
		super(profile);
		if(initialized)
			throw new UnloggedException("You may only use the TelnetEventHandler once!");
		initialized = true;

		new Thread(this).start();
	}

	@Override
	public void run() {
		try (ServerSocket ss = new ServerSocket(6112)) {
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

				int loginAttempts = 0;
				while(!connected && (++loginAttempts < 3)) {
					if(loginAttempts == 1) {
						// This is the first login attempt; require them to shake hands
						int b = is.read();
						while(b == 255) {
							sleep(500);
							b = is.read();
						}
						if((b != 'c') && (b != 3))
							break;

						Out.debug(TelnetEventHandler.class, "Connection established from " + socket.getRemoteSocketAddress().toString());
					}

					sendInternal("Username: ");
					String username = br.readLine();
					if(username.length() == 0)
						username = br.readLine(); // Fool me once, shame on you
					if(username.length() == 0)
						continue; // Fool me twice, shame on me

					while(username.charAt(0) < 0x20)
						username = username.substring(1);

					ConnectionSettings cs = pri.getConnectionSettings();
					if(!username.equalsIgnoreCase(cs.username)) {
						sendInternal("1019 Error \"Invalid username\"");
						continue;
					}

					sendInternal("Password: ");
					String password = br.readLine();

					if(!password.equalsIgnoreCase(cs.password)) {
						sendInternal("1019 Error \"Invalid password\"");
						continue;
					}

					connected = true;
				}

				if(connected) {
					Out.debug(TelnetEventHandler.class, "Login accepted from " + socket.getRemoteSocketAddress().toString());
					dispatch(ChatEvent.NAME, pri.getMyUser().getShortLogonName());
					dispatch(ChatEvent.CHANNEL, quoteText(pri.getChannel()));
					for(BNetUser user : pri.getUsers())
						dispatchUserDetail(ChatEvent.USER, user);
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

	private void dispatch(ChatEvent event, String details) {
		dispatch(event.id + " " + event.name() + " " + details);
	}

	private void dispatch(ChatEvent event, String username, int flags, String text) {
		dispatch(event, username + " " + HexDump.toHexWord(flags) + " " + text);
	}

	private void dispatch(ChatEvent event, BNetUser user, String text) {
		dispatch(event, user.getShortLogonName(), user.getFlags(), text);
	}

	private void dispatchUserDetail(ChatEvent event, BNetUser user) {
		dispatch(event, user, "[" + user.getStatString().getProduct().name() + "] " + user.getPing().toString());
	}

	@Override
	public void channelUser(Connection source, BNetUser user) {
		dispatchUserDetail(ChatEvent.USER, user);
	}

	@Override
	public void channelJoin(Connection source, BNetUser user) {
		dispatchUserDetail(ChatEvent.JOIN, user);
	}

	@Override
	public void channelLeave(Connection source, BNetUser user) {
		dispatch(ChatEvent.LEAVE, user.getShortLogonName());
	}

	@Override
	public void whisperRecieved(Connection source, BNetUser user, String text) {
		dispatch(ChatEvent.WHISPERRECIEVED, user, quoteText(text));
	}

	@Override
	public void recieveChat(Connection source, BNetUser user, String text) {
		if(source.getProfile().isOneOfMyUsers(user))
			return;
		dispatch(ChatEvent.TALK, user, quoteText(text));
	}

	@Override
	public void recieveBroadcast(Connection source, String username, int flags, String text) {
		dispatch(ChatEvent.BROADCAST, username, flags, quoteText(text));
	}

	@Override
	public void joinedChannel(Connection source, String channel) {
		dispatch(ChatEvent.CHANNEL, quoteText(channel));
	}

	//1009 USERFLAGS [username] [flags]

	@Override
	public void whisperSent(Connection source, BNetUser user, String text) {
		dispatch(ChatEvent.WHISPERSENT, user, quoteText(text));
	}

	@Override
	public void recieveInfo(Connection source, String text) {
		dispatch(ChatEvent.INFO, quoteText(text));
	}

	@Override
	public void recieveServerInfo(Connection source, String text) {
		recieveInfo(source, text);
	}

	@Override
	public void recieveError(Connection source, String text) {
		dispatch(ChatEvent.ERROR, quoteText(text));
	}

	@Override
	public void recieveServerError(Connection source, String text) {
		recieveError(source, text);
	}

	@Override
	public void recieveEmote(Connection source, BNetUser user, String text) {
		dispatch(ChatEvent.EMOTE, user, quoteText(text));
	}

	// 2000 is a keepalive

	@Override
	public void enterChat(Connection source, BNetUser user) {
		dispatch(ChatEvent.NAME, user.getShortLogonName());
	}

}
