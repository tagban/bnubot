package bnubot.bot.gui.textwindow;

import java.awt.*;

import javax.swing.*;

@SuppressWarnings("serial")
public class TextWindow extends JPanel {
	private static final int HEIGHT_PER_LINE = 15;
	
	public TextWindow() {
		//super(ScrollPane.SCROLLBARS_AS_NEEDED);
		super(new GridLayout(0, 1));
	}
	
	public void channelInfo(String text) {
		JTextPane jtp = new JTextPane();
		jtp.setText(text);
		jtp.setForeground(Color.GREEN);
		jtp.setBackground(Color.BLACK);
		this.add(jtp);
	}
	
	public void recieveInfo(String text) {
		JTextPane jtp = new JTextPane();
		jtp.setText(text);
		jtp.setForeground(Color.BLUE);
		jtp.setBackground(Color.BLACK);
		this.add(jtp);
	}
	
	public void recieveError(String text) {
		JTextPane jtp = new JTextPane();
		jtp.setText(text);
		jtp.setForeground(Color.RED);
		jtp.setBackground(Color.BLACK);
		this.add(jtp);
	}
	
	public void userChat(String user, String text) {
		JTextPane jtp = new JTextPane();
		jtp.setText("<" + user + "> " + text);
		jtp.setForeground(Color.GRAY);
		jtp.setBackground(Color.BLACK);
		this.add(jtp);
	}
	
	public void userEmote(String user, String text) {
		JTextPane jtp = new JTextPane();
		jtp.setText("<" + user + " " + text + ">");
		jtp.setForeground(Color.GRAY);
		jtp.setBackground(Color.BLACK);
		this.add(jtp);
	}
	
	public Component add(Component comp) {
		super.add(comp);
		if(getComponents().length * HEIGHT_PER_LINE > getHeight())
			remove(getComponents()[0]);
		return comp;
	}
}
