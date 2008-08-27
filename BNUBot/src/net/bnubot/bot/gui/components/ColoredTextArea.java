/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui.components;

import javax.swing.JTextArea;

import net.bnubot.bot.gui.colors.ColorScheme;

/**
 * @author scotta
 */
public class ColoredTextArea extends JTextArea {
	private static final long serialVersionUID = 2905756736511967581L;

	public ColoredTextArea() {
		super();
		ColorScheme colors = ColorScheme.getColors();
		setBackground(colors.getBackgroundColor());
		setForeground(colors.getForegroundColor());
		setCaretColor(colors.getForegroundColor());
	}
}
