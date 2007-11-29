/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui.components;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPasswordField;

public class ConfigFactory {
	private static final int lblWidth = 100;
	private static final Dimension maxSize = new Dimension(lblWidth, 0);
	
	public static final Dimension getMaxSize() {
		return maxSize;
	}

	public static ConfigTextArea makeText(String label, String value, Box parent) {
		JLabel jl = new JLabel(label);
		jl.setPreferredSize(maxSize);

		ConfigTextArea txt = new ConfigTextArea(value);

		Box boxLine = new Box(BoxLayout.X_AXIS);
		boxLine.add(jl);
		boxLine.add(txt);
		parent.add(boxLine);

		return txt;
	}

	public static JPasswordField makePass(String label, String value, Box parent) {
		JLabel jl = new JLabel(label);
		jl.setPreferredSize(maxSize);

		JPasswordField pass = new JPasswordField(value);

		Box boxLine = new Box(BoxLayout.X_AXIS);
		boxLine.add(jl);
		boxLine.add(pass);
		parent.add(boxLine);

		return pass;
	}

	public static ConfigComboBox makeCombo(String label, Object[] values, boolean editable, Box parent) {
		JLabel jl = new JLabel(label);
		jl.setPreferredSize(maxSize);

		ConfigComboBox cmb = new ConfigComboBox(values);
		cmb.setEditable(editable);
		cmb.setBorder(null);

		Box boxLine = new Box(BoxLayout.X_AXIS);
		boxLine.add(jl);
		boxLine.add(cmb);
		parent.add(boxLine);

		return cmb;
	}
}
