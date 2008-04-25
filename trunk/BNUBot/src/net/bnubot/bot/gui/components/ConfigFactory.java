/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui.components;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;

public class ConfigFactory {
	private static final int lblWidth = 100;
	private static final int height = 25;
	private static final Dimension preferredLabelSize = new Dimension(lblWidth, 0);
	private static final Dimension preferredTextSize = new Dimension(350, 20);
	private static final Dimension maxComponentSize = new Dimension(Integer.MAX_VALUE, height);

	public static final Dimension getMaxComponentSize() {
		return maxComponentSize;
	}

	public static JLabel makeLabel(String label) {
		JLabel jl = new JLabel(label);
		jl.setPreferredSize(preferredLabelSize);
		return jl;
	}

	public static ConfigTextArea makeText(String label, String value, Box parent) {
		ConfigTextArea txt = new ConfigTextArea(value);
		txt.setMaximumSize(maxComponentSize);
		txt.setPreferredSize(preferredTextSize);

		Box boxLine = new Box(BoxLayout.X_AXIS);
		boxLine.add(makeLabel(label));
		boxLine.add(txt);
		parent.add(boxLine);
		return txt;
	}

	public static JPasswordField makePass(String label, String value, Box parent) {
		JPasswordField pass = new JPasswordField(value);
		pass.setMaximumSize(maxComponentSize);

		Box boxLine = new Box(BoxLayout.X_AXIS);
		boxLine.add(makeLabel(label));
		boxLine.add(pass);
		parent.add(boxLine);
		return pass;
	}

	public static JComboBox makeCombo(String label, Object[] values, boolean editable, Box parent) {
		JComboBox cmb = new JComboBox(values);
		cmb.setEditable(editable);
		cmb.setMaximumSize(maxComponentSize);

		Box boxLine = new Box(BoxLayout.X_AXIS);
		boxLine.add(makeLabel(label));
		boxLine.add(cmb);
		parent.add(boxLine);
		return cmb;
	}

	public static ConfigSpinner makeSpinner(String label, Integer value, Box parent) {
		ConfigSpinner spinner = new ConfigSpinner(value);
		spinner.setMaximumSize(maxComponentSize);

		Box boxLine = new Box(BoxLayout.X_AXIS);
		boxLine.add(makeLabel(label));
		boxLine.add(spinner);
		parent.add(boxLine);
		return spinner;
	}
}
