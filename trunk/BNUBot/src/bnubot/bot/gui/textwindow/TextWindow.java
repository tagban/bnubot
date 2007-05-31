package bnubot.bot.gui.textwindow;

import java.awt.*;

import javax.swing.*;

@SuppressWarnings("serial")
public class TextWindow extends JScrollPane {
	private static final Color bgColor = Color.BLACK;
	
	private Box b = null;
	
	private class Emote extends JTextPane {
		public Emote(String name, String text) {
			setText("<" + name + " " + text + ">");
			setBackground(bgColor);
			setForeground(Color.YELLOW);
		}
	}

	private class Chat extends JPanel {
		public Chat(String name, String text) {
			super(new FlowLayout(FlowLayout.LEFT));
			FlowLayout layout = (FlowLayout)getLayout();
			layout.setHgap(1);
			layout.setVgap(1);
			setBackground(bgColor);
			
			JTextPane jtp = new JTextPane();
			jtp.setText("<" + name + "> ");
			jtp.setBackground(bgColor);
			jtp.setForeground(Color.YELLOW);
			add(jtp);
			
			jtp = new JTextPane();
			jtp.setText(text);
			jtp.setBackground(bgColor);
			jtp.setForeground(Color.LIGHT_GRAY);
			add(jtp);
		}
	}
	
	public TextWindow() {
		super(new Box(BoxLayout.Y_AXIS));
		setBackground(bgColor);
		JViewport vp = (JViewport)getComponent(0);
		b = (Box)vp.getComponent(0);
	}
	
	public void channelInfo(String text) {
		JTextPane jtp = new JTextPane();
		jtp.setText(text);
		jtp.setForeground(Color.GREEN);
		jtp.setBackground(bgColor);
		insert(jtp);
	}
	
	public void recieveInfo(String text) {
		JTextPane jtp = new JTextPane();
		jtp.setText(text);
		jtp.setForeground(Color.BLUE);
		jtp.setBackground(bgColor);
		insert(jtp);
	}
	
	public void recieveError(String text) {
		JTextPane jtp = new JTextPane();
		jtp.setText(text);
		jtp.setForeground(Color.RED);
		jtp.setBackground(bgColor);
		insert(jtp);
	}
	
	public void userChat(String user, String text) {
		insert(new Chat(user, text));
	}
	
	public void userEmote(String user, String text) {
		insert(new Emote(user, text));
	}
	
	public void insert(Component c) {
		b.add(c);
		
		//Scroll to the bottom
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JScrollBar vsb = getVerticalScrollBar();
				vsb.setValue(vsb.getMaximum() + 1);
			}
		});
	}
}
