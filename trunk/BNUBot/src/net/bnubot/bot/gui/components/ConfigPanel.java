/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.gui.components;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author scotta
 */
public class ConfigPanel extends JPanel {
	private static final long serialVersionUID = -6204927361481125183L;

	private final Component vglue = Box.createVerticalGlue();

	public ConfigPanel() {
		super(new GridBagLayout());
		super.add(vglue, vglue());
	}

	private int gridy = 0;

	public GridBagConstraints left() {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridx = 0;
		gbc.gridy = this.gridy;
		gbc.weightx = 0;
		return gbc;
	}

	public GridBagConstraints right() {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = this.gridy++;
		gbc.weightx = 1;
		return gbc;
	}

	public GridBagConstraints full() {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = this.gridy++;
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		return gbc;
	}

	private GridBagConstraints vglue() {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		// Don't increment this.gridy since vglue is moved to the end every time
		gbc.gridy = this.gridy;
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		gbc.weighty = 1; // This is the key; soak up extra space
		return gbc;
	}

	@Override
	public Component add(Component comp) {
		remove(vglue);
		add(comp, full());
		add(vglue, vglue());
		return comp;
	}

	@Override
	public Component add(String name, Component comp) {
		remove(vglue);
		add(new JLabel(name), left());
		add(comp, right());
		add(vglue, vglue());
		return comp;
	}
}
