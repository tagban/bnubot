/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.gui.components;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


/**
 * @author scotta
 */
public class GhostDefaultTextField extends ConfigTextField implements MouseListener, FocusListener {
	private static final long serialVersionUID = 5908386893553368742L;

	private String defaultText;

	/**
	 * @param defaultText
	 */
	public GhostDefaultTextField(String defaultText) {
		super(defaultText);
		this.defaultText = defaultText;
		addMouseListener(this);
		addFocusListener(this);
		setEnabled(false);
	}

	public void reset() {
		setEnabled(false);
		super.setText(defaultText);
	}

	public boolean isGhosted() {
		if((getText() == null) || (getText().length() == 0))
			return true;
		return !isEnabled();
	}

	public void focusGained(FocusEvent e) {}
	public void focusLost(FocusEvent e) {
		if((getText() == null) || (getText().length() == 0))
			reset();
	}

	@Override
	public void setText(String t) {
		setEnabled(true);
		super.setText(t);
	}

	public void mouseClicked(MouseEvent e) {
		if(isEnabled())
			return;
		setText(null);
		requestFocus();
	}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}


}
