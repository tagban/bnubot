/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.gui.components;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * @author scotta
 */
public class ConfigPanel extends JPanel implements ConfigValueChangeListener, DocumentListener {
	private static final long serialVersionUID = -6204927361481125183L;
	private static final int height = 25;
	private static final int spacing = 3;
	private static final Dimension preferredTextSize = new Dimension(350, 20);
	private static final Dimension maxComponentSize = new Dimension(Integer.MAX_VALUE, height);

	public static final Dimension getMaxComponentSize() {
		return maxComponentSize;
	}

	private final Component vglue = Box.createVerticalGlue();
	private int gridy = 0;

	private List<ConfigValueChangeListener> listeners = new ArrayList<ConfigValueChangeListener>();

	public ConfigPanel() {
		super(new GridBagLayout());
		super.add(vglue, constraintVglue());
	}

	private GridBagConstraints constraintLeft() {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, 0, spacing);
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridx = 0;
		gbc.gridy = this.gridy;
		gbc.weightx = 0;
		return gbc;
	}

	private GridBagConstraints constraintRight() {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = this.gridy++;
		gbc.weightx = 1;
		return gbc;
	}

	private GridBagConstraints constraintFull() {
		GridBagConstraints gbc = new GridBagConstraints();
		//gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = this.gridy++;
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		return gbc;
	}

	private GridBagConstraints constraintVglue() {
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
	public void configValueChanged() {
		for(ConfigValueChangeListener l : listeners)
			l.configValueChanged();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		// DocumentListener
		configValueChanged();
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		// DocumentListener
		configValueChanged();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		// DocumentListener
		configValueChanged();
	}

	public void addConfigValueChangeListener(ConfigValueChangeListener cl) {
		listeners.add(cl);
	}

	@Override
	public Component add(Component comp) {
		remove(vglue);
		add(comp, constraintFull());
		add(vglue, constraintVglue());
		return comp;
	}

	@Override
	public Component add(String name, Component comp) {
		remove(vglue);
		add(new JLabel(name), constraintLeft());
		add(comp, constraintRight());
		add(vglue, constraintVglue());
		return comp;
	}

	public ConfigPanel makePanel(String label) {
		ConfigPanel cp = new ConfigPanel();
		cp.addConfigValueChangeListener(this);

		add(label, cp);
		return cp;
	}

	public ConfigTextField makeText(String label, String value) {
		ConfigTextField txt = new ConfigTextField(value);
		txt.setMaximumSize(maxComponentSize);
		txt.setPreferredSize(preferredTextSize);
		txt.getDocument().addDocumentListener(this);

		add(label, txt);
		return txt;
	}

	public GhostDefaultTextField makeGhost(String label, String value) {
		GhostDefaultTextField txt = new GhostDefaultTextField(value);
		txt.setMaximumSize(maxComponentSize);
		txt.setPreferredSize(preferredTextSize);
		txt.getDocument().addDocumentListener(this);

		add(label, txt);
		return txt;
	}

	public JPasswordField makePass(String label, String value) {
		JPasswordField pass = new JPasswordField(value);
		pass.setMaximumSize(maxComponentSize);
		pass.setPreferredSize(preferredTextSize);
		pass.getDocument().addDocumentListener(this);

		add(label, pass);
		return pass;
	}

	public <T> JComboBox<T> makeCombo(String label, T[] values, boolean editable) {
		JComboBox<T> cmb = new JComboBox<T>(values);
		cmb.setEditable(editable);
		cmb.setMaximumSize(maxComponentSize);
		cmb.setPreferredSize(preferredTextSize);

		add(label, cmb);
		return cmb;
	}

	public ConfigSpinner makeSpinner(String label, Integer value) {
		ConfigSpinner spinner = new ConfigSpinner(value);
		spinner.setMaximumSize(maxComponentSize);

		add(label, spinner);
		return spinner;
	}

	public JCheckBox makeCheck(String label, boolean selected) {
		JCheckBox check = new JCheckBox(label, selected);

		add(check);
		return check;
	}
}
