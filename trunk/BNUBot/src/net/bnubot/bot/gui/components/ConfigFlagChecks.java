/**
 * This file is distributed under the GPL
 * $Id$
 */
package net.bnubot.bot.gui.components;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Hashtable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.bnubot.bot.gui.icons.BNetIcon;
import net.bnubot.bot.gui.icons.IconsDotBniReader;

/**
 * @author scotta
 */
public class ConfigFlagChecks extends Box {
	private static final long serialVersionUID = 7429733084778682272L;

	private final Hashtable<JCheckBox, Integer> cbs = new Hashtable<JCheckBox, Integer>();
	private final ConfigNumericHexTextField ctf;
	private final ChangeListener cl = new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
			JCheckBox cb = (JCheckBox) e.getSource();
			int f = cbs.get(cb).intValue();

			int flags = getFlags();
			if(cb.isSelected())
				flags |= f;
			else
				flags &= ~f;
			setFlags(flags);
			ctf.requestFocus();
		}};

	public ConfigFlagChecks(int flags) {
		super(BoxLayout.Y_AXIS);

		for(BNetIcon bni : IconsDotBniReader.getIcons()) {
			int f = bni.getFlags();
			if(f == 0)
				continue;

			JCheckBox cb = new JCheckBox("", false);
			Box b = new Box(BoxLayout.X_AXIS);
			b.add(cb);
			b.add(new JLabel(
					" 0x" + Integer.toHexString(f),
					bni.getIcon(),
					SwingConstants.LEFT));
			b.add(Box.createHorizontalGlue());
			add(b);

			cb.addChangeListener(cl);

			cbs.put(cb, new Integer(f));
		}

		add(ctf = new ConfigNumericHexTextField(flags));
		ctf.setEnabled(false);
		ctf.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {}
			public void focusLost(FocusEvent e) {
				setFlags(ctf.getValue());
			}});

		setFlags(flags);
	}

	@Override
	public synchronized void addFocusListener(FocusListener l) {
		super.addFocusListener(l);
		for(JCheckBox cb : cbs.keySet())
			cb.addFocusListener(l);
	}

	public void setFlags(int flags) {
		for(JCheckBox cb : cbs.keySet())
			cb.setSelected((flags & cbs.get(cb).intValue()) != 0);
		ctf.setValue(flags);
	}

	public int getFlags() {
		return ctf.getValue();
	}
}
