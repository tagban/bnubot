/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui.components;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JEditorPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import net.bnubot.bot.gui.colors.ColorScheme;
import net.bnubot.settings.GlobalSettings;
import net.bnubot.util.BNetUser;
import net.bnubot.util.BrowserLauncher;
import net.bnubot.util.Out;
import net.bnubot.util.TimeFormatter;

public class TextWindow extends JScrollPane {
	private static final long serialVersionUID = -8607940925721829314L;

	private class myJEP extends JEditorPane {
		private static final long serialVersionUID = 7313639261308578778L;

		public myJEP() {
			super();
			addHyperlinkListener(new HyperlinkListener() {
				public void hyperlinkUpdate(HyperlinkEvent e) {
					if(e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
						try {
							BrowserLauncher.openURL(e.getDescription());
						} catch (Exception e1) {
							Out.exception(e1);
						}
				}
			});
			setEditable(false);
			try {
				setContentType("text/html");
			} catch(Exception e) {}
			setBackground(cs.getBackgroundColor());
		}

		@Override
		public void paintComponents(Graphics g) {
			if(!disableRedraw)
				super.paintComponents(g);
		}
	}


	private static final ColorScheme cs = ColorScheme.getColors();
	private static String head = null;
	private static final String foot = "</body></html>";

	private Runnable scrollDown = null;
	private final JEditorPane jep;
	private String html = "";
	private boolean disableRedraw = false;

	/**
	 * When set, a separator will be added before the next line of information
	 */
	private boolean addSeparator = false;

