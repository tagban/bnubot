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
		public void componentMoved(ComponentEvent event) {
			save((Window)event.getSource());
		}
		public void componentResized(ComponentEvent event) {
			save((Window)event.getSource());
		}};

	public static void load(Window w) {
		String header = w.getClass().getSimpleName();
		Rectangle bounds = w.getBounds();
		if((w instanceof Frame) && ((Frame)w).isResizable()) {
			bounds.height = Integer.valueOf(Settings.read(header, "height", Integer.toString(bounds.height)));
			bounds.width = Integer.valueOf(Settings.read(header, "width", Integer.toString(bounds.width)));
		}
		bounds.x = Integer.valueOf(Settings.read(header, "x", Integer.toString(bounds.x)));
		bounds.y = Integer.valueOf(Settings.read(header, "y", Integer.toString(bounds.y)));
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
				Settings.write(header, "height", Integer.toString(bounds.height));
				Settings.write(header, "width", Integer.toString(bounds.width));
			}
		}
		Settings.write(header, "x", Integer.toString(bounds.x));
		Settings.write(header, "y", Integer.toString(bounds.y));
		Settings.store();
	}
}
