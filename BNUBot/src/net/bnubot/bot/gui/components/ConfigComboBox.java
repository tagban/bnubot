/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui.components;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

public class ConfigComboBox extends JComboBox {
	private static final long serialVersionUID = 4793810467982453882L;

	public ConfigComboBox(Object[] items) {
		super(items);
		setBorder(BorderFactory.createLoweredBevelBorder());
	}

	public ConfigComboBox(ComboBoxModel model) {
		super(model);
		setBorder(BorderFactory.createLoweredBevelBorder());
	}
}
