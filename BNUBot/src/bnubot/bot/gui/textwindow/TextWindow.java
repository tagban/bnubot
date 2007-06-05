package bnubot.bot.gui.textwindow;

import java.awt.*;
import java.util.GregorianCalendar;

import javax.swing.*;

import bnubot.bot.gui.ColorScheme.ColorScheme;
import bnubot.bot.gui.ColorScheme.Diablo2ColorScheme;
import bnubot.core.BNetUser;

@SuppressWarnings("serial")
public class TextWindow extends JScrollPane {
	private ColorScheme cs = null;
	private Box b = null;
	private Font font = new Font("Verdana", Font.PLAIN, 12);
	
	private class twTextPane extends JTextPane {
		public twTextPane(Color foreground) {
			setBackground(cs.getBackgroundColor());
			setForeground(foreground);
			setEditable(false);
			setFont(font);
		}
	}
	
	private class TimedMessage extends JPanel {
		public TimedMessage() {
			super(new FlowLayout(FlowLayout.LEFT));
			FlowLayout layout = (FlowLayout)getLayout();
			layout.setHgap(1);
			layout.setVgap(1);
			setBackground(cs.getBackgroundColor());

			twTextPane jtp = new twTextPane(cs.getTimeStampColor());
			jtp.setText(String.format("[%1$tH:%1$tM:%1$tS] ", new GregorianCalendar()));
			add(jtp);
		}
	}
	
	private class SingleColorMessage extends TimedMessage {
		public SingleColorMessage(String text, Color color) {
			super();

			twTextPane jtp = new twTextPane(color);
			jtp.setText(text);
			add(jtp);
		}
	}

	private class DoubleColorMessage extends SingleColorMessage {
		public DoubleColorMessage(String text1, Color color1, String text2, Color color2) {
			super(text1, color1);
			
			twTextPane jtp = new twTextPane(color2);
			jtp.setText(text2);
			add(jtp);
		}
	}
	
	public TextWindow(ColorScheme cs) {
		super(new Box(BoxLayout.Y_AXIS));
		this.cs = cs;
		setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		JViewport vp = (JViewport)getComponent(0);
		b = (Box)vp.getComponent(0);
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
				"<" + user + " " + text + ">",
				cs.getEmoteColor(flags)));
	}
	
	public void insert(Component c) {
		b.add(c);
		while(b.getComponentCount() > 200)
			b.remove(0);
		
		//Scroll to the bottom
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				validate();
				JScrollBar vsb = getVerticalScrollBar();
				vsb.setValue(vsb.getMaximum());
				JScrollBar hsb = getHorizontalScrollBar();
				hsb.setValue(0);
			}
		});
	}
}
