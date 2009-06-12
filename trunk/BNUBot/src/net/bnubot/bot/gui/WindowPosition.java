/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import net.bnubot.settings.Settings;

/**
 * Globally load/save window position
 * @author scotta
 */
public class WindowPosition {
	private static final ComponentAdapter windowSaver = new ComponentAdapter() {
		@Override
		public void componentMoved(ComponentEvent event) {
			save((Window)event.getSource());
		}
		@Override
		public void componentResized(ComponentEvent event) {
			save((Window)event.getSource());
		}};

	public static void load(Window w) {
		String header = w.getClass().getSimpleName();
		Rectangle bounds = w.getBounds();

		try {
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			bounds.x = (screenSize.width - bounds.width) / 2;
			bounds.y = (screenSize.height - bounds.height) / 2;
		} catch(Exception e) {
			// Do nothing
		}

		if((w instanceof Frame) && ((Frame)w).isResizable()) {
			bounds.height = Settings.getSection(header).read("height", bounds.height);
			bounds.width = Settings.getSection(header).read("width", bounds.width);
		}
		bounds.x = Settings.getSection(header).read("x", bounds.x);
		bounds.y = Settings.getSection(header).read("y", bounds.y);
		w.setBounds(bounds);
		w.addComponentListener(windowSaver);
	}

	public static void save(Window w) {
		String header = w.getClass().getSimpleName();
		Rectangle bounds = w.getBounds();
		if(w instanceof Frame) {
			Frame f = (Frame)w;
			if(f.getExtendedState() == Frame.MAXIMIZED_BOTH)
				return;

			if(f.isResizable()) {
				Settings.getSection(header).write("height", bounds.height);
				Settings.getSection(header).write("width", bounds.width);
			}
		}
		Settings.getSection(header).write("x", bounds.x);
		Settings.getSection(header).write("y", bounds.y);
		Settings.store();
	}
}
