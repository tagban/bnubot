/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui.components;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;

public class ConfigCheckBox extends JCheckBox {
	private static final long serialVersionUID = 1831878850976738056L;

	public ConfigCheckBox(String text, boolean checked) {
		super(text, checked);
		setBorder(BorderFactory.createLoweredBevelBorder());
	}
}