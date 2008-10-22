/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.gui.components;

/**
 * @author scotta
 */
public class ConfigNumericHexTextField extends ConfigTextField {
	private static final long serialVersionUID = 5440170203920634560L;

	public ConfigNumericHexTextField(int value) {
		super("0x" + Integer.toHexString(value));
	}

	public void setValue(int value) {
		setText("0x" + Integer.toHexString(value));
	}

	public int getValue() {
		String x = getText();
		if(!x.startsWith("0x"))
			throw new RuntimeException("Invalid text format");
		return Integer.valueOf(x.substring(2), 16).intValue();
	}
}
