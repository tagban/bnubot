/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.bot.gui.components;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

public class TextWindowLayout implements LayoutManager {

	public void addLayoutComponent(String arg0, Component arg1) { }
	public void removeLayoutComponent(Component arg0) { }
	public Dimension minimumLayoutSize(Container arg0) { return null; }
	public Dimension preferredLayoutSize(Container arg0) { return null; }

	public void layoutContainer(Container parent) {
		final int width = parent.getWidth();
		int y = 0;
		for(int i = 0; i < parent.getComponentCount(); i++) {
			Component c = parent.getComponent(i);
			Dimension d = c.getPreferredSize();
			c.setBounds(0, y, width, d.height);
			y += d.height;
		}
	}
}
