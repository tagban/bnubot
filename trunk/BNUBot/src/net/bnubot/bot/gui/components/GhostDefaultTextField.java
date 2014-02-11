/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.gui.components;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


/**
 * @author scotta
 */
public class GhostDefaultTextField extends ConfigTextField implements MouseListener, FocusListener {
	private static final long serialVersionUID = 5908386893553368742L;

	private static final Color ghostColor = Color.GRAY;

	private final Color defaultColor;
	private final String defaultText;

	private boolean ghosted = false;

	/**
	 * @param defaultText
	 */
	public GhostDefaultTextField(String defaultText) {
		super(defaultText);
		this.defaultText = defaultText;
		this.defaultColor = getForeground();
		addMouseListener(this);
		addFocusListener(this);

		setGhosted(true);
	}

	public void reset() {
		setGhosted(true);
	}

	public boolean isGhosted() {
		if((getText() == null) || (getText().length() == 0))
			return true;
		return ghosted;
	}

	private void setGhosted(boolean ghosted) {
		if(ghosted != this.ghosted) {
			this.ghosted = ghosted;
			if(ghosted) {
				setForeground(ghostColor);
				super.setText(defaultText);
			} else {
				setForeground(defaultColor);
				super.setText(null);
			}
		}
	}

	@Override
	public void focusGained(FocusEvent e) {
		setGhosted(false);
	}
	@Override
	public void focusLost(FocusEvent e) {
		if((getText() == null) || (getText().length() == 0))
			reset();
	}

	@Override
	public void setText(String t) {
		setGhosted(false);
		super.setText(t);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if(!ghosted)
			return;
		setText(null);
		requestFocus();
	}
	@Override
	public void mouseEntered(MouseEvent e) {}
	@Override
	public void mouseExited(MouseEvent e) {}
	@Override
	public void mousePressed(MouseEvent e) {}
	@Override
	public void mouseReleased(MouseEvent e) {}


}