	public TextWindow() {
		super(VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
		jep = new myJEP();
		((Container)getComponent(0)).add(jep);

		if(head == null)
			resetHead();
		setText();
	}

	public static void resetHead() {
		head = "<html><head><style type=\"text/css\">";
		head += " body	{font-family: " + GlobalSettings.guiFontFamily + ", verdana, courier, sans-serif; font-size: " + GlobalSettings.guiFontSize + "px;}";
		head += " .timestamp	{color: #" + makeColor(cs.getForegroundColor()) + ";}";
		head += " .channel	{color: #" + makeColor(cs.getChannelColor()) + ";}";
		head += " .info	{color: #" + makeColor(cs.getInfoColor()) + ";}";
		head += " .error	{color: #" + makeColor(cs.getErrorColor()) + ";}";
		head += " .debug	{font-family: courier; color: #" + makeColor(cs.getDebugColor()) + ";}";
		head += "</style></head><body>";
	}

	private void setText() {
		while(html.length() > 0x8000) {
			int i = html.indexOf("\n", 0);
			if(i > 0)
				html = html.substring(i + 1);
		}

		if(scrollDown == null)
			scrollDown = new Runnable() {
				public void run() {
					disableRedraw = true;
					jep.setText(head + html + foot);
					validate();

					try {
						JScrollBar vsb = getVerticalScrollBar();
						vsb.setValue(vsb.getMaximum());
					} catch(Exception e) {}

					disableRedraw = false;
					validate();
				}
			};

		//Scroll to the bottom
		SwingUtilities.invokeLater(scrollDown);
	}

	private static String makeColor(Color c) {
		String color = "000000" + Integer.toHexString(c.getRGB());
		return color.substring(color.length() - 6);
	}

	private void makeFont(Color c) {
		html += "<font color=\"#" + makeColor(c) + "\">";
	}

	private void appendDate() {
		if(addSeparator) {
			html += "<hr>\n";
			addSeparator = false;
		}

		html += "<font class=\"timestamp\">[";
		html += TimeFormatter.getTimestamp();
		html += "] </font>";
	}

	private static Pattern pattern = null;
	private String safeHtml(String in) {
		if(pattern == null)
			pattern = Pattern.compile("((.|\n)*?)\\b((([a-zA-Z]{3,6}://)|(www.)){1}([a-zA-Z0-9-.]+)([^-]\\.[a-zA-Z]{2,5}){1}((/\\S+){1}|\\s*?)/?)((.|\n)*)");

		try {
			Matcher matcher = pattern.matcher(in);

			if(matcher.matches())
				return safeHtml(matcher.group(1))
					+ "<a href=\"" + matcher.group(3) + "\">" + matcher.group(3) + "</a>"
					+ safeHtml(matcher.group(11));
		} catch(StackOverflowError e) {}

		return in
			.replaceAll("&", "&amp;")
			.replaceAll("<", "&lt;")
			.replaceAll(">", "&gt;")
			.replaceAll("\n", "<br>\n")
			.replaceAll("  ", " &nbsp;");
	}

	private void append(String text, Color col) {
		appendDate();
		makeFont(col);
		html += safeHtml(text);
		html += "</font><br>\n";
		setText();
	}

	private void append(String text, String clazz) {
		appendDate();
		html += "<font class=\"" + clazz + "\">";
		html += safeHtml(text);
		html += "</font><br>\n";
		setText();
	}

	private void append2(String text, Color col, String text2, String clazz) {
		appendDate();
		makeFont(col);
		html += safeHtml(text);
		html += "</font>";
		html += "<font class=\"" + clazz + "\">";
		html += safeHtml(text2);
		html += "</font><br>\n";
		setText();
	}

	private void append2(String text, Color col, String text2, Color col2) {
		appendDate();
		makeFont(col);
		html += safeHtml(text);
		html += "</font>";
		makeFont(col2);
		html += safeHtml(text2);
		html += "</font><br>\n";
		setText();
	}

	private void append3(String text, Color col, String text2, Color col2, String text3, Color col3) {
		appendDate();
		makeFont(col);
		html += safeHtml(text);
		html += "</font>";
		makeFont(col2);
		html += safeHtml(text2);
		html += "</font>";
		makeFont(col3);
		html += safeHtml(text3);
		html += "</font><br>\n";
		setText();
	}

	public void addSeparator() {
		addSeparator = true;
	}

	public void channelInfo(String text) {
		append(text, "channel");
	}

	public void recieveInfo(String type, String text) {
		if(type == null)
			append(text, "info");
		else
			append2(
					"(" + type + ") ",
					cs.getTypeColor(),
					text,
					"info");
	}

	public void recieveError(String type, String text) {
		if(type == null)
			append(text, "error");
		else
			append2(
					"(" + type + ") ",
					cs.getTypeColor(),
					text,
					"error");
	}

	public void recieveDebug(String type, String text) {
		if(type == null)
			append(text, "debug");
		else
			append2(
					"(" + type + ") ",
					cs.getTypeColor(),
					text,
					"debug");
	}

	public void userChat(String type, BNetUser user, String text, boolean isSelf) {
		Color c;
		if(isSelf)
			c = cs.getSelfUserNameColor(user.getFlags());
		else
			c = cs.getUserNameColor(user.getFlags());

		if(type == null)
			append2(
				"<" + user.toString() + "> ",
				c,
				text,
				cs.getChatColor(user.getFlags()));
		else
			append3(
				"(" + type + ") ",
				cs.getTypeColor(),
				"<" + user.toString() + "> ",
				c,
				text,
				cs.getChatColor(user.getFlags()));
	}

	public void whisperSent(String type, BNetUser user, String text) {
		if(type == null)
			append2(
				"<To: " + user.toString() + "> ",
				cs.getUserNameColor(user.getFlags()),
				text,
				cs.getWhisperColor(user.getFlags()));
		else
			append3(
				"(" + type + ") ",
				cs.getTypeColor(),
				"<To: " + user.toString() + "> ",
				cs.getUserNameColor(user.getFlags()),
				text,
				cs.getWhisperColor(user.getFlags()));
	}

	public void whisperRecieved(String type, BNetUser user, String text) {
		if(type == null)
			append2(
				"<From: " + user.toString() + "> ",
				cs.getUserNameColor(user.getFlags()),
				text,
				cs.getWhisperColor(user.getFlags()));
		else
			append3(
				"(" + type + ") ",
				cs.getTypeColor(),
				"<From: " + user.toString() + "> ",
				cs.getUserNameColor(user.getFlags()),
				text,
				cs.getWhisperColor(user.getFlags()));
	}

	public void userEmote(String type, BNetUser user, String text) {
		if(type == null)
			append(
				"<" + user.toString() + " " + text + ">",
				cs.getEmoteColor(user.getFlags()));
		else
			append2(
				"(" + type + ") ",
				cs.getTypeColor(),
				"<" + user.toString() + " " + text + ">",
				cs.getEmoteColor(user.getFlags()));
	}
}
