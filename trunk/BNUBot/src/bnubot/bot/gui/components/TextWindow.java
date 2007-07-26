/**
 * This file is distributed under the GPL 
 * $Id$
 */

package bnubot.bot.gui.components;

import java.awt.*;
import java.util.GregorianCalendar;

import javax.swing.*;

import bnubot.bot.gui.ColorScheme.ColorScheme;
import bnubot.core.BNetUser;

@SuppressWarnings("serial")
public class TextWindow extends JScrollPane {
	private ColorScheme cs = null;
	private Container view;
	private static final Font font = new Font("Verdana", Font.PLAIN, 12);
	
	private class twTextPane extends JTextPane {
		public twTextPane(Color foreground) {
			setBackground(cs.getBackgroundColor());
			setForeground(foreground);
			setEditable(false);
			setFont(font);
			setBorder(null);
		}
	}
	
	private class TimedMessage extends JPanel {
		protected Box b;
		
		public TimedMessage() {
			super(new FlowLayout(FlowLayout.LEFT));
			FlowLayout layout = (FlowLayout)getLayout();
			layout.setHgap(1);
			layout.setVgap(1);
			setBackground(cs.getBackgroundColor());
			
			b = new Box(BoxLayout.X_AXIS);
			add(b);

			// Add the timestamp to the panel
			twTextPane jtp = new twTextPane(cs.getTimeStampColor());
			jtp.setText(String.format("[%1$tH:%1$tM:%1$tS] ", new GregorianCalendar()));
			b.add(jtp);
		}
	}
	
	private class SingleColorMessage extends TimedMessage {
		public SingleColorMessage(String text, Color color) {
			super();

			twTextPane jtp = new twTextPane(color);
			jtp.setText(text);
			b.add(jtp);
		}
	}

	private class DoubleColorMessage extends SingleColorMessage {
		public DoubleColorMessage(String text1, Color color1, String text2, Color color2) {
			super(text1, color1);
			
			twTextPane jtp = new twTextPane(color2);
			jtp.setText(text2);
			b.add(jtp);
		}
	}
	
	public TextWindow(ColorScheme cs) {
		super(new Box(BoxLayout.Y_AXIS));
		this.cs = cs;
		
		setBorder(null);
		
		JViewport vp = getViewport();
		view = (Container) vp.getView();
		vp.setBackground(cs.getBackgroundColor());
	}
	
	public void channelInfo(String text) {
		insert(new SingleColorMessage(
				text,
				cs.getChannelColor()));
	}
	
	public void recieveInfo(String text) {
		insert(new SingleColorMessage(
				text,
				cs.getInfoColor()));
	}
	
	public void recieveError(String text) {
		insert(new SingleColorMessage(
				text,
				cs.getErrorColor()));
	}
	
	public void userChat(BNetUser user, int flags, String text) {
		insert(new DoubleColorMessage(
				"<" + user + "> ",
				cs.getUserNameColor(flags),
				text,
				cs.getChatColor(flags)));
	}
	
	public void whisperSent(BNetUser user, int flags, String text) {
		insert(new DoubleColorMessage(
				"<To: " + user + "> ",
				cs.getUserNameColor(flags),
				text,
				cs.getWhisperColor(flags)));
	}
	
	public void whisperRecieved(BNetUser user, int flags, String text) {
		insert(new DoubleColorMessage(
				"<From: " + user + "> ",
				cs.getUserNameColor(flags),
				text,
				cs.getWhisperColor(flags)));
	}
	
	public void userEmote(BNetUser user, int flags, String text) {
		insert(new SingleColorMessage(
				"<" + user + " " + text + "> ",
				cs.getEmoteColor(flags)));
	}
	
	public void insert(Component c) {
		view.add(c);
		while(view.getComponentCount() > 200)
			view.remove(0);
		
		//Scroll to the bottom
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				validate();
				JScrollBar vsb = getVerticalScrollBar();
				vsb.setValue(vsb.getMaximum());
				JScrollBar hsb = getHorizontalScrollBar();
				hsb.setValue(0);
				validate();
			}
		});
	}
}
