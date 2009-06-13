/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.gui.components;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author scotta
 */
public class ConfigPanel extends JPanel {
	private static final long serialVersionUID = -6204927361481125183L;

	public ConfigPanel() {
		super(new GridBagLayout());
	}

	private int gridy = 0;

	public GridBagConstraints left() {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridx = 0;
		gbc.gridy = this.gridy;
		gbc.weightx = 1;
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

	@Override
	public Component add(Component comp) {
		super.add(comp, full());
		return comp;
	}

	@Override
	public Component add(String name, Component comp) {
		super.add(new JLabel(name), left());
		super.add(comp, right());
		return comp;
	}
}
