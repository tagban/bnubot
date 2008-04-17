/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.bot.gui;

import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import net.bnubot.settings.Settings;

/**
 * Globally load/save window position
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
		if((w instanceof Frame) && ((Frame)w).isResizable()) {
			bounds.height = Settings.read(header, "height", bounds.height);
			bounds.width = Settings.read(header, "width", bounds.width);
		}
		bounds.x = Settings.read(header, "x", bounds.x);
		bounds.y = Settings.read(header, "y", bounds.y);
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
				Settings.write(header, "height", bounds.height);
				Settings.write(header, "width", bounds.width);
			}
		}
		Settings.write(header, "x", bounds.x);
		Settings.write(header, "y", bounds.y);
		Settings.store();
	}
}
