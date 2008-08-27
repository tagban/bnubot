/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui.components;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputVerifier;
import javax.swing.JSpinner;

/**
 * @author scotta
 */
public class ConfigSpinner extends Box {
	private static final long serialVersionUID = 1831878850976738056L;
	private final JSpinner theSpinner;

	public ConfigSpinner(Integer value) {
		super(BoxLayout.X_AXIS);
		add(theSpinner = new JSpinner());
		theSpinner.setValue(value);
		add(Box.createHorizontalGlue());
		theSpinner.setBorder(BorderFactory.createLoweredBevelBorder());
	}

	public void setValue(Integer value) {
		theSpinner.setValue(value);
	}

	public Integer getValue() {
		return (Integer)theSpinner.getValue();
	}

	@Override
	public void setInputVerifier(InputVerifier inputVerifier) {
		theSpinner.setInputVerifier(inputVerifier);
	}
}