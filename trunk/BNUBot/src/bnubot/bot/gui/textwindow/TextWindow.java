package bnubot.bot.gui.textwindow;

import java.awt.*;
import java.util.GregorianCalendar;

import javax.swing.*;

import bnubot.bot.gui.ColorScheme.Diablo2ColorConstants;

@SuppressWarnings("serial")
public class TextWindow extends JScrollPane {
	private static final Color bgColor = Diablo2ColorConstants.D2Black;
	
	private Box b = null;
	
	private class TimedMessage extends JPanel {
		public TimedMessage() {
			super(new FlowLayout(FlowLayout.LEFT));
			FlowLayout layout = (FlowLayout)getLayout();
			layout.setHgap(1);
			layout.setVgap(1);
			setBackground(bgColor);

			JTextPane jtp = new JTextPane();
			jtp.setText(String.format("[%1$tH:%1$tM:%1$tS] ", new GregorianCalendar()));
			jtp.setBackground(bgColor);
			jtp.setForeground(Color.LIGHT_GRAY);
			jtp.setEditable(false);
			add(jtp);
		}
	}
	
	private class SingleColorMessage extends TimedMessage {
		public SingleColorMessage(String text, Color color) {
			super();

			JTextPane jtp = new JTextPane();
			jtp.setText(text);
			jtp.setBackground(bgColor);
			jtp.setForeground(color);
			jtp.setEditable(false);
			add(jtp);
		}
	}

	private class DoubleColorMessage extends SingleColorMessage {
		public DoubleColorMessage(String text1, Color color1, String text2, Color color2) {
			super(text1, color1);
			
			JTextPane jtp = new JTextPane();
			jtp.setText(text2);
			jtp.setBackground(bgColor);
			jtp.setForeground(color2);
			jtp.setEditable(false);
			add(jtp);
		}
	}
	
	public TextWindow() {
		super(new Box(BoxLayout.Y_AXIS));
		setBackground(bgColor);
		setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		JViewport vp = (JViewport)getComponent(0);
		b = (Box)vp.getComponent(0);
	}
	
	public void channelInfo(String text) {
		insert(new SingleColorMessage(
				text,
				Diablo2ColorConstants.D2Green));
	}
	
	public void recieveInfo(String text) {
		insert(new SingleColorMessage(
				text,
				Diablo2ColorConstants.D2Blue));
	}
	
	public void recieveError(String text) {
		insert(new SingleColorMessage(
				text,
				Diablo2ColorConstants.D2Red));
	}
	
	public void userChat(String user, int flags, String text) {
		insert(new DoubleColorMessage(
				"<" + user + "> ",
				Diablo2ColorConstants.getUserNameColor(flags),
				text,
				Diablo2ColorConstants.getChatColor(flags)));
	}
	
	public void userEmote(String user, int flags, String text) {
		insert(new SingleColorMessage(
				"<" + user + " " + text + ">",
				Diablo2ColorConstants.getUserNameColor(flags)));
	}
	
	public void insert(Component c) {
		b.add(c);
		
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
