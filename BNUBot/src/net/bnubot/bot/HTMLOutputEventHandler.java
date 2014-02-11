/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot;

import java.awt.Color;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.SwingUtilities;

import net.bnubot.bot.gui.colors.ColorScheme;
import net.bnubot.core.Connection;
import net.bnubot.core.EventHandler;
import net.bnubot.core.Profile;
import net.bnubot.logging.Out;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetUser;
import net.bnubot.util.StatString;
import net.bnubot.util.TimeFormatter;
import net.bnubot.util.crypto.HexDump;

/**
 * @author scotta
 */
public class HTMLOutputEventHandler extends EventHandler {
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private boolean generationNeeded = false;
	private Runnable writeUserListRunnable = null;
	private final ColorScheme cs = ColorScheme.getColors();

	public HTMLOutputEventHandler(Profile profile) {
		super(profile);
	}

	@Override
	public void bnetDisconnected(Connection source) {
		writeUserList(source);
		recieveError(source, "Disconnected from battle.net.");
	}

	@Override
	public void titleChanged(Connection source) {}

	@Override
	public void channelJoin(Connection source, BNetUser user) {
		writeUserList(source);

		if(GlobalSettings.getDisplayJoinParts())
			append(source,
				user.toString() + " has joined the channel" + user.getStatString().toString() + ".",
				cs.getChannelColor());
	}

	@Override
	public void channelLeave(Connection source, BNetUser user) {
		writeUserList(source);

		if(GlobalSettings.getDisplayJoinParts())
			append(source,
				user.toString() + " has left the channel.",
				cs.getChannelColor());
	}

	@Override
	public void channelUser(Connection source, BNetUser user) {
		writeUserList(source);

		if(GlobalSettings.displayChannelUsers)
			append(source,
				user + user.getStatString().toString() + ".",
				cs.getChannelColor());
	}

	@Override
	public void initialize(Connection source) {
		checkFolder("logs");
		checkFolder("logs/" + source.getProfile().getName());
	}
	@Override
	public void disable(Connection source) {}

	private void checkFolder(String fName) {
		File f = new File(fName);
		if(!f.exists())
			f.mkdir();
		if(!f.isDirectory())
			Out.fatalException(new Exception(fName + " is not a directory!"));
	}

	@Override
	public void joinedChannel(Connection source, String channel) {
		append(source,
			"Joining channel " + channel + ".", cs.getChannelColor());
	}

	@Override
	public void recieveError(Connection source, String text) {
		append(source,
			text,
			cs.getErrorColor());
	}

	@Override
	public void recieveServerError(Connection source, String text) {
		this.recieveError(source, text);
	}

	@Override
	public void recieveInfo(Connection source, String text) {
		append(source,
			text,
			cs.getInfoColor());
	}

	@Override
	public void recieveServerInfo(Connection source, String text) {
		this.recieveInfo(source, text);
	}

	@Override
	public void recieveDebug(Connection source, String text) {
		append(source,
				text,
				cs.getDebugColor());
	}

	private String getIcon(int product, int icon, int flags) {
		if((flags & 0x01) != 0)	return "blizrep";
		if((flags & 0x08) != 0)	return "bnetrep";
		if((flags & 0x02) != 0)	return "op";
		if((flags & 0x04) != 0)	return "spkr";
		if((flags & 0x40) != 0)	return "blizguest";

		if((flags & 0x20) != 0)	return "squelch";

		if(icon != 0)
			return HexDump.DWordToPretty(icon);
		return HexDump.DWordToPretty(product);
	}

	private String getLagIcon(int flags, int ping) {
		if((flags & 0x10) != 0)
			return "PLUG";
		if(ping < 0)
			return "LAG6";
		if(ping < 10)
			return "LAG0";
		if(ping < 200)
			return "LAG1";
		if(ping < 300)
			return "LAG2";
		if(ping < 400)
			return "LAG3";
		if(ping < 500)
			return "LAG4";
		if(ping < 600)
			return "LAG5";
		return "LAG6";
	}

