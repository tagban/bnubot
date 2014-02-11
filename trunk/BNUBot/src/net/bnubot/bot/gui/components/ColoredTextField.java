/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui.components;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.bnubot.bot.gui.colors.ColorScheme;

/**
 * @author scotta
 */
public class ColoredTextField extends JTextField {
	private static final long serialVersionUID = 2905756736511967581L;

	public ColoredTextField() {
		super();
		ColorScheme colors = ColorScheme.getColors();
		setBackground(colors.getBackgroundColor());
		setForeground(colors.getForegroundColor());
		setCaretColor(colors.getForegroundColor());
	}

	@Override
	public void select(final int start, final int end) {
		final ColoredTextField ctf = this;
		// Bury the selection three full queue lengths deep to avoid the select all bug in windows/osx native look and feels
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								ctf.select2(start, end);
							}});
					}});
			}});
	}

	public void select2(int start, int end) {
		super.select(start, end);
	}
}
