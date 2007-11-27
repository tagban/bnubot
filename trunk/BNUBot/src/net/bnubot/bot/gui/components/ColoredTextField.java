/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui.components;

import javax.swing.JTextField;

import net.bnubot.bot.gui.colors.ColorScheme;

/**
 * @author sanderson
 *
 */
public class ColoredTextField extends JTextField {
	private static final long serialVersionUID = 2905756736511967581L;

	public ColoredTextField(ColorScheme colors) {
		super();
		setBackground(colors.getBackgroundColor());
		setForeground(colors.getForegroundColor());
		setCaretColor(colors.getForegroundColor());
	}
}