	private void writeUserList(final Connection source) {
		if(writeUserListRunnable == null)
			writeUserListRunnable = new Runnable() {
				@Override
				public void run() {
					if(!generationNeeded)
						return;
					generationNeeded = false;

					try {
						File f = new File("logs/userlist_" + source.getProfile().getName() + ".html");
						DataOutputStream fos = new DataOutputStream(new FileOutputStream(f));
						List<BNetUser> users = source.getSortedUsers();

						fos.write("<table><tr><td colspan=\"4\"><b>".getBytes());
						String channel = source.getChannel();
						if(channel != null)
							fos.write(channel.getBytes());
						fos.write("</b> (".getBytes());
						fos.write(Integer.toString(users.size()).getBytes());
						fos.write(")</td></tr>".getBytes());

						for(BNetUser ui : users) {
							StatString ss = ui.getStatString();
							String product = getIcon(ss.getProduct().getDword(), ss.getIcon(), ui.getFlags());
							String lag = getLagIcon(ui.getFlags(), ui.getPing());

							fos.write("<tr>".getBytes());
							fos.write(("<td><img src=\"images/" + product + ".jpg\"></td>").getBytes());
							fos.write(("<td>" + ui.toString(GlobalSettings.bnUserToStringUserList) + "</td>").getBytes());
							fos.write(("<td><img src=\"images/" + lag + ".jpg\"></td>").getBytes());
							fos.write("</tr>".getBytes());
						}
						fos.write("</table>".getBytes());
						fos.close();
					} catch (Exception e) {
						Out.exception(e);
					}
				}
			};

		generationNeeded = true;
		SwingUtilities.invokeLater(writeUserListRunnable);
	}

	private void logChat(Connection source, String text) {
		String fName = "logs/" + source.getProfile().getName() + "/";
		sdf.setTimeZone(TimeFormatter.timeZone);
		fName += sdf.format(new Date());
		fName += ".log";
		File f = new File(fName);
		try {
			if(!f.exists())
				f.createNewFile();
			// Create a new FOS for appending
			FileOutputStream fos = new FileOutputStream(f, true);
			fos.write(text.getBytes());
			fos.close();
		} catch (Exception e) {
			Out.fatalException(e);
		}
	}

	private String getDate() {
		try {
			return getColor(cs.getForegroundColor()) + String.format("[%1$tH:%1$tM:%1$tS] ", new GregorianCalendar(TimeFormatter.timeZone));
		} catch(NoSuchMethodError e) {
			return "";
		}
	}

	private String getColor(Color col) {
		String out = "000000";
		out += Integer.toHexString(col.getRGB());
		out = out.substring(out.length() - 6);
		out = "[COLOR:#" + out + "]";
		return out;
	}

	private void append(Connection source, String text, Color col) {
		String out = "\n";
		out += getDate();
		out += getColor(col);
		out += text;
		logChat(source, out);
	}

	private void append2(Connection source, String text, Color col, String text2, Color col2) {
		String out = "\n";
		out += getDate();
		out += getColor(col);
		out += text;
		if(!col.equals(col2))
			out += getColor(col2);
		out += text2;
		logChat(source, out);
	}

	private void append3(Connection source, String text, Color col, String text2, Color col2, String text3, Color col3) {
		String out = "\n";
		out += getDate();
		out += getColor(col);
		out += text;
		if(!col.equals(col2))
			out += getColor(col2);
		out += text2;
		if(!col2.equals(col3))
			out += getColor(col3);
		out += text3;
		logChat(source, out);
	}

	@Override
	public void recieveChat(Connection source, BNetUser user, String text) {
		append2(source,
			"<" + user.toString() + "> ",
			cs.getUserNameColor(user.getFlags()),
			text,
			cs.getChatColor(user.getFlags()));
	}

	@Override
	public void recieveBroadcast(Connection source, String username, int flags, String text) {
		append3(source,
				"(Broadcast) ",
				cs.getTypeColor(),
				"<" + username + "> ",
				cs.getUserNameColor(flags),
				text,
				cs.getChatColor(flags));
	}

	@Override
	public void recieveEmote(Connection source, BNetUser user, String text) {
		append(source,
			"<" + user.toString() + " " + text + "> ", cs.getEmoteColor(user.getFlags()));
	}

	@Override
	public void whisperSent(Connection source, BNetUser user, String text) {
		append2(source,
			"<To: " + user.toString() + "> ",
			cs.getUserNameColor(user.getFlags()),
			text,
			cs.getWhisperColor(user.getFlags()));
	}

	@Override
	public void whisperRecieved(Connection source, BNetUser user, String text) {
		append2(source,
			"<From: " + user.toString() + "> ",
			cs.getUserNameColor(user.getFlags()),
			text,
			cs.getWhisperColor(user.getFlags()));
	}
}
