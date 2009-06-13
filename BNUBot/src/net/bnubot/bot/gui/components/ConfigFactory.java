/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui.components;

import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.JPasswordField;

/**
 * @author scotta
 */
public class ConfigFactory {
	private static final int height = 25;
	private static final Dimension preferredTextSize = new Dimension(350, 20);
	private static final Dimension maxComponentSize = new Dimension(Integer.MAX_VALUE, height);

	public static final Dimension getMaxComponentSize() {
		return maxComponentSize;
	}

	public static ConfigTextField makeText(String label, String value, ConfigPanel parent) {
		ConfigTextField txt = new ConfigTextField(value);
		txt.setMaximumSize(maxComponentSize);
		txt.setPreferredSize(preferredTextSize);

		parent.add(label, txt);
		return txt;
	}

	public static GhostDefaultTextField makeGhost(String label, String value, ConfigPanel parent) {
		GhostDefaultTextField txt = new GhostDefaultTextField(value);
		txt.setMaximumSize(maxComponentSize);
		txt.setPreferredSize(preferredTextSize);

		parent.add(label, txt);
		return txt;
	}

	public static JPasswordField makePass(String label, String value, ConfigPanel parent) {
		JPasswordField pass = new JPasswordField(value);
		pass.setMaximumSize(maxComponentSize);

		parent.add(label, pass);
		return pass;
	}

	public static JComboBox makeCombo(String label, Object[] values, boolean editable, ConfigPanel parent) {
		JComboBox cmb = new JComboBox(values);
		cmb.setEditable(editable);
		cmb.setMaximumSize(maxComponentSize);

		parent.add(label, cmb);
		return cmb;
	}

	public static ConfigSpinner makeSpinner(String label, Integer value, ConfigPanel parent) {
		ConfigSpinner spinner = new ConfigSpinner(value);
		spinner.setMaximumSize(maxComponentSize);

		parent.add(label, spinner);
		return spinner;
	}
}
