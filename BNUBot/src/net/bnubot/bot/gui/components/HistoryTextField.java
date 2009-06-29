/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.gui.components;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;

/**
 * @author scotta
 */
public class HistoryTextField extends ColoredTextField {
	private static final long serialVersionUID = 999612270576101699L;

	private int current = 0;
	private final List<String> history = new LinkedList<String>();

	public HistoryTextField() {
		super();
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode()) {
				case KeyEvent.VK_UP:
					up();
					break;
				case KeyEvent.VK_DOWN:
					down();
					break;
				}
			}});
	}

	private void beep() {
		// TODO Auto-generated method stub
	}

	private void down() {
		if(current == 0) {
			beep();
			return;
		}
		current--;
		if(current == 0) {
			super.setText(null);
		} else {
			super.setText(history.get(history.size() - current));
		}
	}

	private void up() {
		if(current == history.size()) {
			beep();
			return;
		}
		current++;
		super.setText(history.get(history.size() - current));
	}

	@Override
	public void setText(String t) {
		if(t == null) {
			current = 0;
			String text = getText();
			if((text != null) && (text.length() > 0)) {
				if(!history.contains(text)) {
					history.add(text);
					if(history.size() > 20)
						history.remove(0);
				}
			}
		}
		super.setText(t);
	}
}
