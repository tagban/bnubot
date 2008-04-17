/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui.components;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.event.ChangeListener;

public class ConfigCheckBox extends Box {
	private static final long serialVersionUID = 1831878850976738056L;
	private final JCheckBox theBox;

	public ConfigCheckBox(String text, boolean checked) {
		super(BoxLayout.X_AXIS);
		add(theBox = new JCheckBox(text, checked));
		add(Box.createHorizontalGlue());
		theBox.setBorder(BorderFactory.createLoweredBevelBorder());
	}

	public void setSelected(boolean b) {
		theBox.setSelected(b);
	}

	public boolean isSelected() {
		return theBox.isSelected();
	}

	public void addChangeListener(ChangeListener l) {
		theBox.addChangeListener(l);
	}

	public String getText() {
		return theBox.getText();
	}
}