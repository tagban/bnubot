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
			setContentType("text/html");
			setBackground(cs.getBackgroundColor());
		}
		
		public void paintComponents(Graphics g) {
			if(!disableRedraw)
				super.paintComponents(g);
		}
	}
	
	private Runnable scrollDown = null;
	
	private final ColorScheme cs = ColorScheme.getColors();
	private final JEditorPane jep;
	private String head;
	private final String foot;
	private String html;
	private boolean disableRedraw = false;
	
	/**
	 * When set, a separator will be added before the next line of information
	 */
	private boolean addSeparator = false;

	public TextWindow() {
		super(VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
		jep = new myJEP();
		((Container)getComponent(0)).add(jep);
		
		head = "<html><head><style type=\"text/css\">";
		head += " body	{font-family: verdana, courier, sans-serif; font-size: 10px;}";
		head += " .timestamp	{color: #" + makeColor(cs.getForegroundColor()) + ";}";
		head += " .channel	{color: #" + makeColor(cs.getChannelColor()) + ";}";
		head += " .info	{color: #" + makeColor(cs.getInfoColor()) + ";}";
		head += " .error	{color: #" + makeColor(cs.getErrorColor()) + ";}";
		head += " .debug	{font-family: courier; color: #" + makeColor(cs.getDebugColor()) + ";}";
		head += "</style></head><body>";
		html = "";
		foot = "</body></html>";
		setText();
	}
	
	public void setText() {
		if(html.length() > 0x8000) {
			int i = html.indexOf("\n", 0);
			if(i > 0)
				html = html.substring(i + 6);
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
	
	public String makeColor(Color c) {
		String color = "000000" + Integer.toHexString(c.getRGB());
		return color.substring(color.length() - 6);
	}
	
	public void makeFont(Color c) {
		html += "<font color=\"#" + makeColor(c) + "\">";
	}
	
	public void appendDate() {
		if(addSeparator) {
			html += "<hr>\n";
			addSeparator = false;
		}
		
		html += "<font class=\"timestamp\">[";
		html += TimeFormatter.getTimestamp();
		html += "] </font>";
	}
	
	private static Pattern pattern = null;
	public String safeHtml(String in) {
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
	
	public void append(String text, Color col) {
		appendDate();
		makeFont(col);
		html += safeHtml(text);
		html += "</font><br>\n";
		setText();
	}
	
	public void append(String text, String clazz) {
		appendDate();
		html += "<font class=\"" + clazz + "\">";
		html += safeHtml(text);
		html += "</font><br>\n";
		setText();
	}
	
	public void append2(String text, Color col, String text2, Color col2) {
		appendDate();
		makeFont(col);
		html += safeHtml(text);
		html += "</font>";
		makeFont(col2);
		html += safeHtml(text2);
		html += "</font><br>\n";
		setText();
	}
	
	public void addSeparator() {
		addSeparator = true;
	}
	
	public void channelInfo(String text) {
		append(text, "channel");
	}
	
	public void recieveInfo(String text) {
		append(text, "info");
	}
	
	public void recieveError(String text) {
		append(text, "error");
	}
	
	public void recieveDebug(String text) {
		append(text, "debug");
	}
	
	public void userChat(BNetUser user, String text, boolean isSelf) {
		Color c;
		if(isSelf)
			c = cs.getSelfUserNameColor(user.getFlags());
		else
			c = cs.getUserNameColor(user.getFlags());
		
		append2(
			"<" + user.toString() + "> ",
			c,
			text,
			cs.getChatColor(user.getFlags()));
	}
	
	public void whisperSent(BNetUser user, String text) {
		append2(
			"<To: " + user.toString() + "> ",
			cs.getUserNameColor(user.getFlags()),
			text,
			cs.getWhisperColor(user.getFlags()));
	}
	
	public void whisperRecieved(BNetUser user, String text) {
		append2(
			"<From: " + user.toString() + "> ",
			cs.getUserNameColor(user.getFlags()),
			text,
			cs.getWhisperColor(user.getFlags()));
	}
	
	public void userEmote(BNetUser user, String text) {
		append(
			"<" + user.toString() + " " + text + ">",
			cs.getEmoteColor(user.getFlags()));
	}
}
